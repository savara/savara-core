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
package org.savara.protocol.util;

import java.util.logging.Logger;

import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.DefaultVisitor;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.ModelProperties;
import org.scribble.protocol.model.Parameter;
import org.scribble.protocol.model.ParameterDefinition;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.Run;
import org.scribble.protocol.util.RoleUtil;
import org.scribble.protocol.util.RunUtil;

/**
 * This class defines a set of protocol related utility functions.
 */
public final class ProtocolUtils {
	
	private static final Logger LOG=Logger.getLogger(ProtocolUtils.class.getName());
		
	/**
	 * This method calculates the start and end position of a supplied DOM element, within
	 * the supplied text contents, and sets the values on the supplied ModelObject.
	 * 
	 * @param obj The ModelObject to be initialized
	 * @param contents The text contents
	 * @param elem The DOM element to be located in the text
	 */
	public static void setStartAndEndPosition(ModelObject obj, String contents, org.w3c.dom.Element elem) {

		if (contents != null) {
			org.w3c.dom.NodeList nl=elem.getOwnerDocument().getElementsByTagName(elem.getNodeName());
			int elempos=-1;
			
			for (int i=0; elempos == -1 && i < nl.getLength(); i++) {
				if (nl.item(i) == elem) {
					elempos = i;
				}
			}
			
			if (elempos != -1) {
				int startpos=-1;
					
				for (int i=0; i <= elempos; i++) {
					int val1=contents.indexOf("<"+elem.getNodeName()+">", startpos+1);
					int val2=contents.indexOf("<"+elem.getNodeName()+" ", startpos+1);
					
					if (val1 == -1 && val2 != -1) {
						startpos = val2;
					} else if (val1 != -1 && val2 == -1) {
						startpos = val1;
					} else if (val1 == -1 && val2 == -1) {
						// TODO: Error condition
						break;
					} else if (val1 > val2) {
						startpos = val2;
					} else {
						startpos = val1;
					}
				}
				
				if (startpos != -1) {
					//obj.getSource().setStartPosition(startpos);
					obj.getProperties().put(ModelProperties.START_LOCATION, startpos);
					
					// Check if single node
					int p1=contents.indexOf('>', startpos);
					
					if (p1 != -1 && contents.charAt(p1-1) == '/') {
						//obj.getSource().setEndPosition(p1);
						obj.getProperties().put(ModelProperties.END_LOCATION, p1);
					} else {
					
						org.w3c.dom.NodeList enl=elem.getElementsByTagName(elem.getNodeName());
						
						int endpos=startpos;
						String nodetxt="</"+elem.getNodeName()+">";
						
						for (int i=0; endpos != -1 && i <= enl.getLength(); i++) {
							endpos = contents.indexOf(nodetxt, endpos+1);
						}
						
						if (endpos != -1) {
							//obj.getSource().setEndPosition(endpos+nodetxt.length()-1);
							obj.getProperties().put(ModelProperties.END_LOCATION, endpos+nodetxt.length()-1);
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method finds a prefix associated with a supplied namespace using the
	 * type annotation information associated with a protocol model.
	 * 
	 * @param model The model
	 * @param namespace The namespace
	 * @return The prefix, or null if not found
	 */
	public static String getNamespacePrefix(ProtocolModel model, String namespace) {
		Annotation annotation=null;
		
		if (namespace == null || namespace.trim().length() == 0) {
			return("");
		}
		
		if (model != null && model.getProtocol() != null) {
			annotation = AnnotationDefinitions.getAnnotationWithProperty(
					model.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE,
					AnnotationDefinitions.NAMESPACE_PROPERTY, namespace);	
		}
		
		return(annotation == null ? null :
				(String)annotation.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
	}
	
	/**
	 * This method ensures that all introduced roles are declared at
	 * the most local block where their behaviour is defined.
	 * 
	 * @param pm The protocol model
	 */
	public static void localizeRoleIntroductions(ProtocolModel pm) {
		final java.util.List<Role> unused=new java.util.ArrayList<Role>();
		
		pm.visit(new DefaultVisitor() {
			
			public boolean start(Protocol p) {
				
				java.util.Iterator<Activity> iter=p.getBlock().getContents().iterator();
				
				while (iter.hasNext()) {
					Activity act=iter.next();
					
					if (act instanceof Introduces) {
						Introduces rl=(Introduces)act;
						
						for (int i=rl.getIntroducedRoles().size()-1; i >= 0; i--) {
							Role r=rl.getIntroducedRoles().get(i);
							Block b=RoleUtil.getEnclosingBlock(p, r, false);
							
							if (b == null) {
								// Report unused role
								unused.add(r);
								
							} else if (b != p.getBlock()) {
								Introduces innerrl=null;
								
								if (b.size() > 0 && b.get(0) instanceof Introduces &&
										((Introduces)b.get(0)).getIntroducer().equals(rl.getIntroducer())) {
									innerrl = (Introduces)b.get(0);
								} else {
									innerrl = new Introduces();
									innerrl.setIntroducer(rl.getIntroducer());
									b.getContents().add(0, innerrl);
								}
								
								rl.getIntroducedRoles().remove(r);
								innerrl.getIntroducedRoles().add(r);
							}
						}
						
						if (rl.getIntroducedRoles().size() == 0) {
							iter.remove();
						}					
					} else {
						break;
					}
				}
				
				return (true);
			}			
		});
		

		for (Role role : unused) {
			
			if (role.getParent() instanceof Introduces) {
				// Locate introduces
				Introduces introduces=(Introduces)role.getParent();
			
				Protocol protocol=introduces.getEnclosingProtocol();
				
				locateRoleIntroductionWithinRun(protocol, role, introduces.getIntroducer());

				introduces.getIntroducedRoles().remove(role);
				
				if (introduces.getIntroducedRoles().size() == 0) {
					((Block)introduces.getParent()).remove(introduces);
				}
			}
		}
	}

	protected static void locateRoleIntroductionWithinRun(Protocol protocol,
						final Role role, final Role introducer) {
		
		// Find run constructs that use the role
		protocol.visit(new DefaultVisitor() {
			
			public void accept(Run elem) {
				Parameter p=elem.getParameter(role.getName());

				if (p != null) {
					// Remove role parameter
					elem.getParameters().remove(p);

					Protocol target=RunUtil.getInnerProtocol(elem.getEnclosingProtocol(),
							elem.getProtocolReference());
					
					if (target == null) {
						// Error
						LOG.severe("Failed to find inner protocol '"
								+elem.getProtocolReference()+"'");
					} else {
						
						// Remove role as parameter
						ParameterDefinition pd=target.getParameterDefinition(role.getName());
						
						if (pd != null) {
							target.getParameterDefinitions().remove(pd);
						} else {
							LOG.severe("Failed to find parameter definition for role '"
									+role.getName()+"'");
						}
						
						// Find enclosing block in target protocol
						Block b=RoleUtil.getEnclosingBlock(target,
										role, false);
						
						if (b == null) {
							
							// Find run constructs
							locateRoleIntroductionWithinRun(target, role, introducer);
							
						// Check if should add to existing Introduces
						} else if (b.size() > 0 &&
								b.get(0) instanceof Introduces &&
								((Introduces)b.get(0)).getIntroducer().equals(introducer)) {
							((Introduces)b.get(0)).getIntroducedRoles().add(new Role(role));
							
						// Create new Introduces
						} else {
							Introduces intro=new Introduces();
							intro.setIntroducer(new Role(introducer));
							
							intro.getIntroducedRoles().add(new Role(role));
							
							b.getContents().add(0, intro);
						}
					}
				}
			}
		});
	}
	
}
