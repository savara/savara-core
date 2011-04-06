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

import java.io.File;

import org.junit.Test;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.simulation.DefaultSimulationContext;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.ScenarioSimulator;
import org.savara.scenario.simulation.ScenarioSimulatorFactory;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationModel;
import org.savara.scenario.util.ScenarioModelUtil;

import static org.junit.Assert.*;

public class CDMRoleSimulatorTest {

	@Test
	public void testGetSupportedModel() {
		
		String filename="cdm/PurchaseGoods.cdm";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			CDMRoleSimulator rsim=new CDMRoleSimulator();
			
			Object model=rsim.getSupportedModel(sm);
			
			if (model == null) {
				fail("No model returned");
			}
			
			if ((model instanceof org.pi4soa.cdl.Package) == false) {
				fail("Model not cdl package: "+model);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get supported model: "+e);
		}
	}

	@Test
	public void testGetModelRoles() {
		
		String filename="cdm/PurchaseGoods.cdm";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			CDMRoleSimulator rsim=new CDMRoleSimulator();
			
			Object model=rsim.getSupportedModel(sm);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			if (roles.size() != 3) {
				fail("Incorrect number of roles, expecting 3, but got: "+roles.size());
			}
			
			if (roles.get(0).getName().equals("{http://www.jboss.org/savara/examples}Buyer") == false) {
				fail("First role name incorrect");
			}
			
			if (roles.get(1).getName().equals("{http://www.jboss.org/examples/creditAgency}CreditAgency") == false) {
				fail("Second role name incorrect");
			}
			
			if (roles.get(2).getName().equals("{http://www.jboss.org/examples/store}Store") == false) {
				fail("Third role name incorrect");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testSimulatePurchaseGoodsSuccessfulPurchase() {
		
		String filename="cdm/PurchaseGoods.cdm";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			CDMRoleSimulator rsim=new CDMRoleSimulator();
			
			Object model=rsim.getSupportedModel(sm);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			// Load the scenario
			String scenarioFile="scn/purchasegoods/SuccessfulPurchase.scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioFile);
			
			java.io.InputStream sis=url.openStream();
			
			Scenario scenario=ScenarioModelUtil.deserialize(sis);
			
			sis.close();

			// Create the simulator
			ScenarioSimulator ssim=ScenarioSimulatorFactory.getScenarioSimulator();
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			java.util.Map<Role,RoleSimulator> roleSimulators=new java.util.HashMap<Role,RoleSimulator>();
			java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
			
			// Reorder roles
			roles.add(1, roles.remove(2));
			
			for (int i=0; i < roles.size(); i++) {
				roleSimulators.put(scenario.getRole().get(i), rsim);
				
				DefaultSimulationContext context=new DefaultSimulationContext(new File(url.getFile()));
				
				context.setModel(rsim.getModelForRole(model, roles.get(i)));
				
				rsim.initialize(context);
				
				contexts.put(scenario.getRole().get(i), context);
			}
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 8) {
				fail("Eight events were not processed");
			}
			
			if (handler.getUnexpectedEvents().size() != 0) {
				fail("Should be no unexpected events");
			}
			
			if (handler.getErrorEvents().size() != 0) {
				fail("Should be no error events");
			}
			
			if (handler.getNoSimulatorEvents().size() != 0) {
				fail("Should be no 'no simulator' events");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testSimulatePurchaseGoodsInvalidPurchase() {
		
		String filename="cdm/PurchaseGoods.cdm";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			CDMRoleSimulator rsim=new CDMRoleSimulator();
			
			Object model=rsim.getSupportedModel(sm);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			// Load the scenario
			String scenarioFile="scn/purchasegoods/InvalidPurchase.scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioFile);
			
			java.io.InputStream sis=url.openStream();
			
			Scenario scenario=ScenarioModelUtil.deserialize(sis);
			
			sis.close();

			// Create the simulator
			ScenarioSimulator ssim=ScenarioSimulatorFactory.getScenarioSimulator();
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			java.util.Map<Role,RoleSimulator> roleSimulators=new java.util.HashMap<Role,RoleSimulator>();
			java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
			
			// Reorder roles
			roles.add(1, roles.remove(2));
			
			for (int i=0; i < roles.size(); i++) {
				roleSimulators.put(scenario.getRole().get(i), rsim);
				
				DefaultSimulationContext context=new DefaultSimulationContext(new File(url.getFile()));
				
				context.setModel(rsim.getModelForRole(model, roles.get(i)));
				
				rsim.initialize(context);
				
				contexts.put(scenario.getRole().get(i), context);
			}
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 7) {
				fail("Should be 7 processed events: "+handler.getProcessedEvents().size());
			}
			
			if (handler.getUnexpectedEvents().size() != 1) {
				fail("Should be 1 unexpected event: "+handler.getUnexpectedEvents().size());
			}
			
			if (handler.getErrorEvents().size() != 0) {
				fail("Should be no error events");
			}
			
			if (handler.getNoSimulatorEvents().size() != 0) {
				fail("Should be no 'no simulator' events");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testSimulatePurchaseGoodsExpectedErrorInvalidPurchase() {
		
		String filename="cdm/PurchaseGoods.cdm";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			CDMRoleSimulator rsim=new CDMRoleSimulator();
			
			Object model=rsim.getSupportedModel(sm);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			// Load the scenario
			String scenarioFile="scn/purchasegoods/ExpectedErrorInvalidPurchase.scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioFile);
			
			java.io.InputStream sis=url.openStream();
			
			Scenario scenario=ScenarioModelUtil.deserialize(sis);
			
			sis.close();

			// Create the simulator
			ScenarioSimulator ssim=ScenarioSimulatorFactory.getScenarioSimulator();
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			java.util.Map<Role,RoleSimulator> roleSimulators=new java.util.HashMap<Role,RoleSimulator>();
			java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
			
			// Reorder roles
			roles.add(1, roles.remove(2));
			
			for (int i=0; i < roles.size(); i++) {
				roleSimulators.put(scenario.getRole().get(i), rsim);
				
				DefaultSimulationContext context=new DefaultSimulationContext(new File(url.getFile()));
				
				context.setModel(rsim.getModelForRole(model, roles.get(i)));
				
				rsim.initialize(context);
				
				contexts.put(scenario.getRole().get(i), context);
			}
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 8) {
				fail("Should be 8 processed events: "+handler.getProcessedEvents().size());
			}
			
			if (handler.getUnexpectedEvents().size() != 0) {
				fail("Should be 0 unexpected event: "+handler.getUnexpectedEvents().size());
			}
			
			if (handler.getErrorEvents().size() != 0) {
				fail("Should be no error events");
			}
			
			if (handler.getNoSimulatorEvents().size() != 0) {
				fail("Should be no 'no simulator' events");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}
	
	@Test
	public void testSimulatePurchaseGoodsExpectingErrorSuccessfulPurchase() {
		
		String filename="cdm/PurchaseGoods.cdm";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			CDMRoleSimulator rsim=new CDMRoleSimulator();
			
			Object model=rsim.getSupportedModel(sm);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			// Load the scenario
			String scenarioFile="scn/purchasegoods/ExpectingErrorSuccessfulPurchase.scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioFile);
			
			java.io.InputStream sis=url.openStream();
			
			Scenario scenario=ScenarioModelUtil.deserialize(sis);
			
			sis.close();

			// Create the simulator
			ScenarioSimulator ssim=ScenarioSimulatorFactory.getScenarioSimulator();
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			java.util.Map<Role,RoleSimulator> roleSimulators=new java.util.HashMap<Role,RoleSimulator>();
			java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
			
			// Reorder roles
			roles.add(1, roles.remove(2));
			
			for (int i=0; i < roles.size(); i++) {
				roleSimulators.put(scenario.getRole().get(i), rsim);
				
				DefaultSimulationContext context=new DefaultSimulationContext(new File(url.getFile()));
				
				context.setModel(rsim.getModelForRole(model, roles.get(i)));
				
				rsim.initialize(context);
				
				contexts.put(scenario.getRole().get(i), context);
			}
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 6) {
				fail("Expecting 6 processed events: "+handler.getProcessedEvents().size());
			}
			
			if (handler.getErrorEvents().size() != 2) {
				fail("Expecting 2 error events: "+handler.getErrorEvents().size());
			}
			
			if (handler.getUnexpectedEvents().size() != 0) {
				fail("Should be no unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() != 0) {
				fail("Should be no 'no simulator' events");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}
}
