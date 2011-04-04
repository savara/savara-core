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

import org.junit.Test;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.model.SendEvent;

import static org.junit.Assert.*;

public class ScenarioSimulatorTest {

	@Test
	public void testSimulate() {
		Scenario scenario=new Scenario();
		
		Role r1=new Role();
		scenario.getRole().add(r1);
		
		Role r2=new Role();
		scenario.getRole().add(r2);
		
		SendEvent e1=new SendEvent();
		e1.setRole(r1);
		scenario.getEvent().add(e1);
		
		ReceiveEvent e2=new ReceiveEvent();
		e2.setRole(r2);
		scenario.getEvent().add(e2);
		
		ScenarioSimulator sim=ScenarioSimulatorFactory.getScenarioSimulator();
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		java.util.Map<Role,RoleSimulator> roleSim=new java.util.HashMap<Role,RoleSimulator>();
		java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
		
		roleSim.put(r1, new TestRoleSimulator("r1sim", false));
		roleSim.put(r2, new TestRoleSimulator("r2sim", false));
		contexts.put(r1, new DefaultSimulationContext(null));
		contexts.put(r2, new DefaultSimulationContext(null));
		
		sim.simulate(scenario, roleSim, contexts, handler);
		
		if (handler.getProcessedEvents().size() != 2) {
			fail("Expecting 2 'processed' events");
		}
		
		if (handler.getProcessedEvents().get(0) != e1) {
			fail("Expecting e1 as first processed event");
		}
		
		if (handler.getProcessedEvents().get(1) != e2) {
			fail("Expecting e2 as second processed event");
		}
	}

	@Test
	public void testSimulateNoSimulator() {
		Scenario scenario=new Scenario();
		
		Role r1=new Role();
		scenario.getRole().add(r1);
		
		Role r2=new Role();
		scenario.getRole().add(r2);
		
		SendEvent e1=new SendEvent();
		e1.setRole(r1);
		scenario.getEvent().add(e1);
		
		ReceiveEvent e2=new ReceiveEvent();
		e2.setRole(r2);
		scenario.getEvent().add(e2);
		
		ScenarioSimulator sim=ScenarioSimulatorFactory.getScenarioSimulator();
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		java.util.Map<Role,RoleSimulator> roleSim=new java.util.HashMap<Role,RoleSimulator>();
		java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
		
		roleSim.put(r1, new TestRoleSimulator("r1sim", false));
		contexts.put(r1, new DefaultSimulationContext(null));
		
		sim.simulate(scenario, roleSim, contexts, handler);
		
		if (handler.getNoSimulatorEvents().size() != 1) {
			fail("Expecting 1 'no simulator' event");
		}
		
		if (handler.getNoSimulatorEvents().get(0) != e2) {
			fail("Expecting e2 as no simulator event");
		}
	}

	@Test
	public void testSimulateExceptionNoContext() {
		Scenario scenario=new Scenario();
		
		Role r1=new Role();
		scenario.getRole().add(r1);
		
		Role r2=new Role();
		scenario.getRole().add(r2);
		
		SendEvent e1=new SendEvent();
		e1.setRole(r1);
		scenario.getEvent().add(e1);
		
		ReceiveEvent e2=new ReceiveEvent();
		e2.setRole(r2);
		scenario.getEvent().add(e2);
		
		ScenarioSimulator sim=ScenarioSimulatorFactory.getScenarioSimulator();
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		java.util.Map<Role,RoleSimulator> roleSim=new java.util.HashMap<Role,RoleSimulator>();
		java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
		
		roleSim.put(r1, new TestRoleSimulator("r1sim", false));
		
		sim.simulate(scenario, roleSim, contexts, handler);
		
		if (handler.getErrorEvents().size() != 1) {
			fail("Expecting 1 'exception' event");
		}
		
		if (handler.getErrorEvents().get(0) != e1) {
			fail("Expecting e1 as exception event");
		}
	}
}
