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
package org.savara.scenario.simulator.switchyard;

import java.util.logging.Logger;

import org.savara.scenario.model.Event;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.SimulationHandler;

public class TestSimulationHandler implements SimulationHandler {

	private static final Logger logger=Logger.getLogger(TestSimulationHandler.class.getName());
	
	private java.util.List<Event> m_noSimulator=new java.util.Vector<Event>();
	private java.util.List<Event> m_processed=new java.util.Vector<Event>();
	private java.util.List<Event> m_unexpected=new java.util.Vector<Event>();
	private java.util.List<Event> m_error=new java.util.Vector<Event>();

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
		logger.info("START: "+printable(event));
	}

	public void end(Event event) {
		logger.info("END: "+printable(event));
	}

	public void noSimulator(Event event) {
		m_noSimulator.add(event);
		logger.info("NO SIMULATOR: "+printable(event));
	}

	public void processed(Event event) {
		m_processed.add(event);
		logger.info("PROCESSED: "+printable(event));
	}

	public void unexpected(Event event) {
		m_unexpected.add(event);
		logger.info("UNEXPECTED: "+printable(event));
	}

	public void error(String mesg, Event event, Throwable e) {
		m_error.add(event);
		logger.info("ERROR: ("+mesg+") "+printable(event)+" exception="+e);
		e.printStackTrace();
	}
	
	public java.util.List<Event> getNoSimulatorEvents() {
		return(m_noSimulator);
	}
	
	public java.util.List<Event> getProcessedEvents() {
		return(m_processed);
	}
	
	public java.util.List<Event> getUnexpectedEvents() {
		return(m_unexpected);
	}
	
	public java.util.List<Event> getErrorEvents() {
		return(m_error);
	}
	
	protected String printable(Event event) {
		String ret=null;
		
		if (event instanceof SendEvent) {
			SendEvent send=(SendEvent)event;
			ret = "send "+send.getOperationName();
		} else {
			ReceiveEvent recv=(ReceiveEvent)event;
			ret = "recv "+recv.getOperationName();
		}
		
		return(ret);
	}
}
