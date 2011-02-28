/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.common.util;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

public class MessageUtils {

	private static Logger logger = Logger.getLogger(MessageUtils.class.getName());

	/**
	 * This method returns the type name associated with the supplied message
	 * value. This method assumes the value is either XML based, or a Java
	 * type.
	 * 
	 * @param value The value
	 * @return The type name
	 */
	public static String getMessageType(Object value) {
		String ret=null;
		
		if (value instanceof org.w3c.dom.Node) {
			String namespace=((org.w3c.dom.Node)value).getNamespaceURI();
			String localpart=((org.w3c.dom.Node)value).getLocalName();
			
			if (value instanceof org.w3c.dom.Element &&
					((org.w3c.dom.Element)value).hasAttributeNS(
							"http://www.w3.org/2001/XMLSchema-instance", "type")) {
				String type=((org.w3c.dom.Element)value).getAttributeNS(
						"http://www.w3.org/2001/XMLSchema-instance", "type");
				
				String prefix=XMLUtils.getPrefix(type);
				
				localpart = XMLUtils.getLocalname(type);

				if (prefix == null) {
					namespace = null;
				} else {
					namespace = ((org.w3c.dom.Element)value).
							getAttribute("xmlns:"+prefix);
					if (namespace != null && namespace.trim().length() == 0) {
						namespace = null;
					}
				}
			}
			
			if (namespace == null) {
				ret = localpart;
			} else {
				ret = new QName(namespace, localpart).toString();
			}
		} else if (value instanceof String) {
			ret = String.class.getName();
			
			try {
				org.w3c.dom.Node node=XMLUtils.getNode((String)value);
				
				ret = getMessageType(node);
			} catch(Exception e) {
				logger.warning("Failed to obtain message type from value: "+value);
			}
			
		} else if (value != null) {
			ret = value.getClass().getName();
		}
		
		return(ret);
	}

}
