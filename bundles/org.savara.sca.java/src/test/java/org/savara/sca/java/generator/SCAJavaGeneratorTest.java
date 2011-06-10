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

import org.junit.Test;

public class SCAJavaGeneratorTest {

	private static final String SRC_PATH = System.getProperty("java.io.tmpdir")+"/savara/sca";
	private static final String WSDL_LOCATION = "wsdl/PurchaseGoodsProcess_Store.wsdl";

	@Test
	public void testGenerateServiceInterfaceFromWSDL() {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(WSDL_LOCATION);
			java.io.InputStream is=ClassLoader.getSystemClassLoader().getResourceAsStream("expected/StoreInterface.txt");
			
			gen.createServiceInterfaceFromWSDL(url.getFile(), WSDL_LOCATION, SRC_PATH);
			
			compare("expected/StoreInterface.java.txt",
					SRC_PATH+"/org/jboss/examples/store/StoreInterface.java");
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
		generatedContent = removeComments(generatedContent);
		
		if (expectedContent.equals(generatedContent) == false) {
			System.out.println(">> expected=");
			System.out.println(expectedContent);
			System.out.println(">> generated=");
			System.out.println(generatedContent);
			System.out.println(">> end");
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
