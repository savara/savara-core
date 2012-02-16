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
package org.savara.switchyard.java.generator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.parser.antlr.ANTLRProtocolParser;

public class SwitchyardJavaGeneratorTest {

	private static final String SRC_PATH = System.getProperty("java.io.tmpdir")+"/savara/switchyard";
	private static final String STORE_WSDL_LOCATION = "wsdl/PurchaseGoods_Store.wsdl";
	private static final String LOGISTICS_WSDL_LOCATION = "wsdl/PurchaseGoods_Logistics.wsdl";
	private static final String CREDITAGENCY_WSDL_LOCATION = "wsdl/PurchaseGoods_CreditAgency.wsdl";

	public ProtocolModel getProtocol(String name, String roleName) {
		ProtocolModel ret=null;
		String filename="models/"+name+".spr";
		
		java.net.URL url=
			ClassLoader.getSystemResource(filename);
		
		if (url == null) {
			fail("Unable to locate resource: "+filename);
		} else {			
			DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
			Journal journal=new JournalProxy(handler);
			
			org.scribble.protocol.model.ProtocolModel model=null;
			
			ANTLRProtocolParser parser=new ANTLRProtocolParser();
			parser.setAnnotationProcessor(new org.savara.protocol.parser.AnnotationProcessor());
			
			try {
				Content content=new ResourceContent(url.toURI());
				
				model = parser.parse(null, content, journal);
			} catch(Exception e) {
				fail("Parsing protocol failed");
			}
			
			if (model == null) {
				fail("Model is null");
			} else {
				java.util.List<Role> roles=model.getRoles();
				
				for (Role role : roles) {
					
					if (role.getName().equals(roleName)) {
						DefaultProtocolContext context=
								new DefaultProtocolContext(ProtocolServices.getParserManager(),
										null);
						ProtocolModel local=ProtocolServices.getProtocolProjector().project(context, model,
										role, new JournalProxy(handler));
		
						if (local != null) {
							// TODO: SAVARA-167 - issue when projection is based on a sub-protocol
							if (AnnotationDefinitions.getAnnotation(local.getProtocol().getAnnotations(),
											AnnotationDefinitions.TYPE) == null &&
									AnnotationDefinitions.getAnnotation(model.getProtocol().getAnnotations(),
													AnnotationDefinitions.TYPE) != null) {				
								AnnotationDefinitions.copyAnnotations(model.getProtocol().getAnnotations(),
										local.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE);
							}
							
							ret = local;
						}
						
						break;
					}
				}
			}
		}
		
		return(ret);
	}

	@Test
	public void testGenerateStoreServiceInterfaceFromWSDL() {
		SwitchyardJavaGenerator gen=new SwitchyardJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), STORE_WSDL_LOCATION, SRC_PATH);
			
			compare("expected/Store.java.txt",
					SRC_PATH+"/org/savara/examples/store/Store.java");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate interface: "+e);
		}
	}

	@Test
	public void testGenerateStoreServiceImplementationFromWSDL() {
		SwitchyardJavaGenerator gen=new SwitchyardJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			java.util.List<Role> refRoles=new java.util.Vector<Role>();
			refRoles.add(new Role("CreditAgency"));
			refRoles.add(new Role("Logistics"));
			
			java.util.List<String> refWsdlPaths=new java.util.Vector<String>();
			refWsdlPaths.add(ClassLoader.getSystemClassLoader().getResource(CREDITAGENCY_WSDL_LOCATION).getFile());
			refWsdlPaths.add(ClassLoader.getSystemClassLoader().getResource(LOGISTICS_WSDL_LOCATION).getFile());
			
			gen.createServiceImplementationFromWSDL(new Role("Store"), refRoles,
					getProtocol("PurchaseGoods", "Store"),
					url.getFile(), STORE_WSDL_LOCATION, refWsdlPaths, SRC_PATH);
			
			compare("expected/StoreImpl.java.txt",
					SRC_PATH+"/org/savara/examples/store/StoreImpl.java");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate interface: "+e);
		}
	}

	@Test
	public void testGenerateStoreServiceCompositeJustService() {
		SwitchyardJavaGenerator gen=new SwitchyardJavaGenerator();
		
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
		SwitchyardJavaGenerator gen=new SwitchyardJavaGenerator();
		
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
		SwitchyardJavaGenerator gen=new SwitchyardJavaGenerator();
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource(STORE_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), STORE_WSDL_LOCATION, SRC_PATH);

			url=ClassLoader.getSystemClassLoader().getResource(CREDITAGENCY_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), CREDITAGENCY_WSDL_LOCATION, SRC_PATH);

			url=ClassLoader.getSystemClassLoader().getResource(LOGISTICS_WSDL_LOCATION);

			gen.createServiceInterfaceFromWSDL(url.getFile(), LOGISTICS_WSDL_LOCATION, SRC_PATH);
			
			compare("expected/Store.java.txt",
					SRC_PATH+"/org/savara/examples/store/Store.java");
			
			compare("expected/CreditAgency.java.txt",
					SRC_PATH+"/org/savara/examples/creditagency/CreditAgency.java");
			
			compare("expected/Logistics.java.txt",
					SRC_PATH+"/org/savara/examples/logistics/Logistics.java");
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
