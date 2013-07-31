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
package org.savara.sca.java.generator;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.scribble.protocol.model.Role;

public class SCAJavaGeneratorTest {

	private static final String SRC_PATH = System.getProperty("java.io.tmpdir")+"/savara/sca";
	private static final String STORE_WSDL_LOCATION = "wsdl/PurchaseGoodsProcess_Store.wsdl";
	private static final String LOGISTICS_WSDL_LOCATION = "wsdl/PurchaseGoodsProcess_Logistics.wsdl";
	private static final String CREDITAGENCY_WSDL_LOCATION = "wsdl/PurchaseGoodsProcess_CreditAgency.wsdl";

	@Test
	public void testGenerateStoreServiceInterfaceFromWSDL() {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), STORE_WSDL_LOCATION, SRC_PATH);
			
			compare("expected/StoreInterface.java.txt",
					SRC_PATH+"/org/jboss/examples/store/StoreInterface.java");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate interface: "+e);
		}
	}

	@Test
	public void testGenerateStoreServiceImplementationFromWSDL() {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			java.util.List<Role> refRoles=new java.util.Vector<Role>();
			refRoles.add(new Role("CreditAgency"));
			refRoles.add(new Role("Logistics"));
			
			java.util.List<String> refWsdlPaths=new java.util.Vector<String>();
			refWsdlPaths.add(ClassLoader.getSystemClassLoader().getResource(CREDITAGENCY_WSDL_LOCATION).getFile());
			refWsdlPaths.add(ClassLoader.getSystemClassLoader().getResource(LOGISTICS_WSDL_LOCATION).getFile());
			
			gen.createServiceImplementationFromWSDL(new Role("Store"), refRoles,
					url.getFile(), STORE_WSDL_LOCATION, refWsdlPaths, SRC_PATH);
			
			compare("expected/StoreInterfaceImpl.java.txt",
					SRC_PATH+"/org/jboss/examples/store/StoreInterfaceImpl.java");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate interface: "+e);
		}
	}

	@Test
	public void testGenerateStoreServiceCompositeJustService() {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			gen.createServiceComposite(new Role("Store"), new java.util.Vector<Role>(),
					url.getFile(), new java.util.Vector<String>(), SRC_PATH);
			
			compare("expected/Store.composite",
					SRC_PATH+"/Store.composite");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate Store composite: "+e);
		}
	}

	@Test
	public void testGenerateStoreServiceCompositeWithReferences() {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			java.util.List<Role> refRoles=new java.util.Vector<Role>();
			refRoles.add(new Role("CreditAgency"));
			refRoles.add(new Role("Logistics"));
			
			java.util.List<String> refWsdlPaths=new java.util.Vector<String>();
			refWsdlPaths.add(ClassLoader.getSystemClassLoader().getResource(CREDITAGENCY_WSDL_LOCATION).getFile());
			refWsdlPaths.add(ClassLoader.getSystemClassLoader().getResource(LOGISTICS_WSDL_LOCATION).getFile());
			
			gen.createServiceComposite(new Role("Store"), refRoles,
					url.getFile(), refWsdlPaths, SRC_PATH);
			
			compare("expected/StoreWithReferences.composite",
					SRC_PATH+"/Store.composite");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate Store composite: "+e);
		}
	}

	@Test
	public void testGenerateAllServiceInterfaceFromWSDL() {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), STORE_WSDL_LOCATION, SRC_PATH);

			url=ClassLoader.getSystemClassLoader().getResource(CREDITAGENCY_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), CREDITAGENCY_WSDL_LOCATION, SRC_PATH);

			url=ClassLoader.getSystemClassLoader().getResource(LOGISTICS_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), LOGISTICS_WSDL_LOCATION, SRC_PATH);
			
			compare("expected/StoreInterface.java.txt",
					SRC_PATH+"/org/jboss/examples/store/StoreInterface.java");
			
			compare("expected/CreditAgencyInterface.java.txt",
					SRC_PATH+"/org/jboss/examples/creditagency/CreditAgencyInterface.java");
			
			compare("expected/LogisticsInterface.java.txt",
					SRC_PATH+"/org/jboss/examples/logistics/LogisticsInterface.java");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate interface: "+e);
		}
	}

	protected void compare(String expected, String generated) throws Exception {
		java.io.InputStream is=ClassLoader.getSystemClassLoader().getResourceAsStream(expected);
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();
		
		String expectedContent=new String(b);
		
		is = new java.io.FileInputStream(generated);
		
		b=new byte[is.available()];
		is.read(b);
		
		is.close();
		
		String generatedContent=new String(b);
		
		// Remove comment blocks
		expectedContent = removeComments(expectedContent);
		expectedContent = expectedContent.replaceAll("\r\n", "\n");
		
		generatedContent = removeComments(generatedContent);
		generatedContent = generatedContent.replaceAll("\r\n", "\n");
		
		if (expectedContent.equals(generatedContent) == false) {
			System.out.println(">> expected=");
			System.out.println(expectedContent);
			System.out.println(">> generated=");
			System.out.println(generatedContent);
			System.out.println(">> end");
			
			if (expectedContent.length() != generatedContent.length()) {
				System.out.println("LENGTHS are different: expected="+expectedContent.length()+
						" generated="+generatedContent.length());
			}
			for (int i=0; i < expectedContent.length() && i < generatedContent.length(); i++) {
				if (expectedContent.charAt(i) != generatedContent.charAt(i)) {
					System.out.println("MISMATCH at character "+i);
					int end=i+10;
					if (end >= generatedContent.length()) {
						end = generatedContent.length()-1;
					}
					System.out.println(" GENERATED starting \""+generatedContent.substring(i, end)+"\"");
					end = i+10;
					if (end >= expectedContent.length()) {
						end = expectedContent.length()-1;
					}
					System.out.println(" EXPECTED starting \""+expectedContent.substring(i, end)+"\"");
					break;
				}
			}
			fail("Content does not match");
		}
	}
	
	protected String removeComments(String text) {
		StringBuffer ret=new StringBuffer();
		
		int fromIndex=0;
		int startIndex=0;
		
		while ((startIndex=text.indexOf("/*", fromIndex)) != -1) {
			ret.append(text.substring(fromIndex, startIndex));
			
			// Find end of comment block
			int endIndex=text.indexOf("*/", startIndex);
			
			fromIndex = endIndex+2;
		}
		
		if (fromIndex < text.length()) {
			ret.append(text.substring(fromIndex));
		}
		
		return(ret.toString());
	}
}
