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
package org.savara.scenario.simulation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.simulation.model.RoleDetails;
import org.savara.scenario.simulation.model.Simulation;
import org.savara.scenario.simulation.model.SimulatorDetails;

public class ScenarioSimulatorMainTest {

	private static final String TEST_ROLE_SIMULATOR = "TestRoleSimulator";
	private static final String STORE_ROLE = "Store";

	@Test
	public void testLoadScenarioValid() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		simulation.setScenario("scenarios/SuccessfulPurchase.scn");
		
		if (simulator.loadScenario(simulation) == null) {
			fail("Simulation failed to load scenario");
		}
	}

	@Test
	public void testLoadScenarioInvalid() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		simulation.setScenario("scenarios/SuccessfulPurchaseX.scn");
		
		if (simulator.loadScenario(simulation) != null) {
			fail("Simulation should have failed to load scenario");
		}
	}

	@Test
	public void testLoadRoleSimulators() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		
		RoleDetails details=new RoleDetails();
		details.setScenarioRole(STORE_ROLE);
		details.setSimulator(TEST_ROLE_SIMULATOR);		
		simulation.getRoles().add(details);
		
		SimulatorDetails simdetails=new SimulatorDetails();
		simdetails.setName(TEST_ROLE_SIMULATOR);
		simdetails.setClassName(TestRoleSimulator.class.getName());
		simulation.getSimulators().add(simdetails);
		
		simulation.setScenario("scenarios/SuccessfulPurchase.scn");
		
		Scenario scenario=simulator.loadScenario(simulation);
		
		java.util.Map<Role, RoleSimulator> rsims=simulator.loadRoleSimulators(scenario, simulation);
		
		if (rsims == null) {
			fail("No role simulators");
		}
		
		RoleSimulator rsim=rsims.get(scenario.getRole().get(1)); // Should be for 'Store' role
		if (rsim == null) {
			fail("No role simulator for '"+STORE_ROLE+"'");
		}
		
		if ((rsim instanceof TestRoleSimulator) == false) {
			fail("TestRoleSimulator not instantiated: "+rsim);
		}
	}

	@Test
	public void testLoadSimulationContexts() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		
		RoleDetails details=new RoleDetails();
		details.setScenarioRole(STORE_ROLE);
		details.setModel("models/PurchaseGoods.cdm");
		simulation.getRoles().add(details);
		
		simulation.setScenario("scenarios/SuccessfulPurchase.scn");
		
		Scenario scenario=simulator.loadScenario(simulation);
		
		java.util.Map<Role, RoleSimulator> rsims=new java.util.HashMap<Role, RoleSimulator>();
		rsims.put(scenario.getRole().get(1), new TestRoleSimulator());
		
		java.util.Map<Role, SimulationContext> contexts=null;
		
		SimulationHandler handler=new TestSimulationHandler();
		
		try {
			contexts = simulator.loadSimulationContexts(scenario, simulation, rsims, handler);
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load simulation contexts: "+e);
		}
		
		if (contexts == null) {
			fail("No simulation contexts");
		}
		
		SimulationContext context=contexts.get(scenario.getRole().get(1)); // Should be for 'Store' role
		if (context == null) {
			fail("No simulation context for '"+STORE_ROLE+"'");
		}
		
		if (context.getModel() == null) {
			fail("Model should not be null");
		}
	}
}

