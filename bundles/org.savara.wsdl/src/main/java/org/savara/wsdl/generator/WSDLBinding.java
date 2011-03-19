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
package org.savara.wsdl.generator;

/**
 * This interface is used to establish binding information on a
 * WSDL definition.
 *
 */
public interface WSDLBinding {

	/**
	 * This method returns the name of the WSDL binding implementation.
	 * 
	 * @return The WSDL binding
	 */
	public String getName();
	
	/**
	 * This method initializes the WSDL definition for the binding.
	 * 
	 * @param defn The definition
	 */
	public void initDefinition(javax.wsdl.Definition defn);
	
	/**
	 * This method adds WSDL binding information to the port binding.
	 * 
	 * @param defn The definition
	 * @param binding The port type
	 */
	public void updateBinding(javax.wsdl.Definition defn, javax.wsdl.Binding binding);
	
	/**
	 * This method adds WSDL binding information to the operation.
	 * 
	 * @param defn The definition
	 * @param mep The message exchange pattern
	 * @param operation The operation
	 */
	public void updateOperation(javax.wsdl.Definition defn,
			org.savara.contract.model.MessageExchangePattern mep,
			javax.wsdl.BindingOperation operation);
	
	/**
	 * This method adds WSDL binding information to the input.
	 * 
	 * @param defn The definition
	 * @param input The input
	 */
	public void updateInput(javax.wsdl.Definition defn, javax.wsdl.BindingInput input);
	
	/**
	 * This method adds WSDL binding information to the output.
	 * 
	 * @param defn The definition
	 * @param output The output
	 */
	public void updateOutput(javax.wsdl.Definition defn, javax.wsdl.BindingOutput output);
	
	/**
	 * This method adds WSDL binding information to the fault.
	 * 
	 * @param defn The definition
	 * @param fault The fault
	 */
	public void updateFault(javax.wsdl.Definition defn, javax.wsdl.BindingFault fault);
	
	/**
	 * This method adds WSDL binding information to the fault.
	 * 
	 * @param defn The definition
	 * @param port The port
	 */
	public void updatePort(javax.wsdl.Definition defn, javax.wsdl.Port port);
	
	/**
	 * This method determines whether the binding supports the use of XSD types
	 * in the message parts.
	 * 
	 * @return Whether XSD types are supported in message parts
	 */
	public boolean isXSDTypeMessagePartSupported();
	
}
