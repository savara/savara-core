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
package org.savara.protocol.model.stateless;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.export.text.ForkTextProtocolExporterRule;
import org.savara.protocol.export.text.JoinTextProtocolExporterRule;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.parser.antlr.ANTLRProtocolParser;

public class StatefulToStatelessProtocolTransformerTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Stateful -> Stateless Protocol Transformer Tests");

        suite.addTest(new StatefulToStatelessProtocolTester("Run","Buyer",true));
        suite.addTest(new StatefulToStatelessProtocolTester("Run","Buyer",false));
        suite.addTest(new StatefulToStatelessProtocolTester("Run","CreditAgency",true));
        suite.addTest(new StatefulToStatelessProtocolTester("Run","CreditAgency",false));
        suite.addTest(new StatefulToStatelessProtocolTester("Run","Logistics",true));
        suite.addTest(new StatefulToStatelessProtocolTester("Run","Logistics",false));
        suite.addTest(new StatefulToStatelessProtocolTester("Run","Store",true));        
        suite.addTest(new StatefulToStatelessProtocolTester("Run","Store",false));
        
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromCDL","Buyer",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromCDL","Buyer",false));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromCDL","CreditAgency",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromCDL","CreditAgency",false));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromCDL","Store",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromCDL","Store",false));

        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","Buyer",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","Buyer",false));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","CreditAgency",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","CreditAgency",false));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","Logistics",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","Logistics",false));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","Store",true));
        suite.addTest(new StatefulToStatelessProtocolTester("PurchaseGoodsFromBPMN2","Store",false));

        suite.addTest(new StatefulToStatelessProtocolTester("ChoiceFollowedByInteraction","Buyer",true));
        suite.addTest(new StatefulToStatelessProtocolTester("ChoiceFollowedByInteraction","Buyer",false));
        suite.addTest(new StatefulToStatelessProtocolTester("ChoiceFollowedByInteraction","Store",true));
        suite.addTest(new StatefulToStatelessProtocolTester("ChoiceFollowedByInteraction","Store",false));
        
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","Buyer",true));
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","Buyer",false));
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","CreditAgency",true));
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","CreditAgency",false));
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","Logistics",true));
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","Logistics",false));
        
        // SAVARA-298
        //suite.addTest(new StatefulToStatelessProtocolTester("Repeat","Store",true));
        
        suite.addTest(new StatefulToStatelessProtocolTester("Repeat","Store",false));

        return suite;
    }
    
    protected static class StatefulToStatelessProtocolTester extends TestCase {

    	private String m_name=null;
    	private String _role=null;
    	private boolean _messageBased=false;

    	/**
    	 * This constructor is initialized with the test
    	 * name.
    	 * 
    	 * @param name The test name
    	 */
    	public StatefulToStatelessProtocolTester(String name, String role, boolean messageBased) {
    		super(name+"@"+role+(messageBased?" message":" rpc"));
    		m_name = name;
    		_role = role;
    		_messageBased = messageBased;
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
    				
    				model = parser.parse(null, content, journal);
    			} catch(Exception e) {
    				result.addError(this, new Throwable("Parsing protocol failed"));
    			}
    			
    			if (model == null) {
    				result.addError(this, new Throwable("Model is null"));
    			} else {
    				java.util.List<Role> roles=model.getRoles();
    				Role role=null;
    				
    				for (Role r : roles) {
    					if (r.getName().equals(_role)) {
    						role = r;
    					}
    				}
    				
    				if (role != null) {
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
    		   				
    	    				DefaultStatelessTransformer translater=
    	    								new DefaultStatelessTransformer();
    	    				
    	    				// Message based
    	    				ProtocolModel target=translater.transform(local, _messageBased, journal);
    	    				
    	    				if (target != null) {
    	    					exportProtocolAndCheck(result, target, journal);
    	    				} else {
    	    					fail("Stateless model is null");
    	    				}
    					}
    				} else {
    					fail("Role '"+_role+"' not found");
    				}
     			}
    		}
    		
    		result.endTest(this);
    	}
    	
    	protected void exportProtocolAndCheck(TestResult result, ProtocolModel target, Journal journal) {
			org.scribble.protocol.export.text.TextProtocolExporter exporter=
					new org.scribble.protocol.export.text.TextProtocolExporter();
				
			// TODO: Temporary until supported in scribble protocol model
			exporter.register(new ForkTextProtocolExporterRule());
			exporter.register(new JoinTextProtocolExporterRule());
			
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			
			exporter.export(target, journal, os);
			
			try {
				os.close();
			} catch(Exception e) {
				fail("Failed to close stream");
			}
			
			String str=os.toString();
			
			checkResults(result, str);
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
    		String type=(_messageBased ? "mb" : "rpc");

    		String filename="results/protocol/stateless/"+m_name+"@"+_role+"-"+type+".spr";
    		
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
    			String bpmnfile="testmodels/protocol/"+m_name+".spr";
    			
    			java.net.URL url=ClassLoader.getSystemResource(bpmnfile);
    			
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
    					
    					java.io.File resultsDir=new java.io.File(f, "results/protocol/stateless");
    					
    					if (resultsDir.exists() == false) {
    						resultsDir.mkdirs();
    					}
    					
    					java.io.File resultFile=new java.io.File(resultsDir,
    										m_name+"@"+_role+"-"+type+".generated");
    					
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
    							m_name+"': "+url));
    				}
    			}
    		}
    	}
    }
}
