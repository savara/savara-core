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
package org.savara.scenario.simulator.sca;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.simulation.SimulationHandler;

public class MessageStore {

	private static java.util.List<ReceiveEvent> m_receiveEvents=new java.util.Vector<ReceiveEvent>();
	private static java.util.List<SendEvent> m_sendEvents=new java.util.Vector<SendEvent>();
	private static SimulationHandler m_handler=null;

	public static java.util.List<Event> getOutstandingEvents() {
		java.util.List<Event> ret=new java.util.Vector<Event>();
		ret.addAll(m_receiveEvents);
		ret.addAll(m_sendEvents);
		return(ret);
	}
	
	public static boolean waitForSendEvent(Message mesg) throws Exception {
		boolean f_found=false;

		
		synchronized(m_sendEvents) {	
			do {
				for (SendEvent send : m_sendEvents) {
					if (send.getOperationName().equals(mesg.getOperation().getName())) {
						f_found = true;
						
						// Remove handled receive event
						m_sendEvents.remove(send);
						
						m_handler.processed(send);
						
						break;
					}
				}
				
				if (!f_found) {
					m_sendEvents.wait();
				}
				
			} while (!f_found);
		}
		
		return(f_found);
	}
	
	public static void handleSendEvent(SendEvent send, SimulationHandler handler) throws Exception {
		// TODO: Need better way to tie receive event with a particular simulation handler
		// in case multiple simulations done?
		m_handler = handler;
		
		synchronized(m_sendEvents) {
			m_sendEvents.add(send);
			
			m_sendEvents.notifyAll();
		}
	}
	
	public static void handleReceiveEvent(ReceiveEvent receive, SimulationHandler handler) throws Exception {
		// TODO: Need better way to tie receive event with a particular simulation handler
		// in case multiple simulations done?
		m_handler = handler;
		
		synchronized(m_receiveEvents) {
			m_receiveEvents.add(receive);
			
			m_receiveEvents.notifyAll();
		}
	}
	
	public static Message waitForReceiveEvent(Operation operation) throws Exception {
		Message ret=null;
		
		synchronized(m_receiveEvents) {
			boolean f_found=false;
			
			do {
				for (ReceiveEvent receive : m_receiveEvents) {
					if (receive.getOperationName().equals(operation.getName())) {
						f_found = true;
						
						org.apache.tuscany.sca.core.invocation.impl.MessageImpl resp=
								new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();

						resp.setOperation(operation);
						
						// TODO: Check if multiple parameters and report error?
						resp.setBody(receive.getParameter().get(0).getValue());
						
						ret = resp;
						
						// Remove handled receive event
						m_receiveEvents.remove(receive);
						
						m_handler.processed(receive);
						
						break;
					}
				}
				
				if (!f_found) {
					m_receiveEvents.wait();
				}
				
			} while (!f_found);
		}
		
		return(ret);
	}
}
