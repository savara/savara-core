/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.scenario.simulator.scribble;

import java.util.List;
import java.util.UUID;

import org.savara.monitor.ConversationId;
import org.savara.monitor.Message;
import org.savara.monitor.Monitor;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulation.SimulationModel;

/**
 * This class provides the scribble implementation of the Role Simulator interface.
 *
 */
public class ScribbleRoleSimulator implements RoleSimulator {

	private Monitor m_monitor=null;
	
	/**
	 * This method sets the monitor used to simulate the scenario.
	 * 
	 * @param monitor The monitor
	 */
	public void setMonitor(Monitor monitor) {
		m_monitor = monitor;
	}
	
	/**
	 * This method simulates the scenario against the pre-configured
	 * monitor. Results from the simulation are notified to the
	 * supplied simulation handler.
	 * 
	 * @param scenario The scenario to be simulated
	 * @param handler The callback to notify of the simulation results
	 */
	public void simulate(Scenario scenario, SimulationHandler handler) {
		
		ConversationId cid=new ConversationId(UUID.randomUUID().toString());
		
		simulateEvents(cid, scenario.getEvent(), handler);
	}
	
	/**
	 * This method simulates a list of events, associated with a scenario.
	 * 
	 * @param cid The conversation instance id
	 * @param events The list of events
	 * @param handler The handler
	 */
	protected void simulateEvents(ConversationId cid,
					java.util.List<Event> events, SimulationHandler handler) {
		
		for (Event event : events) {
			if (event instanceof SendEvent) {
				handleSendEvent(cid, (SendEvent)event, handler);
			} else if (event instanceof ReceiveEvent) {
				handleReceiveEvent(cid, (ReceiveEvent)event, handler);
			} else {
				handler.unexpected(event);
			}
		}
	}
	
	protected Message getMessageForEvent(MessageEvent event) {
		Message mesg=new Message();
		mesg.setOperator(event.getOperationName());
		
		for (Parameter p : event.getParameter()) {
			mesg.getTypes().add(p.getType());
			mesg.getValues().add(p.getValue());
		}
		
		return(mesg);
	}
	
	protected void handleSendEvent(ConversationId cid,
					SendEvent event, SimulationHandler handler) {
		try {
			Message mesg=getMessageForEvent(event);
			
			mesg.setSourceEndpointType(((Role)event.getRole()).getName());

			m_monitor.process(null, cid, mesg);
		} catch(Exception e) {
			handler.error("Failed when handling send event", event, e);
		}
	}
	
	protected void handleReceiveEvent(ConversationId cid,
					ReceiveEvent event, SimulationHandler handler) {
		try {
			Message mesg=getMessageForEvent(event);
			
			mesg.setDestinationEndpointType(((Role)event.getRole()).getName());

			m_monitor.process(null, cid, mesg);
		} catch(Exception e) {
			handler.error("Failed when handling receive event", event, e);
		}		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initialize(SimulationContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public boolean isSupported(SimulationModel model) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getModel(SimulationModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Role> getModelRoles(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getModelForRole(Object model, Role role) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onEvent(SimulationContext context, Event event,
			SimulationHandler handler) {
		// TODO Auto-generated method stub
		
	}

	public void close(SimulationContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
