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
package org.savara.bpmn2.model.util;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.savara.bpmn2.model.TDefinitions;

public class BPMN2ModelUtil {

	/**
	 * This method deserializes the XML representation of the BPMN2 model.
	 * 
	 * @param is The input stream
	 * @return The BPMN2 definition
	 * @throws IOException Failed to deserialize
	 */
	public static TDefinitions deserialize(java.io.InputStream is) throws IOException {
		TDefinitions ret=null;
		
		try {
			JAXBContext context = JAXBContext.newInstance("org.savara.bpmn2.model",
					BPMN2ModelUtil.class.getClassLoader());
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			//note: setting schema to null will turn validator off
			//unmarshaller.setSchema(null);
			Object xmlObject = unmarshaller.unmarshal(is);
			
			if (xmlObject instanceof JAXBElement) {
				ret = (TDefinitions)((JAXBElement<?>)xmlObject).getValue();
			}
		} catch(Exception e) {
			throw new IOException("Failed to deserialize BPMN2 definitions", e);
		}
		
		return(ret);
	}
	
	/**
	 * This method serializes the BPMN2 model into an XML representation.
	 * 
	 * @param defns The BPMN2 model
	 * @param os The output stream
	 * @throws IOException Failed to serialize
	 */
	public static void serialize(TDefinitions defns, java.io.OutputStream os) throws IOException {
		serialize(defns, os, BPMN2ModelUtil.class.getClassLoader());
	}

	/**
	 * This method serializes the BPMN2 model into an XML representation.
	 * 
	 * @param defns The BPMN2 model
	 * @param os The output stream
	 * @param classLoader The classloader to be used
	 * @throws IOException Failed to serialize
	 */
	public static void serialize(TDefinitions defns, java.io.OutputStream os,
					java.lang.ClassLoader classLoader) throws IOException {
		
		try {
			org.savara.bpmn2.model.ObjectFactory factory=
						new org.savara.bpmn2.model.ObjectFactory();
			
			JAXBContext context = JAXBContext.newInstance("org.savara.bpmn2.model",
					classLoader);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// SAVARA-175/378 - jaxb serialization places the prefix/namespace declaration
			// on the same element as it is used. Therefore we need to reparse/serialize
			// the XML document to promote the namespaces to the top of the document
			java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
			
			marshaller.marshal(factory.createDefinitions(defns), baos);
			
			// Convert to DOM
			javax.xml.parsers.DocumentBuilderFactory dbfactory=
							javax.xml.parsers.DocumentBuilderFactory.newInstance();
			dbfactory.setNamespaceAware(false);
			
			org.w3c.dom.Document doc=
				dbfactory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
			
			doc.normalizeDocument();
			
			javax.xml.transform.dom.DOMSource source=new javax.xml.transform.dom.DOMSource(doc);
			javax.xml.transform.stream.StreamResult result=new javax.xml.transform.stream.StreamResult(os);
			
			javax.xml.transform.Transformer transformer=
					javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);
				
		} catch(Exception e) {
			throw new IOException("Failed to serialize BPMN2 definitions", e);
		}
	}
}
