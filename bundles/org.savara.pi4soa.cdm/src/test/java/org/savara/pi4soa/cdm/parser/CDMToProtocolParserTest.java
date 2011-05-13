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
package org.savara.pi4soa.cdm.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.scribble.common.logging.CachedJournal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.savara.pi4soa.cdm.parser.CDMProtocolParser;

public class CDMToProtocolParserTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Choreography->Protocol Parser Tests");

        suite.addTest(new ChoreographyToProtocolTester("ESBBroker"));
        suite.addTest(new ChoreographyToProtocolTester("PolicyQuote"));
        suite.addTest(new ChoreographyToProtocolTester("PurchaseGoods1"));
        suite.addTest(new ChoreographyToProtocolTester("PurchaseGoods2")); // two paths with same fault response
        suite.addTest(new ChoreographyToProtocolTester("PurchaseGoods3")); // separate fault responses
        suite.addTest(new ChoreographyToProtocolTester("ReqRespFault"));
        
        /* SAVARA-221
        suite.addTest(new ChoreographyToProtocolTester("PurchaseGoodsWithException"));
        */
        
        return suite;
    }
    
    protected static class ChoreographyToProtocolTester extends TestCase {

    	private String m_name=null;

    	/**
    	 * This constructor is initialized with the test
    	 * name.
    	 * 
    	 * @param name The test name
    	 */
    	public ChoreographyToProtocolTester(String name) {
    		super(name);
    		m_name = name;
    	}
    	
    	/**
    	 * This method runs the test.
    	 * 
    	 * @param result The test result
    	 */
    	public void run(TestResult result) {
    		
    		// Run test
    		result.startTest(this);
    		
    		String filename="testmodels/cdm/"+m_name+".cdm";
    		
    		java.net.URL url=
    			ClassLoader.getSystemResource(filename);
    		
    		if (url == null) {
    			result.addError(this,
    					new Throwable("Unable to locate resource: "+filename));
    		} else {			
    			CachedJournal journal=new CachedJournal();
    			
    			org.scribble.protocol.model.ProtocolModel model=null;
    			
				CDMProtocolParser parser=new CDMProtocolParser();
				
    			try {
    				Content content=new ResourceContent(url.toURI());
    				
    				model = parser.parse(content, journal, null);
    			} catch(Exception e) {
    				result.addError(this, new Throwable("Parsing choreography failed"));
    			}
    			
    			if (model == null) {
    				result.addError(this, new Throwable("Model is null"));
    			} else {
    				
    				org.scribble.protocol.export.text.TextProtocolExporter exporter=
    					new org.scribble.protocol.export.text.TextProtocolExporter();
    				
    				java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
    				
    				exporter.export(model, journal, os);
    				
    				try {
    					os.close();
    				} catch(Exception e) {
    					fail("Failed to close stream");
    				}
    				
    				String str=os.toString();
    				
    				checkResults(result, str);
    			}
    		}
    		
    		result.endTest(this);
    	}
    	
    	/**
    	 * This method checks the generated protocol against a
    	 * previously stored correct version.
    	 * 
    	 * @param result The test result
    	 * @param protocol The protocol
    	 */
    	protected void checkResults(TestResult result, String protocol) {
    		boolean f_valid=false;

    		String filename="results/protocol/"+m_name+".spr";
    		
    		java.io.InputStream is=
    				ClassLoader.getSystemResourceAsStream(filename);
    		
    		if (is != null) {
    			
    			try {
    				byte[] b=new byte[is.available()];
    			
    				is.read(b);
    				
    				is.close();
    				
    				String orig=new String(b);
    				
    				if (orig.equals(protocol) == false) {
    					result.addError(this,
    							new Throwable("Generated protocol does not match stored version"));
    				} else {
    					f_valid = true;
    				}
    			} catch(Exception e) {
    				result.addError(this, e);
    			}
    		} else {
    			result.addError(this,
    					new Throwable("Resulting protocol '"+filename+
    							"' not found for comparison"));
    		}
    		
    		if (f_valid == false) {
    			String bpelfile="testmodels/cdm/"+m_name+".cdm";
    			
    			java.net.URL url=ClassLoader.getSystemResource(bpelfile);
    			
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
    					result.addError(this, new Exception("Could not locate results folder to record expected result"));
    				}
    				
    				if (f != null && f.exists()) {
    					f = f.getParentFile().getParentFile().getParentFile();
    					
    					java.io.File resultsDir=new java.io.File(f, "results/protocol");
    					
    					if (resultsDir.exists() == false) {
    						resultsDir.mkdirs();
    					}
    					
    					java.io.File resultFile=new java.io.File(resultsDir,
    										m_name+".generated");
    					
    					if (resultFile.exists() == false) {
    						try {
    							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
    							
    							fos.write(protocol.getBytes());
    							
    							fos.flush();
    							fos.close();
    							
    						} catch(Exception e){
    							result.addError(this, e);
    						}
    					} else {
    						System.err.println("NOTE: Generated output '"+resultFile+
    									"' already exists - not being overwritten");
    					}
    				} else {
    					result.addError(this, new Throwable("Unable to obtain URL for CDM model source '"+
    							m_name+"': "+url));
    				}
    			}
    		}
    	}
    }
}
