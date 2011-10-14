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
package org.savara.bpel.util;

import javax.wsdl.xml.WSDLReader;

import junit.framework.TestCase;

import org.savara.bpel.generator.ProtocolToBPELModelGenerator;
import org.savara.bpel.model.TProcess;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.util.XMLUtils;
import org.savara.protocol.util.JournalProxy;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.parser.antlr.ANTLRProtocolParser;

public class BPELGeneratorUtilTest extends TestCase {

	public void testPolicyQuoteAtPolicyQuoteProcessService() {
		generateDeploymentDescriptor("PolicyQuote", "PolicyQuoteProcessService",
				new String[] {
				"testmodels/wsdl/policyQuote/PolicyQuoteProcess_CreditCheckService.wsdl",
				"testmodels/wsdl/policyQuote/PolicyQuoteProcess_DrivingRecordService.wsdl",
				"testmodels/wsdl/policyQuote/PolicyQuoteProcess_PolicyQuoteCalculationService.wsdl",
				"testmodels/wsdl/policyQuote/PolicyQuoteProcess_PolicyQuoteEntityService.wsdl",
				"testmodels/wsdl/policyQuote/PolicyQuoteProcess_PolicyQuoteProcessService.wsdl",
				}, "testmodels/wsdl/policyQuote/PolicyQuoteProcess_PolicyQuoteProcessServiceArtifacts.wsdl");
	}	
	
	public void generateDeploymentDescriptor(String protocolName, String roleName,
					String[] wsdlLocations, String partnerLinkTypeLocation) {
		String filename="testmodels/protocol/"+protocolName+".spr";
		
		java.net.URL url=
    			ClassLoader.getSystemResource(filename);
    				
		if (url == null) {
			fail("Unable to locate resource: "+filename);
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
				fail("Parsing choreography failed");
			}
			
			if (model == null) {
				fail("Model is null");
			} else {
				org.scribble.protocol.model.Role role=null;
				
				java.util.List<org.scribble.protocol.model.Role> roles=model.getRoles();
					//new org.scribble.model.Role(m_role);
				
				for (int i=0; role == null && i < roles.size(); i++) {
					if (roles.get(i).getName().equals(roleName)) {
						role = roles.get(i);
					}
				}
				
				if (role == null) {
					fail("Role '"+roleName+"' not found");						
				} else {
					org.scribble.protocol.projection.ProtocolProjector projector=
						new org.scribble.protocol.projection.impl.ProtocolProjectorImpl();
					
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
						java.util.Collection<javax.wsdl.Definition> wsdls=
										new java.util.Vector<javax.wsdl.Definition>();
						
						try {
							WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();

							// Load the wsdls
							for (String path : wsdlLocations) {
								java.net.URL wsdlurl=
						    			ClassLoader.getSystemResource(path);
						    				
								javax.wsdl.Definition defn=reader.readWSDL(wsdlurl.toString());
								
								wsdls.add(defn);
							}
							
							java.io.InputStream is=ClassLoader.getSystemResourceAsStream(partnerLinkTypeLocation);
							
							if (is == null) {
								fail("Could not find partner link types");
							}
							
							byte[] b=new byte[is.available()];
							is.read(b);
							
							is.close();
							
							org.w3c.dom.Element partnerLinkTypes=(org.w3c.dom.Element)XMLUtils.getNode(new String(b));
							
							org.w3c.dom.Document deploymentDescriptor=
								BPELGeneratorUtil.generateDeploymentDescriptor(model, role, projected,
								(TProcess)target, wsdls, partnerLinkTypes, handler);
							
							if (deploymentDescriptor == null) {
								fail("Descriptor is null");
							}
							
							String text=XMLUtils.toText(deploymentDescriptor);
							
							is = ClassLoader.getSystemResourceAsStream("deployment/"+protocolName+"-deploy.xml");
							
							if (is == null) {
								fail("Could not find partner link types");
							}
							
							b = new byte[is.available()];
							is.read(b);
							
							is.close();

							String expected=new String(b);
							
							if (!expected.equals(text)) {
								System.out.println("EXPECTED="+expected);
								System.out.println("GENERATED="+text);
								
								fail("Deployment descriptor for protocol '"+protocolName+
										"' and role '"+roleName+"' does not match expected value");
							}
							
						} catch(Exception e) {
							fail("Failed to generate deployment descriptor: "+e);
						}
					}
				}
			}
		}
	}
}
