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

import static org.junit.Assert.*;

import org.junit.Test;
//import org.savara.bpmn2.model.TImport;
import org.savara.bpmn2.model.util.BPMN2ModelUtil;

public class BPMN2ModelJSONUtilTest {

	@Test
	public void testGetDefinitions() {
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream("testmodels/bpmn2/choreo/PurchaseGoods.bpmn");

		try {
			org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
			if (defns == null) {
				fail("No definitions returned");
			}

			java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
			
			BPMN2ModelJSONUtil.serialize(defns, baos);
			
			baos.close();
			
			String json=new String(baos.toByteArray());
			
			System.out.println("JSON:\r\n"+json);
			
			java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(baos.toByteArray());
			
			org.savara.bpmn2.model.TDefinitions defns2=BPMN2ModelJSONUtil.deserialize(bais);
			
			bais.close();
						
			// Create namespace prefix mapping
			/*
			java.util.Map<String, String> prefixes=new java.util.HashMap<String, String>();
			prefixes.put(defns.getTargetNamespace(), "tns");
			int index=1;
			for (TImport imp : defns.getImport()) {
				prefixes.put( imp.getNamespace(), "ns"+(index++));
			}
			*/
			
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			
			BPMN2ModelUtil.serialize(defns2, os, null);
			
			String xml=new String(os.toByteArray());
			
			os.close();
			

			// Check result against stored version
			java.io.InputStream is2=
					ClassLoader.getSystemResourceAsStream("results/bpmn2/choreo/xml/PurchaseGoods.bpmn");

			byte[] b=new byte[is2.available()];
			is2.read(b);
			
			is2.close();
			
			String xml2=new String(b);
			
			if (!xml.equals(xml2)) {
				
				java.io.InputStream is3=
						ClassLoader.getSystemResourceAsStream("results/bpmn2/choreo/xml/PurchaseGoods-v2.bpmn");

				b=new byte[is3.available()];
				is3.read(b);
				
				is3.close();
				
				String xml3=new String(b);
				
				if (!xml.equals(xml3)) {
					System.out.println("GENERATED=\r\n"+xml+"\r\nEXPECTED=\r\n"+xml2
							+"\r\nOR ALTERNATIVELY=\r\n"+xml3);
					
					fail("XML serialization of BPMN2 model does not match stored");
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load BPMN2 definition: "+e);
		}
	}
}
