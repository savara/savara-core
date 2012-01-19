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
package org.savara.protocol.contract.generator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.FeedbackHandler;
import org.savara.contract.model.Contract;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.model.Role;
import org.savara.protocol.contract.generator.ContractGenerator;
import org.savara.protocol.contract.generator.ContractGeneratorFactory;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;

public class ProtocolToContractGeneratorTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Protocol->Contract Generator Tests");

        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Buyer", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Buyer", "CreditAgency"));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "CreditAgency", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Logistics", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Store", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Store", "Buyer"));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Store", "CreditAgency"));
        suite.addTest(new ProtocolToContractTester("PurchaseGoodsFromBPMN2", "Store", "Logistics"));
        
        suite.addTest(new ProtocolToContractTester("PolicyQuote", "CreditCheckService", null));
        suite.addTest(new ProtocolToContractTester("PolicyQuote", "PolicyQuoteProcessService", null));
        
        suite.addTest(new ProtocolToContractTester("ESBBroker", "Broker", null));
        suite.addTest(new ProtocolToContractTester("ESBBroker", "Buyer", null));     
        suite.addTest(new ProtocolToContractTester("ESBBroker", "SupplierTxnProcessor", null));
        suite.addTest(new ProtocolToContractTester("ESBBroker", "CreditAgency", null));
        suite.addTest(new ProtocolToContractTester("ESBBroker", "SupplierQuoteEngine", null));
        
        suite.addTest(new ProtocolToContractTester("PurchaseGoods", "Buyer", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoods", "Buyer", "CreditAgency"));
        suite.addTest(new ProtocolToContractTester("PurchaseGoods", "CreditAgency", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoods", "Store", null));
        suite.addTest(new ProtocolToContractTester("PurchaseGoods", "Store", "CreditAgency"));
        
        suite.addTest(new ProtocolToContractTester("ReqRespFault", "Buyer", null));
        suite.addTest(new ProtocolToContractTester("ReqRespFault", "Seller", null));

        return suite;
    }
    
    protected static class ProtocolToContractTester extends TestCase {

    	private String m_name=null;
    	private String m_role=null;
    	private String m_projectedRole=null;

    	/**
    	 * This constructor is initialized with the test
    	 * name. If the optional projected role is provided, then
    	 * use that role to generated a local model, upon which the
    	 * contract role should be derived.
    	 * 
    	 * @param name The test name
    	 * @param role The role
    	 * @param projectedRole The optional role that should be used to generated the projection
    	 */
    	public ProtocolToContractTester(String name,
    						String role, String projectedRole) {
    		super(name+"@"+role+(projectedRole == null ? "" : "_"+projectedRole));
    		m_name = name;
    		m_role = role;
    		m_projectedRole = projectedRole;
    	}
    	
    	/**
    	 * This method runs the test.
    	 * 
    	 * @param result The test result
    	 */
    	public void run(TestResult result) {
    		
    		// Run test
    		result.startTest(this);
    		
    		String filename="testmodels/protocol/"+m_name+".spr";
    		
    		java.net.URL url=
    					ClassLoader.getSystemResource(filename);
    		
    		if (url == null) {
    			result.addError(this,
    					new Throwable("Unable to locate resource: "+filename));
    		} else {			
    			FeedbackHandler feedback=new DefaultFeedbackHandler();
    			
    			org.scribble.protocol.model.ProtocolModel model=null;
    			
				//ANTLRProtocolParser parser=new ANTLRProtocolParser();
				//parser.setAnnotationProcessor(new org.savara.protocol.parser.AnnotationProcessor());
    			
    			try {
    				Content content=new ResourceContent(url.toURI());
    				
    				model = ProtocolServices.getParserManager().parse(null, content,
    								new JournalProxy(feedback));
    			} catch(Exception e) {
    				result.addError(this, new Throwable("Parsing choreography failed"));
    			}
    			
    			// Check if should project model
    			if (m_projectedRole != null && model != null) {
    				org.scribble.protocol.projection.ProtocolProjector projector=
    							ProtocolServices.getProtocolProjector();
    				
    				if (projector != null) {
    					model = projector.project(null, model, new Role(m_projectedRole),
    									new JournalProxy(feedback));
    					
    					if (model == null) {
    	    				result.addError(this, new Throwable("Projected model is null"));
    					}
    				} else {
        				result.addError(this, new Throwable("Could not located protocol projector"));
    				}
    			}
    			
    			if (model == null) {
    				result.addError(this, new Throwable("Model is null"));
    			} else {
   					ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
					if (cg != null) {
						Contract contract=cg.generate(model.getProtocol(), null,
										new Role(m_role), feedback);
						
						if (contract != null) {
							checkResults(result, contract.toString());
						} else {
							result.addError(this, new Throwable("No contract"));
						}
					}
    			}
    		}
    		
    		result.endTest(this);
    	}
    	
    	/**
    	 * This method checks the generated BPEL against a
    	 * previously stored correct version.
    	 * 
    	 * @param result The test result
    	 * @param bpel The BPEL
    	 */
    	protected void checkResults(TestResult result, String bpel) {
    		boolean f_valid=false;

    		String filename="results/contract/"+m_name+"@"+m_role+
    					(m_projectedRole == null ? "" : "_"+m_projectedRole)+".contract";
    		
    		java.io.InputStream is=
    			//ChoreographyToBPELTester.class.getResourceAsStream(filename);
    				ClassLoader.getSystemResourceAsStream(filename);
    		
    		if (is != null) {
    			
    			try {
    				byte[] b=new byte[is.available()];
    			
    				is.read(b);
    				
    				is.close();
    				
    				String orig=new String(b);
    				
    				if (orig.equals(bpel) == false) {
    					result.addError(this,
    							new Throwable("Generated Contract does not match stored version"));
    				} else {
    					f_valid = true;
    				}
    			} catch(Exception e) {
    				result.addError(this, e);
    			}
    		} else {
    			result.addError(this,
    					new Throwable("Resulting Contract '"+filename+
    							"' not found for comparison"));
    		}
    		
    		if (f_valid == false) {
    			String srcfile="testmodels/protocol/"+m_name+".spr";
    			
    			java.net.URL url=ClassLoader.getSystemResource(srcfile);
    			
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
    				} else if (url.getFile().indexOf("target/classes") != -1) {
    					f = new java.io.File(url.getFile().replaceFirst("target/classes","src/test/resources"));
    				} else if (url.getFile().indexOf("classes") != -1) {
        				f = new java.io.File(url.getFile().replaceFirst("classes","src/test/resources"));
    				} else if (url.getFile().indexOf("bin") != -1) {						
    					f = new java.io.File(url.getFile().replaceFirst("bin","src/test/resources"));
    				} else {
    					result.addError(this, new Exception("Could not locate results folder to record expected result"));
    				}
    				
    				if (f != null && f.exists()) {
    					f = f.getParentFile().getParentFile().getParentFile();
    					
    					java.io.File resultsDir=new java.io.File(f, "results/contract");
    					
    					if (resultsDir.exists() == false) {
    						resultsDir.mkdirs();
    					}
    					
    					java.io.File resultFile=new java.io.File(resultsDir,
    										m_name+"@"+m_role+
    										(m_projectedRole == null ? "" : "_"+m_projectedRole)+".generated");
    					
    					//if (resultFile.exists() == false) {
    						try {
    							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
    							
    							fos.write(bpel.getBytes());
    							
    							fos.flush();
    							fos.close();
    							
    						} catch(Exception e){
    							result.addError(this, e);
    						}
    						/*
    					} else {
    						System.err.println("NOTE: Generated output '"+resultFile+
    									"' already exists - not being overwritten");
    					}
    					*/
    				} else {
    					result.addError(this, new Throwable("Unable to obtain URL for model source '"+
    							m_name+"': "+url));
    				}
    			}
    		}
    	}
    }
}
