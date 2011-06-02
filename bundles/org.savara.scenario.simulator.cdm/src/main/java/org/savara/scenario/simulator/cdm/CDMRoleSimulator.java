/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.scenario.simulator.cdm;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.pi4soa.cdl.CDLManager;
import org.pi4soa.cdl.Participant;
import org.pi4soa.cdl.ParticipantType;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.pi4soa.common.xml.XMLUtils;
import org.pi4soa.service.Channel;
import org.pi4soa.service.DefaultMessage;
import org.pi4soa.service.Message;
import org.pi4soa.service.ServiceException;
import org.pi4soa.service.behavior.MessageDefinition;
import org.pi4soa.service.behavior.Receive;
import org.pi4soa.service.behavior.Send;
import org.pi4soa.service.behavior.ServiceDescription;
import org.pi4soa.service.behavior.projection.BehaviorProjection;
import org.pi4soa.service.monitor.ServiceMonitor;
import org.pi4soa.service.monitor.ServiceMonitorFactory;
import org.pi4soa.service.monitor.XMLMonitorConfiguration;
import org.pi4soa.service.session.Session;
import org.pi4soa.service.tracker.ServiceTracker;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulation.SimulationModel;

/**
 * This class provides the pi4soa CDM implementation of the Role Simulator interface.
 *
 */
public class CDMRoleSimulator implements RoleSimulator {

	private static final String WS_CDL_SIMULATOR = "WS-CDL simulator";

	private static final String CDM_FILE_EXTENSION = ".cdm";
	
	private static final Logger logger=Logger.getLogger(CDMRoleSimulator.class.getName());
	
	/**
	 * This method returns the name of the role simulator.
	 * 
	 * @return The name
	 */
	public String getName() {
		return WS_CDL_SIMULATOR;
	}

	/**
	 * This method indicates whether the supplied model is supported.
	 * 
	 * @param model The simulation model information
	 * @return Whether the model is supported by this role simulator
	 */
	public boolean isSupported(SimulationModel model) {
		return(model.getName().endsWith(CDM_FILE_EXTENSION));
	}
	
	/**
	 * This method identifies whether the role simulator supports
	 * the model information, and if so, returns the specific model
	 * representation. If the model is not supported, then a null
	 * is returned.
	 * 
	 * @param model The simulation model information
	 * @return The supported model, or null if not handled
	 */
	public Object getModel(SimulationModel model) {
		Object ret=null;
		
		if (model.getName().endsWith(CDM_FILE_EXTENSION)) {
			try {
				ret = CDLManager.load(model.getContents());
			} catch(Exception e) {
				logger.log(Level.SEVERE, "Failed to load CDM model", e);
			}
		}
		
		return(ret);
	}

	protected Role getRole(ParticipantType pt) {
		String ns=CDLTypeUtil.getNamespace(pt.getName(), pt, true);
		String lp=XMLUtils.getLocalname(pt.getName());
		
		Role r=new Role();
		r.setName(new QName(ns, lp).toString());

		return(r);
	}
	
	protected Role getRole(Participant p) {
		String ns=CDLTypeUtil.getNamespace(p.getName(), p, true);
		String lp=XMLUtils.getLocalname(p.getName());
		
		Role r=new Role();
		r.setName(new QName(ns, lp).toString());

		return(r);
	}
	
