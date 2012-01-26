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
package org.savara.wsdl.generator;

import javax.wsdl.Definition;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.savara.contract.model.Contract;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.model.Role;
import org.savara.protocol.contract.generator.ContractGenerator;
import org.savara.protocol.contract.generator.ContractGeneratorFactory;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;
import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
import org.savara.wsdl.generator.soap.SOAPRPCWSDLBinding;

public class ProtocolToWSDLGeneratorTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Protocol->WSDL Generator Tests");

        suite.addTest(new ProtocolToWSDLTester("PurchaseGoods", "Buyer"));
        suite.addTest(new ProtocolToWSDLTester("PurchaseGoods", "Store"));
        suite.addTest(new ProtocolToWSDLTester("PurchaseGoods", "CreditAgency"));
        suite.addTest(new ProtocolToWSDLTester("PurchaseGoods", "Logistics"));
        
        suite.addTest(new ProtocolToWSDLTester("PolicyQuote", "CreditCheckService"));
        suite.addTest(new ProtocolToWSDLTester("PolicyQuote", "PolicyQuoteProcessService"));
        
        suite.addTest(new ProtocolToWSDLTester("ESBBroker", "Broker"));
        suite.addTest(new ProtocolToWSDLTester("ESBBroker", "Buyer"));     
        suite.addTest(new ProtocolToWSDLTester("ESBBroker", "SupplierTxnProcessor"));
        suite.addTest(new ProtocolToWSDLTester("ESBBroker", "CreditAgency"));
        suite.addTest(new ProtocolToWSDLTester("ESBBroker", "SupplierQuoteEngine"));
        
        suite.addTest(new ProtocolToWSDLTester("PurchaseGoodsCDL", "Buyer"));
        suite.addTest(new ProtocolToWSDLTester("PurchaseGoodsCDL", "CreditAgency"));
        suite.addTest(new ProtocolToWSDLTester("PurchaseGoodsCDL", "Store"));
        
        suite.addTest(new ProtocolToWSDLTester("ReqRespFault", "Buyer"));
        suite.addTest(new ProtocolToWSDLTester("ReqRespFault", "Seller"));

        return suite;
    }
    
    protected static class ProtocolToWSDLTester extends TestCase {

    	private static final String[] BINDING_NAMES={
    		"rpc",
    		"doclit"
    	};
    	
    	private static final WSDLBinding[] BINDINGS={
    		new SOAPRPCWSDLBinding(),
    		new SOAPDocLitWSDLBinding()
    	};
    	
    	private String m_name=null;
    	private String m_role=null;

    	/**
    	 * This constructor is initialized with the test
    	 * name.
    	 * 
    	 * @param name The test name
    	 * @param role The role
    	 */
    	public ProtocolToWSDLTester(String name,
    						String role) {
    		super(name+"@"+role);
    		m_name = name;
    		m_role = role;
    	}
    	
    	/**
    	 * This method runs the test.
    	 * 
    	 * @param result The test result
    	 */
    	public void run(TestResult result) {
    		
    		// Run test
    		result.startTest(this);
    		
    		String filename="testmodels/contract/"+m_name+".spr";
    		
    		java.net.URL url=
    			ClassLoader.getSystemResource(filename);
    		
    		if (url == null) {
    			result.addError(this,
    					new Throwable("Unable to locate resource: "+filename));
    		} else {			
    			DefaultFeedbackHandler feedback=new DefaultFeedbackHandler();
    			
    			org.scribble.protocol.model.ProtocolModel model=null;
    			
    			try {
    				Content content=new ResourceContent(url.toURI());
    				
    				model = ProtocolServices.getParserManager().parse(null, content,
    								new JournalProxy(feedback));
    			} catch(Exception e) {
    				result.addError(this, new Throwable("Parsing choreography failed"));
    			}
    			
    			if (model == null) {
    				result.addError(this, new Throwable("Model is null"));
    			} else {
   					ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
					if (cg != null) {
						Contract contract=cg.generate(model.getProtocol(), null, new Role(m_role),
											feedback);
						
						if (contract != null) {
							
							// Convert to WSDL
							WSDLGenerator gen=WSDLGeneratorFactory.getWSDLGenerator();
							
							for (int j=0; j < BINDING_NAMES.length; j++) {
								java.util.List<Definition> defns=gen.generate(contract,
											BINDINGS[j], feedback);
								
								for (int i=0; i < defns.size(); i++) {
									Definition defn=defns.get(i);
									
									try {
										javax.wsdl.xml.WSDLWriter writer=
											javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter();
		
										java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
										
										writer.writeWSDL(defn, baos);
										
										byte[] b=baos.toByteArray();
										
										baos.close();
										
										checkResults(result, new String(b), BINDING_NAMES[j], i);
									} catch(Exception e) {
										result.addError(this, e);
									}
								}
							}
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
    	protected void checkResults(TestResult result, String wsdl, String binding, int num) {
    		boolean f_valid=false;

    		String filename="results/wsdl/"+m_name+"_"+m_role+"_"+binding+"_"+num+".wsdl";
    		
    		java.io.InputStream is=
    				ClassLoader.getSystemResourceAsStream(filename);
    		
    		if (is != null) {
    			
    			try {
    				byte[] b=new byte[is.available()];
    			
    				is.read(b);
    				
    				is.close();
    				
    				String orig=new String(b);
    				
    				if (orig.equals(wsdl) == false) {
    					result.addError(this,
    							new Throwable("Generated WSDL does not match stored version"));
    				} else {
    					f_valid = true;
    				}
    			} catch(Exception e) {
    				result.addError(this, e);
    			}
    		} else {
    			result.addError(this,
    					new Throwable("Resulting WSDL '"+filename+
    							"' not found for comparison"));
    		}
    		
    		if (f_valid == false) {
    			String srcfile="testmodels/contract/"+m_name+".spr";
    			
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
    					
    					java.io.File resultsDir=new java.io.File(f, "results/wsdl");
    					
    					if (resultsDir.exists() == false) {
    						resultsDir.mkdirs();
    					}
    					
    					java.io.File resultFile=new java.io.File(resultsDir,
    							m_name+"_"+m_role+"_"+binding+"_"+num+".generated");
    					
    					//if (resultFile.exists() == false) {
    						try {
    							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
    							
    							fos.write(wsdl.getBytes());
    							
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
