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
package org.savara.bpmn2.generation.process;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.util.BPMN2ModelUtil;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.Annotation;
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

public class ProtocolToBPMN2ProcessGeneratorTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Protocol->BPMN2 Generator Tests");

        suite.addTest(new ProtocolToBPMN2GeneratorTester("PurchaseGoods3"));
        suite.addTest(new ProtocolToBPMN2GeneratorTester("Repetition1"));
        suite.addTest(new ProtocolToBPMN2GeneratorTester("Repetition2"));
        suite.addTest(new ProtocolToBPMN2GeneratorTester("Run1"));
        suite.addTest(new ProtocolToBPMN2GeneratorTester("Run2"));
        suite.addTest(new ProtocolToBPMN2GeneratorTester("Parallel1"));
        suite.addTest(new ProtocolToBPMN2GeneratorTester("PurchaseGoodsWithCancel"));
       
        return suite;
    }
    
    protected static class ProtocolToBPMN2GeneratorTester extends TestCase {

    	private String m_name=null;

    	/**
    	 * This constructor is initialized with the test
    	 * name.
    	 * 
    	 * @param name The test name
    	 */
    	public ProtocolToBPMN2GeneratorTester(String name) {
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
    				java.util.List<Role> roles=model.getRoles();
    				
    				for (Role role : roles) {
    					DefaultProtocolContext context=
    							new DefaultProtocolContext(ProtocolServices.getParserManager(),
    									null);
    					/*
    											new org.scribble.common.resource.ResourceLocator() {
    						public URI getResourceURI(String uri) throws Exception {
    							return(locator.getResourceURI(uri));
    						}
    					});
    					*/
    	
    					ProtocolModel local=ProtocolServices.getProtocolProjector().project(model,
    									role, new JournalProxy(handler), context);
    	
    					if (local != null) {
    						// TODO: SAVARA-167 - issue when projection is based on a sub-protocol
    						if (AnnotationDefinitions.getAnnotation(local.getProtocol().getAnnotations(),
    										AnnotationDefinitions.TYPE) == null &&
    								AnnotationDefinitions.getAnnotation(model.getProtocol().getAnnotations(),
    												AnnotationDefinitions.TYPE) != null) {				
    							AnnotationDefinitions.copyAnnotations(model.getProtocol().getAnnotations(),
    									local.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE);
    						}   						
    		   				
    	    				ProtocolToBPMN2ProcessModelGenerator generator=
    	    								new ProtocolToBPMN2ProcessModelGenerator();
    	    				generator.setUseConsecutiveIds(true);
    	    				
    	    				Object target=generator.generate(local, handler, null);
    	    				
    	    				if (target instanceof TDefinitions) {
    	    					
    							// Obtain any namespace prefix map
    							java.util.Map<String, String> prefixes=
    									new java.util.HashMap<String, String>();
    							
    							java.util.List<Annotation> list=
    								AnnotationDefinitions.getAnnotations(model.getProtocol().getAnnotations(),
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
    								
    								BPMN2ModelUtil.serialize((TDefinitions)target, baos, prefixes);
    								
    								baos.close();
    								
    								String text=new String(baos.toByteArray());
    								
    								checkResults(result, local.getProtocol().getRole(), text);
    							} catch(Exception e) {
    								result.addError(this, e);
    							}
    						} else {
    							result.addError(this,
    									new Throwable("No BPMN2 generated"));						
    	    				}
    					}
    				}
     			}
    		}
    		
    		result.endTest(this);
    	}
    	
    	/**
    	 * This method checks the generated protocol against a
    	 * previously stored correct version.
    	 * 
    	 * @param result The test result
    	 * @param role The role
    	 * @param protocol The protocol
    	 */
    	protected void checkResults(TestResult result, Role role, String protocol) {
    		boolean f_valid=false;

    		String filename="results/bpmn2/"+m_name+"@"+role.getName()+".bpmn2";
    		
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
    			/*
    			result.addError(this,
    					new Throwable("Resulting protocol '"+filename+
    							"' not found for comparison"));
    							*/
    			System.err.println("Generating file, as comparison file not found");
    		}
    		
    		if (f_valid == false) {
    			String bpmn2file="testmodels/bpmn2";
    			
    			java.net.URL url=ClassLoader.getSystemResource(bpmn2file);
    			
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
    					f = f.getParentFile().getParentFile(); //.getParentFile();
    					
    					java.io.File resultsDir=new java.io.File(f, "results/bpmn2");
    					
    					if (resultsDir.exists() == false) {
    						resultsDir.mkdirs();
    					}
    					
    					java.io.File resultFile=new java.io.File(resultsDir,
    										m_name+"@"+role.getName()+".generated");
    					
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
    					result.addError(this, new Throwable("Unable to obtain URL for BPMN2 model source '"+
    							m_name+"' role "+role.getName()+": "+url));
    				}
    			}
    		}
    	}
    }
}
