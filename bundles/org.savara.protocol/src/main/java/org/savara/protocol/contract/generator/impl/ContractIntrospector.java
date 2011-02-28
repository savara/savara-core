/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.savara.protocol.contract.generator.impl;

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.savara.protocol.model.util.InteractionUtil;
import org.savara.protocol.model.util.TypeSystem;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.contract.model.Contract;
import org.savara.contract.model.FaultDetails;
import org.savara.contract.model.Interface;
import org.savara.contract.model.MessageExchangePattern;
import org.savara.contract.model.Namespace;
import org.savara.contract.model.OneWayRequestMEP;
import org.savara.contract.model.RequestResponseMEP;
import org.savara.contract.model.Type;
import org.savara.contract.model.TypeDefinition;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.DefaultVisitor;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Run;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.TypeImport;
import org.scribble.protocol.model.TypeImportList;
import org.scribble.protocol.model.TypeReference;
import org.scribble.protocol.model.When;
import org.scribble.protocol.util.RunUtil;
import org.scribble.protocol.util.TypesUtil;

/**
 * This class examines a protocol to determine the contract that represents
 * the static functional interface to the role's behaviour.
 *
 */
public class ContractIntrospector extends DefaultVisitor {

	private Contract m_contract=new Contract();
	private java.util.Set<Protocol> m_processedProtocols=null;
	private Role m_serverRole=null;
	private java.util.Set<Role> m_clientRoles=null;
	private Protocol m_protocol=null;
	private Journal m_journal=null;
	
	private static Logger logger = Logger.getLogger(ContractIntrospector.class.getName());	
	
	/**
	 * Constructor for the contract introspector.
	 * 
	 * @param protocol The protocol to introspect
	 * @param clients The optional set of client roles
	 * @param server The server role
	 * @param journal The journal
	 */
	public ContractIntrospector(Protocol protocol, java.util.Set<Role> clients,
					Role server, Journal journal) {
		this(protocol, clients, server, null, null, journal);
	}
	
	/**
	 * Constructor for the contract introspector.
	 * 
	 * @param protocol The protocol being introspected
	 * @param clients The optional set of client roles
	 * @param server The server role
	 * @param contract The optional contract being derived
	 * @param processed The optional set of protocols currently processed
	 */
	public ContractIntrospector(Protocol protocol, java.util.Set<Role> clients,
			Role server, Contract contract,	java.util.Set<Protocol> processed, Journal journal) {
		m_contract = contract;
		
		m_clientRoles = clients;
		m_serverRole = server;
		
		if (m_contract == null) {
			m_contract = new Contract();
			
			m_contract.setName(m_serverRole.getName());
					
			java.util.List<Annotation> annotations=AnnotationDefinitions.getAnnotations(protocol.getAnnotations(),
								AnnotationDefinitions.NAMESPACE);
			if (annotations != null) {
				for (Annotation annotation : annotations) {
					String namespace=(String)annotation.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
					String role=(String)annotation.getProperties().get(AnnotationDefinitions.ROLE_PROPERTY);
				
					if (namespace != null && role != null && role.equals(m_serverRole.getName())) {
						m_contract.setNamespace(namespace);
						break;
					}	
				}
			}
		}
		
		if (processed != null) {
			m_processedProtocols = processed;
		} else {
			m_processedProtocols = new java.util.HashSet<Protocol>();
		}
		
		m_protocol = protocol;
	}
	
	/**
	 * This method returns the contract being derived.
	 * 
	 * @return The contract
	 */
	public Contract getContract() {
		return(m_contract);
	}
	
	/**
	 * This method returns the journal.
	 * 
	 * @return The journal
	 */
	public Journal getJournal() {
		return(m_journal);
	}
	