	/**
	 * This method returns the list of roles associated with the supplied
	 * model, if the model represents a global conversation type. If the
	 * model is a local conversation type, then an empty list will be
	 * returned.
	 * 
	 * @param model The model
	 * @return The list of roles defined in the supplied model
	 */
	public List<Role> getModelRoles(Object model) {
		java.util.List<Role> ret=new java.util.Vector<Role>();
		
		if (model instanceof org.pi4soa.cdl.Package) {
			java.util.List<ParticipantType> partTypes=((org.pi4soa.cdl.Package)model).getTypeDefinitions().getParticipantTypes();			
			for (ParticipantType pt : partTypes) {
				ret.add(getRole(pt));
			}			
			
			@SuppressWarnings("unchecked")
			java.util.List<Participant> parts=((org.pi4soa.cdl.Package)model).getParticipants();
			
			for (Participant p : parts) {
				ret.add(getRole(p));
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method initializes the simulation context.
	 * 
	 * @param context The simulation context
	 * @throws Exception Failed to initialize the context
	 */
	public void initialize(SimulationContext context) throws Exception {
		
		if (context.getModel() instanceof org.pi4soa.service.behavior.ServiceDescription) {
			
			XMLMonitorConfiguration config=
				new XMLMonitorConfiguration();
			
			SimulationHandlerProxy tracker=new SimulationHandlerProxy();
			context.getProperties().put(SimulationHandlerProxy.class.getName(), tracker);
			
			config.setServiceTracker(tracker);
			config.setEvaluateState(true);
	
			ServiceMonitor mon=ServiceMonitorFactory.getServiceMonitor(config);
			
			mon.getConfiguration().getServiceRepository().
					addServiceDescription((org.pi4soa.service.behavior.ServiceDescription)context.getModel());

			context.getProperties().put(ServiceMonitor.class.getName(), mon);
		} else {
			throw new Exception("Model is not CDL");
		}
	}

	/**
	 * This method returns the model, derived from the supplied model,
	 * that should be used for the specified role.
	 * 
	 * @param model The model
	 * @param role The role
	 * @return The simulation model
	 */
	public Object getModelForRole(Object model, Role role) {
		Object ret=null;
		
		if (model instanceof org.pi4soa.cdl.Package) {
			org.pi4soa.cdl.Package cdlpack=(org.pi4soa.cdl.Package)model;
			
			java.util.List<ParticipantType> participants=
				cdlpack.getTypeDefinitions().getParticipantTypes();
			
			java.util.Iterator<ParticipantType> iter=participants.iterator();
			while (ret == null && iter.hasNext()) {
				ParticipantType partType=iter.next();
				
				Role r=getRole(partType);
				
				if (r.getName().equals(role.getName())) {
					try {
						ret = BehaviorProjection.projectServiceDescription(cdlpack,
									partType, null);
					} catch(ServiceException se) {
						logger.severe("Failed to project service " +
								"description '"+partType.getName()+"'");
					}
				}
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method simulates the supplied event within the specified simulation
	 * context. The results are reported to the supplied handler.
	 * 
	 * @param context The context
	 * @param event The event
	 * @param handler The handler
	 */
	public void onEvent(SimulationContext context, Event event,
							SimulationHandler handler) {
		ServiceMonitor mon=(ServiceMonitor)
				context.getProperties().get(ServiceMonitor.class.getName());
		SimulationHandlerProxy proxy=(SimulationHandlerProxy)
				context.getProperties().get(SimulationHandlerProxy.class.getName());

		if (mon != null && proxy != null) {
			
			synchronized(mon) {
				proxy.setEvent(event);
				proxy.setSimulationHandler(handler);
				
				if (event instanceof MessageEvent) {
					MessageEvent me=(MessageEvent)event;
					
					if (me.getParameter().size() == 1) {
						String type=me.getParameter().get(0).getType();
						String path=me.getParameter().get(0).getValue();
						
						java.io.Serializable value=null;
						
						if (path != null) {
							try {
								java.io.InputStream is=context.getResource(path);
								
								byte[] b=new byte[is.available()];
								
								is.read(b);
								
								is.close();
								
								// Assume value is text
								value = new String(b);
								
							} catch(Exception e) {
								handler.error("Failed to obtain message value", event, e);
							}
						}
						
						Message mesg=mon.createMessage(type,
								null, null, value, null, null);
						
						if (mesg instanceof DefaultMessage) {
							// Not nice, but the factory method does not offer ability
							// to set op/fault names on messgae - only req or resp
							((DefaultMessage)mesg).setOperationName(me.getOperationName());
							((DefaultMessage)mesg).setFaultName(me.getFaultName());
						}
						
						try {
							if (event instanceof SendEvent) {
								mon.messageSent(mesg);
							} else {
								mon.messageReceived(mesg);
							}
						} catch(org.pi4soa.service.OutOfSequenceMessageException oosme) {
							// Ignore, as logged as unexpected messages with handler
							
						} catch(Exception e) {
							handler.error("Failed to simulate message", event, e);
						}
					} else {
						handler.error("Cannot simulate event as does not have single parameter",
										event, null);
					}
				}
			}
			
		} else {
			handler.error("Service monitor not configured", event, null);
		}
	}

	/**
	 * This method closes the role simulator.
	 * 
	 * @param context The context
	 * @throws Exception Failed to close the role simulator
	 */
	public void close(SimulationContext context) throws Exception {
	}

	public static class SimulationHandlerProxy implements ServiceTracker {

		private SimulationHandler m_handler=null;
		private Event m_event=null;
		
		public SimulationHandlerProxy() {
		}
		
		public void setSimulationHandler(SimulationHandler handler) {
			m_handler = handler;
		}
		
		public void setEvent(Event event) {
			m_event = event;
		}
		
		public void close() throws ServiceException {
		}

		public void error(Session arg0, String mesg, Throwable e) {
			m_handler.error(mesg, m_event, e);
		}

		public void information(Session arg0, String arg1) {
		}

		public void initialize() throws ServiceException {
		}

		public void receivedMessage(MessageDefinition arg0, Message arg1) {
			if (!m_event.isErrorExpected()) {
				m_handler.processed(m_event);
			} else {
				m_handler.error("Error was expected but did not occur", m_event, null);
			}
		}

		public void receivedMessage(Receive arg0, Session arg1, Channel arg2,
				Message arg3) {
			if (!m_event.isErrorExpected()) {
				m_handler.processed(m_event);
			} else {
				m_handler.error("Error was expected but did not occur", m_event, null);
			}
		}

		public void sentMessage(MessageDefinition arg0, Message arg1) {
			if (!m_event.isErrorExpected()) {
				m_handler.processed(m_event);
			} else {
				m_handler.error("Error was expected but did not occur", m_event, null);
			}
		}

		public void sentMessage(Send arg0, Session arg1, Channel arg2,
				Message arg3) {
			if (!m_event.isErrorExpected()) {
				m_handler.processed(m_event);
			} else {
				m_handler.error("Error was expected but did not occur", m_event, null);
			}
		}

		public void serviceFinished(ServiceDescription arg0, Session arg1) {
		}

		public void serviceStarted(ServiceDescription arg0, Session arg1) {
		}

		public void subSessionFinished(Session arg0, Session arg1) {
		}

		public void subSessionStarted(Session arg0, Session arg1) {
		}

		public void unexpectedMessage(ServiceDescription arg0, Session arg1,
				Message arg2, String arg3) {
			if (!m_event.isErrorExpected()) {
				m_handler.unexpected(m_event);
			} else {
				m_handler.processed(m_event);
			}
		}

		public void unhandledException(Session arg0, String arg1) {
			// TODO: Determine if this should be reported as unexpected or as an error?
			m_handler.unexpected(m_event);
		}

		public void warning(Session arg0, String arg1, Throwable arg2) {
		}
		
	}
}
