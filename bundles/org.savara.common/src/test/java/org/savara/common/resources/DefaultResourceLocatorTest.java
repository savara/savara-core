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
package org.savara.common.resources;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultResourceLocatorTest {

	@Test
	public void testGetRelativePath() {
		
		try {
			java.net.URL url1=ClassLoader.getSystemResource("project/requirements/SuccessfulPolicyQuote.scn");
			
			java.io.File base=new java.io.File(url1.getFile());
			
			DefaultResourceLocator locator=new DefaultResourceLocator(base.getParentFile());
			
			java.net.URL url2=ClassLoader.getSystemResource("project/requirements/sample-data/CreditCheckRequest.xml");
			
			java.io.File dataFile=new java.io.File(url2.getFile());
			
			java.io.File targetFile=new java.io.File(dataFile.getParentFile(), "../../schema/creditCheck.xsd");
			
			String path=locator.getRelativePath(targetFile.getCanonicalPath());
			
			if (!path.equals("../schema/creditCheck.xsd")) {
				fail("Expecting path '../schema/creditCheck.xsd', but got: "+path);
			}
			
		} catch (Exception e) {
			fail("Failed: "+e);
		}
	}

}
