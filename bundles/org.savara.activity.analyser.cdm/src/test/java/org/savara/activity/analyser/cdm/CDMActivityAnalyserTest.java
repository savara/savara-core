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
package org.savara.activity.analyser.cdm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.activity.model.Activity;
import org.savara.activity.model.ExchangeType;
import org.savara.activity.model.InteractionActivity;
import org.savara.activity.model.Message;
import org.savara.activity.model.ProtocolAnalysis;
import org.savara.common.util.XMLUtils;

public class CDMActivityAnalyserTest {

	@Test
	public void testSuccessfulPurchase() {
		CDMActivityAnalyser analyser=new CDMActivityAnalyser();
		
		Activity recvBuyRequest=createActivity("BuyRequest", "{http://www.jboss.org/examples/store}StoreService", false);
		((InteractionActivity)recvBuyRequest.getType()).setExchangeType(ExchangeType.REQUEST);
		analyser.analyse(recvBuyRequest);	
		validate(recvBuyRequest, true);
		
		Activity sendCreditCheckRequest=createActivity("CreditCheckRequest", "{http://www.jboss.org/examples/creditAgency}CreditAgencyService", true);
		((InteractionActivity)sendCreditCheckRequest.getType()).setExchangeType(ExchangeType.REQUEST);
		analyser.analyse(sendCreditCheckRequest);	
		validate(sendCreditCheckRequest, true);		
		
		Activity recvCreditCheckOk=createActivity("CreditCheckOk", "{http://www.jboss.org/examples/creditAgency}CreditAgencyService", false);
		((InteractionActivity)recvCreditCheckOk.getType()).setExchangeType(ExchangeType.RESPONSE);
		analyser.analyse(recvCreditCheckOk);	
		validate(recvCreditCheckOk, true);
		
		Activity recvBuyConfirmed=createActivity("BuyConfirmed", "{http://www.jboss.org/examples/store}StoreService", true);
		((InteractionActivity)recvBuyConfirmed.getType()).setExchangeType(ExchangeType.RESPONSE);
		analyser.analyse(recvBuyConfirmed);	
		validate(recvBuyConfirmed, true);
	}
	
	@Test
	public void testInvalidPurchase() {
		CDMActivityAnalyser analyser=new CDMActivityAnalyser();
		
		Activity recvBuyRequest=createActivity("BuyRequest", "{http://www.jboss.org/examples/store}StoreService", false);
		((InteractionActivity)recvBuyRequest.getType()).setExchangeType(ExchangeType.REQUEST);
		analyser.analyse(recvBuyRequest);	
		validate(recvBuyRequest, true);
		
		Activity sendCreditCheckRequest=createActivity("CreditCheckRequest2", "{http://www.jboss.org/examples/creditAgency}CreditAgencyService", true);
		((InteractionActivity)sendCreditCheckRequest.getType()).setExchangeType(ExchangeType.REQUEST);
		analyser.analyse(sendCreditCheckRequest);	
		validate(sendCreditCheckRequest, true);		
		
		Activity recvCreditCheckInvalid=createActivity("CreditCheckInvalid", "{http://www.jboss.org/examples/creditAgency}CreditAgencyService", false);
		((InteractionActivity)recvCreditCheckInvalid.getType()).setExchangeType(ExchangeType.RESPONSE);
		analyser.analyse(recvCreditCheckInvalid);	
		validate(recvCreditCheckInvalid, true);
		
		Activity recvBuyConfirmed=createActivity("BuyConfirmed", "{http://www.jboss.org/examples/store}StoreService", true);
		((InteractionActivity)recvBuyConfirmed.getType()).setExchangeType(ExchangeType.RESPONSE);
		analyser.analyse(recvBuyConfirmed);	
		validate(recvBuyConfirmed, false);
	}
	
	@Test
	public void testUnsuccessfulPurchase() {
		CDMActivityAnalyser analyser=new CDMActivityAnalyser();
		
		Activity recvBuyRequest=createActivity("BuyRequest", "{http://www.jboss.org/examples/store}StoreService", false);
		((InteractionActivity)recvBuyRequest.getType()).setExchangeType(ExchangeType.REQUEST);
		analyser.analyse(recvBuyRequest);	
		validate(recvBuyRequest, true);
		
		Activity sendCreditCheckRequest=createActivity("CreditCheckRequest2", "{http://www.jboss.org/examples/creditAgency}CreditAgencyService", true);
		((InteractionActivity)sendCreditCheckRequest.getType()).setExchangeType(ExchangeType.REQUEST);
		analyser.analyse(sendCreditCheckRequest);	
		validate(sendCreditCheckRequest, true);		
		
		Activity recvCreditCheckInvalid=createActivity("CreditCheckInvalid", "{http://www.jboss.org/examples/creditAgency}CreditAgencyService", false);
		((InteractionActivity)recvCreditCheckInvalid.getType()).setExchangeType(ExchangeType.RESPONSE);
		analyser.analyse(recvCreditCheckInvalid);	
		validate(recvCreditCheckInvalid, true);
		
		Activity recvBuyFailed=createActivity("BuyFailed", "{http://www.jboss.org/examples/store}StoreService", true);
		((InteractionActivity)recvBuyFailed.getType()).setExchangeType(ExchangeType.RESPONSE);
		analyser.analyse(recvBuyFailed);	
		validate(recvBuyFailed, true);
	}
	
	protected void validate(Activity activity, boolean expected) {
		if (activity.getAnalysis().size() != 1) {
			fail("Analysis not found");
		}
		
		if ((activity.getAnalysis().get(0).getAny() instanceof ProtocolAnalysis) == false) {
			fail("Should be protocol analysis");
		}
		
		ProtocolAnalysis pa=(ProtocolAnalysis)activity.getAnalysis().get(0).getAny();
		
		if (pa.getExpected() != expected) {
			fail("Message '"+((InteractionActivity)activity.getType()).getMessage().get(0).getType()+
							"' expected value ("+expected+") was incorrect");
		}
	}
	
	protected Activity createActivity(String name, String serviceType, boolean outbound) {
		Activity ret=new Activity();
		org.w3c.dom.Element content=getMessage(name);
		
		Message mesg=new Message();
		mesg.setType("{"+content.getNamespaceURI()+"}"+content.getLocalName());
		mesg.setAny(content);
		
		InteractionActivity ia=new InteractionActivity();
		ia.getMessage().add(mesg);
		ia.setDestinationType(serviceType);
		ia.setOutbound(outbound);
		
		ret.setType(ia);
		
		return(ret);
	}

	protected org.w3c.dom.Element getMessage(String name) {
		org.w3c.dom.Element ret=null;
		String path="messages/"+name+".xml";
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream(path);
			
			byte[] b=new byte[is.available()];
			is.read(b);
			
			is.close();
			
			ret = (org.w3c.dom.Element)XMLUtils.getNode(new String(b));
		} catch(Exception e) {
			fail("Failed to retrieve message '"+name+"': "+e);
		}
		
		return(ret);
	}
}
