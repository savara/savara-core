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
import org.pi4soa.service.monitor.ServiceMonitor;
import org.pi4soa.service.monitor.ServiceMonitorFactory;
import org.pi4soa.service.monitor.XMLMonitorConfiguration;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.Role;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulation.SimulationModel;

import com.sun.xml.internal.ws.util.xml.XmlUtil;

/**
 * This class provides the pi4soa CDM implementation of the Role Simulator interface.
 *
 */
public class CDMRoleSimulator implements RoleSimulator {

	private static final String CDM_FILE_EXTENSION = ".cdm";
	
	private static final Logger logger=Logger.getLogger(CDMRoleSimulator.class.getName());
	
	/**
	 * This method returns the name of the role simulator.
	 * 
	 * @return The name
	 */
	public String getName() {
		return("pi4soa CDM simulator");
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
	public Object getSupportedModel(SimulationModel model) {
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
				String ns=CDLTypeUtil.getNamespace(pt.getName(), pt, true);
				String lp=XmlUtil.getLocalPart(pt.getName());
				
				Role r=new Role();
				r.setName(new QName(ns, lp).toString());
				
				ret.add(r);
			}			
			
			@SuppressWarnings("unchecked")
			java.util.List<Participant> parts=((org.pi4soa.cdl.Package)model).getParticipants();
			
			for (Participant p : parts) {
				String ns=CDLTypeUtil.getNamespace(p.getName(), p, true);
				String lp=XmlUtil.getLocalPart(p.getName());
				
				Role r=new Role();
				r.setName(new QName(ns, lp).toString());
				
				ret.add(r);
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
	public void initialize(SimulationContext context, SimulationHandler handler) throws Exception {
		
		if (context.getModel() instanceof org.pi4soa.cdl.Package) {
			
			XMLMonitorConfiguration config=
				new XMLMonitorConfiguration();
			
			// TODO: Create service tracker proxy to the simulation handler
	
			ServiceMonitor mon=ServiceMonitorFactory.getServiceMonitor(config);

			context.getProperties().put(ServiceMonitor.class.getName(), mon);
		} else {
			throw new Exception("Model is not CDL");
		}
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
		
		if (mon != null) {
			// TODO: Dispatch event to service monitor
			
		} else {
			handler.exception("Service monitor not configured", event, null);
		}
	}

}
