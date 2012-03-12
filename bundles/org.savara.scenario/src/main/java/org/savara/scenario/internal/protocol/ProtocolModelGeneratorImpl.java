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
package org.savara.scenario.internal.protocol;

import java.util.Set;

import javax.xml.namespace.QName;

import org.savara.common.logging.FeedbackHandler;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.model.util.TypeSystem;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.Group;
import org.savara.scenario.model.Import;
import org.savara.scenario.model.Link;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.model.TimeElapsedEvent;
import org.savara.scenario.protocol.ProtocolModelGenerator;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.DataType;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.ParameterDefinition;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.TypeImport;
import org.scribble.protocol.model.TypeImportList;
import org.scribble.protocol.model.TypeReference;
import org.scribble.protocol.util.TypesUtil;

/**
 * The default implementation of the scenario protocol model generation.
 *
 */
public class ProtocolModelGeneratorImpl implements ProtocolModelGenerator {

	/**
	 * {@inheritDoc}
	 */
	public Set<ProtocolModel> generate(Scenario scenario,
						FeedbackHandler handler) {
		java.util.Set<ProtocolModel> ret=new java.util.HashSet<ProtocolModel>();
		
		for (Event event : scenario.getEvent()) {
			processEvent(event, ret, scenario, handler);
		}
		
		return(ret);
	}

	protected void processEvent(Event event, java.util.Set<ProtocolModel> models,
							Scenario scenario, FeedbackHandler handler) {
		if (event instanceof Group) {
			for (Event evt : ((Group)event).getEvent()) {
				processEvent(evt, models, scenario, handler);
			}
		} else if (event instanceof SendEvent) {
			SendEvent se=(SendEvent)event;
			
			ProtocolModel pm=getProtocolModel(se, models, scenario);
			
			Interaction in=new Interaction();
			MessageSignature msig=new MessageSignature();
			msig.setOperation(se.getOperationName());
			
			if (se.getFaultName() != null && se.getFaultName().trim().length() > 0) {
				Annotation annotation=new Annotation(AnnotationDefinitions.FAULT);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY, se.getFaultName());
				in.getAnnotations().add(annotation);
			}

			for (Parameter p : se.getParameter()) {
				TypeReference tref=getTypeReference(p.getType(), pm);
				msig.getTypeReferences().add(tref);
			}
			
			in.setMessageSignature(msig);
			
			// Need to find receive event's role
			for (Link link : scenario.getLink()) {
				if (link.getSource() == event) {
					String otherRole=((Role)((MessageEvent)link.getTarget()).getRole()).getName();
					in.getToRoles().add(new org.scribble.protocol.model.Role(otherRole));
					break;
				}
			}
					
			pm.getProtocol().getBlock().add(in);
			
		} else if (event instanceof ReceiveEvent) {
			ReceiveEvent re=(ReceiveEvent)event;
			
			ProtocolModel pm=getProtocolModel(re, models, scenario);
			
			Interaction in=new Interaction();
			MessageSignature msig=new MessageSignature();
			msig.setOperation(re.getOperationName());
			
			if (re.getFaultName() != null && re.getFaultName().trim().length() > 0) {
				Annotation annotation=new Annotation(AnnotationDefinitions.FAULT);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY, re.getFaultName());
				in.getAnnotations().add(annotation);
			}

			for (Parameter p : re.getParameter()) {
				TypeReference tref=getTypeReference(p.getType(), pm);
				msig.getTypeReferences().add(tref);
			}
			
			in.setMessageSignature(msig);
			
			// Need to find send event's role
			for (Link link : scenario.getLink()) {
				if (link.getTarget() == event) {
					String otherRole=((Role)((MessageEvent)link.getSource()).getRole()).getName();
					in.setFromRole(new org.scribble.protocol.model.Role(otherRole));
					break;
				}
			}
					
			pm.getProtocol().getBlock().add(in);
			
		} else if (event instanceof Import) {
			// TODO: Handle import
		} else if (event instanceof TimeElapsedEvent) {
			// TODO: Handle time elapsed event
		}
	}
	
	protected TypeReference getTypeReference(String type, ProtocolModel model) {
		TypeReference ret=new TypeReference();
		QName qname=QName.valueOf(type);
		
		ret.setName(qname.getLocalPart());
		
		// Check if import required
		TypeImport ti=TypesUtil.getTypeImport(ret);
		
		if (ti == null) {
			ti = new TypeImport();
			ti.setName(qname.getLocalPart());
			DataType dt=new DataType();
			dt.setDetails(type);
			ti.setDataType(dt);
			
			TypeImportList il=new TypeImportList();
			il.setFormat(TypeSystem.XSD);
			il.getTypeImports().add(ti);
			model.getImports().add(il);
		}
		
		return(ret);
	}
	
	protected ProtocolModel getProtocolModel(MessageEvent event,
								java.util.Set<ProtocolModel> models, Scenario scenario) {
		ProtocolModel ret=null;
		String role=((Role)event.getRole()).getName();
		
		for (ProtocolModel pm : models) {
			if (pm.getProtocol().getLocatedRole().getName().equals(role)) {
				ret = pm;
				break;
			}
		}
		
		if (ret == null) {
			ret = new ProtocolModel();
			ret.setProtocol(new Protocol());
			ret.getProtocol().setName(scenario.getName());
			ret.getProtocol().setLocatedRole(new org.scribble.protocol.model.Role(role));
			ret.getProtocol().setBlock(new Block());
			
			if (event instanceof ReceiveEvent) {
				// Need to find send event's role
				for (Link link : scenario.getLink()) {
					if (link.getTarget() == event) {
						ParameterDefinition pd=new ParameterDefinition();
						pd.setName(((Role)((MessageEvent)link.getSource()).getRole()).getName());
						ret.getProtocol().getParameterDefinitions().add(pd);
						break;
					}
				}
			}
			
			models.add(ret);
		}
		
		if (ret != null && event instanceof SendEvent) {
			
			// Need to find receive event's role
			for (Link link : scenario.getLink()) {
				if (link.getSource() == event) {
					// Check if role is client
					String otherRole=((Role)((MessageEvent)link.getTarget()).getRole()).getName();
					
					// Check if role is client
					if (ret.getProtocol().getParameterDefinitions().size() == 0 ||
							!ret.getProtocol().getParameterDefinitions().get(0).getName().equals(otherRole)) {
						// Check if other role is in the introduces list
						Introduces intro=null;
						
						if (ret.getProtocol().getBlock().size() > 0 &&
								ret.getProtocol().getBlock().get(0) instanceof Introduces) {
							intro = (Introduces)ret.getProtocol().getBlock().get(0);
						} else {
							intro = new Introduces();
							intro.setIntroducer(new org.scribble.protocol.model.Role(
									ret.getProtocol().getLocatedRole().getName()));
							ret.getProtocol().getBlock().getContents().add(0, intro);
						}
						
						if (intro.getIntroducedRole(otherRole) == null) {
							intro.getIntroducedRoles().add(new org.scribble.protocol.model.Role(otherRole));
						}
					}
					
					break;
				}
			}
		}
		
		return(ret);
	}
}
