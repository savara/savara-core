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

import java.util.List;

import org.savara.scenario.model.Event;
import org.savara.scenario.model.Role;

public class TestRoleSimulator implements RoleSimulator {

	private String m_name=null;
	private boolean m_unexpected=false;
	
	public TestRoleSimulator(String name, boolean unexpected) {
		m_name = name;
		m_unexpected = unexpected;
	}
	
	public String getName() {
		return(m_name);
	}

	public Object getSupportedModel(SimulationModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Role> getModelRoles(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onEvent(SimulationContext context, Event event,
			SimulationHandler handler) {
		if (m_unexpected) {
			handler.unexpected(event);
		} else {
			handler.processed(event);
		}
	}

	public void initialize(SimulationContext context) throws Exception {
	}

	public Object getModelForRole(Object model, Role role) {
		return null;
	}

}
