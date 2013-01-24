/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.java.generator.util;

import static org.junit.Assert.*;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.savara.common.resources.DefaultResourceLocator;

public class JavaGeneratorUtilTest {

	@Test
	public void testGetElementJavaType() {
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource("models/requirements/PQM.bpmn");

			java.io.File base=new java.io.File(url.getFile());
			
			DefaultResourceLocator locator=new DefaultResourceLocator(base.getParentFile());
			
			String javaType=JavaGeneratorUtil.getElementJavaType(
					new QName("http://www.example.org/policyQuote","policyQuote"),
								"../schema/policyQuoteRequest.xsd", locator);
			
			if (javaType == null) {
				fail("No java type returned");
			}
			
			if (!javaType.equals("org.example.policyquote.PolicyQuote")) {
				fail("Java type should be 'org.example.policyquote.PolicyQuote', but got: "+javaType);
			}
		} catch (Exception e) {
			fail("Failed: "+e);
		}
		
	}

	@Test
	public void testGetElementJavaTypeFromInclude() {
		
		try {
			java.net.URL url=ClassLoader.getSystemClassLoader().getResource("models/requirements/PQM.bpmn");

			java.io.File base=new java.io.File(url.getFile());
			
			DefaultResourceLocator locator=new DefaultResourceLocator(base.getParentFile());
			
			String javaType=JavaGeneratorUtil.getElementJavaType(
					new QName("http://www.example.org/policyQuote","policyQuote"),
								"../schema/policyQuote.xsd", locator);
			
			if (javaType == null) {
				fail("No java type returned");
			}
			
			if (!javaType.equals("org.example.policyquote.PolicyQuote")) {
				fail("Java type should be 'org.example.policyquote.PolicyQuote', but got: "+javaType);
			}
		} catch (Exception e) {
			fail("Failed: "+e);
		}
		
	}

}
