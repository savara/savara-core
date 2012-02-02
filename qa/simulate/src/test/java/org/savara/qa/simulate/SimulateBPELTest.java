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
package org.savara.qa.simulate;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.protocol.util.ProtocolServices;
import org.savara.scenario.simulation.ScenarioSimulatorMain;
import org.savara.scenario.simulation.model.RoleDetails;
import org.savara.scenario.simulation.model.Simulation;
import org.savara.scenario.simulation.model.SimulatorDetails;
import org.savara.scenario.simulator.scribble.ScribbleRoleSimulator;

public class SimulateBPELTest {

	@Test
	public void testSuccessfulPurchase() {
		
		ProtocolServices.getParserManager().getParsers().add(
				new org.savara.bpel.parser.BPELProtocolParser());
		
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		
		RoleDetails details=new RoleDetails();
		details.setScenarioRole("Store");
		details.setModel("qasimmodels/purchasegoods/bpel/PurchaseGoods@Store.bpel");
		details.setModelRole("Store");
		details.setSimulator(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("CreditAgency");
		details.setModel("qasimmodels/purchasegoods/bpel/PurchaseGoods@CreditAgency.bpel");
		details.setModelRole("CreditAgency");
		details.setSimulator(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("Logistics");
		details.setModel("qasimmodels/purchasegoods/bpel/PurchaseGoods@Logistics.bpel");
		details.setModelRole("Logistics");
		details.setSimulator(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		simulation.setScenario("qascenarios/purchasegoods/SuccessfulPurchase.scn");
		
		SimulatorDetails simdetails=new SimulatorDetails();
		simdetails.setName(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simdetails.setClassName(org.savara.scenario.simulator.scribble.ScribbleRoleSimulator.class.getName());
		simulation.getSimulators().add(simdetails);
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		simulator.simulate(simulation, handler);
		
		if (handler.getProcessedEvents().size() != 10) {
			fail("Expecting 10 processed events: "+handler.getProcessedEvents().size());
		}
		
		if (handler.getNoSimulatorEvents().size() != 2) {
			fail("Expecting 2 non-simulated events: "+handler.getNoSimulatorEvents().size());
		}
	}

	@Test
	public void testInvalidStoreBehaviour() {
		
		ProtocolServices.getParserManager().getParsers().add(
				new org.savara.bpel.parser.BPELProtocolParser());
		
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		
		RoleDetails details=new RoleDetails();
		details.setScenarioRole("Store");
		details.setModel("qasimmodels/purchasegoods/bpel/PurchaseGoods@Store.bpel");
		details.setModelRole("Store");
		details.setSimulator(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("CreditAgency");
		details.setModel("qasimmodels/purchasegoods/bpel/PurchaseGoods@CreditAgency.bpel");
		details.setModelRole("CreditAgency");
		details.setSimulator(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("Logistics");
		details.setModel("qasimmodels/purchasegoods/bpel/PurchaseGoods@Logistics.bpel");
		details.setModelRole("Logistics");
		details.setSimulator(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		simulation.setScenario("qascenarios/purchasegoods/InvalidStoreBehaviour.scn");
		
		SimulatorDetails simdetails=new SimulatorDetails();
		simdetails.setName(ScribbleRoleSimulator.PROTOCOL_SIMULATOR);
		simdetails.setClassName(org.savara.scenario.simulator.scribble.ScribbleRoleSimulator.class.getName());
		simulation.getSimulators().add(simdetails);
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		simulator.simulate(simulation, handler);
		
		if (handler.getProcessedEvents().size() != 7) {
			fail("Nine events were not processed: "+handler.getProcessedEvents().size());
		}
		
		if (handler.getUnexpectedEvents().size() != 3) {
			fail("Should be 3 unexpected events: "+handler.getUnexpectedEvents().size());
		}
		
		if (handler.getNoSimulatorEvents().size() != 2) {
			fail("Should be 2 non-simulator events: "+handler.getNoSimulatorEvents().size());
		}
	}

}
