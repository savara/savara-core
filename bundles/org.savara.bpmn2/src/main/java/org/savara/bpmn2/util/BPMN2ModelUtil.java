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
package org.savara.bpmn2.util;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.savara.bpmn2.model.TDefinitions;

public class BPMN2ModelUtil {

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
	
	public static void serialize(TDefinitions defns, java.io.OutputStream os) throws IOException {
		
		try {
			org.savara.bpmn2.model.ObjectFactory factory=
						new org.savara.bpmn2.model.ObjectFactory();
			
			JAXBContext context = JAXBContext.newInstance(TDefinitions.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			marshaller.marshal(factory.createDefinitions(defns), os);
		} catch(Exception e) {
			throw new IOException("Failed to serialize BPMN2 definitions", e);
		}
	}
}
