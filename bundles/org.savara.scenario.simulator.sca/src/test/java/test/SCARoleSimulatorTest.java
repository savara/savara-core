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
package test;

import static org.junit.Assert.*;

import org.apache.tuscany.sca.node.Node;
import org.junit.Test;
import org.savara.scenario.model.Role;
import org.savara.scenario.simulation.SimulationModel;
import org.savara.scenario.simulator.sca.SCARoleSimulator;

public class SCARoleSimulatorTest {

	@Test
	public void testGetSupportedModel() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getSupportedModel(simmodel);
			
			if (model == null) {
				fail("Model is null");
			} else if ((model instanceof Node) == false) {
				fail("Model is not a Tuscany SCA node");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testGetModelRoles() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getSupportedModel(simmodel);
			
			if (model == null) {
				fail("Model is null");
			}
			
			java.util.List<Role> roles=sim.getModelRoles(model);
			
			if (roles.size() > 0) {
				fail("Should be no roles");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

}
