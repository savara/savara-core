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
package org.savara.bpmn2.model.util;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBElement;

import org.junit.Test;
import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TItemDefinition;
import org.savara.bpmn2.model.TRootElement;
import org.savara.bpmn2.model.util.BPMN2ModelUtil;

public class BPMN2ModelUtilTest {

	@Test
	public void testGetDefinitions() {
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream("testmodels/bpmn2/choreo/PurchaseGoods.bpmn");

		try {
			org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
			if (defns == null) {
				fail("No definitions returned");
			}
			
			// Validate by retrieving choreography
			boolean f_choreoFound=false;
			
			for (JAXBElement<? extends TRootElement> elem : defns.getRootElement()) {
				if (elem.getDeclaredType() == TChoreography.class) {
					if (f_choreoFound) {
						fail("Should only be one choreography");
					}
					f_choreoFound = true;
				} else if (elem.getDeclaredType() == TItemDefinition.class) {
					TItemDefinition itemdefn=(TItemDefinition)elem.getValue();
					
					if (itemdefn.getStructureRef() == null) {
						fail("Item definition structure ref is null");
					}
				}
			}
			
			if (f_choreoFound == false) {
				fail("Choreography not found");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load BPMN2 definition: "+e);
		}
	}
}
