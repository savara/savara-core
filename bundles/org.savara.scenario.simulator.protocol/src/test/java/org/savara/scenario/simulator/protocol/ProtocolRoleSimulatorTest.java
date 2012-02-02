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
package org.savara.scenario.simulator.protocol;

import static org.junit.Assert.*;

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
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.monitor.model.Description;

public class ProtocolRoleSimulatorTest {

	@Test
	public void testGetSupportedModel() {
		ProtocolRoleSimulator sim=new ProtocolRoleSimulator();
		
		String filename="monitor/models/PurchaseGoods.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel simmodel=new SimulationModel(filename, is);
			
			Object model=sim.getModel(simmodel,null);
			
			if (model == null) {
				fail("Model is null");
			} else if ((model instanceof ProtocolModel) == false) {
				fail("Model is not a ProtocolModel");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}

	}

	@Test
	public void testGetModelRoles() {
		
		String filename="monitor/models/PurchaseGoods.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator sim=new ProtocolRoleSimulator();
			
			Object model=sim.getModel(sm, null);
			
			java.util.List<Role> roles=sim.getModelRoles(model);
			
			if (roles.size() != 4) {
				fail("Incorrect number of roles, expecting 4, but got: "+roles.size());
			}
			
			Role r1=new Role();
			r1.setName("Buyer");
			
			Role r2=new Role();
			r2.setName("CreditAgency");
			
			Role r3=new Role();
			r3.setName("Store");
			
			Role r4=new Role();
			r4.setName("Logistics");
			
			if (roles.get(0).getName().equals(r1.getName()) == false) {
				fail("Role '"+r1.getName()+"' not found");
			}
			
			if (roles.get(1).getName().equals(r2.getName()) == false) {
				fail("Role '"+r2.getName()+"' not found");
			}
			
			if (roles.get(2).getName().equals(r3.getName()) == false) {
				fail("Role '"+r3.getName()+"' not found");
			}
			
			if (roles.get(3).getName().equals(r4.getName()) == false) {
				fail("Role '"+r4.getName()+"' not found");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testGetModelRolesForLocalModel() {
		
		String filename="monitor/models/PurchaseGoods@Store.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator sim=new ProtocolRoleSimulator();
			
			Object model=sim.getModel(sm, null);
			
			java.util.List<Role> roles=sim.getModelRoles(model);
			
			if (roles.size() != 1) {
				fail("Incorrect number of roles, expecting 1, but got: "+roles.size());
			}
			
			Role r1=new Role();
			r1.setName("Store");
			
			if (roles.get(0).getName().equals(r1.getName()) == false) {
				fail("Role '"+r1.getName()+"' not found");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testGetModelForRoleStore() {
		
		String filename="monitor/models/PurchaseGoods.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator sim=new ProtocolRoleSimulator();
			
			Object model=sim.getModel(sm, null);
			
			Role role=new Role();
			role.setName("Store");
			
			Object localModel=sim.getModelForRole(model, role, null);
			
			if (localModel == null) {
				fail("Local Model is null");
			} else if ((localModel instanceof Description) == false) {
				fail("Local Model is not a Protocol Monitor Description");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testGetModelForRoleStoreFromLocalModel() {
		
		String filename="monitor/models/PurchaseGoods@Store.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator sim=new ProtocolRoleSimulator();
			
			Object model=sim.getModel(sm, null);
			
			Role role=new Role();
			role.setName("Store");
			
			Object localModel=sim.getModelForRole(model, role, null);
			
			if (localModel == null) {
				fail("Local Model is null");
			} else if ((localModel instanceof Description) == false) {
				fail("Local Model is not a Protocol Monitor Description");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testSimulatePurchaseGoods_SuccessfulPurchase() {
		
		String filename="monitor/models/PurchaseGoods.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator rsim=new ProtocolRoleSimulator();
			
			Object model=rsim.getModel(sm, null);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			// Load the scenario
			String scenarioFile="scenarios/purchasegoods/SuccessfulPurchase.scn";
			
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
				
				context.setModel(rsim.getModelForRole(model, roles.get(i), null));
				
				rsim.initialize(context);
				
				contexts.put(scenario.getRole().get(i), context);
			}
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 12) {
				fail("Twelve events were not processed: "+handler.getProcessedEvents().size());
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
	public void testSimulatePurchaseGoodsAtStore_SuccessfulPurchase() {
		
		String filename="monitor/models/PurchaseGoods@Store.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator rsim=new ProtocolRoleSimulator();
			
			Object model=rsim.getModel(sm, null);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			if (roles.size() != 1) {
				fail("Expecting 1 role: "+roles.size());
			}
			
			// Load the scenario
			String scenarioFile="scenarios/purchasegoods/SuccessfulPurchase.scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioFile);
			
			java.io.InputStream sis=url.openStream();
			
			Scenario scenario=ScenarioModelUtil.deserialize(sis);
			
			sis.close();

			// Create the simulator
			ScenarioSimulator ssim=ScenarioSimulatorFactory.getScenarioSimulator();
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			java.util.Map<Role,RoleSimulator> roleSimulators=new java.util.HashMap<Role,RoleSimulator>();
			java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
			
			Role storeRole=null;
			for (Role r : scenario.getRole()) {
				if (r.getName().equals("Store")) {
					storeRole = r;
					break;
				}
			}
			
			roleSimulators.put(storeRole, rsim);
				
			DefaultSimulationContext context=new DefaultSimulationContext(new File(url.getFile()));
				
			context.setModel(rsim.getModelForRole(model, roles.get(0), null));
				
			rsim.initialize(context);
				
			contexts.put(storeRole, context);
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 6) {
				fail("Six events were not processed: "+handler.getProcessedEvents().size());
			}
			
			if (handler.getUnexpectedEvents().size() != 0) {
				fail("Should be no unexpected events");
			}
			
			if (handler.getErrorEvents().size() != 0) {
				fail("Should be no error events");
			}
			
			if (handler.getNoSimulatorEvents().size() != 6) {
				fail("Should be 6 'no simulator' events: "+handler.getNoSimulatorEvents().size());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

	@Test
	public void testSimulatePurchaseGoods_InvalidStoreBehaviour() {
		
		String filename="monitor/models/PurchaseGoods.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator rsim=new ProtocolRoleSimulator();
			
			Object model=rsim.getModel(sm, null);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			// Load the scenario
			String scenarioFile="scenarios/purchasegoods/InvalidStoreBehaviour.scn";
			
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
				
				context.setModel(rsim.getModelForRole(model, roles.get(i), null));
				
				rsim.initialize(context);
				
				contexts.put(scenario.getRole().get(i), context);
			}
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 9) {
				fail("Nine events were not processed: "+handler.getProcessedEvents().size());
			}
			
			if (handler.getUnexpectedEvents().size() != 3) {
				fail("Should be 3 unexpected events: "+handler.getUnexpectedEvents().size());
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
	public void testSimulatePurchaseGoodsAtStore_InvalidStoreBehaviour() {
		
		String filename="monitor/models/PurchaseGoods@Store.spr";
		
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream(filename);
		
		try {
			SimulationModel sm=new SimulationModel(filename, is);
			
			ProtocolRoleSimulator rsim=new ProtocolRoleSimulator();
			
			Object model=rsim.getModel(sm, null);
			
			java.util.List<Role> roles=rsim.getModelRoles(model);
			
			if (roles.size() != 1) {
				fail("Expecting 1 role: "+roles.size());
			}
			
			// Load the scenario
			String scenarioFile="scenarios/purchasegoods/InvalidStoreBehaviour.scn";
			
			java.net.URL url=ClassLoader.getSystemResource(scenarioFile);
			
			java.io.InputStream sis=url.openStream();
			
			Scenario scenario=ScenarioModelUtil.deserialize(sis);
			
			sis.close();

			// Create the simulator
			ScenarioSimulator ssim=ScenarioSimulatorFactory.getScenarioSimulator();
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			java.util.Map<Role,RoleSimulator> roleSimulators=new java.util.HashMap<Role,RoleSimulator>();
			java.util.Map<Role,SimulationContext> contexts=new java.util.HashMap<Role,SimulationContext>();
			
			Role storeRole=null;
			for (Role r : scenario.getRole()) {
				if (r.getName().equals("Store")) {
					storeRole = r;
					break;
				}
			}
			
			roleSimulators.put(storeRole, rsim);
				
			DefaultSimulationContext context=new DefaultSimulationContext(new File(url.getFile()));
				
			context.setModel(rsim.getModelForRole(model, roles.get(0), null));
				
			rsim.initialize(context);
				
			contexts.put(storeRole, context);
			
			ssim.simulate(scenario, roleSimulators, contexts, handler);
			
			if (handler.getProcessedEvents().size() != 3) {
				fail("Three events were not processed: "+handler.getProcessedEvents().size());
			}
			
			if (handler.getUnexpectedEvents().size() != 3) {
				fail("Should be 3 unexpected events: "+handler.getUnexpectedEvents().size());
			}
			
			if (handler.getErrorEvents().size() != 0) {
				fail("Should be no error events");
			}
			
			if (handler.getNoSimulatorEvents().size() != 6) {
				fail("Should be 6 'no simulator' events: "+handler.getNoSimulatorEvents().size());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to get model roles: "+e);
		}
	}

}
