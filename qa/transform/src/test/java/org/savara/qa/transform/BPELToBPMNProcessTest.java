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
package org.savara.qa.transform;

import java.net.URI;
import java.net.URL;

import org.savara.bpel.parser.BPELProtocolParser;
import org.savara.bpmn2.generation.process.ProtocolToBPMN2ProcessModelGenerator;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.util.BPMN2ModelUtil;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.common.logging.CachedJournal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.common.resource.ResourceLocator;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.model.ProtocolModel;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class BPELToBPMNProcessTest extends TestCase {

	public static TestSuite suite() {
        TestSuite suite = new TestSuite("BPEL->BPMN Process Transform Tests");
        
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoodsWithANDJoinActivity@Store", true));
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoodsWithXORJoinActivity@Store", true));
        
        // SAVARA-325 - need to also review the output from these
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoodsWithANDJoinActivity@Store", false));
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoodsWithXORJoinActivity@Store", false));
        
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoods@Store", true));       
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoods@CreditAgency", true));       
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoods@Logistics", true));       

        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoods@Store", false));       
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoods@CreditAgency", false));       
        suite.addTest(new BPELToBPMNProcessTestCase("PurchaseGoods@Logistics", false));       

        return suite;
    }
    
    /**
     * The test case for running the BPEL to BPMN Process test.
     */
	protected static class BPELToBPMNProcessTestCase extends TestCase {

		private String _name=null;
		private boolean _useMessageBasedInvocation=false;
		
		/**
		 * This constructor is initialized with the test
		 * name.
		 * 
		 * @param name The test name
		 */
		public BPELToBPMNProcessTestCase(String name, boolean mom) {
			super(name+"[mom="+mom+"]");
			_name = name;
			_useMessageBasedInvocation = mom;
		}
		
		/**
		 * This method runs the test.
		 * 
		 * @param result The test result
		 */
		public void run(TestResult result) {
			result.startTest(this);
			
			String filename="qamodels/bpel/"+_name+".bpel";
			
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
					result.addError(this, new Throwable("Failed to parse model"));
					
					for (int i=0; i < journal.getIssues().size(); i++) {
						System.err.println("Issue: "+
								journal.getIssues().get(i).getMessage());
					}
				} else {
					ProtocolModel lm=(ProtocolModel)model;

    				ProtocolToBPMN2ProcessModelGenerator generator=
							new ProtocolToBPMN2ProcessModelGenerator();
					generator.setUseConsecutiveIds(true);
					generator.setMessageBasedInvocation(_useMessageBasedInvocation);
					
        			DefaultFeedbackHandler handler=new DefaultFeedbackHandler();

 					java.util.Map<String,Object> map=generator.generate(lm, handler, null);
					
					if (map == null || map.size() != 1) {
						fail("Protocol to BPMN model generator didn't return a single BPMN process definition");
					}
					
					Object target=map.values().iterator().next();
					
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
							
							checkResults(result, text);
						} catch(Exception e) {
							result.addError(this, e);
						}
					} else {
						result.addError(this,
								new Throwable("No BPMN2 generated"));						
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

			String filename=_name+(_useMessageBasedInvocation?"_mom":"")+".bpmn";
			
			String filepath="qaresults/bpmn_from_bpel/"+filename;
			
			java.io.InputStream is=
				//ParserTest.class.getResourceAsStream(filename);
				ClassLoader.getSystemResourceAsStream(filepath);
			
			if (is != null) {
				
				try {
					byte[] b=new byte[is.available()];
				
					is.read(b);
					
					is.close();
					
					String orig=new String(b);
					
					if (orig.equals(conv) == false) {
						result.addError(this,
							new Throwable("Generated BPMN process does not match stored version"));
					} else {
						f_valid = true;
					}
				} catch(Exception e) {
					result.addError(this, e);
				}
			} else {
				result.addError(this,
						new Throwable("Resulting BPMN process '"+filepath+
								"' not found for comparison"));
			}
			
			if (f_valid == false) {
				String bpelfile="qamodels/bpel/"+_name+".bpel";
				
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
						
						java.io.File resultsDir=new java.io.File(f, "qaresults/bpmn_from_bpel");
						
						if (resultsDir.exists() == false) {
							resultsDir.mkdirs();
						}
						
						java.io.File resultFile=new java.io.File(resultsDir, filename+".generated");
						
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
								_name+"': "+url));
					}
				}
			}
		}
	}

	public static class ResourceLoaderImpl implements ResourceLocator {

		public ResourceLoaderImpl() {
		}

		public URI getResourceURI(String uri) {
			// TODO: Create file path based on extension in uri
			String filename="qamodels/wsdl/"+uri;
			
			java.net.URI ret=null;
			
			try {
				URL url=ClassLoader.getSystemResource(filename);
				
				if (url != null) {
					ret = url.toURI();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			return(ret);
		}
		
	}
}
