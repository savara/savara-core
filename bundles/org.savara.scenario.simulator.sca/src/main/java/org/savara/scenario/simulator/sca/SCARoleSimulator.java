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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulation.SimulationModel;

/**
 * The SCA based implementation of the RoleSimulator.
 *
 */
public class SCARoleSimulator implements RoleSimulator {

	private static final String SCA_SIMULATOR = "SCA simulator";
	private static final String SCA_COMPOSITE_FILE_EXTENSION = ".composite";
	
	private int m_eventCounter=0;
	
	private static final Logger logger=Logger.getLogger(SCARoleSimulator.class.getName());

	public String getName() {
		return SCA_SIMULATOR;
	}

	public void initialize(SimulationContext context) throws Exception {
	}

	/**
	 * This method indicates whether the supplied model is supported.
	 * 
	 * @param model The simulation model information
	 * @return Whether the model is supported by this role simulator
	 */
	public boolean isSupported(SimulationModel model) {
		return(model.getName().endsWith(SCA_COMPOSITE_FILE_EXTENSION));
	}
	
	public Object getModel(SimulationModel model) {
		Object ret=null;
		
		if (model.getName().endsWith(SCA_COMPOSITE_FILE_EXTENSION)) {
			try {
				NodeFactory nf=NodeFactory.newInstance();
				
				Node n=nf.createNode(model.getName());
				
				ret = n.start();

			} catch(Throwable e) {
				logger.log(Level.SEVERE, "Failed to load SCA composite model", e);
			}
		}
		
		return(ret);
	}

	public List<Role> getModelRoles(Object model) {
		return(java.util.Collections.emptyList());
	}

	public Object getModelForRole(Object model, Role role) {
		return null;
	}

	public void onEvent(SimulationContext context, final Event event,
					final SimulationHandler handler) {
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("onEvent "+event);
		}
		
		if (event instanceof ReceiveEvent) {
			ReceiveEvent recv=(ReceiveEvent)event;
			boolean handled=false;
			
			incrementEventCounter();
			
			// Should dispatch a received event immediately if service request.
			// If response, then should cache awaiting the reference service invoke.
			
			java.util.Collection<ServiceInvoker> invokers=
					ServiceStore.getServices();
	
			for (final ServiceInvoker invoker : invokers) {
				String opName=recv.getOperationName();
				
		        Operation operation = null;
		        for (InvocationChain invocationChain : invoker.getEndpoint().getInvocationChains()) {
		            if (opName.equals(invocationChain.getSourceOperation().getName())) {
		                operation = invocationChain.getSourceOperation();
		                break;
		            }
		        }
				
		        if (operation != null) {
					final org.apache.tuscany.sca.core.invocation.impl.MessageImpl msg=
							new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();
					
			        msg.setOperation(operation);
			        msg.setBody(getRequestBody(recv.getParameter()));

			        new Thread(new Runnable() {
			        	public void run() {					        
					        // TODO: Handle oneway request
		        			
		        			handler.processed(event);

		        			try {
			        			Message resp=invoker.invoke(msg);
							
			        			if (resp != null) {
			        				MessageStore.waitForSendEvent(resp);
			        			}
			        		} catch(Throwable t){
			        			handler.error("Failed to handle receive event", event, t);
			        		}
		        			
		        			decrementEventCounter();
			        	}
			        }).start();
			        
			        handled = true;
			        
			        break;
		        }
			}
			
			if (handled == false) {
				
				java.util.Collection<ReferenceInvoker> refInvokers=
						ServiceStore.getReferences();
		
				for (final ReferenceInvoker refInvoker : refInvokers) {
					String opName=recv.getOperationName();
					
			        Operation operation = null;
			        for (Operation op : refInvoker.getEndpointReference().getComponentReferenceInterfaceContract().getInterface().getOperations()) {
			            if (opName.equals(op.getName())) {
			                operation = op;
			                break;
			            }
			        }
					
			        if (operation != null) {
				
						// Assume is receiving a response
						try {
							MessageStore.handleReceiveEvent(recv, handler);
						} catch(Throwable t) {
			    			handler.error("Failed to handle receive event", event, t);
						}
				
						decrementEventCounter();
						
						handled = true;
						break;
			        }
				}
				
				if (handled == false) {
					handler.unexpected(event);
					
					decrementEventCounter();
				}
			}
		} else if (event instanceof SendEvent) {
			SendEvent send=(SendEvent)event;
			
			incrementEventCounter();
			
			// Should block waiting for sent event
			try {
				MessageStore.handleSendEvent(send, handler);
				
			} catch(Throwable t) {
    			handler.error("Failed to handle send event", event, t);
			}
			
			decrementEventCounter();
		}
	}
	
	protected void incrementEventCounter() {
		synchronized(this) {
			m_eventCounter++;
			
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Increment event counter: "+m_eventCounter);
			}	
		}
	}

	protected void decrementEventCounter() {
		synchronized(this) {
			m_eventCounter--;
			
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Decrement event counter: "+m_eventCounter);
			}	
			
			this.notifyAll();
		}
	}

	protected Object[] getRequestBody(java.util.List<Parameter> parameters) {
		Object[] ret=new Object[parameters.size()];
		
		for (int i=0; i < parameters.size(); i++) {
			ret[i] = parameters.get(i).getValue();
		}
		
		return(ret);
	}
	
	public void close(SimulationContext context) throws Exception {
		
		// Delay until all events handled
		synchronized(this) {
			while (m_eventCounter > 0) {
				wait(5000);
			}
		}
		
		if (context.getModel() instanceof Node) {
			((Node)context.getModel()).stop();
		}
	}
}
