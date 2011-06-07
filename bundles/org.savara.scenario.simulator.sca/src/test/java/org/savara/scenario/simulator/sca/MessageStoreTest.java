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

import static org.junit.Assert.*;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.invocation.Message;
import org.junit.Test;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.DefaultSimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulator.sca.internal.MessageStore;

public class MessageStoreTest {

	@Test
	public void testHandleSendEvent1() {
		final SendEvent send=new SendEvent();
		send.setOperationName("op");
		
		Parameter p1=new Parameter();
		p1.setValue("req.data");
		
		send.getParameter().add(p1);
		
		try {
			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);

			final MessageStore mstore=new MessageStore();
			mstore.setSimulationContext(context);
			
			final TestSimulationHandler handler=new TestSimulationHandler();
			
			new Thread(new Runnable() {
				public void run() {
					try {
						synchronized(send) {
							send.wait(500);
						}
						mstore.handleSendEvent(send, handler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						fail("Failed: "+e);
					}					
				}
			}).start();
			
			Operation op=new OperationImpl();
			op.setName("op");
			
			org.apache.tuscany.sca.core.invocation.impl.MessageImpl mesg=
					new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();

			mesg.setOperation(op);
			mesg.setBody("hello");
			
			mstore.waitForSendEvent(mesg);
			
			if (handler.getProcessedEvents().size() != 1) {
				fail("Send event failed");
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed: "+e);
		}
	}
	
	@Test
	public void testHandleSendEvent2() {
		final SendEvent send=new SendEvent();
		send.setOperationName("op");
		
		Parameter p1=new Parameter();
		p1.setValue("req.data");
		
		send.getParameter().add(p1);
		
		try {
			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);

			final MessageStore mstore=new MessageStore();
			mstore.setSimulationContext(context);
			
			final TestSimulationHandler handler=new TestSimulationHandler();
			
			new Thread(new Runnable() {
				public void run() {
					try {
						mstore.handleSendEvent(send, handler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						fail("Failed: "+e);
					}					
				}
			}).start();
			
			Operation op=new OperationImpl();
			op.setName("op");
			
			org.apache.tuscany.sca.core.invocation.impl.MessageImpl mesg=
					new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();

			mesg.setOperation(op);
			mesg.setBody("hello");
			
			synchronized(send) {
				send.wait(500);
			}

			mstore.waitForSendEvent(mesg);
			
			if (handler.getProcessedEvents().size() != 1) {
				fail("Send event failed");
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed: "+e);
		}
	}
	
	@Test
	public void testHandleReceiveEvent1() {
		ReceiveEvent receive=new ReceiveEvent();
		receive.setOperationName("op");
		
		Parameter p1=new Parameter();
		p1.setValue("hello");
		
		receive.getParameter().add(p1);
		
		try {
			MessageStore mstore=new MessageStore();
			
			SimulationHandler handler=new TestSimulationHandler();
			
			mstore.handleReceiveEvent(receive, handler);
			
			Operation op=new OperationImpl();
			op.setName("op");
			
			Message mesg=mstore.waitForReceiveEvent(op);
			
			if (mesg == null) {
				fail("Null message");
			} else if (mesg.getOperation().getName().equals("op") == false) {
				fail("Message not correct operation");
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed: "+e);
		}
	}
	
	@Test
	public void testHandleReceiveEvent2() {
		final ReceiveEvent receive=new ReceiveEvent();
		receive.setOperationName("op");
		
		Parameter p1=new Parameter();
		p1.setValue("hello");
		
		receive.getParameter().add(p1);
		
		try {
			final MessageStore mstore=new MessageStore();
			
			new Thread(new Runnable() {
				public void run() {
					Operation op=new OperationImpl();
					op.setName("op");
					
					try {
						Message mesg=mstore.waitForReceiveEvent(op);
						
						if (mesg == null) {
							fail("Null message");
						} else if (mesg.getOperation().getName().equals("op") == false) {
							fail("Message not correct operation");
						}
					} catch(Exception e) {
						e.printStackTrace();
						fail("Failed: "+e);
					}
				}
			}).start();
			
			synchronized(receive) {
				// Wait until receive event has been registered
				receive.wait(100);
			}

			SimulationHandler handler=new TestSimulationHandler();
			
			mstore.handleReceiveEvent(receive, handler);
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed: "+e);
		}
	}
}
