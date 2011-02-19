/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.parser.rules;

import java.util.List;

import org.savara.bpel.model.TFlow;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.*;

/**
 * This class represents a flow grouping activity.
 *  
 */
public class FlowParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TFlow);
	}
		
	public void convert(ConversionContext context, Object component, List<Activity> activities,
									Journal journal) {
		TFlow elem=(TFlow)component;
		
		//getSource().setComponentURI(getURI());
		
		// If links have been defined, represent them as boolean
		// variables that can be tested using a 'when' clause
		
		/* TODO: Basis for source link mechanism - but no variables
		 * supported yet
		 *
		if (getLinks().size() > 0) {
			org.scribble.conversation.model.VariableList vl=
				new org.scribble.conversation.model.VariableList();
			
			org.scribble.protocol.model.TypeReference tref=
					new org.scribble.protocol.model.TypeReference();
			tref.setName("boolean");
			
			vl.setType(tref);
			
			for (int i=0; i < getLinks().size(); i++) {
				Link l=getLinks().get(i);
				
				org.scribble.conversation.model.Variable var=
					new org.scribble.conversation.model.Variable();
				
				var.setName(l.getName());
				
				vl.getVariables().add(var);
			}
			
			activities.add(vl);
		}
		*/
		
		org.scribble.protocol.model.Parallel parallel=
					new org.scribble.protocol.model.Parallel();
		
		for (int i=0; i < elem.getActivity().size(); i++) {
			Block b=new Block();
			
			context.convert(elem.getActivity().get(i), b.getContents(), journal);
			
			parallel.getBlocks().add(b);
		}
		
		activities.add(parallel);
	}
}
