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
package org.savara.wsdl.generator.soap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.savara.wsdl.generator.WSDLBinding;

/**
 * This interface is used to establish binding information on a
 * WSDL definition.
 *
 */
public class SOAPRPCWSDLBinding implements WSDLBinding {
	private static Log logger = LogFactory.getLog(SOAPRPCWSDLBinding.class);

	/**
	 * This method returns the name of the WSDL binding implementation.
	 * 
	 * @return The WSDL binding
	 */
	public String getName() {
		return("SOAP RPC");
	}
	
	/**
	 * This method initializes the WSDL definition for the binding.
	 * 
	 * @param defn The definition
	 */
	public void initDefinition(javax.wsdl.Definition defn) {
		defn.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
	}
	
	/**
	 * This method adds WSDL binding information to the port binding.
	 * 
	 * @param defn The definition
	 * @param binding The port type
	 */
	public void updateBinding(javax.wsdl.Definition defn, javax.wsdl.Binding binding) {
		try {
			javax.wsdl.extensions.soap.SOAPBinding sb=(javax.wsdl.extensions.soap.SOAPBinding)
				defn.getExtensionRegistry().createExtension(javax.wsdl.Binding.class,
							new javax.xml.namespace.QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding"));
			sb.setStyle("rpc");
			sb.setTransportURI("http://schemas.xmlsoap.org/soap/http");
			binding.addExtensibilityElement(sb);
		} catch(Exception e) {
			logger.error("Failed to add SOAP binding", e);
		}
	}
	
	/**
	 * This method adds WSDL binding information to the operation.
	 * 
	 * @param defn The definition
	 * @param mep The message exchange pattern
	 * @param operation The operation
	 */
	public void updateOperation(javax.wsdl.Definition defn,
			org.savara.contract.model.MessageExchangePattern mep,
			javax.wsdl.BindingOperation operation) {
		try {
			javax.wsdl.extensions.soap.SOAPOperation soap=(javax.wsdl.extensions.soap.SOAPOperation)
				defn.getExtensionRegistry().createExtension(javax.wsdl.BindingOperation.class,
							new javax.xml.namespace.QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation"));
			soap.setStyle("rpc");
			soap.setSoapActionURI("");
			operation.addExtensibilityElement(soap);
		} catch(Exception e) {
			logger.error("Failed to add SOAP operation", e);
		}
	}
	
	/**
	 * This method adds WSDL binding information to the input.
	 * 
	 * @param defn The definition
	 * @param input The input
	 */
	public void updateInput(javax.wsdl.Definition defn, javax.wsdl.BindingInput input) {
		try {
			javax.wsdl.extensions.soap.SOAPBody soap=(javax.wsdl.extensions.soap.SOAPBody)
				defn.getExtensionRegistry().createExtension(javax.wsdl.BindingInput.class,
							new javax.xml.namespace.QName("http://schemas.xmlsoap.org/wsdl/soap/", "body"));
			soap.setUse("literal");
			soap.setNamespaceURI(defn.getTargetNamespace());
			input.addExtensibilityElement(soap);
		} catch(Exception e) {
			logger.error("Failed to add SOAP body to input", e);
		}
	}
	
	/**
	 * This method adds WSDL binding information to the output.
	 * 
	 * @param defn The definition
	 * @param output The output
	 */
	public void updateOutput(javax.wsdl.Definition defn, javax.wsdl.BindingOutput output) {
		try {
			javax.wsdl.extensions.soap.SOAPBody soap=(javax.wsdl.extensions.soap.SOAPBody)
				defn.getExtensionRegistry().createExtension(javax.wsdl.BindingOutput.class,
							new javax.xml.namespace.QName("http://schemas.xmlsoap.org/wsdl/soap/", "body"));
			soap.setUse("literal");
			soap.setNamespaceURI(defn.getTargetNamespace());
			output.addExtensibilityElement(soap);
		} catch(Exception e) {
			logger.error("Failed to add SOAP body to output", e);
		}
	}
	
	/**
	 * This method adds WSDL binding information to the fault.
	 * 
	 * @param defn The definition
	 * @param fault The fault
	 */
	public void updateFault(javax.wsdl.Definition defn, javax.wsdl.BindingFault fault) {
		try {
			javax.wsdl.extensions.soap.SOAPFault soap=(javax.wsdl.extensions.soap.SOAPFault)
				defn.getExtensionRegistry().createExtension(javax.wsdl.BindingFault.class,
							new javax.xml.namespace.QName("http://schemas.xmlsoap.org/wsdl/soap/", "fault"));
			soap.setUse("literal");
			soap.setNamespaceURI(defn.getTargetNamespace());
			soap.setName(fault.getName());
			
			fault.addExtensibilityElement(soap);
		} catch(Exception e) {
			logger.error("Failed to add SOAP body to fault", e);
		}
	}
	
	/**
	 * This method adds WSDL binding information to the fault.
	 * 
	 * @param defn The definition
	 * @param port The port
	 */
	public void updatePort(javax.wsdl.Definition defn, javax.wsdl.Port port) {
		try {
			javax.wsdl.extensions.soap.SOAPAddress soap=(javax.wsdl.extensions.soap.SOAPAddress)
				defn.getExtensionRegistry().createExtension(javax.wsdl.Port.class,
							new javax.xml.namespace.QName("http://schemas.xmlsoap.org/wsdl/soap/", "address"));
			soap.setLocationURI("http://localhost:8080/"+defn.getQName().getLocalPart()+"Service/"+
								port.getName());
			
			port.addExtensibilityElement(soap);
		} catch(Exception e) {
			logger.error("Failed to add SOAP address to port", e);
		}
	}
	
	/**
	 * This method determines whether the binding supports the use of XSD types
	 * in the message parts.
	 * 
	 * @return Whether XSD types are supported in message parts
	 */
	public boolean isXSDTypeMessagePartSupported() {
		return(true);
	}
	
}
