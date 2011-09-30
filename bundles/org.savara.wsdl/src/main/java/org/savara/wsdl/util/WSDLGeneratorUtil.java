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
package org.savara.wsdl.util;

import org.scribble.protocol.model.Role;

/**
 * This class provides general utility functions for
 * use when generating artefacts.
 */
public class WSDLGeneratorUtil {

	private static final String RESPONSE = "Response";
	private static final String FAULT = "Fault";
	private static final String REQUEST = "Request";

	/**
	 * This method returns the request message type QName.
	 * 
	 * @param intfNamespace The interface namespace
	 * @param op The operation name
	 * @param prefix The optional prefix
	 * @return The request message type
	 */
	public static javax.xml.namespace.QName getRequestMessageType(String intfNamespace, String op, String prefix) {
		javax.xml.namespace.QName ret=null;
		if (prefix != null) {
			ret = new javax.xml.namespace.QName(intfNamespace, op+REQUEST, prefix);
		} else {
			ret = new javax.xml.namespace.QName(intfNamespace, op+REQUEST);
		}
		return(ret);
	}

	/**
	 * This method returns the response message type QName.
	 * 
	 * @param intfNamespace The interface namespace
	 * @param op The operation name
	 * @param prefix The optional prefix
	 * @return The response message type
	 */
	public static javax.xml.namespace.QName getResponseMessageType(String intfNamespace, String op, String prefix) {
		javax.xml.namespace.QName ret=null;
		if (prefix != null) {
			ret = new javax.xml.namespace.QName(intfNamespace, op+RESPONSE, prefix);
		} else {
			ret = new javax.xml.namespace.QName(intfNamespace, op+RESPONSE);
		}
		return(ret);
	}

	/**
	 * This method returns the fault message type QName.
	 * 
	 * @param intfNamespace The interface namespace
	 * @param faultName The fault name
	 * @param prefix The optional prefix
	 * @return The fault message type
	 */
	public static javax.xml.namespace.QName getFaultMessageType(String intfNamespace, String faultName, String prefix) {
		javax.xml.namespace.QName ret=null;
		if (prefix != null) {
			ret = new javax.xml.namespace.QName(intfNamespace, faultName+FAULT, prefix);
		} else {
			ret = new javax.xml.namespace.QName(intfNamespace, faultName+FAULT);
		}
		return(ret);
	}
	
	/**
	 * This method returns the WSDL file name for the supplied role and local
	 * conversation model.
	 * 
	 * @param role The role
	 * @param localcm The local conversation model
	 * @param fileNum The file name (zero being the main wsdl file)
	 * @return The file name
	 */
	public static String getWSDLFileName(Role role, String modelName, String suffix) {
		return(modelName+"_"+role.getName()+suffix+".wsdl");
	}
			
}
