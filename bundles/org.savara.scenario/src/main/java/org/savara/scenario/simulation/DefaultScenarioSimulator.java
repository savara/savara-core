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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.common.logging.MessageFormatter;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.RoleEvent;
import org.savara.scenario.model.Scenario;

/**
 * This class provides the default implementation for the scenario
 * simulator interface.
 *
 */
public class DefaultScenarioSimulator implements ScenarioSimulator {

	private static final Logger LOG=Logger.getLogger(DefaultScenarioSimulator.class.getName());
	
	/**
	 * This method simulates the scenario against the pre-configured
	 * monitor. Results from the simulation are notified to the
	 * supplied simulation handler.
	 * 
	 * @param scenario The scenario to be simulated
	 * @param roleSimulators The simulators for the relevant roles in the scenario
	 * @oaram contexts The simulation contexts for each role being simulated
	 * @param handler The callback to notify of the simulation results
	 * @throws Exception Failed to simulate
	 */
	public void simulate(Scenario scenario, java.util.Map<Role,RoleSimulator> roleSimulators,
			java.util.Map<Role,SimulationContext> contexts, SimulationHandler handler) throws Exception {
		
		// Initialize the simulation contexts against the appropriate simulators
		for (Role role : contexts.keySet()) {
			RoleSimulator rsim=roleSimulators.get(role);
			
			handler.roleStart(role);
			
			if (rsim != null) {
				
				try {
					rsim.initialize(contexts.get(role));
					
					handler.roleInitialized(role);
					
				} catch (Throwable t) {
					LOG.log(Level.SEVERE, 
							"Failed to initialize role '"+role.getName()+"'", t);
					
					handler.roleFailed(role,
							"Failed to initialize role simulator");
				}
			}
		}
		
		for (Event event : scenario.getEvent()) {
			
			handler.start(event);
			
			if (event instanceof RoleEvent) {
				simulateAtRole((Role)((RoleEvent)event).getRole(), event,
								roleSimulators, contexts, handler);
			} else {
				// Need to distribute to all role simulators
				for (Role role : roleSimulators.keySet()) {
					simulateAtRole(role, event, roleSimulators, contexts, handler);
				}
			}
			
			handler.end(event);
		}
		
		// Close the simulation contexts against the appropriate simulators
		for (Role role : contexts.keySet()) {
			RoleSimulator rsim=roleSimulators.get(role);	
			if (rsim != null) {
				rsim.close(contexts.get(role));
			}
		}
		
	}
	
	protected void simulateAtRole(Role role, Event event, java.util.Map<Role,RoleSimulator> roleSimulators,
			java.util.Map<Role,SimulationContext> contexts, SimulationHandler handler) {
		RoleSimulator sim=roleSimulators.get(role);
		
		if (sim != null) {
			SimulationContext context=contexts.get(role);
			
			if (context != null) {
				sim.onEvent(context, event, handler);
			} else {
				handler.error(MessageFormatter.format(java.util.PropertyResourceBundle.getBundle(
						"org.savara.scenario.Messages"), "SAVARA-SCENARIO-00001",
							role.getName()), event, null);
			}
		} else {
			handler.noSimulator(event);
		}
	}
}
