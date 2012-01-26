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
package org.savara.bpel.parser;

import java.net.URI;

import org.savara.bpel.parser.BPELProtocolParser;
import org.scribble.common.logging.CachedJournal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.common.resource.ResourceLocator;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.model.ProtocolModel;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class ParserTest extends TestCase {

	public static TestSuite suite() {
        TestSuite suite = new TestSuite("BPEL->Protocol Parser Tests");
        
        suite.addTest(new BPELToProtocolTest("PurchaseGoods@Store", "PurchaseGoods@Store"));
        
        /*
        suite.addTest(new BPELToProtocolTest("PurchaseGoodsProcess_Buyer", "PurchaseGoods@Buyer"));
        suite.addTest(new BPELToProtocolTest("PurchaseGoodsProcess_CreditAgency", "PurchaseGoods@CreditAgency"));
        suite.addTest(new BPELToProtocolTest("PurchaseGoodsProcess_Store", "PurchaseGoods@Store"));

        suite.addTest(new BPELToProtocolTest("ESBBrokerProcess_Broker", "ESBBroker@Broker"));       
        suite.addTest(new BPELToProtocolTest("ESBBrokerProcess_Buyer", "ESBBroker@Buyer"));  
        suite.addTest(new BPELToProtocolTest("ESBBrokerProcess_CreditAgency", "ESBBroker@CreditAgency"));

        suite.addTest(new BPELToProtocolTest("RequestForQuote_SupplierQuoteEngine", "ESBBroker@SupplierQuoteEngine"));
        suite.addTest(new BPELToProtocolTest("CompleteTransaction_SupplierTxnProcessor", "ESBBroker@SupplierTxnProcessor"));

        suite.addTest(new BPELToProtocolTest("PurchaseGoodsProcess_Buyer", "PurchaseGoodsProcess@Buyer"));
        suite.addTest(new BPELToProtocolTest("PurchaseGoodsProcess_CreditAgency", "PurchaseGoodsProcess@CreditAgency"));
        suite.addTest(new BPELToProtocolTest("PurchaseGoodsProcess_Store", "PurchaseGoodsProcess@Store"));

        suite.addTest(new BPELToProtocolTest("ReqRespFaultProcess_Buyer", "ReqRespFault@Buyer"));
        suite.addTest(new BPELToProtocolTest("ReqRespFaultProcess_Seller", "ReqRespFault@Seller"));
        */
        /**
         * TODO: (SAVARA-150) Commenting out this test for now, as we now need the accompanying WSDL to be able to
         * resolve the message type's underlying XSD element or type.
         *
        suite.addTest(new BPELToConversationTest("LoanApprovalService@Service", "LoanApprovalService@Service"));
         */

        return suite;
    }
    
    /**
     * The test case for running the BPEL to Protocol test.
     */
	protected static class BPELToProtocolTest extends TestCase {

		/**
		 * This constructor is initialized with the test
		 * name.
		 * 
		 * @param name The test name
		 */
		public BPELToProtocolTest(String bpelName, String scvName) {
			super(bpelName+"->"+scvName);
			m_bpelName = bpelName;
			m_scvName = scvName;
		}
		
		/**
		 * This method runs the test.
		 * 
		 * @param result The test result
		 */
		public void run(TestResult result) {
			result.startTest(this);
			
			String filename="testmodels/bpel/"+m_bpelName+".bpel";
			
			java.net.URL url=
				ClassLoader.getSystemResource(filename);
			
			if (url == null) {
				result.addError(this,
						new Throwable("Unable to locate resource: "+filename));
			} else {			
    			CachedJournal journal=new CachedJournal();
    			
    			org.scribble.protocol.model.ProtocolModel model=null;
    			
    			try {
    				BPELProtocolParser parser=new BPELProtocolParser();
    				
    				Content content=new ResourceContent(url.toURI());
    				
    				model = parser.parse(new DefaultProtocolContext(null,
							new ResourceLoaderImpl()), content, journal);
    			} catch(Exception e) {
    				e.printStackTrace();
    				result.addError(this, new Throwable("Parsing BPEL failed"));
    			}
    			
				if (model == null) {
					result.addError(this, new Throwable("Model is null"));
						
				} else if (journal.hasErrors()) {
					result.addError(this, new Throwable("Failed to parse CDM Model"));
					
					for (int i=0; i < journal.getIssues().size(); i++) {
						System.err.println("Issue: "+
								journal.getIssues().get(i).getMessage());
					}
				} else {
					ProtocolModel lm=(ProtocolModel)model;

					try {
						org.scribble.protocol.export.text.TextProtocolExporter exporter=
							new org.scribble.protocol.export.text.TextProtocolExporter();
				
						java.io.ByteArrayOutputStream os=
							new java.io.ByteArrayOutputStream();
						
						exporter.export(lm, journal, os);
						
						os.close();
						
						String str=new String(os.toByteArray());
						
						checkResults(result, str);
						
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			result.endTest(this);
		}
		
		/**
		 * This method checks the generated jboss-esb.xml against a
		 * previously stored correct version.
		 * 
		 * @param result The test result
		 * @param conv The conversation
		 */
		protected void checkResults(TestResult result, String conv) {
			boolean f_valid=false;

			String filename="results/protocol/"+m_scvName+".spr";
			
			java.io.InputStream is=
				//ParserTest.class.getResourceAsStream(filename);
				ClassLoader.getSystemResourceAsStream(filename);
			
			if (is != null) {
				
				try {
					byte[] b=new byte[is.available()];
				
					is.read(b);
					
					is.close();
					
					String orig=new String(b);
					
					if (orig.equals(conv) == false) {
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
				String bpelfile="testmodels/bpel/"+m_bpelName+".bpel";
				
				java.net.URL url=//ParserTest.class.getResource(bpelfile);
						ClassLoader.getSystemResource(bpelfile);
				
				/*
				try {
					url = org.eclipse.core.runtime.FileLocator.toFileURL(url);
				} catch(Exception e) {
					e.printStackTrace();
				}
				*/
				
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
						
						java.io.File resultFile=new java.io.File(resultsDir, m_scvName+".generated");
						
						if (resultFile.exists() == false) {
							try {
								java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
								
								fos.write(conv.getBytes());
								
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
						result.addError(this, new Throwable("Unable to obtain URL for BPEL model source '"+
								m_bpelName+"': "+url));
					}
				}
			}
		}

		private String m_bpelName=null;
		private String m_scvName=null;
	}

	public static class ResourceLoaderImpl implements ResourceLocator {

		public ResourceLoaderImpl() {
		}

		public URI getResourceURI(String uri) {
			// TODO: Create file path based on extension in uri
			String filename="testmodels/wsdl/"+uri;
			
			java.net.URI ret=null;
			
			try {
				ret = ClassLoader.getSystemResource(filename).toURI();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			return(ret);
		}
		
	}
}
