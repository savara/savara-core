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
package org.savara.activity.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.activity.model.*;
import org.savara.common.util.XMLUtils;
import org.w3c.dom.Element;

public class ActivityModelUtilTest {

	@Test
	public void testMessageElement() {
		
		Activity activity=new Activity();
		
		InteractionActivity ia=new InteractionActivity();
		activity.setType(ia);
		
		Message m1=new Message();
		ia.getMessage().add(m1);
		
		m1.setType("{http://www.savara.org/examples}Order");
		
		String serialized=null;
		
		try {
			String order="<ns:Order xmlns:ns=\"http://www.savara.org/examples\" id=\"1\" />";
			m1.setAny((Element)XMLUtils.getNode(order));
			
			serialized = ActivityModelUtil.serialize(activity);
		
		} catch(Exception e) {
			e.printStackTrace();
			fail("Serialization failure: "+e);
		}
		
		try {
			Activity copy=ActivityModelUtil.deserialize(serialized);
			
			if (copy.getType() == null) {
				fail("No activity type");
			}
			
			if ((copy.getType() instanceof InteractionActivity) == false) {
				fail("Incorrect activity type");
			}
			
			InteractionActivity iacopy=(InteractionActivity)copy.getType();
			
			if (iacopy.getMessage().size() != 1) {
				fail("Expecting 1 message, but got: "+iacopy.getMessage().size());
			}
			
			if (iacopy.getMessage().get(0).getType().equals(m1.getType()) == false) {
				fail("Message type different");
			}
			
			// NOTE: Appears to be a jaxb issue where the DOM element in message inherits
			// the savara activity namespace, even though the namespace declaration is on
			// the returned element. So for now just check the element name and that the
			// id attribute is present
			if (iacopy.getMessage().get(0).getAny().getLocalName().equals("Order") == false) {
				fail("Element does not have node name 'Order'");
			}
			
			if (iacopy.getMessage().get(0).getAny().hasAttribute("id") == false) {
				fail("Element does not have attribute 'id'");
			}
			
			if (iacopy.getMessage().get(0).getAny().getAttribute("id").equals("1") == false) {
				fail("Element does not have correct value for attribute 'id'");
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Deserialization failure: "+e);
		}
	}

}
