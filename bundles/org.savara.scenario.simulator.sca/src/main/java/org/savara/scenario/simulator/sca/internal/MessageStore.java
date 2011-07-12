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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.impl.TransformationContextImpl;
import org.apache.tuscany.sca.databinding.jaxb.JAXB2Node;
import org.apache.tuscany.sca.databinding.jaxb.String2JAXB;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulator.sca.internal.binding.ws.runtime.WSBindingProviderFactory;
import org.savara.scenario.util.MessageUtil;

public class MessageStore {

	private static final Logger logger=Logger.getLogger(MessageStore.class.getName());
	
	private java.util.List<ReceiveEvent> m_receiveEvents=new java.util.Vector<ReceiveEvent>();
	private java.util.concurrent.SynchronousQueue<SendEvent> m_sendEvents=
			new java.util.concurrent.SynchronousQueue<SendEvent>();
	//private java.util.concurrent.SynchronousQueue<SendEvent> m_sentEvents=
	//		new java.util.concurrent.SynchronousQueue<SendEvent>();
	private SimulationHandler m_handler=null;
	private SimulationContext m_context=null;

	public MessageStore() {
	}
	
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
		
		synchronized(send) {
			send.notifyAll();
		}
		
		//m_sentEvents.offer(send, 5000, TimeUnit.MILLISECONDS);
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
						@SuppressWarnings("rawtypes")
						DataType<List<DataType>> dtypes=mesg.getOperation().getInputType();
						ret = isValidParameter(event.getParameter().get(i), transformJAXBToNodeValue(params[i],
										mesg.getOperation(), dtypes.getLogical().get(i)));
					}
				}
			} else if (event.getParameter().size() == 1) {
				DataType<?> dtype=null;
				Object content=mesg.getBody();
				
				if (mesg.isFault()) {
					@SuppressWarnings("rawtypes")
					List<DataType> dtypes=mesg.getOperation().getFaultTypes();
					
					for (DataType<?> dt : dtypes) {
						
						if (logger.isLoggable(Level.FINEST)) {
							logger.finest("Checking fault body '"+mesg.getBody().getClass()+
									"' against data type '"+dt.getPhysical()+"'");
						}
						
						if (mesg.getBody().getClass() == dt.getPhysical()) {
							dtype = dt;
							break;
						}
					}
					
					if (dtype.getDataBinding() == null && dtype.getLogical() instanceof DataType<?>) {
						dtype = (DataType<?>)dtype.getLogical();
						
						// Extract the jaxb based object from the exception/fault
						try {
							for (int i=0; i < content.getClass().getMethods().length; i++) {
								java.lang.reflect.Method method = content.getClass().getMethods()[i];
								
								if (method.getReturnType().getName().equals(dtype.getPhysical().getName()) &&
										method.getParameterTypes().length == 0) {
									content = method.invoke(content);
								}
							}
						} catch(Exception e) {
							logger.log(Level.SEVERE, "Failed to extract fault content from exception", e);
						}
					}
					
					if (dtype == null) {
						logger.severe("Cannot find data type for fault body '"+mesg.getBody().getClass()+"'");
					} else if (content == null) {
						logger.severe("Cannot find content for fault body '"+mesg.getBody()+"'");
					}
				} else {
					@SuppressWarnings("rawtypes")
					List<DataType> dtypes=mesg.getOperation().getOutputType().getLogical();
					
					if (dtypes.size() > 0) {
						dtype = dtypes.get(0);
					}
				}

				ret = isValidParameter(event.getParameter().get(0),
						transformJAXBToNodeValue(content, mesg.getOperation(), dtype));
			}
		}
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Is message "+mesg+" valid against event "+event+"? = "+ret);
		}
		
		return(ret);
	}
	
	public static Object transformJAXBToNodeValue(Object source, Operation op, DataType<?> dtype) {
		Object ret=source;
		
		if (dtype != null && (source instanceof String) == false &&
					(source instanceof org.w3c.dom.Node) == false) {
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Transform "+source+" of type "+dtype);
			}
			
			JAXB2Node transformer=new JAXB2Node(WSBindingProviderFactory.getRegistry());
			
			TransformationContext context=new TransformationContextImpl();
			context.setSourceDataType(dtype);
			context.setSourceOperation(op);
			
			ret = transformer.transform(source, context);
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Transformed into "+ret);
			}
		}
		
		return(ret);
	}
	
	public static Object transformRequestStringToJAXBValue(Object source, Operation op, DataType<?> dtype) {
		Object ret=source;
		
		if (source instanceof String && dtype.getPhysical() != String.class) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Transform "+source+" of type "+dtype);
			}
			
			String2JAXB transformer=new String2JAXB(WSBindingProviderFactory.getRegistry());
			
			TransformationContext context=new TransformationContextImpl();
			context.setTargetDataType(dtype);
			context.setTargetOperation(op);
			
			ret = transformer.transform((String)source, context);
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Transformed into "+ret);
			}
		}
		
		return(ret);
	}

	public static Object transformResponseStringToJAXBValue(Object source, Operation op, DataType<?> dtype) {
		Object ret=source;
		
		if (dtype != null && source instanceof String && dtype.getPhysical() != String.class) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Transform "+source+" of type "+dtype);
			}
			
			String2JAXB transformer=new String2JAXB(WSBindingProviderFactory.getRegistry());
			
			TransformationContext context=new TransformationContextImpl();
			context.setTargetDataType(dtype);
			context.setTargetOperation(op);
			
			ret = transformer.transform((String)source, context);
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Transformed into "+ret);
			}
		}
		
		return(ret);
	}

	protected boolean isValidParameter(Parameter param, Object value) {
		boolean ret=false;
		
		String paramValue=getValue(param.getValue());
		
		if (paramValue != null && MessageUtil.isValid(paramValue, value)) {
			ret = true;
		}
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Is valid parameter '"+param.getValue()+":"+paramValue+"' = '"+value+"'? "+ret);
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

		synchronized(send) {
			m_sendEvents.offer(send, 5000, TimeUnit.MILLISECONDS);
			
			send.wait(20000);
		} 

		// Wait for event to be processed
		//m_sentEvents.take();
	}
	
	public void handleReceiveEvent(ReceiveEvent receive, SimulationHandler handler) throws Exception {
		// TODO: Need better way to tie receive event with a particular simulation handler
		// in case multiple simulations done?
		m_handler = handler;
		
		synchronized(receive) {
			m_receiveEvents.add(receive);
			
			synchronized(m_receiveEvents) {
				m_receiveEvents.notifyAll();
			}
			
			receive.wait(20000);
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
						
						DataType<?> dtype=null;
						DataType<?> excDType=null;
						
						if (receive.getFaultName() != null && receive.getFaultName().trim().length() > 0) {
							
							// Identify data type associated with the fault name
							excDType = getDataTypeForFaultName(operation, receive.getFaultName());

							dtype = (DataType<?>)excDType.getLogical();

						} else if (operation.getOutputType().getLogical().size() > 0) {
							dtype = operation.getOutputType().getLogical().get(0);
						}
						
						// TODO: Check if multiple parameters and report error?
						Object value=transformResponseStringToJAXBValue(getValue(receive.getParameter().get(0).getValue()),
								operation, dtype);
						
						// Remove handled receive event
						m_receiveEvents.remove(receive);
						
						// Mark received messages as processed, as they are supplied
						// by the scenario, not generated by the service under test.
						// Will only report unexpected if the service cannot accept
						// the message.
						m_handler.processed(receive);
						
						synchronized(receive) {
							receive.notifyAll();
						}
						
						// Check if exception needs to be generated
						if (excDType != null) {
							java.lang.reflect.Constructor<?> con=
									excDType.getPhysical().getConstructor(String.class,
												dtype.getPhysical());
							
							resp.setFaultBody(con.newInstance(receive.getFaultName(), value));
							
							ret = resp;
							
						} else {
							resp.setBody(value);
							
							ret = resp;
						}
						
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
	
	public static DataType<?> getDataTypeForFaultName(Operation operation, String faultName) {
		DataType<?> ret=null;
		
		for (DataType<?> faultDataType : operation.getFaultTypes()) {
			javax.xml.ws.WebFault wf=faultDataType.getPhysical().getAnnotation(javax.xml.ws.WebFault.class);
			
			if (wf != null && wf.name() != null && wf.name().equals(faultName)) {
				ret = faultDataType;
				break;
			}
		}
		
		return(ret);
	}
}
