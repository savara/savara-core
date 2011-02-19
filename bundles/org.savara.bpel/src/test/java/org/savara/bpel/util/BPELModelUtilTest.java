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
package org.savara.bpel.util;

import junit.framework.TestCase;

import org.savara.bpel.model.TProcess;

public class BPELModelUtilTest extends TestCase {

	public void testDeserialize() {
		String filename="bpel/PolicyQuoteProcess_PolicyQuoteProcessService.bpel";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
	
		if (is == null) {
			fail("Failed to load BPEL model file '"+filename+"'");
		}
		
		try {
			TProcess process=BPELModelUtil.deserialize(is);
			
			if (process == null) {
				fail("Failed to retrieve BPEL process");
			}
		} catch(Exception e) {
			fail("Failed to deserialise BPEL process: "+e);
		}
	}	

	public void testSerialize() {
		String filename="bpel/SerializeTest.bpel";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
	
		if (is == null) {
			fail("Failed to load BPEL model file '"+filename+"'");
		}
		
		try {
			TProcess process=BPELModelUtil.deserialize(is);
			
			if (process == null) {
				fail("Failed to retrieve BPEL process");
			}
			
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			
			java.util.Map<String,String> prefixes=new java.util.HashMap<String, String>();
			
			//prefixes.put("http://docs.oasis-open.org/wsbpel/2.0/process/executable","bpel");
			prefixes.put("http://creditagency.com/creditCheckService","ca");
			prefixes.put("http://creditagency.com/creditCheck","cred");
			prefixes.put("http://dmv.com/drivingRecordService","dmv");
			prefixes.put("http://dmv.com/drivingRecord","drv");
			prefixes.put("http://www.example.org/policyQuoteCalculationService","pcs");
			prefixes.put("http://www.example.org/policyQuoteEntityService","pes");
			prefixes.put("http://www.example.org/policyQuote","pol");
			prefixes.put("http://www.example.org/policyQuoteProcessService","pps"); 
			prefixes.put("http://www.pi4soa.org/PolicyQuote","tns");
			prefixes.put("http://www.w3.org/2001/XMLSchema","xsd");
			prefixes.put("http://www.scribble.org/conversation","ns0"); 

			BPELModelUtil.serialize(process, os, prefixes);
			
			String serialized=new String(os.toByteArray());
			
			os.close();
			
			is = ClassLoader.getSystemResourceAsStream(filename);
		
			byte[] b=new byte[is.available()];
			is.read(b);
			
			String original=new String(b);
			
			if (serialized.equals(original) == false) {
				System.out.println("BPEL serialize: original (len="+original.length()+
						") ::"+original+":: serialized (len="+serialized.length()+") ::"+serialized+"::");
				
				fail("Serialized BPEL not same as original");
			}
			
		} catch(Exception e) {
			fail("Failed to serialise BPEL process: "+e);
		}
	}	

	public void testSerialize2() {
		try {
			String process="<?xml version=\"1.0\" encoding=\"UTF-8\"?><process xmlns=\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\" xmlns:test=\"http://www.savara.org/schema\">\r\n"+
				"    <variables>\r\n"+
				"        <variable messageType=\"test:receivePolicyQuoteRequest\" name=\"policyQuoteVar\"/>\r\n"+
				"        <variable messageType=\"test:receivePolicyQuoteRequest2\" name=\"policyQuoteVar2\"/>\r\n"+
				"    </variables>\r\n"+
				"</process>";

			TProcess desc=BPELModelUtil.deserialize(new java.io.ByteArrayInputStream(process.getBytes()));
			
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			
			java.util.Map<String,String> prefixes=new java.util.HashMap<String, String>();
			prefixes.put("http://www.savara.org/schema", "test");
			
			BPELModelUtil.serialize(desc, os, prefixes);
			
			String str=os.toString();
			str = str.replaceAll("\r\n", "\n");
			
			process = process.replaceAll("\r\n", "\n");
			
			if (process.equals(str) == false) {
				System.out.println("BPEL="+process);
				System.out.println("SERIALIZED="+str);
				fail("Serialized version does not match");
			} 
		} catch(Exception e) {
			fail("Failed to serialise BPEL process: "+e);
		}
	}

	public void testSerialize3() {
		try {
			String process="<?xml version=\"1.0\" encoding=\"UTF-8\"?><process xmlns=\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\" xmlns:test=\"http://www.savara.org/schema\"/>";

			TProcess desc=BPELModelUtil.deserialize(new java.io.ByteArrayInputStream(process.getBytes()));
			
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			
			java.util.Map<String,String> prefixes=new java.util.HashMap<String, String>();
			prefixes.put("http://www.savara.org/schema", "test");
			
			BPELModelUtil.serialize(desc, os, prefixes);
			
			String str=os.toString();
			str = str.replaceAll("\r\n", "\n");
			
			process = process.replaceAll("\r\n", "\n");
			
			if (process.equals(str) == false) {
				System.out.println("BPEL="+process);
				System.out.println("SERIALIZED="+str);
				fail("Serialized version does not match");
			} 
		} catch(Exception e) {
			fail("Failed to serialise BPEL process: "+e);
		}
	}
	
	public void testSerialize4() {
		try {
			String process="<?xml version=\"1.0\" encoding=\"UTF-8\"?><process xmlns=\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\" xmlns:test=\"http://www.savara.org/schema\">\r\n"+
				"    <variables>\r\n"+
				"        <variable messageType=\"test:receivePolicyQuoteRequest\" name=\"policyQuoteVar\"/>\r\n"+
				"        <variable xmlns:other=\"http://www.savara.org/schema\" messageType=\"other:receivePolicyQuoteRequest2\" name=\"policyQuoteVar2\"/>\r\n"+
				"    </variables>\r\n"+
				"</process>";

			TProcess desc=BPELModelUtil.deserialize(new java.io.ByteArrayInputStream(process.getBytes()));
			
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			
			java.util.Map<String,String> prefixes=new java.util.HashMap<String, String>();
			prefixes.put("http://www.savara.org/schema", "test");
			
			BPELModelUtil.serialize(desc, os, prefixes);
			
			String str=os.toString();
			str = str.replaceAll("\r\n", "\n");
			
			process = process.replaceAll("\r\n", "\n");
			
			if (process.equals(str) == false) {
				System.out.println("BPEL="+process);
				System.out.println("SERIALIZED="+str);
				fail("Serialized version does not match");
			} 
		} catch(Exception e) {
			fail("Failed to serialise BPEL process: "+e);
		}
	}

}
