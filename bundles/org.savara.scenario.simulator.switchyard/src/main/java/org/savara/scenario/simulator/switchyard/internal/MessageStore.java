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
package org.savara.scenario.simulator.switchyard.internal;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.util.MessageUtil;

public class MessageStore {

	private static final int TIMEOUT = 2000;

	private static final Logger LOG=Logger.getLogger(MessageStore.class.getName());
	
	private java.util.List<ReceiveEvent> _receiveEvents=new java.util.Vector<ReceiveEvent>();
	private java.util.concurrent.SynchronousQueue<SendEvent> _sendEvents=
			new java.util.concurrent.SynchronousQueue<SendEvent>();
	private SimulationHandler _handler=null;
	private SimulationContext _context=null;

	public MessageStore() {	
	}
	
	public void setHandler(SimulationHandler handler) {
		_handler = handler;
	}
	
	public java.util.Collection<ReceiveEvent> getReceiveEvents() {
		return (_receiveEvents);
	}
	
	public java.util.Collection<SendEvent> getSendEvents() {
		return (_sendEvents);
	}
	
	public void setSimulationContext(SimulationContext context) {
		_context = context;
	}
	
	public void waitForSendEvent(String operation, Object content) throws Exception {

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("waitForSendEvent: op="+operation+" content="+content);
		}
		
		SendEvent send=_sendEvents.take();
		
		// Check if value is correct
		if (isValidMessage(send, operation, content)) {
			_handler.processed(send);
		} else {
			_handler.unexpected(send);
		}
		
		synchronized(send) {
			send.notifyAll();
		}
		
	}
	
	public String getMessageContent(String path) {
		String ret=null;
		
		try {
			java.io.InputStream is=_context.getResource(path);
			
			byte[] b=new byte[is.available()];
			is.read(b);
			
			ret = new String(b);
			
			is.close();
		} catch(Exception e) {
			LOG.log(Level.SEVERE, "Failed to get parameter value '"+path+"'", e);
		}
		
		if (LOG.isLoggable(Level.INFO)) {
			LOG.info("Get value = "+ret);
		}	
		
		return(ret);
	}
	
	protected boolean isValidMessage(MessageEvent event, String operation,
					Object content) {
		boolean ret=false;
		
		// Check the operation names
		if (event.getOperationName().equals(operation)) {
			
			if (event.getParameter().size() == 1) {
				ret = isValidParameter(event.getParameter().get(0),
						content);
						//transformJAXBToNodeValue(content, mesg.getOperation(), dtype));
			}
		}
		
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Is message op="+operation+" content="+content
					+" valid against event "+event+"? = "+ret);
		}
		
		return(ret);
	}
	
	protected boolean isValidParameter(Parameter param, Object value) {
		boolean ret=false;
		
		String paramValue=getMessageContent(param.getValue());
		
		if (paramValue != null && MessageUtil.isValid(paramValue, value)) {
			ret = true;
		}
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Is valid parameter '"+param.getValue()+":"+paramValue+"' = '"+value+"'? "+ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method blocks waiting for the simulation engine to send the message
	 * associated with the supplied SendEvent.
	 * 
	 * @param send The send event
	 * @throws Exception Failed to handle send event
	 */
	public void handleSendEvent(SendEvent send) throws Exception {
		synchronized(send) {
			if (!_sendEvents.offer(send, TIMEOUT, TimeUnit.MILLISECONDS)) {
				_handler.unexpected(send);
			}
			
			send.wait(TIMEOUT);
		} 
	}
	
	public void handleReceiveEvent(ReceiveEvent receive) throws Exception {
		synchronized(receive) {
			_receiveEvents.add(receive);
			
			synchronized(_receiveEvents) {
				_receiveEvents.notifyAll();
			}
			
			receive.wait(TIMEOUT);
		}
	}
	
	public ReceiveEvent waitForReceiveEvent(String operation) throws Exception {
		ReceiveEvent ret=null;
		
		LOG.info("Wait for receive event: op="+operation);
		
		synchronized(_receiveEvents) {
			boolean f_found=false;
			long endtime=System.currentTimeMillis()+TIMEOUT;
			
			do {
				for (ReceiveEvent receive : _receiveEvents) {
					if (receive.getOperationName().equals(operation)) {
						f_found = true;
						
						// TODO: Check if multiple parameters and report error?
						ret = receive;
						
						// Remove handled receive event
						_receiveEvents.remove(receive);
						
						// Mark received messages as processed, as they are supplied
						// by the scenario, not generated by the service under test.
						// Will only report unexpected if the service cannot accept
						// the message.
						_handler.processed(receive);
						
						synchronized(receive) {
							receive.notifyAll();
						}

						break;
					}
				}
				
				if (!f_found) {
					long delay=endtime-System.currentTimeMillis();
					if (delay <= 0) {
						f_found = true;
					} else {
						_receiveEvents.wait(delay);
					}
				}
				
			} while (!f_found);
		}
		
		return(ret);
	}
	
	public void close() {
		if (_handler != null) {
			for (MessageEvent me : getReceiveEvents()) {
				_handler.unexpected(me);
			}
			for (MessageEvent me : getSendEvents()) {
				_handler.unexpected(me);
			}
		}
	}
}
