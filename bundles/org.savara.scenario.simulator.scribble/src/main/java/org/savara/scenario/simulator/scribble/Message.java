/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and others contributors as indicated
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

/**
 * This class represents a message to be simulated.
 *
 */
public class Message extends org.scribble.protocol.monitor.DefaultMessage  {

	private String _fault=null;
	private java.util.List<String> _values=new java.util.Vector<String>();
	
	/**
	 * This method sets the fault.
	 * 
	 * @param fault The fault
	 */
	public void setFault(String fault) {
		_fault = fault;
	}
	
	/**
	 * This method returns the optional fault.
	 * 
	 * @return The optional fault
	 */
	public String getFault() {
		return(_fault);
	}
	
	/**
	 * This method returns the list of values associated with the message.
	 * 
	 * NOTE: The number of values must match the number of types.
	 * 
	 * @return The list of values
	 */
	public java.util.List<String> getValues() {
		return(_values);
	}
	
}
