/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.scenario.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.resources.DefaultResourceLocator;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.protocol.ProtocolModelGenerator;
import org.savara.scenario.util.ScenarioModelUtil;
import org.scribble.common.logging.CachedJournal;
import org.scribble.protocol.model.ProtocolModel;

public class ProtocolModelGeneratorImplTest {

	@Test
	public void testSuccessfulPurchase() {
		testScenarioToProtocolModels("SuccessfulPurchase");
	}
	
	@Test
	public void testCustomerUnknown() {
		testScenarioToProtocolModels("CustomerUnknown");
	}
	
	@Test
	public void testInsufficientCredit() {
		testScenarioToProtocolModels("InsufficientCredit");
	}
	
	@Test
	public void testFirstOfferAccept() {
		testScenarioToProtocolModels("FirstOfferAccept");
	}
	
	@Test
	public void testSecondOfferAccept() {
		testScenarioToProtocolModels("SecondOfferAccept");
	}
	
	@Test
	public void testThirdOfferAccept() {
		testScenarioToProtocolModels("ThirdOfferAccept");
	}
	
	@Test
	public void testThirdOfferReject() {
		testScenarioToProtocolModels("ThirdOfferRejected");
	}
	
	protected void testScenarioToProtocolModels(String scenarioName) {
		try {
			String scenarioPath="scenarios/"+scenarioName+".scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioPath);
			
			Scenario scenario=ScenarioModelUtil.deserialize(url.openStream());
			
			ProtocolModelGenerator pmg=new ProtocolModelGeneratorImpl();
			
			FeedbackHandler handler=new DefaultFeedbackHandler();
			
			DefaultResourceLocator locator=
					new DefaultResourceLocator(new java.io.File(url.getFile()).getParentFile());
			
			java.util.Set<ProtocolModel> models=pmg.generate(scenario, locator, handler);
			boolean fail=false;
			
			for (ProtocolModel model : models) {
				if (!checkResult(model)) {
					fail = true;
				}
			}
			
			if (fail) {
				fail("One or more local models for scenario '"+scenarioName+
						"' did not match expected description - see generated files");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load scenario: "+e);
		}
	}

	protected boolean checkResult(ProtocolModel model) {
		
		org.scribble.protocol.export.text.TextProtocolExporter exporter=
				new org.scribble.protocol.export.text.TextProtocolExporter();
			
		java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
		
		CachedJournal journal=new CachedJournal();
		
		exporter.export(model, journal, os);
		
		try {
			os.close();
		} catch(Exception e) {
			fail("Failed to close stream");
		}
		
		String protocol=os.toString();
			
		boolean f_valid=false;

		String filename="protocols/"+model.getProtocol().getName()+"@"+
						model.getProtocol().getLocatedRole().getName()+".spr";
		
		java.io.InputStream is=
				ClassLoader.getSystemResourceAsStream(filename);
		
		if (is != null) {
			
			try {
				byte[] b=new byte[is.available()];
			
				is.read(b);
				
				is.close();
				
				String orig=new String(b);
				
				if (orig.equals(protocol)) {
					f_valid = true;
				}
			} catch(Exception e) {
				fail("Failed to load original: "+e);
			}
		} else {
			fail("Resulting protocol '"+filename+
							"' not found for comparison");
		}
		
		if (f_valid == false) {
			java.net.URL url=ClassLoader.getSystemResource(filename);
			
			if (url != null) {
				// URL will point to copy of test models in the classes folder, so need
				// to obtain reference back to source version
				java.io.File f=null;
				
				if (url.getFile().indexOf("target/test-classes") != -1) {
					f = new java.io.File(url.getFile().replaceFirst("target/test-classes","src/test/resources"));
				} else if (url.getFile().indexOf("classes") != -1) {
					f = new java.io.File(url.getFile().replaceFirst("classes","src/test/resources"));
				} else if (url.getFile().indexOf("bin") != -1) {						
					f = new java.io.File(url.getFile().replaceFirst("bin","src/test/resources"));
				} else {
					fail("Could not locate results folder to record expected result");
				}
				
				if (f != null && f.exists()) {
					java.io.File resultsDir=f.getParentFile();
					
					java.io.File resultFile=new java.io.File(resultsDir,
							model.getProtocol().getName()+"@"+
									model.getProtocol().getLocatedRole().getName()+".generated");
					
					if (resultFile.exists() == false) {
						try {
							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
							
							fos.write(protocol.getBytes());
							
							fos.flush();
							fos.close();
							
						} catch(Exception e){
							fail("Failed to write generated output: "+e);
						}
					} else {
						System.err.println("NOTE: Generated output '"+resultFile+
									"' already exists - not being overwritten");
					}
				} else {
					fail("Unable to obtain URL for model source '"+
							filename+"': "+url);
				}
			}
		}

		return(f_valid);
	}
}
