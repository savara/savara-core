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
package org.savara.scenario.simulator.switchyard;

import static org.junit.Assert.*;

import org.junit.Test;

import org.switchyard.SwitchYard;
import org.switchyard.test.mixins.CDIMixIn;

public class SwitchyardTest {

	@Test
	public void testSwitchyard() throws Exception {
		CDIMixIn mix=new CDIMixIn();
		mix.initialize();
		
		java.io.InputStream config =
				getClass().getResourceAsStream("/descriptors/java/store/switchyard.xml");
		
		if (config == null) {
			fail("Failed to get switchyard descriptor");
		}
		
		SwitchYard sy = new SwitchYard(config);
		config.close();
		
		if (sy.getActivatorList().size() != 2) {
			fail("2 activator expected: "+sy.getActivatorList().size());
		}
		
		sy.start();
	}

}
