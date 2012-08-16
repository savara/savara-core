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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.savara.common.resources.ResourceLocator;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulation.SimulationModel;
import org.savara.scenario.simulator.switchyard.binding.soap.OutboundHandler;
import org.savara.scenario.simulator.switchyard.binding.soap.deploy.SOAPActivator;
import org.savara.scenario.simulator.switchyard.internal.MessageStore;
import org.switchyard.SwitchYard;
import org.switchyard.deploy.Activator;
import org.switchyard.test.mixins.CDIMixIn;

/**
 * The Switchyard based implementation of the RoleSimulator.
 *
 */
public class SwitchyardRoleSimulator implements RoleSimulator {

	private static final String SWITCHYARD_SIMULATOR = "Switchyard simulator";
	private static final String SWITCHYARD_DESCRIPTOR = "switchyard.xml";
	
	private SimulationContext _context=null;
	
	private SwitchYard _switchyard=null;
	private SOAPActivator _activator=null;
	private MessageStore _messageStore=new MessageStore();
	private int _eventCounter=0;
	
	private static final Logger LOG=Logger.getLogger(SwitchyardRoleSimulator.class.getName());

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return SWITCHYARD_SIMULATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(SimulationContext context) throws Exception {
		_context = context;
		
		if (_messageStore != null) {
			_messageStore.setSimulationContext(context);
		}
		
		if (context.getModel() instanceof java.io.File) {
			java.io.InputStream is=new java.io.FileInputStream(
							(java.io.File)context.getModel());
			
			_switchyard = new SwitchYard(is);
			
			is.close();
			
			CDIMixIn mix=new CDIMixIn();
			mix.initialize();
			
			_switchyard.start();
			
			for (Activator activator : _switchyard.getActivatorList()) {
				
				if (activator instanceof SOAPActivator) {
					_activator = (SOAPActivator)activator;
					
					for (OutboundHandler oh : _activator.getOutboundHandlers().values()) {
						oh.setMessageStore(_messageStore);
					}
					
					break;
				}
			}
		}
	}

	/**
	 * This method returns the activator.
	 * 
	 * @return The activator
	 */
	protected SOAPActivator getActivator() {
		return (_activator);
	}
	
	/**
	 * This method indicates whether the supplied model is supported.
	 * 
	 * @param model The simulation model information
	 * @return Whether the model is supported by this role simulator
	 */
	public boolean isSupported(SimulationModel model) {
		return(model.getName().equals(SWITCHYARD_DESCRIPTOR));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getModel(SimulationModel model, ResourceLocator locator) {
		Object ret=null;
		
		if (model.getName().endsWith(SWITCHYARD_DESCRIPTOR)) {
			try {
				ret = new java.io.File(model.getName());
				
				if (!((java.io.File)ret).exists()) {
					ret = null;
					
					java.net.URL url=Thread.currentThread().getContextClassLoader().getResource(model.getName());
					
					if (url != null) {
						ret = new java.io.File(url.getFile());
						
						if (!((java.io.File)ret).exists()) {
							ret = null;
						}
					}
				}
				
			} catch(Throwable e) {
				LOG.log(Level.SEVERE, "Failed to load Switchyard descriptor model", e);
			}
		}
		
		return(ret);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Role> getModelRoles(Object model) {
		return(java.util.Collections.emptyList());
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModelForRole(Object model, Role role, ResourceLocator locator) {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onEvent(SimulationContext context, final Event event,
					final SimulationHandler handler) {
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("onEvent "+event);
		}
		
		// Associate handler with message store
		_messageStore.setHandler(handler);
		
		if (event instanceof ReceiveEvent) {
			final ReceiveEvent recv=(ReceiveEvent)event;
						
			incrementEventCounter();
			
			if (_activator.getInboundHandler().isOperation(recv.getOperationName())) {
				LOG.fine("Invoke REQUEST: "+recv);
				
				Thread t=new Thread(new Runnable() {
					public void run() {
						
						try {
							java.io.InputStream is=
									_context.getResource(recv.getParameter().get(0).getValue());
							
							byte[] b=new byte[is.available()];
							is.read(b);
							
							is.close();
							
							handler.processed(recv);
							
							String resp=_activator.getInboundHandler().invoke(
									recv.getOperationName(), new String(b),
									QName.valueOf(recv.getParameter().get(0).getType()));
							
		        			if (resp != null) {
		        				_messageStore.waitForSendEvent(recv.getOperationName(),
		        									resp);
		        			}

						} catch (Throwable t) {
							LOG.log(Level.SEVERE, "Failed to invoke service", t);
						}
						
						decrementEventCounter();						
					}
				});
				
				t.start();
				
			} else {
				LOG.fine("Receive RESPONSE: "+recv);
				
				try {
					_messageStore.handleReceiveEvent(recv);
				} catch(Throwable t) {
	    			handler.error("Failed to handle receive event", event, t);
				}
						
				decrementEventCounter();
			}
			
		} else if (event instanceof SendEvent) {
			SendEvent send=(SendEvent)event;
			
			incrementEventCounter();
			
			// Should block waiting for sent event
			try {
				_messageStore.handleSendEvent(send);
				
			} catch(Throwable t) {
    			handler.error("Failed to handle send event", event, t);
			}
			
			decrementEventCounter();
		}
	}
	
	protected void incrementEventCounter() {
		synchronized(this) {
			_eventCounter++;
			
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Increment event counter: "+_eventCounter);
			}	
		}
	}

	protected void decrementEventCounter() {
		synchronized(this) {
			_eventCounter--;
			
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Decrement event counter: "+_eventCounter);
			}
			
			this.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void close(SimulationContext context) throws Exception {

		// Delay until all events handled
		synchronized(this) {
			while (_eventCounter > 0) {
				wait(5000);
			}
		}
		
		// Close the message store
		if (_messageStore != null) {
			_messageStore.close();
		}
		
		// Close switchyard
		if (_switchyard != null) {
			_switchyard.stop();
		}
	}
}
