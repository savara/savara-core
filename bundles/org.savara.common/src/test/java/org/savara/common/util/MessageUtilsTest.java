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
package org.savara.common.util;

import static org.junit.Assert.*;

import javax.xml.namespace.QName;

public class MessageUtilsTest {

	@org.junit.Test
	public void testXMLElement() {
		String ns="http://www.savara.org/types";
		String lp="MyType";
		
		String xml="<ns1:"+lp+" xmlns:ns1=\""+ns+"\" />";
		
		String type=MessageUtils.getMessageType(xml);
		
		if (type == null) {
			fail("Failed to get message type");
		}
		
		QName qname=new QName(ns, lp);
		
		if (qname.toString().equals(type) == false) {
			fail("Was expecting '"+qname+"', but got '"+type+"'");
		}
	}
}
