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

public class TestSimulationHandler implements SimulationHandler {

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
	}
}
