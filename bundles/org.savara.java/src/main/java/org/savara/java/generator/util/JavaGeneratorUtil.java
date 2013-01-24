/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.java.generator.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.savara.common.resources.ResourceLocator;

/**
 * This class provides Java generation utility functions.
 *
 */
public class JavaGeneratorUtil {
	
	private static final Logger LOG=Logger.getLogger(JavaGeneratorUtil.class.getName());

	/**
	 * This method identifies the java type associated with the supplied element within a
	 * schema defined by the location.
	 * 
	 * @param element The element
	 * @param location The location of the schema
	 * @param locator The locator used to retrieve the schema
	 * @return The java type, or null if not found
	 */
	public static String getElementJavaType(QName element, String location, ResourceLocator locator) {
		String ret=null;
		
		try {
			java.net.URI uri=locator.getResourceURI(location);
			
			DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			
			DocumentBuilder builder=fact.newDocumentBuilder();
			
			java.io.InputStream is=uri.toURL().openStream();
			
			org.w3c.dom.Document doc=builder.parse(is);
			
			is.close();
			
			org.w3c.dom.NodeList elemList=
					doc.getDocumentElement().getElementsByTagNameNS(
							"http://www.w3.org/2001/XMLSchema", "element");
			
			for (int i=0; i < elemList.getLength(); i++) {
				org.w3c.dom.Element elem=(org.w3c.dom.Element)elemList.item(i);
				
				String name=elem.getAttribute("name");
				String elemType=elem.getAttribute("type");
				
				if (!elem.hasAttribute("type")) {
					// Assume element has an inline type definition, so use element name
					elemType = name;
					
					if (elemType.length() > 0) {
						elemType = Character.toUpperCase(elemType.charAt(0))+elemType.substring(1);
					}
				}
				
				if (name.equals(element.getLocalPart())) {
					String prefix=org.savara.common.util.XMLUtils.getPrefix(elemType);
					String ns=null;
					
					if (prefix != null && prefix.trim().length() > 0) {
						ns = elem.lookupNamespaceURI(prefix);
					} else {
						ns = elem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
					}
					
					ret = getJavaPackage(ns);
					ret += "."+org.savara.common.util.XMLUtils.getLocalname(elemType);
				}
			}
			
			if (ret == null) {
				org.w3c.dom.NodeList includeList=
						doc.getDocumentElement().getElementsByTagNameNS(
								"http://www.w3.org/2001/XMLSchema", "include");
				
				for (int i=0; ret == null && i < includeList.getLength(); i++) {
					org.w3c.dom.Element elem=(org.w3c.dom.Element)includeList.item(i);
					
					String schemaLocation=elem.getAttribute("schemaLocation");
					
					java.io.File f=new java.io.File(location);
					
					if (f.getParentFile() != null) {
						schemaLocation = f.getParentFile().getPath()+java.io.File.separator+schemaLocation;
					}
					
					ret = getElementJavaType(element, schemaLocation, locator);
				}
			}

		} catch(Exception e) {
			LOG.log(Level.SEVERE, "Failed to obtain XSD schema '"+location+"'", e);
		}
		
		return (ret);
	}

	/**
	 * This method converts the supplied namespace into a Java package.
	 * 
	 * @param namespace The namespace
	 * @return The Java package
	 */
	public static String getJavaPackage(String namespace) {
		String ret=null;
		
		try {
			java.net.URI uri=new java.net.URI(namespace);
			
			String host=uri.getHost();
			
			// Removing preceding www
			if (host.startsWith("www.")) {
				host = host.substring(4);
			}
			
			// Place the suffix at the beginning
			int index=host.lastIndexOf('.');
			
			if (index != -1) {
				ret = host.substring(index+1);
				
				ret += "."+host.substring(0, index);
			} else {
				ret = host;
			}
			
			ret += uri.getPath().replace('/', '.');
			
			ret = ret.toLowerCase();
			
		} catch(Exception e) {
			LOG.log(Level.SEVERE, "Failed to get java package from namespace '"+namespace+"'", e);
		}
		
		return(ret);
	}
	
}
