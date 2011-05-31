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
package org.savara.scenario.simulator.cdm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.scenario.simulation.ScenarioSimulatorMain;
import org.savara.scenario.simulation.model.RoleDetails;
import org.savara.scenario.simulation.model.Simulation;
import org.savara.scenario.simulation.model.SimulatorDetails;

public class ScenarioSimulatorMainTest {

	@Test
	public void testPurchaseGoodsSuccessfulPurchase() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		simulation.setScenario("scn/purchasegoods/SuccessfulPurchase.scn");
		
		SimulatorDetails sdetails=new SimulatorDetails();
		sdetails.setName("cdm");
		sdetails.setClassName(org.savara.scenario.simulator.cdm.CDMRoleSimulator.class.getName());
		simulation.getSimulators().add(sdetails);
		
		RoleDetails rdetails1=new RoleDetails();
		rdetails1.setScenarioRole("Buyer");
		rdetails1.setSimulator(sdetails.getName());
		rdetails1.setModel("cdm/PurchaseGoods.cdm");
		rdetails1.setScenarioRole("{http://www.jboss.org/savara/examples}Buyer");
		simulation.getRoles().add(rdetails1);
		
		RoleDetails rdetails2=new RoleDetails();
		rdetails2.setScenarioRole("Store");
		rdetails2.setSimulator(sdetails.getName());
		rdetails2.setModel("cdm/PurchaseGoods.cdm");
		rdetails2.setModelRole("{http://www.jboss.org/examples/store}Store");
		simulation.getRoles().add(rdetails2);
		
		RoleDetails rdetails3=new RoleDetails();
		rdetails3.setScenarioRole("CreditAgency");
		rdetails3.setSimulator(sdetails.getName());
		rdetails3.setModel("cdm/PurchaseGoods.cdm");
		rdetails3.setModelRole("{http://www.jboss.org/examples/creditAgency}CreditAgency");
		simulation.getRoles().add(rdetails3);
		
		if (simulator.simulate(simulation) != 0) {
			fail("Simulation should not have failed");
		}		
	}

	@Test
	public void testPurchaseGoodsExpectingErrorSuccessfulPurchase() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		simulation.setScenario("scn/purchasegoods/ExpectingErrorSuccessfulPurchase.scn");
		
		SimulatorDetails sdetails=new SimulatorDetails();
		sdetails.setName("cdm");
		sdetails.setClassName(org.savara.scenario.simulator.cdm.CDMRoleSimulator.class.getName());
		simulation.getSimulators().add(sdetails);
		
		RoleDetails rdetails1=new RoleDetails();
		rdetails1.setScenarioRole("Buyer");
		rdetails1.setSimulator(sdetails.getName());
		rdetails1.setModel("cdm/PurchaseGoods.cdm");
		rdetails1.setScenarioRole("{http://www.jboss.org/savara/examples}Buyer");
		simulation.getRoles().add(rdetails1);
		
		RoleDetails rdetails2=new RoleDetails();
		rdetails2.setScenarioRole("Store");
		rdetails2.setSimulator(sdetails.getName());
		rdetails2.setModel("cdm/PurchaseGoods.cdm");
		rdetails2.setModelRole("{http://www.jboss.org/examples/store}Store");
		simulation.getRoles().add(rdetails2);
		
		RoleDetails rdetails3=new RoleDetails();
		rdetails3.setScenarioRole("CreditAgency");
		rdetails3.setSimulator(sdetails.getName());
		rdetails3.setModel("cdm/PurchaseGoods.cdm");
		rdetails3.setModelRole("{http://www.jboss.org/examples/creditAgency}CreditAgency");
		simulation.getRoles().add(rdetails3);
		
		if (simulator.simulate(simulation) == 0) {
			fail("Simulation should have failed");
		}
		
	}

	@Test
	public void testPurchaseGoodsExpectedErrorInvalidPurchase() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		simulation.setScenario("scn/purchasegoods/ExpectedErrorInvalidPurchase.scn");
		
		SimulatorDetails sdetails=new SimulatorDetails();
		sdetails.setName("cdm");
		sdetails.setClassName(org.savara.scenario.simulator.cdm.CDMRoleSimulator.class.getName());
		simulation.getSimulators().add(sdetails);
		
		RoleDetails rdetails1=new RoleDetails();
		rdetails1.setScenarioRole("Buyer");
		rdetails1.setSimulator(sdetails.getName());
		rdetails1.setModel("cdm/PurchaseGoods.cdm");
		rdetails1.setScenarioRole("{http://www.jboss.org/savara/examples}Buyer");
		simulation.getRoles().add(rdetails1);
		
		RoleDetails rdetails2=new RoleDetails();
		rdetails2.setScenarioRole("Store");
		rdetails2.setSimulator(sdetails.getName());
		rdetails2.setModel("cdm/PurchaseGoods.cdm");
		rdetails2.setModelRole("{http://www.jboss.org/examples/store}Store");
		simulation.getRoles().add(rdetails2);
		
		RoleDetails rdetails3=new RoleDetails();
		rdetails3.setScenarioRole("CreditAgency");
		rdetails3.setSimulator(sdetails.getName());
		rdetails3.setModel("cdm/PurchaseGoods.cdm");
		rdetails3.setModelRole("{http://www.jboss.org/examples/creditAgency}CreditAgency");
		simulation.getRoles().add(rdetails3);
		
		if (simulator.simulate(simulation) != 0) {
			fail("Simulation should not have failed");
		}
		
	}

	@Test
	public void testPurchaseGoodsInvalidPurchase() {
		ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
		
		Simulation simulation=new Simulation();
		simulation.setScenario("scn/purchasegoods/InvalidPurchase.scn");
		
		SimulatorDetails sdetails=new SimulatorDetails();
		sdetails.setName("cdm");
		sdetails.setClassName(org.savara.scenario.simulator.cdm.CDMRoleSimulator.class.getName());
		simulation.getSimulators().add(sdetails);
		
		RoleDetails rdetails1=new RoleDetails();
		rdetails1.setScenarioRole("Buyer");
		rdetails1.setSimulator(sdetails.getName());
		rdetails1.setModel("cdm/PurchaseGoods.cdm");
		rdetails1.setScenarioRole("{http://www.jboss.org/savara/examples}Buyer");
		simulation.getRoles().add(rdetails1);
		
		RoleDetails rdetails2=new RoleDetails();
		rdetails2.setScenarioRole("Store");
		rdetails2.setSimulator(sdetails.getName());
		rdetails2.setModel("cdm/PurchaseGoods.cdm");
		rdetails2.setModelRole("{http://www.jboss.org/examples/store}Store");
		simulation.getRoles().add(rdetails2);
		
		RoleDetails rdetails3=new RoleDetails();
		rdetails3.setScenarioRole("CreditAgency");
		rdetails3.setSimulator(sdetails.getName());
		rdetails3.setModel("cdm/PurchaseGoods.cdm");
		rdetails3.setModelRole("{http://www.jboss.org/examples/creditAgency}CreditAgency");
		simulation.getRoles().add(rdetails3);
		
		if (simulator.simulate(simulation) == 0) {
			fail("Simulation should have failed");
		}
	}

}

