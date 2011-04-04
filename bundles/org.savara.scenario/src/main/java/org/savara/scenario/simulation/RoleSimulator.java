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

import org.savara.scenario.model.Event;
import org.savara.scenario.model.Role;

/**
 * This interface represents the simulator for a particular role within
 * a scenario.
 *
 */
public interface RoleSimulator {
	
	/**
	 * This method returns the name of the role simulator.
	 * 
	 * @return The name
	 */
	public String getName();
	
	/**
	 * This method initializes the simulation context.
	 * 
	 * @param context The simulation context
	 * @throws Exception Failed to initialize the context
	 */
	public void initialize(SimulationContext context) throws Exception;
	
	/**
	 * This method identifies whether the role simulator supports
	 * the model information, and if so, returns the specific model
	 * representation. If the model is not supported, then a null
	 * is returned.
	 * 
	 * @param model The simulation model information
	 * @return The supported model, or null if not handled
	 */
	public Object getSupportedModel(SimulationModel model);
	
	/**
	 * This method returns the list of roles associated with the supplied
	 * model, if the model represents a global conversation type. If the
	 * model is a local conversation type, then an empty list will be
	 * returned.
	 * 
	 * @param model The model
	 * @return The list of roles defined in the supplied model
	 */
	public java.util.List<Role> getModelRoles(Object model);

	/**
	 * This method returns the model, derived from the supplied model,
	 * that should be used for the specified role.
	 * 
	 * @param model The model
	 * @param role The role
	 * @return The simulation model
	 */
	public Object getModelForRole(Object model, Role role);
	
	/**
	 * This method simulates the supplied event within the specified simulation
	 * context. The results are reported to the supplied handler.
	 * 
	 * @param context The context
	 * @param event The event
	 * @param handler The handler
	 */
	public void onEvent(SimulationContext context, Event event, SimulationHandler handler);
	
}
