/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.scenario.model.Parameter;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.DefaultSimulationContext;
import org.savara.scenario.simulation.SimulationModel;

public class SwitchyardRoleSimulatorTest {

	@Test
	public void testGetSupportedModelRelativePath() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			SimulationModel simmodel=new SimulationModel("descriptors/java/store/switchyard.xml",null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			} else if (!(model instanceof java.io.File)) {
				fail("Model not a file");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testGetSupportedModelAbsolutePath() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			java.net.URL url=ClassLoader.getSystemResource("descriptors/java/store/switchyard.xml");
			
			SimulationModel simmodel=new SimulationModel(url.getFile(),null);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			} else if ((model instanceof java.io.File) == false) {
				fail("Model is not a file");
			}
		} catch(Exception e) {
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testGetModelRoles() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream("descriptors/java/store/switchyard.xml");
			
			SimulationModel simmodel=new SimulationModel("descriptors/java/store/switchyard.xml",is);
			
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
	public void testSimSampleHandlers() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream("descriptors/java/store/switchyard.xml");
			
			SimulationModel simmodel=new SimulationModel("descriptors/java/store/switchyard.xml",is);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			//java.net.URL url=ClassLoader.getSystemResource("req.data");
			
			java.io.File f=new java.io.File("scenario");
			
			DefaultSimulationContext context=new DefaultSimulationContext(f);
			context.setModel(model);
			sim.initialize(context);
			
			if (sim.getActivator() == null) {
				fail("Activator not defined");
			}
			
			if (sim.getActivator().getInboundHandler() == null) {
				fail("Inbound handler not defined");
			}
			
			if (sim.getActivator().getOutboundHandler("Logistics") == null) {
				fail("Logistics outbound handler not defined");
			}
			
			if (sim.getActivator().getOutboundHandler("CreditAgency") == null) {
				fail("CreditAgency outbound handler not defined");
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testPurchaseSuccessful() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream("descriptors/java/store/switchyard.xml");
			
			SimulationModel simmodel=new SimulationModel("descriptors/java/store/switchyard.xml",is);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("placeholder.txt");
			
			DefaultSimulationContext context=
					new DefaultSimulationContext(new java.io.File(url.getFile()));
			
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param1=new Parameter();
			param1.setValue("messages/purchasing/BuyRequest.xml");
			param1.setType("{http://www.jboss.org/examples/store}BuyRequest");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("buy");
			event1.getParameter().add(param1);
			
			sim.onEvent(context, event1, handler);

			Parameter param2=new Parameter();
			param2.setValue("messages/purchasing/CreditCheckRequest.xml");
			param2.setType("{http://www.jboss.org/examples/creditAgency}CreditCheckRequest");

			SendEvent event2=new SendEvent();
			event2.setOperationName("creditCheck");
			event2.getParameter().add(param2);
			
			sim.onEvent(context, event2, handler);

			Parameter param3=new Parameter();
			param3.setValue("messages/purchasing/CreditRating1.xml");
			param3.setType("{http://www.jboss.org/examples/creditAgency}CreditRating");

			ReceiveEvent event3=new ReceiveEvent();
			event3.setOperationName("creditCheck");
			event3.getParameter().add(param3);
			
			sim.onEvent(context, event3, handler);

			Parameter param4=new Parameter();
			param4.setValue("messages/purchasing/DeliveryRequest.xml");
			param4.setType("{http://www.jboss.org/examples/logistics}DeliveryRequest");

			SendEvent event4=new SendEvent();
			event4.setOperationName("delivery");
			event4.getParameter().add(param4);
			
			sim.onEvent(context, event4, handler);

			Parameter param5=new Parameter();
			param5.setValue("messages/purchasing/DeliveryConfirmed.xml");
			param5.setType("{http://www.jboss.org/examples/logistics}DeliveryConfirmed");

			ReceiveEvent event5=new ReceiveEvent();
			event5.setOperationName("delivery");
			event5.getParameter().add(param5);
			
			sim.onEvent(context, event5, handler);

			Parameter param6=new Parameter();
			param6.setValue("messages/purchasing/BuyConfirmed.xml");
			param6.setType("{http://www.jboss.org/examples/store}BuyConfirmed");

			SendEvent event6=new SendEvent();
			event6.setOperationName("buy");
			event6.getParameter().add(param6);
			
			sim.onEvent(context, event6, handler);

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

			if (handler.getProcessedEvents().size() != 6) {
				fail("Should be 6 processed events: "+handler.getProcessedEvents().size());
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception occurred: "+e);
		}
	}

	@Test
	public void testPurchaseInvalidSendMessageTypeBehaviour() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream("descriptors/java/store/switchyard.xml");
			
			SimulationModel simmodel=new SimulationModel("descriptors/java/store/switchyard.xml",is);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("placeholder.txt");
			
			DefaultSimulationContext context=
					new DefaultSimulationContext(new java.io.File(url.getFile()));
			
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param1=new Parameter();
			param1.setValue("messages/purchasing/BuyRequest.xml");
			param1.setType("{http://www.jboss.org/examples/store}BuyRequest");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("buy");
			event1.getParameter().add(param1);
			
			sim.onEvent(context, event1, handler);

			Parameter param4=new Parameter();
			param4.setValue("messages/purchasing/DeliveryRequest.xml");
			param4.setType("{http://www.jboss.org/examples/logistics}DeliveryRequest");

			SendEvent event4=new SendEvent();
			event4.setOperationName("delivery");
			event4.getParameter().add(param4);
			
			sim.onEvent(context, event4, handler);

			Parameter param5=new Parameter();
			param5.setValue("messages/purchasing/DeliveryConfirmed.xml");
			param5.setType("{http://www.jboss.org/examples/logistics}DeliveryConfirmed");

			ReceiveEvent event5=new ReceiveEvent();
			event5.setOperationName("delivery");
			event5.getParameter().add(param5);
			
			sim.onEvent(context, event5, handler);

			Parameter param6=new Parameter();
			param6.setValue("messages/purchasing/BuyConfirmed.xml");
			param6.setType("{http://www.jboss.org/examples/store}BuyConfirmed");

			SendEvent event6=new SendEvent();
			event6.setOperationName("buy");
			event6.getParameter().add(param6);
			
			sim.onEvent(context, event6, handler);

			sim.close(context);
			
			if (handler.getErrorEvents().size() > 0) {
				fail("Should be no errors");
			}
			
			if (handler.getUnexpectedEvents().size() != 3) {
				fail("Should be 3 unexpected events: "+handler.getUnexpectedEvents().size());
			}
			
			if (handler.getNoSimulatorEvents().size() > 0) {
				fail("Should be no 'no simulator' events");
			}

			if (handler.getProcessedEvents().size() != 1) {
				fail("Should be 1 processed events: "+handler.getProcessedEvents().size());
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception occurred: "+e);
		}
	}

	//@Test
	public void testCustomerUnknown() {
		SwitchyardRoleSimulator sim=new SwitchyardRoleSimulator();
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream("descriptors/java/store/switchyard.xml");
			
			SimulationModel simmodel=new SimulationModel("descriptors/java/store/switchyard.xml",is);
			
			Object model=sim.getModel(simmodel, null);
			
			if (model == null) {
				fail("Model is null");
			}

			java.net.URL url=ClassLoader.getSystemResource("placeholder.txt");
			
			DefaultSimulationContext context=
					new DefaultSimulationContext(new java.io.File(url.getFile()));
			
			context.setModel(model);
			sim.initialize(context);
			
			TestSimulationHandler handler=new TestSimulationHandler();
			
			Parameter param1=new Parameter();
			param1.setValue("messages/purchasing/BuyRequest.xml");
			param1.setType("{http://www.jboss.org/examples/store}BuyRequest");

			ReceiveEvent event1=new ReceiveEvent();
			event1.setOperationName("buy");
			event1.getParameter().add(param1);
			
			sim.onEvent(context, event1, handler);

			Parameter param2=new Parameter();
			param2.setValue("messages/purchasing/CreditCheckRequest2.xml");
			param2.setType("{http://www.jboss.org/examples/creditAgency}CreditCheckRequest");

			SendEvent event2=new SendEvent();
			event2.setOperationName("creditCheck");
			event2.getParameter().add(param2);
			
			sim.onEvent(context, event2, handler);

			Parameter param3=new Parameter();
			param3.setValue("messages/purchasing/CustomerUnknown.xml");
			param3.setType("{http://www.jboss.org/examples/creditAgency}CustomerUnknown");

			ReceiveEvent event3=new ReceiveEvent();
			event3.setOperationName("creditCheck");
			event3.setFaultName("CustomerUnknown");
			event3.getParameter().add(param3);
			
			sim.onEvent(context, event3, handler);

			Parameter param4=new Parameter();
			param4.setValue("messages/purchasing/AccountNotFound.xml");
			param4.setType("{http://www.jboss.org/examples/store}AccountNotFound");

			SendEvent event4=new SendEvent();
			event4.setOperationName("buy");
			event4.setFaultName("AccountNotFound");
			event4.getParameter().add(param4);
			
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
				fail("Should be 4 processed events: "+handler.getProcessedEvents().size());
			}

		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception occurred: "+e);
		}
	}

}
