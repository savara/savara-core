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
package org.savara.bpel.generator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.savara.bpel.generator.ProtocolToBPELModelGenerator;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.util.BPELModelUtil;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.protocol.util.JournalProxy;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.parser.antlr.ANTLRProtocolParser;

public class ProtocolToBPELGeneratorTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Protocol->BPEL Generator Tests");
        
        // TODO: SAVARA-225 - participant types only used in sub-choreographies need to be
        // declared as roles in just those sub-protocols
        //suite.addTest(new ProtocolToBPELTester("ESBBroker", "CreditAgency"));

        suite.addTest(new ProtocolToBPELTester("ESBBroker", "Buyer"));     
        suite.addTest(new ProtocolToBPELTester("ESBBroker", "Broker"));  
        suite.addTest(new ProtocolToBPELTester("ESBBroker", "SupplierQuoteEngine"));
        suite.addTest(new ProtocolToBPELTester("ESBBroker", "SupplierTxnProcessor"));
        
        suite.addTest(new ProtocolToBPELTester("PurchaseGoods1", "Buyer"));
        suite.addTest(new ProtocolToBPELTester("PurchaseGoods1", "CreditAgency"));
        suite.addTest(new ProtocolToBPELTester("PurchaseGoods1", "Store"));

        suite.addTest(new ProtocolToBPELTester("PurchaseGoods3", "Buyer"));
        suite.addTest(new ProtocolToBPELTester("PurchaseGoods3", "CreditAgency"));
        suite.addTest(new ProtocolToBPELTester("PurchaseGoods3", "Store"));
        suite.addTest(new ProtocolToBPELTester("PurchaseGoods3", "Logistics"));
        
        suite.addTest(new ProtocolToBPELTester("ReqRespFault", "Buyer"));
        suite.addTest(new ProtocolToBPELTester("ReqRespFault", "Seller"));
        
        suite.addTest(new ProtocolToBPELTester("PolicyQuote", "PolicyQuoteProcessService"));  

        return suite;
    }
    
    protected static class ProtocolToBPELTester extends TestCase {

    	/**
    	 * This constructor is initialized with the test
    	 * name.
    	 * 
    	 * @param name The test name
    	 * @param role The role
    	 */
    	public ProtocolToBPELTester(String name,
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
    		// Setup scribble services
    		//ProtocolServices.setProtocolProjector(
    		//		new org.scribble.protocol.projection.impl.ProtocolProjectorImpl());
    		
    		// Run test
    		result.startTest(this);
    		
    		String filename="testmodels/protocol/"+m_name+".spr";
    		
    		java.net.URL url=
    			ClassLoader.getSystemResource(filename);
    		
    		if (url == null) {
    			result.addError(this,
    					new Throwable("Unable to locate resource: "+filename));
    		} else {		
    			DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
    			Journal journal=new JournalProxy(handler);
    			
    			org.scribble.protocol.model.ProtocolModel model=null;
    			
				ANTLRProtocolParser parser=new ANTLRProtocolParser();
				parser.setAnnotationProcessor(new org.savara.protocol.parser.AnnotationProcessor());
				
    			try {
    				Content content=new ResourceContent(url.toURI());
    				
    				model = parser.parse(content, journal, null);
    			} catch(Exception e) {
    				result.addError(this, new Throwable("Parsing choreography failed"));
    			}
    			
    			if (model == null) {
    				result.addError(this, new Throwable("Model is null"));
    			} else {
    				//org.scribble.protocol.projection.ProtocolProjector projector=
    				//	new org.scribble.projector.DefaultProjector();
    				
    				org.scribble.protocol.model.Role role=null;
    				
    				// Obtain role from definition
     				//Protocol defn=model.getProtocol();
    				    				
    				// Check if subpath definition defined
    				/*
    				if (m_subDefinitionPath != null) {
    					defn = defn.getSubDefinition(m_subDefinitionPath);
    				}
    				*/
    				
    				java.util.List<org.scribble.protocol.model.Role> roles=model.getRoles();
    					//new org.scribble.model.Role(m_role);
    				
    				for (int i=0; role == null && i < roles.size(); i++) {
    					if (roles.get(i).getName().equals(m_role)) {
    						role = roles.get(i);
    					}
    				}
    				
    				if (role == null) {
    					result.addError(this,
    							new Throwable("Role '"+m_role+"' not found"));						
    				} else {
    					org.scribble.protocol.projection.ProtocolProjector projector=
    						new org.scribble.protocol.projection.impl.ProtocolProjectorImpl();
    					
    					/* GPB: CONTRACT
   						ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
						if (cg != null) {
							cg.generate(model, journal, null);
						}
						*/
						
						org.scribble.protocol.model.ProtocolModel projected=
    									projector.project(model, role, journal, null);
    					
    					/* Currently if a 'fail' is performed in Tycho build, it
    					 * hangs the build.
    					if (l.getErrors().size() > 0) {
    						fail("Projection has errors: "+l.getErrors());
    					} else if (projected == null) {
    						fail("Projected model is null, for "+filename+" role "+role);
    					}
    					 */
    					
    					// TODO: SAVARA-167 - issue when projection is based on a sub-protocol
    					if (AnnotationDefinitions.getAnnotation(projected.getProtocol().getAnnotations(),
    									AnnotationDefinitions.TYPE) == null &&
    							AnnotationDefinitions.getAnnotation(model.getProtocol().getAnnotations(),
    	    									AnnotationDefinitions.TYPE) != null) {
    						AnnotationDefinitions.copyAnnotations(model.getProtocol().getAnnotations(),
    								projected.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE);
    					}
    					
    					//java.util.List<Role> projectedRoles=projected.getRoles();
    					
    					ModelGenerator generator=new ProtocolToBPELModelGenerator();
    				
						Object target=generator.generate(projected, handler, null);
						
						/*
						ModelReference targetRef=
							new ModelReference(BPELNotation.NOTATION_CODE);
						targetRef.setAlias(m_name);
						targetRef.setLocatedRole(m_role);
						
						DefaultBPELLanguageModel target=
							new DefaultBPELLanguageModel(targetRef);

						generator.generate(targetRef, role,
									target, projected);
						*/
						
						if (target instanceof TProcess) {
							// Obtain any namespace prefix map
							java.util.Map<String, String> prefixes=
									new java.util.HashMap<String, String>();
							
							java.util.List<Annotation> list=
								AnnotationDefinitions.getAnnotations(projected.getProtocol().getAnnotations(),
										AnnotationDefinitions.TYPE);
							
							for (Annotation annotation : list) {
								if (annotation.getProperties().containsKey(AnnotationDefinitions.NAMESPACE_PROPERTY) &&
										annotation.getProperties().containsKey(AnnotationDefinitions.PREFIX_PROPERTY)) {
									prefixes.put((String)annotation.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY),
											(String)annotation.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
								}
							}
							
							try {
								java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
								
								BPELModelUtil.serialize((TProcess)target, baos, prefixes);
								
								baos.close();
								
								String text=new String(baos.toByteArray());
								
								checkResults(result, text);
							} catch(Exception e) {
								result.addError(this, e);
							}
						} else {
							result.addError(this,
									new Throwable("No BPEL generated"));						
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

    		String filename="results/bpel/"+m_name+"@"+m_role+".bpel";
    		
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
    							new Throwable("Generated BPEL does not match stored version"));
    				} else {
    					f_valid = true;
    				}
    			} catch(Exception e) {
    				result.addError(this, e);
    			}
    		} else {
    			result.addError(this,
    					new Throwable("Resulting BPEL '"+filename+
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
    				} else if (url.getFile().indexOf("classes") != -1) {
        				f = new java.io.File(url.getFile().replaceFirst("classes","src/test/resources"));
    				} else if (url.getFile().indexOf("bin") != -1) {						
    					f = new java.io.File(url.getFile().replaceFirst("bin","src/test/resources"));
    				} else {
    					result.addError(this, new Exception("Could not locate results folder to record expected result"));
    				}
    				
    				if (f != null && f.exists()) {
    					f = f.getParentFile().getParentFile().getParentFile();
    					
    					java.io.File resultsDir=new java.io.File(f, "results/bpel");
    					
    					if (resultsDir.exists() == false) {
    						resultsDir.mkdirs();
    					}
    					
    					java.io.File resultFile=new java.io.File(resultsDir,
    										m_name+"@"+m_role+".generated");
    					
    					if (resultFile.exists() == false) {
    						try {
    							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
    							
    							fos.write(bpel.getBytes());
    							
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

    	private String m_name=null;
    	private String m_role=null;
    }
}
