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
 * This interface represents the callback handler for results from
 * simulating a scenario.
 * 
 * Each event will result in exactly one
 * and only one of these methods being invoked, to represent the
 * outcome of simulating the particular event.
 *
 */
public interface SimulationHandler {

	/**
	 * This method indicates that the role has begun initialization.
	 * 
	 * @param role The role
	 */
	public void roleStart(Role role);
	
	/**
	 * This method indicates that the role has been initialized.
	 * 
	 * @param role The role
	 */
	public void roleInitialized(Role role);
	
	/**
	 * This method indicates that the simulation initialization of a
	 * role has failed.
	 * 
	 * @param role The role
	 * @param mesg The message
	 */
	public void roleFailed(Role role, String mesg);
	
	/**
	 * This method is called to indicate that the event has started
	 * simulation.
	 * 
	 * @param event The event
	 */
	public void start(Event event);
	
	/**
	 * This method is called to indicate that there was no appropriate
	 * simulator for the supplied event.
	 * 
	 * @param event The unsimulated event
	 */
	public void noSimulator(Event event);
	
	/**
	 * This method indicates that the event has been successfully processed
	 * by the simulator.
	 * 
	 * @param event The event
	 */
	public void processed(Event event);
	
	/**
	 * This method is called when an unknown event is detected.
	 * 
	 * @param event The unknown event
	 */
	public void unexpected(Event event);
	
	/**
	 * This method is invoked if an exception is thrown while
	 * simulating an event.
	 * 
	 * @param mesg The message
	 * @param event The event
	 * @param e The exception
	 */
	public void error(String mesg, Event event, Throwable e);
	
	/**
	 * This method is called to indicate that the event has completed
	 * simulation.
	 * 
	 * @param event The event
	 */
	public void end(Event event);
	
}
