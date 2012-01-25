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
package org.savara.qa.transform;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.savara.bpel.generator.ProtocolToBPELModelGenerator;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.util.BPELModelUtil;
import org.savara.bpmn2.parser.choreo.BPMN2ChoreographyProtocolParser;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.generator.ModelGenerator;
import org.scribble.common.logging.ConsoleJournal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;

public class BPMNChoreographyToBPELTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("BPMN2 Choreo->BPEL Transform Tests");

        suite.addTest(new BPMN2ChoreographyToBPELTester("PurchaseGoodsWithANDJoinActivity", "Store"));
        suite.addTest(new BPMN2ChoreographyToBPELTester("PurchaseGoodsWithANDJoinActivity", "CreditAgency"));
        suite.addTest(new BPMN2ChoreographyToBPELTester("PurchaseGoodsWithANDJoinActivity", "Logistics"));
        suite.addTest(new BPMN2ChoreographyToBPELTester("PurchaseGoodsWithXORJoinActivity", "Store"));
        suite.addTest(new BPMN2ChoreographyToBPELTester("PurchaseGoodsWithXORJoinActivity", "CreditAgency"));
        suite.addTest(new BPMN2ChoreographyToBPELTester("PurchaseGoodsWithXORJoinActivity", "Logistics"));

        return suite;
    }
    
    protected static class BPMN2ChoreographyToBPELTester extends TestCase {

    	private String m_name=null;
    	private String m_role=null;

    	/**
    	 * This constructor is initialized with the test
    	 * name.
    	 * 
    	 * @param name The test name
    	 */
    	public BPMN2ChoreographyToBPELTester(String name, String role) {
    		super(name+"["+role+"]");
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
    		
    		String filename="qamodels/bpmn/"+m_name+".bpmn";
    		
    		java.net.URL url=
    			ClassLoader.getSystemResource(filename);
    		
    		if (url == null) {
    			result.addError(this,
    					new Throwable("Unable to locate resource: "+filename));
    		} else {			
    			ConsoleJournal journal=new ConsoleJournal();
    			
    			org.scribble.protocol.model.ProtocolModel model=null;
    			
				BPMN2ChoreographyProtocolParser parser=new BPMN2ChoreographyProtocolParser();
				
    			try {
    				Content content=new ResourceContent(url.toURI());
    				
    				model = parser.parse(null, content, journal);
    			} catch(Exception e) {
    				e.printStackTrace();
    				result.addError(this, new Throwable("Parsing BPMN2 failed"));
    			}
    			
    			if (model == null) {
    				result.addError(this, new Throwable("Model is null"));
    			} else {
        			DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
    				
    				org.scribble.protocol.model.Role role=null;
    				
    				java.util.List<org.scribble.protocol.model.Role> roles=model.getRoles();
    				
    				for (int i=0; role == null && i < roles.size(); i++) {
    					if (roles.get(i).getName().equals(m_role)) {
    						role = roles.get(i);
    					}
    				}
    				
    				if (role == null) {
    					result.addError(this,
    							new Throwable("Role '"+m_role+"' not found"));						
    				} else {
    					org.scribble.protocol.projection.impl.ProtocolProjectorImpl projector=
    						new org.scribble.protocol.projection.impl.ProtocolProjectorImpl();
    					projector.getCustomRules().add(new org.savara.protocol.projection.JoinProjectorRule());
    					projector.getCustomRules().add(new org.savara.protocol.projection.SyncProjectorRule());
						
						org.scribble.protocol.model.ProtocolModel projected=
    									projector.project(null, model, role, journal);
    					
    					// TODO: SAVARA-167 - issue when projection is based on a sub-protocol
    					if (AnnotationDefinitions.getAnnotation(projected.getProtocol().getAnnotations(),
    									AnnotationDefinitions.TYPE) == null &&
    							AnnotationDefinitions.getAnnotation(model.getProtocol().getAnnotations(),
    	    									AnnotationDefinitions.TYPE) != null) {
    						AnnotationDefinitions.copyAnnotations(model.getProtocol().getAnnotations(),
    								projected.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE);
    					}
    					
    					ModelGenerator generator=new ProtocolToBPELModelGenerator();
    				
						Object target=generator.generate(projected, handler, null);
						
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

    		String filename="qaresults/bpel/"+m_name+"@"+m_role+".bpel";
    		
    		java.io.InputStream is=
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
    			String srcfile="qamodels/bpmn/"+m_name+".bpmn";
    			
    			java.net.URL url=ClassLoader.getSystemResource(srcfile);
    			
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
    					
    					java.io.File resultsDir=new java.io.File(f, "qaresults/bpel");
    					
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
    					result.addError(this, new Throwable("Unable to obtain URL for model source '"+
    							m_name+"': "+url));
    				}
    			}
    		}
    	}
    }
}
