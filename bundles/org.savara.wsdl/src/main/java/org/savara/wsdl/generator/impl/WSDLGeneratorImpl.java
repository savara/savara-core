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
package org.savara.wsdl.generator.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.savara.protocol.model.util.TypeSystem;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.logging.MessageFormatter;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.contract.model.FaultDetails;
import org.savara.contract.model.Interface;
import org.savara.contract.model.MessageExchangePattern;
import org.savara.contract.model.Namespace;
import org.savara.contract.model.RequestResponseMEP;
import org.savara.contract.model.Type;
import org.savara.contract.model.TypeDefinition;
import org.savara.wsdl.generator.WSDLBinding;
import org.savara.wsdl.generator.WSDLGenerator;
import org.savara.wsdl.util.WSDLGeneratorUtil;

/**
 * This class generates a WSDL definition from a Contract model.
 *
 */
public class WSDLGeneratorImpl implements WSDLGenerator {

	public static final String BINDING_SUFFIX = "Binding";
	
	private static Logger logger = Logger.getLogger(WSDLGeneratorImpl.class.getName());
	
	public WSDLGeneratorImpl() {
	}
	
	/**
	 * This method generates a WSDL definition from a Scribble contract model.
	 * 
	 * @param contract The contract model
	 * @param wsdlBinding The WSDL binding to use, or null if no binding
	 * @param journal The journal
	 * @return The WSDL definition
	 */
	public java.util.List<javax.wsdl.Definition> generate(org.savara.contract.model.Contract contract,
								WSDLBinding wsdlBinding, FeedbackHandler handler) {
		java.util.List<javax.wsdl.Definition> ret=new java.util.Vector<javax.wsdl.Definition>();
		
		try {
			// Create definition for contract's target namespace
			javax.wsdl.Definition main=getDefinition(ret, contract, contract.getNamespace(), wsdlBinding);
			
			// If no definition, then return
			if (main == null) {
				return(ret);
			}
			
			// Create service
			javax.wsdl.Service service=main.createService();
			service.setQName(new javax.xml.namespace.QName(contract.getNamespace(),
								contract.getName()+"Service"));

			main.addService(service);
			
			// Define a port type per interface
			java.util.Iterator<Interface> iter=contract.getInterfaces().iterator();
			
			while (iter.hasNext()) {
				Interface intf=iter.next();
				
				javax.wsdl.PortType portType=
							createPortType(ret, contract, intf, wsdlBinding, handler);
				
				javax.wsdl.Binding binding=
							createBinding(ret, contract, intf, portType, wsdlBinding, handler);
				
				// Create service port for interface
				javax.wsdl.Port port=main.createPort();
				
				port.setName(intf.getName()+"Port");
				port.setBinding(binding);
				
				service.addPort(port);
				
				if (wsdlBinding != null) {
					wsdlBinding.updatePort(main, port);
				}
			}
			
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to generate WSDL", e);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the definition associated with the supplied target namespace.
	 * 
	 * @param wsdls The list of current WSDL definitions
	 * @param contract The contract
	 * @param targetNamespace The target namespace
	 * @param wsdlBinding The WSDL binding to use, or null if no binding
	 * @return The WSDL definition for the target namespace, or null if unable to find or create
	 */
	protected javax.wsdl.Definition getDefinition(java.util.List<javax.wsdl.Definition> wsdls,
				org.savara.contract.model.Contract contract, String targetNamespace,
				WSDLBinding wsdlBinding) {
		javax.wsdl.Definition ret=null;
		
		if (targetNamespace != null) {
			for (int i=0; ret == null && i < wsdls.size(); i++) {
				ret = wsdls.get(i);
				
				if (ret.getTargetNamespace() == null || 
						ret.getTargetNamespace().equals(targetNamespace) == false) {
					ret = null;
				}
			}
			
			if (ret == null) {
				ret = createDefinition(contract, targetNamespace);
				
				if (ret != null) {
					
					// Initialize definition using the WSDL binding
					if (wsdlBinding != null) {
						wsdlBinding.initDefinition(ret);
					}
					
					wsdls.add(ret);
				}
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method creates a new WSDL definition associated with the supplied
	 * target namespace.
	 * 
	 * @param contract The contract
	 * @param targetNamespace The target namespace
	 * @return The WSDL definition
	 */
	protected javax.wsdl.Definition createDefinition(org.savara.contract.model.Contract contract,
									String targetNamespace) {
		javax.wsdl.Definition ret=null;
		
		try {
			javax.wsdl.factory.WSDLFactory fact=
				javax.wsdl.factory.WSDLFactory.newInstance();
	
			ret = fact.newDefinition();
			
			// Set details on the definition
			if (contract.getName() != null) {
				ret.setQName(new javax.xml.namespace.QName(contract.getName()));
			}
			ret.setTargetNamespace(targetNamespace);
			//ret.addNamespace("tns", targetNamespace);
			
			// Set up namespace mappings
			java.util.List<Namespace> nss=new java.util.Vector<Namespace>(contract.getNamespaces());
			
			Collections.sort(nss, new Comparator<Namespace>() {
				public int compare(Namespace o1, Namespace o2) {
					return o2.getPrefix().compareTo(o1.getPrefix());
				}				
			});
			
			for (Namespace ns : nss) {
				ret.addNamespace(ns.getPrefix(), ns.getURI());
			}

		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to create WSDL definition for target namespace '"+targetNamespace+"'", e);
		}

		return(ret);
	}

	/**
	 * This method generates a port type, using the supplied WSDL definition,
	 * based on the information in the supplied interface definition.
	 * 
	 * @param wsdls The list of current WSDL definitions
	 * @param contract The contract
	 * @param intf The interface model
	 * @return The WSDL port type
	 */
	public javax.wsdl.PortType createPortType(java.util.List<javax.wsdl.Definition> wsdls,
						org.savara.contract.model.Contract contract,
								org.savara.contract.model.Interface intf,
								WSDLBinding wsdlBinding, FeedbackHandler handler) {
		javax.wsdl.PortType ret=null;
		
		if (intf != null) {
			javax.wsdl.Definition defn=getDefinition(wsdls, contract, intf.getNamespace(), wsdlBinding);

			if (defn != null) {
				ret = defn.createPortType();
				ret.setUndefined(false);
				
				if (intf.getName() != null) {
					ret.setQName(new javax.xml.namespace.QName(intf.getNamespace(), intf.getName()));
				}
				
				for (int i=0; i < intf.getMessageExchangePatterns().size(); i++) {
					
					// Only create operations for meps with type parameters
					MessageExchangePattern mep=intf.getMessageExchangePatterns().get(i);
					
					if (mep.getTypes().size() > 0) {
						createOperation(wsdls, contract, ret,
								mep, wsdlBinding, handler);
					}
				}

				// Only add portType to definition if they have atleast one operation
				if (ret != null && ret.getOperations().size() > 0) {
					defn.addPortType(ret);
				}
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method generates a port type binding, using the supplied WSDL definition,
	 * based on the information in the supplied interface definition.
	 * 
	 * @param wsdls The list of current WSDL definitions
	 * @param contract The contract
	 * @param intf The interface model
	 * @param portType The port type
	 * @return The WSDL port type binding
	 */
	public javax.wsdl.Binding createBinding(java.util.List<javax.wsdl.Definition> wsdls,
						org.savara.contract.model.Contract contract,
								org.savara.contract.model.Interface intf,
								javax.wsdl.PortType portType,
								WSDLBinding wsdlBinding, FeedbackHandler handler) {
		javax.wsdl.Binding ret=null;
		
		if (intf != null) {
			javax.wsdl.Definition defn=getDefinition(wsdls, contract, intf.getNamespace(), wsdlBinding);

			if (defn != null) {
				ret = defn.createBinding();
				ret.setUndefined(false);
				
				// Check if WSDL binding details
				if (wsdlBinding != null) {
					wsdlBinding.updateBinding(defn, ret);
				}
				
				if (intf.getName() != null) {
					ret.setQName(new javax.xml.namespace.QName(intf.getNamespace(), 
							intf.getName()+BINDING_SUFFIX));
				}
				
				ret.setPortType(portType);
				
				for (int i=0; i < intf.getMessageExchangePatterns().size(); i++) {
					// Only create operations for meps with type parameters
					MessageExchangePattern mep=intf.getMessageExchangePatterns().get(i);
					
					if (mep.getTypes().size() > 0) {
						createBindingOperation(wsdls, contract, ret,
									mep, wsdlBinding, handler);
					}
				}

				// Only add portType to definition if they have atleast one operation
				if (ret != null && ret.getBindingOperations().size() > 0) {
					defn.addBinding(ret);
				}
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method generates an operation, using the supplied WSDL definition,
	 * based on the information in the supplied message exchange pattern.
	 * 
	 * @param wsdls The list of current WSDL definitions
	 * @param contract The contract
	 * @param portType The port type
	 * @param mep The message exchange pattern
	 * @return The WSDL operation
	 */
	public javax.wsdl.Operation createOperation(java.util.List<javax.wsdl.Definition> wsdls,
			org.savara.contract.model.Contract contract, javax.wsdl.PortType portType,
								org.savara.contract.model.MessageExchangePattern mep,
								WSDLBinding wsdlBinding, FeedbackHandler handler) {
		javax.wsdl.Operation ret=null;
		
		javax.wsdl.Definition defn=null;
		
		if (portType != null) {
			defn = getDefinition(wsdls, contract, portType.getQName().getNamespaceURI(), wsdlBinding);
		}

		if (defn != null && mep != null) {
			ret = defn.createOperation();
			ret.setUndefined(false);
			
			ret.setName(mep.getOperation());
			
			QName msgname=WSDLGeneratorUtil.getRequestMessageType(portType.getQName().getNamespaceURI(),
								mep.getOperation(), null);
			
			javax.wsdl.Message mesg=getMessage(wsdls, contract, msgname,
								mep.getTypes(), wsdlBinding, handler);
			
			if (mesg != null) {
				javax.wsdl.Input input=defn.createInput();
				input.setMessage(mesg);
				ret.setInput(input);
			}
			
			// Check if a request/response MEP
			if (mep instanceof RequestResponseMEP) {
				RequestResponseMEP rr=(RequestResponseMEP)mep;
				
				msgname=WSDLGeneratorUtil.getResponseMessageType(portType.getQName().getNamespaceURI(),
								mep.getOperation(), null);
			
				javax.wsdl.Message om=getMessage(wsdls, contract, msgname,
									rr.getResponseTypes(), wsdlBinding, handler);
				if (om != null) {
					javax.wsdl.Output output=defn.createOutput();
					output.setMessage(om);
					ret.setOutput(output);
				}
				
				// Generate fault details
				if (rr.getFaultDetails() != null) {
					for (int i=0; i < rr.getFaultDetails().size(); i++) {
						FaultDetails fd=rr.getFaultDetails().get(i);
						
						msgname = WSDLGeneratorUtil.getFaultMessageType(portType.getQName().getNamespaceURI(),
												fd.getName(), null);
					
						javax.wsdl.Message fm=getMessage(wsdls, contract, msgname,
											fd.getTypes(), wsdlBinding, handler);
						if (fm != null) {
							javax.wsdl.Fault fault=defn.createFault();
							fault.setName(fd.getName());
							fault.setMessage(fm);
							
							ret.addFault(fault);
						}
					}
				}
			}
			
			portType.addOperation(ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method generates a binding operation, using the supplied WSDL definition,
	 * based on the information in the supplied message exchange pattern.
	 * 
	 * @param wsdls The list of current WSDL definitions
	 * @param contract The contract
	 * @param binding The port type binding
	 * @param mep The message exchange pattern
	 * @return The WSDL binding operation
	 */
	public javax.wsdl.BindingOperation createBindingOperation(java.util.List<javax.wsdl.Definition> wsdls,
			org.savara.contract.model.Contract contract, javax.wsdl.Binding binding,
								org.savara.contract.model.MessageExchangePattern mep,
								WSDLBinding wsdlBinding, FeedbackHandler handler) {
		javax.wsdl.BindingOperation ret=null;
		
		javax.wsdl.Definition defn=null;
		
		if (binding != null) {
			defn = getDefinition(wsdls, contract, binding.getQName().getNamespaceURI(), wsdlBinding);
		}

		if (defn != null && mep != null) {
			ret = defn.createBindingOperation();
			//ret.setUndefined(false);
			
			// Check if WSDL operation details
			if (wsdlBinding != null) {
				wsdlBinding.updateOperation(defn, mep, ret);
			}
			
			ret.setName(mep.getOperation());
			
			QName msgname=WSDLGeneratorUtil.getRequestMessageType(binding.getQName().getNamespaceURI(),
											mep.getOperation(), null);
		
			javax.wsdl.Message mesg=getMessage(wsdls, contract,msgname,
								mep.getTypes(), wsdlBinding, handler);
			
			if (mesg != null) {
				javax.wsdl.BindingInput input=defn.createBindingInput();
				
				// Check if WSDL operation details
				if (wsdlBinding != null) {
					wsdlBinding.updateInput(defn, input);
				}

				ret.setBindingInput(input);
			}
			
			// Check if a request/response MEP
			if (mep instanceof RequestResponseMEP) {
				RequestResponseMEP rr=(RequestResponseMEP)mep;
				
				msgname=WSDLGeneratorUtil.getResponseMessageType(binding.getQName().getNamespaceURI(),
										mep.getOperation(), null);
	
				javax.wsdl.Message om=getMessage(wsdls, contract, msgname,
									rr.getResponseTypes(), wsdlBinding, handler);
				if (om != null) {
					javax.wsdl.BindingOutput output=defn.createBindingOutput();
					
					// Check if WSDL operation details
					if (wsdlBinding != null) {
						wsdlBinding.updateOutput(defn, output);
					}

					ret.setBindingOutput(output);
				}
				
				// Generate fault details
				if (rr.getFaultDetails() != null) {
					for (int i=0; i < rr.getFaultDetails().size(); i++) {
						FaultDetails fd=rr.getFaultDetails().get(i);
						
						msgname = WSDLGeneratorUtil.getFaultMessageType(binding.getQName().getNamespaceURI(),
												fd.getName(), null);
			
						javax.wsdl.Message fm=getMessage(wsdls, contract, msgname,
									fd.getTypes(), wsdlBinding, handler);
						if (fm != null) {
							javax.wsdl.BindingFault fault=defn.createBindingFault();
							fault.setName(fd.getName());
							
							// Check if WSDL operation details
							if (wsdlBinding != null) {
								wsdlBinding.updateFault(defn, fault);
							}

							ret.addBindingFault(fault);
						}
					}
				}
			}
			
			binding.addBindingOperation(ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns a message, using the supplied WSDL definition,
	 * based on the information supplied in the list of type references. If
	 * a single type reference is supplied, this will be the message type,
	 * if multiple type references are supplied, then these will be considered
	 * the message parts.<p>
	 * <p>
	 * The returned message will be part of the supplied definition. If it
	 * does not exist prior to the call to this method, then it will be
	 * created and added to the definition upon returning the message.
	 * 
	 * @param wsdls The list of current WSDL definitions
	 * @param contract The contract
	 * @param msgname The message name
	 * @param types The list of type references
	 * @return The WSDL message
	 */
	public javax.wsdl.Message getMessage(java.util.List<javax.wsdl.Definition> wsdls,
						org.savara.contract.model.Contract contract,
						javax.xml.namespace.QName msgname,
						java.util.List<org.savara.contract.model.Type> types,
								WSDLBinding wsdlBinding, FeedbackHandler handler) {
		javax.wsdl.Message ret=null;
		
		if (types == null || types.size() == 0) {
			throw new UnsupportedOperationException("Expecting single type reference");
		} else if (types.size() > 1) {
			throw new UnsupportedOperationException("Currently only supports single type reference");			
		} else {
			Type ref=types.get(0);
			
			TypeDefinition td=contract.getTypeDefinition(ref.getName());
				
			if (td != null && TypeSystem.XSD.equals(td.getTypeSystem())) {
				javax.xml.namespace.QName qname=
					javax.xml.namespace.QName.valueOf(td.getDataType());
				javax.wsdl.Definition defn=
					getDefinition(wsdls, contract, msgname.getNamespaceURI(), wsdlBinding);
						
				if (defn != null && qname != null &&
						(ret = defn.getMessage(msgname)) == null) {
					ret = defn.createMessage();
					ret.setUndefined(false);
					
					ret.setQName(msgname);
					
					Part part=createPart(defn, td, qname,
							wsdlBinding, handler);
					
					ret.addPart(part);
					
					defn.addMessage(ret);
				}
			}
		}
		
		return(ret);
	}
	
	protected Part createPart(javax.wsdl.Definition defn, TypeDefinition td,
					javax.xml.namespace.QName qname, WSDLBinding wsdlBinding, FeedbackHandler handler) {
		// Create single part for type or element
		Part part=defn.createPart();
		part.setName("content");
		
		if (AnnotationDefinitions.getAnnotation(td.getAnnotations(),
						AnnotationDefinitions.XSD_ELEMENT) != null) {
			part.setElementName(qname);					
		} else {
			part.setTypeName(qname);
			
			if (!wsdlBinding.isXSDTypeMessagePartSupported()) {
				// Raise error
				handler.error(MessageFormatter.format("org.savara.wsdl",
							"SAVARA-WSDL-00001", wsdlBinding.getName(), qname.toString()), null);
			}
		}

		return(part);
	}
}
