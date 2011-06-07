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
package org.savara.scenario.simulator.sca.internal;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;

public class MessageStore {

	private static final Logger logger=Logger.getLogger(MessageStore.class.getName());
	
	private java.util.List<ReceiveEvent> m_receiveEvents=new java.util.Vector<ReceiveEvent>();
	private java.util.concurrent.SynchronousQueue<SendEvent> m_sendEvents=
							new java.util.concurrent.SynchronousQueue<SendEvent>();
	private SimulationHandler m_handler=null;
	private SimulationContext m_context=null;

	public void setSimulationContext(SimulationContext context) {
		m_context = context;
	}
	
	public void waitForSendEvent(Message mesg) throws Exception {

		SendEvent send=m_sendEvents.take();
		
		// Check if value is correct
		if (isValidMessage(send, mesg)) {
			m_handler.processed(send);
		} else {
			m_handler.unexpected(send);
		}
		
	}
	
	protected String getValue(String path) {
		String ret=null;
		
		try {
			java.io.InputStream is=m_context.getResource(path);
			
			byte[] b=new byte[is.available()];
			is.read(b);
			
			ret = new String(b);
			
			is.close();
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to get parameter value '"+path+"'", e);
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Get value = "+ret);
		}	
		
		return(ret);
	}
	
	protected boolean isValidMessage(MessageEvent event, Message mesg) {
		boolean ret=false;
		
		// Check the operation names
		if (event.getOperationName().equals(mesg.getOperation().getName())) {

			if (mesg.getBody() instanceof Object[]) {
				// Multiple parameters
				Object[] params=(Object[])mesg.getBody();
				
				if (event.getParameter().size() == params.length) {
					// Validate the message bodies
					ret = true;
					for (int i=0; ret && i < event.getParameter().size(); i++) {
						ret = isValidParameter(event.getParameter().get(i), params[i]);
					}
				}
			} else if (event.getParameter().size() == 1) {
				ret = isValidParameter(event.getParameter().get(0), mesg.getBody());
			}
		}
		
		return(ret);
	}
	
	protected boolean isValidParameter(Parameter param, Object value) {
		boolean ret=false;
		
		String paramValue=getValue(param.getValue());
		
		if (paramValue != null && paramValue.equals(value)) {
			ret = true;
		}

		if (logger.isLoggable(Level.INFO)) {
			logger.info("Is valid parameter '"+param.getValue()+":"+paramValue+"' = '"+value+"'? "+ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method blocks waiting for the simulation engine to send the message
	 * associated with the supplied SendEvent.
	 * 
	 * @param send The send event
	 * @param handler The handler
	 * @throws Exception Failed to handle send event
	 */
	public void handleSendEvent(SendEvent send, SimulationHandler handler) throws Exception {
		// TODO: Need better way to tie receive event with a particular simulation handler
		// in case multiple simulations done?
		m_handler = handler;
		
		m_sendEvents.offer(send, 5000, TimeUnit.MILLISECONDS);
	}
	
	public void handleReceiveEvent(ReceiveEvent receive, SimulationHandler handler) throws Exception {
		// TODO: Need better way to tie receive event with a particular simulation handler
		// in case multiple simulations done?
		m_handler = handler;
		
		synchronized(m_receiveEvents) {
			m_receiveEvents.add(receive);
			
			m_receiveEvents.notifyAll();
		}
	}
	
	public Message waitForReceiveEvent(Operation operation) throws Exception {
		Message ret=null;
		
		synchronized(m_receiveEvents) {
			boolean f_found=false;
			long endtime=System.currentTimeMillis()+5000;
			
			do {
				for (ReceiveEvent receive : m_receiveEvents) {
					if (receive.getOperationName().equals(operation.getName())) {
						f_found = true;
						
						org.apache.tuscany.sca.core.invocation.impl.MessageImpl resp=
								new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();

						resp.setOperation(operation);
						
						// TODO: Check if multiple parameters and report error?
						String value=getValue(receive.getParameter().get(0).getValue());
						resp.setBody(value);
						
						ret = resp;
						
						// Remove handled receive event
						m_receiveEvents.remove(receive);
						
						// Mark received messages as processed, as they are supplied
						// by the scenario, not generated by the service under test.
						// Will only report unexpected if the service cannot accept
						// the message.
						m_handler.processed(receive);
						
						break;
					}
				}
				
				if (!f_found) {
					long delay=endtime-System.currentTimeMillis();
					if (delay <= 0) {
						f_found = true;
					} else {
						m_receiveEvents.wait(delay);
					}
				}
				
			} while (!f_found);
		}
		
		return(ret);
	}
}
