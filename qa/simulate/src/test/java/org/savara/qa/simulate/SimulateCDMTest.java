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
import org.savara.scenario.simulator.protocol.ProtocolRoleSimulator;

public class SimulateCDMTest {

	@Test
	public void testSuccessfulPurchase() {
		
		ProtocolServices.getParserManager().getParsers().add(
				new org.savara.pi4soa.cdm.parser.CDMProtocolParser());
		
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		
		RoleDetails details=new RoleDetails();
		details.setScenarioRole("Buyer");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("Buyer");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("Store");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("Store");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("CreditAgency");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("CreditAgency");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("Logistics");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("Logistics");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		simulation.setScenario("qasim/purchasing/requirements/SuccessfulPurchase.scn");
		
		SimulatorDetails simdetails=new SimulatorDetails();
		simdetails.setName(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simdetails.setClassName(ProtocolRoleSimulator.class.getName());
		simulation.getSimulators().add(simdetails);
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		simulator.simulate(simulation, handler);
		
		if (handler.getProcessedEvents().size() != 12) {
			fail("Expecting 12 processed events: "+handler.getProcessedEvents().size());
		}
		
		if (handler.getNoSimulatorEvents().size() != 0) {
			fail("Expecting 0 non-simulated events: "+handler.getNoSimulatorEvents().size());
		}
	}

	@Test
	public void testInvalidStoreBehaviour() {
		
		ProtocolServices.getParserManager().getParsers().add(
				new org.savara.pi4soa.cdm.parser.CDMProtocolParser());
		
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		
		RoleDetails details=new RoleDetails();
		details.setScenarioRole("Buyer");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("Buyer");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("Store");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("Store");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("CreditAgency");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("CreditAgency");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		details=new RoleDetails();
		details.setScenarioRole("Logistics");
		details.setModel("qasim/purchasing/cdm/PurchaseGoods.cdm");
		details.setModelRole("Logistics");
		details.setSimulator(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simulation.getRoles().add(details);
		
		simulation.setScenario("qasim/purchasing/requirements/InvalidStoreBehaviour.scn");
		
		SimulatorDetails simdetails=new SimulatorDetails();
		simdetails.setName(ProtocolRoleSimulator.PROTOCOL_SIMULATOR);
		simdetails.setClassName(ProtocolRoleSimulator.class.getName());
		simulation.getSimulators().add(simdetails);
		
		TestSimulationHandler handler=new TestSimulationHandler();
		
		simulator.simulate(simulation, handler);
		
		if (handler.getProcessedEvents().size() != 9) {
			fail("Nine events were not processed: "+handler.getProcessedEvents().size());
		}
		
		if (handler.getUnexpectedEvents().size() != 3) {
			fail("Should be 3 unexpected events: "+handler.getUnexpectedEvents().size());
		}
		
		if (handler.getNoSimulatorEvents().size() != 0) {
			fail("Should be 0 non-simulator events: "+handler.getNoSimulatorEvents().size());
		}
	}

}
