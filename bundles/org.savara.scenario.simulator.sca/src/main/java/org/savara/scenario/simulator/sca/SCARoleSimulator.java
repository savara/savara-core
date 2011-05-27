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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.savara.scenario.model.Event;
import org.savara.scenario.model.Role;
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

	private static final Logger logger=Logger.getLogger(SCARoleSimulator.class.getName());

	public String getName() {
		return SCA_SIMULATOR;
	}

	public void initialize(SimulationContext context) throws Exception {
		System.out.println("CONTEXT="+context);
		System.out.println("MODEL="+context.getModel());
	}

	public Object getSupportedModel(SimulationModel model) {
		Object ret=null;
		
		if (model.getName().endsWith(SCA_COMPOSITE_FILE_EXTENSION)) {
			try {
				DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
				fact.setNamespaceAware(true);
				
				java.io.InputStream is=model.getContents();

				DocumentBuilder builder=fact.newDocumentBuilder();
				org.w3c.dom.Document doc=builder.parse(is);
				
				is.close();
				
				ret = doc.getDocumentElement();
			} catch(Exception e) {
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

	public void onEvent(SimulationContext context, Event event,
			SimulationHandler handler) {
		System.out.println("EVENT="+event);
	}

}
