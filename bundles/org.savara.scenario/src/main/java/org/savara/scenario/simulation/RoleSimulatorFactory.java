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

import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * This class represents the factory for managing access to
 * role simulators.
 *
 */
public class RoleSimulatorFactory {

	private static java.util.Map<String,RoleSimulator> m_roleSimulators=
					new java.util.HashMap<String,RoleSimulator>();
	
	private static final Logger logger=Logger.getLogger(RoleSimulatorFactory.class.getName());
	
	/**
	 * This method registers the supplied role simulator.
	 *  
	 * @param rsim The role simulator
	 */
	public static void register(RoleSimulator rsim) {
		logger.info("Registering role simulator: "+rsim.getName());
		m_roleSimulators.put(rsim.getName(), rsim);
	}
	
	/**
	 * This method unregisters the supplied role simulator.
	 *  
	 * @param rsim The role simulator
	 */
	public static void unregister(RoleSimulator rsim) {
		logger.info("Unregistering role simulator: "+rsim.getName());
		m_roleSimulators.remove(rsim.getName());
	}
	
	/**
	 * This method returns the list of role simulators.
	 * 
	 * @return The role simulators
	 */
	public static java.util.List<RoleSimulator> getRoleSimulators() {
		java.util.List<RoleSimulator> ret=new java.util.Vector<RoleSimulator>(m_roleSimulators.values());
		
		Collections.sort(ret, new Comparator<RoleSimulator>() {
			public int compare(RoleSimulator o1, RoleSimulator o2) {
				return(o1.getName().compareTo(o2.getName()));
			}
		});
		
		return(ret);
	}
	
	/**
	 * This method returns the role simulator associated with the
	 * supplied name.
	 * 
	 * @param name The role simulator name
	 * @return The role simulator, or null if not found
	 */
	public static RoleSimulator getRoleSimulator(String name) {
		return(m_roleSimulators.get(name));
	}
}
