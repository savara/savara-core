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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.savara.common.logging.FeedbackHandler;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.resources.ResourceLocator;
import org.savara.common.util.XMLUtils;
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

	private static final Logger LOG=Logger.getLogger(ProtocolModelGeneratorImpl.class.getName());
	
	private static final String PREFIX = "savns";
	private static final String NAMESPACE_PREFIX = "http://namespace/";
	private static final String INTERFACE_SUFFIX = "Interface";

	/**
	 * {@inheritDoc}
	 */
	public Set<ProtocolModel> generate(Scenario scenario,
			ResourceLocator locator, FeedbackHandler handler, String namespace) {
		java.util.Set<ProtocolModel> ret=new java.util.HashSet<ProtocolModel>();
		
		for (Event event : scenario.getEvent()) {
			processEvent(event, ret, scenario, locator, handler, namespace);
		}
		
		return(ret);
	}

	protected void processEvent(Event event, java.util.Set<ProtocolModel> models,
				Scenario scenario, ResourceLocator locator, FeedbackHandler handler, String namespace) {
		if (event instanceof Group) {
			for (Event evt : ((Group)event).getEvent()) {
				processEvent(evt, models, scenario, locator, handler, namespace);
			}
		} else if (event instanceof SendEvent) {
			SendEvent se=(SendEvent)event;
			
			ProtocolModel pm=getProtocolModel(se, models, scenario, namespace);
			
			Interaction in=new Interaction();
			MessageSignature msig=new MessageSignature();
			msig.setOperation(se.getOperationName());
			
			if (se.getFaultName() != null && se.getFaultName().trim().length() > 0) {
				Annotation annotation=new Annotation(AnnotationDefinitions.FAULT);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY, se.getFaultName());
				in.getAnnotations().add(annotation);
			}
			
			if (isRequest(scenario, pm.getProtocol(), se)) {
				Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
				annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY, se.getOperationName());
				in.getAnnotations().add(annotation);
			} else {
				Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
				annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY, se.getOperationName());
				in.getAnnotations().add(annotation);
			}

			for (Parameter p : se.getParameter()) {
				TypeReference tref=getTypeReference(p, pm, locator);
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
			
			ProtocolModel pm=getProtocolModel(re, models, scenario, namespace);
			
			Interaction in=new Interaction();
			MessageSignature msig=new MessageSignature();
			msig.setOperation(re.getOperationName());
			
			if (re.getFaultName() != null && re.getFaultName().trim().length() > 0) {
				Annotation annotation=new Annotation(AnnotationDefinitions.FAULT);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY, re.getFaultName());
				in.getAnnotations().add(annotation);
			}

			if (isRequest(scenario, pm.getProtocol(), re)) {
				Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
				annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY, re.getOperationName());
				in.getAnnotations().add(annotation);
			} else {
				Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
				annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY, re.getOperationName());
				in.getAnnotations().add(annotation);
			}

			for (Parameter p : re.getParameter()) {
				TypeReference tref=getTypeReference(p, pm, locator);
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
	
	protected TypeReference getTypeReference(Parameter p,
				ProtocolModel model, ResourceLocator locator) {
		TypeReference ret=new TypeReference();
		QName qname=QName.valueOf(p.getType());
		
		ret.setName(qname.getLocalPart());
		
		// Check if import required
		TypeImport ti=TypesUtil.getTypeImport(ret);
		
		if (ti == null) {
			ti = new TypeImport();
			ti.setName(qname.getLocalPart());
			DataType dt=new DataType();
			dt.setDetails(p.getType());
			ti.setDataType(dt);
			
			TypeImportList il=new TypeImportList();
			il.setFormat(TypeSystem.XSD);
			il.getTypeImports().add(ti);
			model.getImports().add(il);
			
			// Obtain schema location for namespace
			String location=obtainSchemaLocation(p.getValue(),
					qname.getNamespaceURI(), locator);
			
			il.setLocation(location);
			
			// Create annotation to provide type details
			if (AnnotationDefinitions.getAnnotationWithProperty(model.getProtocol().getAnnotations(),
					AnnotationDefinitions.TYPE, AnnotationDefinitions.NAMESPACE_PROPERTY,
					qname.getNamespaceURI()) == null) {
			
				int nsCount=AnnotationDefinitions.getAnnotations(model.getProtocol().getAnnotations(),
											AnnotationDefinitions.TYPE).size();
				
				org.savara.common.model.annotation.Annotation protocolAnn=
						new org.savara.common.model.annotation.Annotation(AnnotationDefinitions.TYPE);
				protocolAnn.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY, qname.getNamespaceURI());
				protocolAnn.getProperties().put(AnnotationDefinitions.PREFIX_PROPERTY, PREFIX+nsCount);
				
				if (location != null) {
					protocolAnn.getProperties().put(AnnotationDefinitions.LOCATION_PROPERTY, location);
				}
	
				model.getProtocol().getAnnotations().add(protocolAnn);
			}
		}
		
		return(ret);
	}
	
	protected String obtainSchemaLocation(String path, String ns,
							ResourceLocator locator) {
		String ret=null;
		
		try {
			java.net.URI uri=locator.getResourceURI(path);
			
			java.io.File f=new java.io.File(uri.getPath());
			
			if (f.exists()) {
				java.io.FileInputStream fis=new java.io.FileInputStream(f);
				
				byte[] b=new byte[fis.available()];
				fis.read(b);
				
				fis.close();
				
				org.w3c.dom.Node node=XMLUtils.getNode(new String(b));
				
				if (node instanceof org.w3c.dom.Element) {
					String location=((org.w3c.dom.Element)node).getAttributeNS(
							"http://www.w3.org/2001/XMLSchema-instance",
								"schemaLocation");
					
					if (location != null) {
						StringTokenizer st=new StringTokenizer(location, " ");
						
						while (st.hasMoreTokens()) {
							String stns=st.nextToken();
							
							if (st.hasMoreTokens()) {
								String stval=st.nextToken();
								
								if (stns.equals(ns)) {
									
									java.io.File other=new java.io.File(
											f.getParentFile(), stval);
									
									ret = locator.getRelativePath(other.getCanonicalPath());
									break;
								}
							}
						}
					}
				}
			}
		} catch(Exception e) {
			LOG.log(Level.SEVERE, "Failed to load message", e);
		}
		
		return (ret);
	}
	
	protected ProtocolModel getProtocolModel(MessageEvent event,
								java.util.Set<ProtocolModel> models, Scenario scenario, String namespace) {
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
			
			createInterface(ret.getProtocol(), role, namespace);
			
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
							
							createInterface(ret.getProtocol(), otherRole, namespace);
						}
					}
					
					break;
				}
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method creates an interface annotation for the supplied role
	 * on the supplied protocol.
	 * 
	 * @param p The protocol
	 * @param role The role
	 * @param namespace The optional
	 */
	protected void createInterface(Protocol p, String role, String namespace) {
		String ns=(namespace == null ? NAMESPACE_PREFIX : namespace);
		
		if (ns.endsWith("/")) {
			ns += role;
		} else {
			ns += "/"+role;
		}
		
		Annotation annotation=new Annotation(AnnotationDefinitions.INTERFACE);
		annotation.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY,
				ns);
		annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
				role+INTERFACE_SUFFIX);
		annotation.getProperties().put(AnnotationDefinitions.ROLE_PROPERTY,
				role);
		p.getAnnotations().add(annotation);
	}
	
	/**
	 * This method determines if this message event relates to a request.
	 * 
	 * @param me The message event
	 * @return Whether it is a request
	 */
	protected static boolean isRequest(Scenario scenario, Protocol p, MessageEvent me) {
		boolean ret=true;
		
		// Need to find event's other role
		String otherRole=null;
				
		for (Link link : scenario.getLink()) {
			if (me instanceof SendEvent && link.getSource() == me) {
				otherRole=((Role)((MessageEvent)link.getTarget()).getRole()).getName();
				break;
			} else if (me instanceof ReceiveEvent && link.getTarget() == me) {
				otherRole=((Role)((MessageEvent)link.getSource()).getRole()).getName();
				break;
			}
		}
		
		if (otherRole != null) {
			
			// Check whether there is a preceding message event of the same type
			// with the same operation name, and inverted roles, where that
			// message event is a request - in which case, this message event is 
			// a response
			int pos=scenario.getEvent().indexOf(me);
			
			for (int i=pos-1; i >= 0; i--) {
				Event evt=scenario.getEvent().get(i);
				
				if (evt.getClass() == me.getClass()) {
					MessageEvent me2=(MessageEvent)evt;
					
					if (me2.getOperationName().equals(me.getOperationName())) {
						if (((Role)me2.getRole()).getName().equals(otherRole)) {
							ret = !isRequest(scenario, p, me2);
							break;
						}
					}
				}
			}
			
			/*
			// Check if client role
			if (p.getParameterDefinitions().size() > 0 &&
					p.getParameterDefinitions().get(0).getName().equals(otherRole)) {
				ret = me instanceof ReceiveEvent;
				
			} else if (me instanceof SendEvent) {
				
				if (p.getBlock().size() > 0 &&
						p.getBlock().get(0) instanceof Introduces) {
					Introduces intro = (Introduces)p.getBlock().get(0);

					ret = intro.getIntroducedRole(otherRole) != null;
				}
			}
			*/
		}
		
		return (ret);
	}
}
