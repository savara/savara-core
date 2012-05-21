/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.protocol.internal.aggregator;

import java.util.logging.Logger;

import org.savara.protocol.internal.aggregator.LocalProtocolUnit.ActivityCursor;
import org.savara.protocol.model.util.ChoiceUtil;
import org.scribble.common.model.Annotation;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.ParameterDefinition;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Role;

public class GlobalProtocolUnit {
	
	private static final Logger LOG=Logger.getLogger(GlobalProtocolUnit.class.getName());
	private Container _container=null;
	private ProtocolModel _model=null;

	public GlobalProtocolUnit(ProtocolModel model) {
		_model = model;
		_container = new Container(model.getProtocol().getBlock());
	}
	
	public void process(Activity send, ActivityCursor sendCursor,
			Activity receive, ActivityCursor receiveCursor) {
	
		Container container=sendCursor.getContainer();
		if (container == null) {
			container = receiveCursor.getContainer();
			if (container == null) {
				sendCursor.setContainer(_container);
				receiveCursor.setContainer(_container);
				container = _container;
			} else {
				sendCursor.setContainer(container);
			}
		} else if (receiveCursor.getContainer() == null) {
			receiveCursor.setContainer(container);
		}
		
		if (send instanceof Interaction && receive instanceof Interaction) {
			Interaction interaction=createInteraction((Interaction)send, (Interaction)receive);
			
			container.add(interaction);
			
		} else if (send instanceof Choice && receive instanceof Choice) {
			Choice choice=new Choice();
			choice.setRole(new Role(((Choice)send).getRole()));
			
			container.add(choice);
			
			// Create a container for each send path, and associate a
			// cursor from the local projection with the container
			for (Block sendb : ((Choice)send).getPaths()) {
				Block newblock=new Block();
				Container newcontainer=container.createContainer(newblock);
				
				choice.getPaths().add(newblock);
				
				ActivityCursor sendChildCursor=sendCursor.createCursor(sendb);
				sendChildCursor.setContainer(newcontainer);
			}
			
			// Just create a cursor for each choice path, but don't assign a
			// container yet, as we don't know which order the paths will
			// be processed. They will become attached to a container when
			// the first 'send' match is made.
			for (Block recvb : ((Choice)receive).getPaths()) {
				receiveCursor.createCursor(recvb);
			}
		}
	
	}

	public void process(Activity individual, ActivityCursor individualCursor) {
	
		Container container=individualCursor.getContainer();
		if (container == null) {
			individualCursor.setContainer(_container);
			container = _container;
		}

		if (individual instanceof Introduces) {
			Introduces introduces=new Introduces((Introduces)individual);
			
			// TODO: Temporary - need to add to specific path
			container.add(introduces);
			
			// Check if process model has an initial role
			if (_model.getProtocol().getParameterDefinitions().size() == 0) {
				ParameterDefinition pd=new ParameterDefinition();
				pd.setName(introduces.getIntroducer().getName());
				_model.getProtocol().getParameterDefinitions().add(pd);
			}
		} else if (individual instanceof Choice) {
			
			if (ChoiceUtil.isDecisionMaker((Choice)individual)) {
				Choice choice=new Choice();
				choice.setRole(new Role(((Choice)individual).getRole()));
				
				container.add(choice);
				
				// Create a container for each send path, and associate a
				// cursor from the local projection with the container
				for (Block sendb : ((Choice)individual).getPaths()) {
					Block newblock=new Block();
					Container newcontainer=container.createContainer(newblock);
					
					choice.getPaths().add(newblock);
					
					ActivityCursor sendChildCursor=individualCursor.createCursor(sendb);
					sendChildCursor.setContainer(newcontainer);
				}
			} else {
			
				// Just create a cursor for each choice path, but don't assign a
				// container yet, as we don't know which order the paths will
				// be processed. They will become attached to a container when
				// the first 'send' match is made.
				// (Comment copied from matched process() above)
				for (Block recvb : ((Choice)individual).getPaths()) {
					individualCursor.createCursor(recvb);
				}
			}
		}
	}

	public void processRepetition(java.util.Collection<ActivityCursor> cursors) {
	
		// Find container
		Container container=null;
		
		for (ActivityCursor cursor : cursors) {
			if (container == null) {
				container = cursor.getContainer();
			} else if (cursor.getContainer() != null &&
					container != cursor.getContainer()) {
				LOG.severe("Repeat cursors have different containers");
			}
		}
		
		if (container == null) {
			container = _container;
		}
		
		Repeat repeat=new Repeat();
		container.add(repeat);
		
		Block newblock=new Block();
		Container newcontainer=container.createContainer(newblock);
		
		repeat.setBlock(newblock);
		
		for (ActivityCursor cursor : cursors) {
			ActivityCursor repeatCursor=cursor.createCursor(((Repeat)cursor.peek()).getBlock());
			repeatCursor.setContainer(newcontainer);
		}
	}

	protected Interaction createInteraction(Interaction send, Interaction receive) {
		Interaction ret=new Interaction();
		ret.setMessageSignature(new MessageSignature(send.getMessageSignature()));
		ret.setFromRole(new Role(receive.getFromRole()));
		for (Role r : send.getToRoles()) {
			ret.getToRoles().add(new Role(r));
		}
		
		// Copy send annotations
		for (Annotation an : send.getAnnotations()) {
			if (an instanceof org.savara.common.model.annotation.Annotation) {
				Annotation copy=new org.savara.common.model.annotation.Annotation(
							(org.savara.common.model.annotation.Annotation)an);
				ret.getAnnotations().add(copy);
			}
		}
		
		// Merge annotations from receive??? Not for now.
		//for (Annotation an : receive.getAnnotations()) {
		//}
		
		return(ret);
	}

	public class Container {
		
		private Block _block=null;
		private java.util.List<Container> _children=new java.util.Vector<Container>();
		
		public Container(Block b) {
			_block = b;
		}
		
		public void add(Activity act) {
			//if (_children.size() > 0) {
				// TODO: Do we need to add activity to children instead???
				//LOG.severe("NOT YET SUPPORTED: Adding activity '"+act+"' when children are defined");
			//} else {
				_block.add(act);
			//}
		}
		
		public Container createContainer(Block b) {
			Container ret=new Container(b);
			_children.add(ret);
			return(ret);
		}
		
		public void remove(Container c) {
			_children.remove(c);
		}
	}
}