	/**
	 * This method returns the interface.
	 * 
	 * @return The interface
	 */
	public Interface getInterface(ProtocolModel model, String intfName) {
		QName qname=null;
		
		// TODO: Check Contract/Interface - whether should have separate name/namespace?
		
		if (intfName == null || intfName.trim().length() == 0) {
			qname = new QName(m_contract.getNamespace(), m_serverRole.getName());
		} else {
			qname = QName.valueOf(intfName);
		}
		
		Interface ret=getContract().getInterface(qname.getLocalPart());
		
		if (ret == null) {
			// Create interface for the role
			ret = new Interface();
			ret.setName(qname.getLocalPart());
			
			ret.setNamespace(qname.getNamespaceURI());
			
			addNamespace(model, qname.getNamespaceURI());
			
			getContract().getInterfaces().add(ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the set of processed protocols.
	 * 
	 * @return The processed protocols
	 */
	public java.util.Set<Protocol> getProcessedProtocols() {
		return(m_processedProtocols);
	}
	
	/**
	 * This method introspects the supplied protocol to derive information
	 * that can be used to define the functional contract for the role
	 * associated with the protocol.
	 * 
	 * @param conv The located protocol
	 */
	public void process() throws IllegalStateException {
		
		if (m_protocol == null) {
			throw new IllegalStateException(MessageFormat.format(
					java.util.PropertyResourceBundle.getBundle(
						"org.savara.contract.Messages").
							getString("SAVARAPC-00001"), (Object)null));
		}
		
		m_protocol.visit(this);
		
		// Ensure only interfaces with atleast one operation remain
		java.util.Iterator<Interface> iter=getContract().getInterfaces().iterator();
		
		while (iter.hasNext()) {
			Interface intf=iter.next();
			
			if (intf.getMessageExchangePatterns().size() == 0) {
				iter.remove();
			}
		}
	}
	

	public boolean start(Protocol elem) {
		// Only visit children if same protocol that is being visited
		return(elem == m_protocol);
	}

	public boolean start(Run elem) {
		Protocol toProtocol=RunUtil.getInnerProtocol(elem.enclosingProtocol(),
								elem.getProtocolReference());
		
		if (toProtocol != null) {
			// Check if protocol already processed to avoid stack overflow
			if (m_processedProtocols.contains(toProtocol) == false) {
				m_processedProtocols.add(toProtocol);

				// TODO: Need to map roles
				Role mappedServerRole=m_serverRole;
				java.util.Set<Role> mappedClientRoles=m_clientRoles;
				
				ContractIntrospector ci=new ContractIntrospector(toProtocol,
							mappedClientRoles, mappedServerRole,
							getContract(), getProcessedProtocols(), getJournal());
					
				ci.process();
			} else {
				logger.fine("Invoked definition not found for "+elem.getProtocolReference());
			}
		} else {
			logger.fine("Run protocol not returned - possibly external");
		}
		
		return(true);
	}
	
	/*
	public boolean process(ModelObject obj) {
		boolean f_visitChildren=true;
		
		if (obj.getClass() == Protocol.class) {
			
			// Only visit children if same protocol that is being visited
			f_visitChildren = (obj == m_protocol);
			
		} else if (obj.getClass() == Run.class) {
			Run run=(Run)obj;
			
			Protocol toProtocol=run.getProtocol();
			
			if (toProtocol != null) {
				// Check if protocol already processed to avoid stack overflow
				if (m_processedProtocols.contains(toProtocol) == false) {
					m_processedProtocols.add(toProtocol);
	
					// TODO: Need to map roles
					Role mappedServerRole=m_serverRole;
					java.util.Set<Role> mappedClientRoles=m_clientRoles;
					
					ContractIntrospector ci=new ContractIntrospector(toProtocol,
								mappedClientRoles, mappedServerRole,
								getContract(), getProcessedProtocols(), getJournal());
						
					ci.process();
				} else {
					logger.fine("Invoked definition not found for "+run.getProtocolReference());
				}
			} else {
				logger.fine("Run protocol not returned - possibly external");
			}
		} else if (obj.getClass() == Interaction.class) {
			accept((Interaction)obj);
		}
		
		return(f_visitChildren);
	}
	*/

	protected boolean isRoleRelevant(java.util.List<Role> roles) {
		boolean ret=false;
		
		for (Role role : roles) {
			ret = isRoleRelevant(role);
			if (ret) {
				break;
			}
		}
		
		return(ret);
	}
	
	protected boolean isRoleRelevant(Role role) {
		boolean ret=false;
		
		ret = (role != null && role.equals(m_serverRole));
		
		return(ret);
	}
	
	public void accept(Interaction interaction) {
		
		// Check if interaction is relevant for the server role 
		if (!isRoleRelevant(interaction.enclosingProtocol().getRole()) &&
				!isRoleRelevant(interaction.getFromRole()) &&
				!isRoleRelevant(interaction.getToRoles())) {
			return;		
		}
		
		// Check if interface has been specified
		String intfName=null;
		
		Annotation intfAnn=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(), AnnotationDefinitions.INTERFACE);
		if (intfAnn != null) {
			intfName = (String)intfAnn.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
		}
		
		// Receiving a request - so record this in the contract
		Interface intf=getInterface(interaction.getModel(), intfName);

		// Check if receiving a request
		if (InteractionUtil.isRequest(interaction) && !InteractionUtil.isSend(interaction, m_serverRole)) {
				
				if (interaction.getMessageSignature().getOperation() != null) {
					
					// Check if message exchange pattern exists for operation
					MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(
								interaction.getMessageSignature().getOperation());
					
					if (mep == null) {
						// Create new MEP
						if (InteractionUtil.getRequestLabel(interaction) != null) {
							mep = new RequestResponseMEP();
						} else {
							mep = new OneWayRequestMEP();
						}
						
						mep.setOperation(interaction.getMessageSignature().getOperation());
						
						for (int i=0; i < interaction.getMessageSignature().getTypeReferences().size(); i++) {
							mep.getTypes().add(convertType(interaction.getMessageSignature().getTypeReferences().get(i)));
						}
						
						intf.getMessageExchangePatterns().add(mep);
					}
				}

			//} else {
				
			//}
				
		} else if (InteractionUtil.isResponse(interaction) && InteractionUtil.isSend(interaction, m_serverRole)) {
			
			if (interaction.getMessageSignature().getOperation() != null) {
				
				// Check if message exchange pattern exists for operation
				MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(
							interaction.getMessageSignature().getOperation());
				
				if (mep instanceof RequestResponseMEP) {
					RequestResponseMEP rrmep=(RequestResponseMEP)mep;
					
					if (InteractionUtil.isFaultResponse(interaction)) {
						String faultName=InteractionUtil.getFaultName(interaction);
												
						if (rrmep.getFaultDetails(faultName) == null) {
							FaultDetails fd=new FaultDetails();
							fd.setName(faultName);
							
							for (int i=0; i < interaction.getMessageSignature().getTypeReferences().size(); i++) {
								fd.getTypes().add(convertType(interaction.getMessageSignature().getTypeReferences().get(i)));
							}
							
							rrmep.getFaultDetails().add(fd);
						}
						
					} else if (rrmep.getResponseTypes().size() == 0) {
						
						if (interaction.getMessageSignature().getTypeReferences().size() > 1) {
							getJournal().error("Response has more than one type", null);
						} else if (interaction.getMessageSignature().getTypeReferences().size() == 1) {
							rrmep.getResponseTypes().add(convertType(
									interaction.getMessageSignature().getTypeReferences().get(0)));
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method introspects the supplied interaction to generate Message Exchange
	 * Patterns on the contract interface.
	 * 
	 * @param interaction The interaction
	 */
	public boolean start(When when) {	
		
		// Check if interaction is relevant for the server role 
		if (!isRoleRelevant(((Choice)when.getParent()).enclosingProtocol().getRole()) &&
				!isRoleRelevant(((Choice)when.getParent()).getFromRole()) &&
				!isRoleRelevant(((Choice)when.getParent()).getToRole())) {
			return(true);		
		}
		
		// Check if interface has been specified
		String intfName=null;
		
		Annotation intfAnn=AnnotationDefinitions.getAnnotation(when.getAnnotations(), AnnotationDefinitions.INTERFACE);
		if (intfAnn != null) {
			intfName = (String)intfAnn.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
		}
		
		// Receiving a request - so record this in the contract
		Interface intf=getInterface(when.getModel(), intfName);

		// Check if receiving a request
		if (InteractionUtil.isRequest(when) && !InteractionUtil.isSend(when, m_serverRole)) {
				
			if (when.getMessageSignature().getOperation() != null) {
				
				// Check if message exchange pattern exists for operation
				MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(
						when.getMessageSignature().getOperation());
				
				if (mep == null) {
					// Create new MEP
					if (InteractionUtil.getRequestLabel(when) != null) {
						mep = new RequestResponseMEP();
					} else {
						mep = new OneWayRequestMEP();
					}
					
					mep.setOperation(when.getMessageSignature().getOperation());
					
					for (int i=0; i < when.getMessageSignature().getTypeReferences().size(); i++) {
						mep.getTypes().add(convertType(when.getMessageSignature().getTypeReferences().get(i)));
					}
					
					intf.getMessageExchangePatterns().add(mep);
				}
			}
		} else if (InteractionUtil.isResponse(when) && InteractionUtil.isSend(when, m_serverRole)) {
			
			if (when.getMessageSignature().getOperation() != null) {
				
				// Check if message exchange pattern exists for operation
				MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(
						when.getMessageSignature().getOperation());
				
				if (mep instanceof RequestResponseMEP) {
					RequestResponseMEP rrmep=(RequestResponseMEP)mep;
					
					if (InteractionUtil.isFaultResponse(when)) {
						String faultName=InteractionUtil.getFaultName(when);
												
						if (rrmep.getFaultDetails(faultName) == null) {
							FaultDetails fd=new FaultDetails();
							fd.setName(faultName);
							
							for (int i=0; i < when.getMessageSignature().getTypeReferences().size(); i++) {
								fd.getTypes().add(convertType(when.getMessageSignature().getTypeReferences().get(i)));
							}
							
							rrmep.getFaultDetails().add(fd);
						}
						
					} else if (rrmep.getResponseTypes().size() == 0) {
						
						if (when.getMessageSignature().getTypeReferences().size() > 1) {
							getJournal().error("Response has more than one type", null);
						} else if (when.getMessageSignature().getTypeReferences().size() == 1) {
							rrmep.getResponseTypes().add(convertType(
									when.getMessageSignature().getTypeReferences().get(0)));
						}
					}
				}
			}
		}

		/*
		Choice choice=(Choice)when.getParent();
		
		// Check if the interacton is being received
		if (choice.getFromRole() != null &&
				choice.getFromRole().equals(m_role) == false) {
			
			// Received by the role, but need to check if its replyTo
			// has been set, indicating it is receiving a response
			if (InteractionUtil.getReplyToLabel(when) == null ||
					InteractionUtil.getReplyToLabel(when).trim().length() == 0) {
				
				// Receiving a request - so record this in the contract
				Interface intf=getInterface();
				
				if (when.getMessageSignature().getOperation() != null) {
					
					// Check if message exchange pattern exists for operation
					MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(
							when.getMessageSignature().getOperation());
					
					if (mep == null) {
						// Create new MEP
						if (InteractionUtil.getRequestLabel(when) != null) {
							mep = new RequestResponseMEP();
						} else {
							mep = new OneWayRequestMEP();
						}
						
						mep.setOperation(when.getMessageSignature().getOperation());
						
						for (int i=0; i < when.getMessageSignature().getTypeReferences().size(); i++) {
							mep.getTypes().add(convertType(when.getMessageSignature().getTypeReferences().get(i)));
						}
						
						intf.getMessageExchangePatterns().add(mep);
					}
				}

			} else {
				
			}
		}
		*/
		
		return(true);
	}
	
	/**
	 * This method converts a protocol type reference into a contract model type.
	 * 
	 * @param tref The protocol type reference
	 * @return The type
	 */
	public Type convertType(TypeReference tref) {
		Type ret=new Type();
		
		if (getContract().getTypeDefinition(tref.getName()) == null) {
			TypeDefinition td=new TypeDefinition();
			td.setName(tref.getName());
			
			TypeImport ti=TypesUtil.getTypeImport(tref);
			if (ti != null) {
				TypeImportList til=(TypeImportList)ti.getParent();
				
				td.setDataType(ti.getDataType().getDetails());
				
				if (til != null) {
					
					// Associate annotations from type import list with type definition
					for (org.scribble.common.model.Annotation ann : til.getAnnotations()) {
						if (ann instanceof org.savara.common.model.annotation.Annotation) {
							td.getAnnotations().add((org.savara.common.model.annotation.Annotation)ann);
						}
					}
					
					td.setTypeSystem(til.getFormat());
					
					if (til.getFormat() != null && TypeSystem.XSD.equals(til.getFormat()) &&
								til.getLocation() != null) {
						
						addNamespace(tref.getModel(), til.getLocation());
					}
					
					/*
							getContract().getNamespaceForURI(til.getLocation()) == null) {
						
						// Check if namespace has been defined for location
						//tref.getModel().getProtocol().
						java.util.List<Annotation> annotations=
							AnnotationDefinitions.getAnnotations(tref.getModel().getProtocol().getAnnotations(),
										AnnotationDefinitions.TYPE);
						
						for (Annotation ann : annotations) {
							String nstxt=(String)ann.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY);
							
							if (nstxt != null && nstxt.equals(til.getLocation())) {
								Namespace ns=new Namespace();
								ns.setURI(til.getLocation());
								ns.setPrefix((String)ann.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
								ns.setSchemaLocation((String)ann.getProperties().get(AnnotationDefinitions.LOCATION_PROPERTY));
								getContract().getNamespaces().add(ns);
								
								break;
							}
						}
					}
					*/
				}
			}
			
			//td.setDataType((String)tref.getProperties().get(PropertyName.DATA_TYPE));
			
			// Copy properties that may provide additional information about the type
			td.getProperties().putAll(tref.getProperties());
			
			//td.setTypeSystem((String)tref.getProperties().get(PropertyName.TYPE_SYSTEM));
			
			getContract().getTypeDefinitions().add(td);
		}
		
		ret.setName(tref.getName());
		
		return(ret);
	}
	
	protected void addNamespace(ProtocolModel model, String namespace) {
		// Check if namespace has been defined for location
		if (getContract().getNamespaceForURI(namespace) == null && model != null) {
			java.util.List<Annotation> annotations=
				AnnotationDefinitions.getAnnotations(model.getProtocol().getAnnotations(),
							AnnotationDefinitions.TYPE);
			
			for (Annotation ann : annotations) {
				String nstxt=(String)ann.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY);
				
				if (nstxt != null && nstxt.equals(namespace)) {
					Namespace ns=new Namespace();
					ns.setURI(namespace);
					ns.setPrefix((String)ann.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
					ns.setSchemaLocation((String)ann.getProperties().get(AnnotationDefinitions.LOCATION_PROPERTY));
					getContract().getNamespaces().add(ns);
					
					break;
				}
			}
		}
	}
}
