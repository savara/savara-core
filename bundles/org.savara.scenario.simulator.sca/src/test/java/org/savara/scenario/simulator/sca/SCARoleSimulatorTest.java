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

import org.apache.tuscany.sca.node.Node;
import org.junit.Test;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.DefaultSimulationContext;
import org.savara.scenario.simulation.SimulationModel;
import org.savara.scenario.simulator.sca.SCARoleSimulator;

public class SCARoleSimulatorTest {

	@Test
	public void testGetSupportedModel() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			} else if ((model instanceof Node) == false) {
				fail("Model is not a Tuscany SCA node");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testGetModelRoles() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}
			
			java.util.List<Role> roles=sim.getModelRoles(model);
			
			if (roles.size() > 0) {
				fail("Should be no roles");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testSimSampleOnEventAllProcessed() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param=new Parameter();
			param.setValue("req.data");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("call");
			event1.getParameter().add(param);
			
			sim.onEvent(context, event1, handler);

			SendEvent event2=new SendEvent();
			event2.setOperationName("callOut");
			event2.getParameter().add(param);
			
			sim.onEvent(context, event2, handler);

			Parameter resp=new Parameter();
			resp.setValue("resp.data");

			ReceiveEvent event3=new ReceiveEvent();
			event3.setOperationName("callOut");
			event3.getParameter().add(resp);
			
			sim.onEvent(context, event3, handler);

			SendEvent event4=new SendEvent();
			event4.setOperationName("call");
			event4.getParameter().add(resp);
			
			sim.onEvent(context, event4, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() > 0) {
				fail("Should be no errors");
			}
			
			if (handler.getUnexpectedEvents().size() > 0) {
				fail("Should be no unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() != 4) {
				fail("Should be 4 processed events");
			}

		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}
	
	@Test
	public void testSimSampleOnEventResponseValueUnexpected() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param=new Parameter();
			param.setValue("req.data");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("call");
			event1.getParameter().add(param);
			
			sim.onEvent(context, event1, handler);

			SendEvent event2=new SendEvent();
			event2.setOperationName("callOut");
			event2.getParameter().add(param);
			
			sim.onEvent(context, event2, handler);

			Parameter resp=new Parameter();
			resp.setValue("resp.data");

			ReceiveEvent event3=new ReceiveEvent();
			event3.setOperationName("callOut");
			event3.getParameter().add(resp);
			
			sim.onEvent(context, event3, handler);

			// Create an invalid response
			resp=new Parameter();
			resp.setValue("resp2.data");

			SendEvent event4=new SendEvent();
			event4.setOperationName("call");
			event4.getParameter().add(resp);
			
			sim.onEvent(context, event4, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() > 0) {
				fail("Should be no errors");
			}
			
			if (handler.getUnexpectedEvents().size() != 1) {
				fail("Should be 1 unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() != 3) {
				fail("Should be 3 processed events");
			}

		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}
	
	@Test
	public void testSimSampleOnEventRequestOperationUnexpected() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param=new Parameter();
			param.setValue("req.data");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("callX");
			event1.getParameter().add(param);
			
			sim.onEvent(context, event1, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() > 0) {
				fail("Should be no errors");
			}
			
			if (handler.getUnexpectedEvents().size() != 1) {
				fail("Should be 1 unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() > 0) {
				fail("Should be 0 processed events");
			}

		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}
	
	@Test
	public void testSimSampleOnEventExternalRequestOperationUnexpected() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("simsample.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param=new Parameter();
			param.setValue("req.data");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("call");
			event1.getParameter().add(param);
			
			sim.onEvent(context, event1, handler);

			SendEvent event2=new SendEvent();
			event2.setOperationName("callOutX");
			event2.getParameter().add(param);
			
			sim.onEvent(context, event2, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() != 1) {
				fail("Should be 1 errors");
			}
			
			if (handler.getUnexpectedEvents().size() != 1) {
				fail("Should be 1 unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() != 1) {
				fail("Should be 1 processed events");
			}

		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}
	
	@Test
	public void testExternalServiceOnEventAllProcessed() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("externalservice.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param=new Parameter();
			param.setValue("req.data");

			ReceiveEvent event2=new ReceiveEvent();
			event2.setOperationName("callOut");
			event2.getParameter().add(param);
			
			sim.onEvent(context, event2, handler);

			Parameter resp=new Parameter();
			resp.setValue("resp.data");

			SendEvent event3=new SendEvent();
			event3.setOperationName("callOut");
			event3.getParameter().add(resp);
			
			sim.onEvent(context, event3, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() > 0) {
				fail("Should be no errors");
			}
			
			if (handler.getUnexpectedEvents().size() > 0) {
				fail("Should be no unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() != 2) {
				fail("Should be 2 processed events");
			}

		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testStoreServiceOnEventAllProcessed() {
		SCARoleSimulator sim=new SCARoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("store/Store.composite",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("store/BuyRequest.xml");
			
			java.io.File f=new java.io.File(url.getFile());
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param=new Parameter();
			param.setValue("BuyRequest.xml");

			ReceiveEvent event2=new ReceiveEvent();
			event2.setOperationName("buy");
			event2.getParameter().add(param);
			
			sim.onEvent(context, event2, handler);

			Parameter resp=new Parameter();
			resp.setValue("BuyConfirmed.xml");

			SendEvent event3=new SendEvent();
			event3.setOperationName("buy");
			event3.getParameter().add(resp);
			
			sim.onEvent(context, event3, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() > 0) {
				fail("Should be no errors");
			}
			
			if (handler.getUnexpectedEvents().size() > 0) {
				fail("Should be no unexpected events");
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() != 2) {
				fail("Should be 2 processed events");
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception occurred: "+e);
		}
	}
}
