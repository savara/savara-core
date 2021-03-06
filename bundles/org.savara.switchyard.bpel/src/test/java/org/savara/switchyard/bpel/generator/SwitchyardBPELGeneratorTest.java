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
package org.savara.switchyard.bpel.generator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.common.util.XMLUtils;

public class SwitchyardBPELGeneratorTest {

	@Test
	public void testCreateSwitchyardDescriptorLoanApproval() {
		SwitchyardBPELGenerator gen=new SwitchyardBPELGenerator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemClassLoader().getResourceAsStream("descriptors/loan_approval/deploy.xml");

			byte[] b=new byte[is.available()];
			is.read(b);
			
			is.close();
			
			// Build the descriptor
			org.w3c.dom.Element descriptor=(org.w3c.dom.Element)XMLUtils.getNode(new String(b));			
			
			// Get wsdl
			javax.wsdl.xml.WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
			java.util.Map<String,javax.wsdl.Definition> wsdls=
					new java.util.HashMap<String,javax.wsdl.Definition>();
			
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource("descriptors/loan_approval/loanServicePT.wsdl");		
			javax.wsdl.Definition loanServicePT=reader.readWSDL(url.getFile());			
			wsdls.put("loanServicePT.wsdl", loanServicePT);
			
			url = ClassLoader.getSystemClassLoader().getResource("descriptors/loan_approval/riskAssessmentPT.wsdl");		
			javax.wsdl.Definition riskAssessmentPT=reader.readWSDL(url.getFile());			
			wsdls.put("riskAssessmentPT.wsdl", riskAssessmentPT);
			
			org.w3c.dom.Element elem=gen.createSwitchyardDescriptor("loanapproval", descriptor, wsdls);
			
			compare("expected/loan_approval/switchyard.xml",
								XMLUtils.toText(elem));
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to create switchyard descriptor: "+e);
		}
	}

	@Test
	public void testCreateSwitchyardDescriptorPolicyQuote() {
		SwitchyardBPELGenerator gen=new SwitchyardBPELGenerator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemClassLoader().getResourceAsStream("descriptors/policy_quote/deploy.xml");

			byte[] b=new byte[is.available()];
			is.read(b);
			
			is.close();
			
			// Build the descriptor
			org.w3c.dom.Element descriptor=(org.w3c.dom.Element)XMLUtils.getNode(new String(b));			
			
			// Get wsdl
			javax.wsdl.xml.WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
			java.util.Map<String,javax.wsdl.Definition> wsdls=
					new java.util.HashMap<String,javax.wsdl.Definition>();
			
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource("descriptors/policy_quote/PolicyQuoteProcess_CreditCheckService.wsdl");		
			javax.wsdl.Definition wsdl1=reader.readWSDL(url.getFile());			
			wsdls.put("PolicyQuoteProcess_CreditCheckService.wsdl", wsdl1);
			
			url = ClassLoader.getSystemClassLoader().getResource("descriptors/policy_quote/PolicyQuoteProcess_DrivingRecordService.wsdl");		
			javax.wsdl.Definition wsdl2=reader.readWSDL(url.getFile());			
			wsdls.put("PolicyQuoteProcess_DrivingRecordService.wsdl", wsdl2);
			
			url = ClassLoader.getSystemClassLoader().getResource("descriptors/policy_quote/PolicyQuoteProcess_PolicyQuoteCalculationService.wsdl");		
			javax.wsdl.Definition wsdl3=reader.readWSDL(url.getFile());			
			wsdls.put("PolicyQuoteProcess_PolicyQuoteCalculationService.wsdl", wsdl3);
			
			url = ClassLoader.getSystemClassLoader().getResource("descriptors/policy_quote/PolicyQuoteProcess_PolicyQuoteEntityService.wsdl");		
			javax.wsdl.Definition wsdl4=reader.readWSDL(url.getFile());			
			wsdls.put("PolicyQuoteProcess_PolicyQuoteEntityService.wsdl", wsdl4);
			
			url = ClassLoader.getSystemClassLoader().getResource("descriptors/policy_quote/PolicyQuoteProcess_PolicyQuoteProcessService.wsdl");		
			javax.wsdl.Definition wsdl5=reader.readWSDL(url.getFile());			
			wsdls.put("PolicyQuoteProcess_PolicyQuoteProcessService.wsdl", wsdl5);
			
			org.w3c.dom.Element elem=gen.createSwitchyardDescriptor("policyquote", descriptor, wsdls);
			
			compare("expected/policy_quote/switchyard.xml",
								XMLUtils.toText(elem));
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to create switchyard descriptor: "+e);
		}
	}

	protected void compare(String expected, String generated) throws Exception {
		java.io.InputStream is=ClassLoader.getSystemClassLoader().getResourceAsStream(expected);
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();
		
		String expectedContent=new String(b);
		
		expectedContent = expectedContent.replaceAll("\r\n", "\n");
		
		generated = generated.replaceAll("\r\n", "\n");
		
		if (expectedContent.equals(generated) == false) {
			System.out.println(">> expected=");
			System.out.println(expectedContent);
			System.out.println(">> generated=");
			System.out.println(generated);
			System.out.println(">> end");
			fail("Content does not match");
		}
	}
}
