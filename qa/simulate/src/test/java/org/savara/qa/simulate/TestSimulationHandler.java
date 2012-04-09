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

import java.util.logging.Logger;

import org.savara.scenario.model.Event;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.simulation.SimulationHandler;

public class TestSimulationHandler implements SimulationHandler {

	private static final Logger LOG=Logger.getLogger(TestSimulationHandler.class.getName());
	
	private java.util.List<Event> m_noSimulatorEvents=new java.util.Vector<Event>();
	private java.util.List<Event> m_processedEvents=new java.util.Vector<Event>();
	private java.util.List<Event> m_unexpectedEvents=new java.util.Vector<Event>();
	private java.util.List<Event> m_errorEvents=new java.util.Vector<Event>();
	
	public java.util.List<Event> getNoSimulatorEvents() {
		return(m_noSimulatorEvents);
	}
	
	public java.util.List<Event> getProcessedEvents() {
		return(m_processedEvents);
	}
	
	public java.util.List<Event> getUnexpectedEvents() {
		return(m_unexpectedEvents);
	}
	
	public java.util.List<Event> getErrorEvents() {
		return(m_errorEvents);
	}

	public void roleStart(Role role) {
		// TODO Auto-generated method stub
		
	}

	public void roleInitialized(Role role) {
		// TODO Auto-generated method stub
		
	}

	public void roleFailed(Role role, String mesg) {
		// TODO Auto-generated method stub
		
	}
	
	public void start(Event event) {
	}
	
	public void end(Event event) {
	}
	
	public void noSimulator(Event event) {
		m_noSimulatorEvents.add(event);
	}

	public void processed(Event event) {
		m_processedEvents.add(event);
	}

	public void unexpected(Event event) {
		m_unexpectedEvents.add(event);
	}

	public void error(String mesg, Event event, Throwable e) {
		m_errorEvents.add(event);
		LOG.severe("ERROR: "+mesg+" Event="+event+" Exception="+e);
	}
	
	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		buf.append("Test Simulation Handler Report\r\n");
		buf.append("==============================\r\n");
		
		if (m_noSimulatorEvents.size() > 0) {
			buf.append("\r\nNo Simulator Events:\r\n");
			for (Event event : m_noSimulatorEvents) {
				buf.append(describe(event));
			}
		}
		
		if (m_processedEvents.size() > 0) {
			buf.append("\r\nProcessed Events:\r\n");
			for (Event event : m_processedEvents) {
				buf.append(describe(event));
			}
		}
		
		if (m_unexpectedEvents.size() > 0) {
			buf.append("\r\nUnexpected Events:\r\n");
			for (Event event : m_unexpectedEvents) {
				buf.append(describe(event));
			}
		}
		
		if (m_errorEvents.size() > 0) {
			buf.append("\r\nError Events:\r\n");
			for (Event event : m_errorEvents) {
				buf.append(describe(event));
			}
		}
		
		buf.append("\r\n==============================\r\n");
		
		return(buf.toString());
	}
	
	protected String describe(Event event) {
		String ret=event.getClass().getSimpleName();
		
		if (event instanceof MessageEvent) {
			MessageEvent me=(MessageEvent)event;
			
			ret += " operation="+me.getOperationName();
			if (me.getFaultName() != null) {
				ret += " fault="+me.getFaultName();
			}
		}
		
		ret += "\r\n";
		
		return(ret);
	}
}
