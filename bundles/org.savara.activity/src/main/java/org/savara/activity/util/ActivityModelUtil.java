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
package org.savara.activity.util;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.savara.activity.model.Activity;

public class ActivityModelUtil {

	public static Activity deserialize(java.io.InputStream is) throws IOException {
		Activity ret=null;
		
		try {
			JAXBContext context = JAXBContext.newInstance("org.savara.activity.model",
					ActivityModelUtil.class.getClassLoader());
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			//note: setting schema to null will turn validator off
			//unmarshaller.setSchema(null);
			Object xmlObject = unmarshaller.unmarshal(is);
			
			if (xmlObject instanceof JAXBElement) {
				ret = (Activity)((JAXBElement<?>)xmlObject).getValue();
			}
			
		} catch(Exception e) {
			throw new IOException("Failed to deserialize activity", e);
		}
		
		return(ret);
	}
	
	public static void serialize(Activity activity, java.io.OutputStream os) throws IOException {
		
		try {
			org.savara.activity.model.ObjectFactory factory=
						new org.savara.activity.model.ObjectFactory();
			
			JAXBContext context = JAXBContext.newInstance(Activity.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			marshaller.marshal(factory.createActivity(activity), os);
		} catch(Exception e) {
			throw new IOException("Failed to serialize activity", e);
		}
	}
}
