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
package org.savara.scenario.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.common.util.XMLUtils;

public class MessageUtilTest {

	@Test
	public void testIsValidSimpleTextValid() {
		String paramValue="hello";
		String mesgValue="hello";
		
		if (MessageUtil.isValid(paramValue, mesgValue) == false) {
			fail("Should be valid");
		}
	}

	@Test
	public void testIsValidSimpleTextInvalid() {
		String paramValue="hello";
		String mesgValue="world";
		
		if (MessageUtil.isValid(paramValue, mesgValue)) {
			fail("Should be invalid");
		}
	}

	@Test
	public void testIsValidXMLValidSingleElement() {
		String paramValue="<order xmlns=\"myns\" id=\"1\">" +
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<item quantity=\"6\" price=\"12345\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == false) {
				fail("Should be valid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}

	@Test
	public void testIsValidXMLValidMultipleElement() {
		String paramValue="<order xmlns=\"myns\" id=\"1\">" +
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<item price=\"54321\" quantity=\"2\" >" +
								"<productCode>cba</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<item price=\"54321\" quantity=\"2\" >" +
								"<productCode>cba</productCode>" +
							"</item>"+
							"<item quantity=\"6\" price=\"12345\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == false) {
				fail("Should be valid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}

	@Test
	public void testIsValidXMLInvalidAttr() {
		String paramValue="<order xmlns=\"myns\" id=\"1\">" +
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fredX\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<item quantity=\"6\" price=\"12345\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == true) {
				fail("Should be invalid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}

	@Test
	public void testIsValidXMLInvalidSingleElement() {
		String paramValue="<order xmlns=\"myns\" id=\"1\">" +
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<itemX quantity=\"6\" price=\"12345\" >" +
								"<productCode>abc</productCode>" +
							"</itemX>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == true) {
				fail("Should be invalid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}
	
	@Test
	public void testIsValidXMLInvalidMultipleElement() {
		String paramValue="<order xmlns=\"myns\" id=\"1\">" +
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<item price=\"54321\" quantity=\"2\" >" +
								"<productCode>cba</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<item price=\"54321\" quantity=\"2\" >" +
								"<productCode>cba</productCode>" +
							"</item>"+
							"<itemX quantity=\"6\" price=\"12345\" >" +
								"<productCode>abc</productCode>" +
							"</itemX>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == true) {
				fail("Should be invalid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}
	
	@Test
	public void testIsValidXMLInvalidMultipleElementWithIgnore() {
		String paramValue="<!-- @IgnoreElem(item) -->"+
							"<order xmlns=\"myns\" id=\"1\">" +
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<item price=\"54321\" quantity=\"2\" >" +
								"<productCode>cba</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<item price=\"54321\" quantity=\"2\" >" +
								"<productCode>cba</productCode>" +
							"</item>"+
							"<item quantity=\"6\" price=\"12345\" >" +
								"<productCodeX>abc</productCodeX>" + // This makes the item element invalid,
																	// if not ignored
							"</item>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == false) {
				fail("Should be valid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}
	
	@Test
	public void testIsValidXMLInvalidAttrWithIgnore() {
		String paramValue="<order xmlns=\"myns\" id=\"1\">" +
							"<!-- @IgnoreAttr(price) -->"+			
							"<item price=\"12345\" quantity=\"6\" >" +
								"<productCode>abc</productCode>" +
							"</item>"+
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"</order>";
		String mesgValue="<order xmlns=\"myns\" id=\"1\">" +
							"<customer name=\"fred\">" +
								"<address>XYZ</address>" +
							"</customer>" +
							"<item quantity=\"6\" price=\"54321\" >" + // Price is invalid
								"<productCode>abc</productCode>" +
							"</item>"+
							"</order>";
		
		try {
			if (MessageUtil.isValid(paramValue, XMLUtils.getNode(mesgValue)) == false) {
				fail("Should be valid");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
	}
}
