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
package org.savara.bpmn2.util;

import static org.junit.Assert.*;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.savara.bpmn2.model.TInterface;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TOperation;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.util.BPMN2ServiceUtil.ModelInfo;

public class BPMN2ServiceUtilTest {

	@Test
	public void testIntrospect() {
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream("testmodels/bpmn2/choreo/PurchaseGoodsNoInterfaces.bpmn");

		try {
			org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
			if (defns == null) {
				fail("No definitions returned");
			}
			
			java.util.Map<TParticipant, TInterface> intfs=BPMN2ServiceUtil.introspect(defns);
			
			if (intfs == null) {
				fail("Null returned");
			}
			
			if (intfs.size() != 3) {
				fail("Expected 3 participants: "+intfs.size());
			}
			
			ModelInfo modelInfo=new ModelInfo(null, null, null, defns.getRootElement());
			
			for (TParticipant p : intfs.keySet()) {
				TInterface intf=intfs.get(p);
				if (intf == null) {
					fail("No interface for participant: "+p.getName());
				}
				if (!intf.getName().equals(BPMN2ServiceUtil.getInterfaceName(p))) {
					fail("Interface name '"+intf.getName()+"' is incorrect");
				}
				if (intf.getOperation().size() != 1) {
					fail("Should only be one operation:"+intf.getOperation().size());
				}
				
				TOperation op=intf.getOperation().get(0);
				
				if (op.getOutMessageRef() == null || op.getInMessageRef() == null) {
					fail("Expected in and out message ref");
				}
				
				String reqType=null;
				java.util.List<String> respTypes=new java.util.Vector<String>();

				if (p.getName().equals("Store")) {
					reqType = "BuyRequest";
					respTypes.add("BuyConfirmed");
					respTypes.add("AccountNotFound");
					respTypes.add("BuyFailed");
				} else if (p.getName().equals("CreditAgency")) {
					reqType = "CreditCheck";
					respTypes.add("CustomerUnknown");
					respTypes.add("CreditRating");
				} else if (p.getName().equals("Logistics")) {
					reqType = "DeliveryRequest";
					respTypes.add("DeliveryConfirmed");
				} else {
					fail("Unknown participant: "+p.getName());
				}

				TMessage m=modelInfo.getMessage(op.getInMessageRef().getLocalPart());
				if (!m.getName().equals(reqType)) {
					fail("Expected "+reqType+": "+m.getName());
				}
				
				m = modelInfo.getMessage(op.getOutMessageRef().getLocalPart());
				if (!respTypes.contains(m.getName())) {
					fail("Invalid response type");
				} else {
					respTypes.remove(m.getName());
				}
				
				for (QName qname : op.getErrorRef()) {
					m = modelInfo.getMessage(qname.getLocalPart());
					if (!respTypes.contains(m.getName())) {
						fail("Invalid response type");
					} else {
						respTypes.remove(m.getName());
					}
				}
				
				if (respTypes.size() > 0) {
					fail("No all expected response types found");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load BPMN2 definition: "+e);
		}
	}
	
	@Test
	public void testGetOperationName() {
		TMessage m=new TMessage();
		m.setName("BuyRequest");
		
		String opName=BPMN2ServiceUtil.getOperationName(m);
		
		if (opName == null) {
			fail("No op name");
		}
		
		if (!opName.equals("buy")) {
			fail("Expecting 'buy', but got: "+opName);
		}
	}
}
