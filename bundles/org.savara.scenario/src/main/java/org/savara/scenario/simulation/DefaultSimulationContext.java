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
package org.savara.scenario.simulation;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class represents the context information used during the simulation
 * of a role against a model.
 *
 */
public class DefaultSimulationContext implements SimulationContext {

	private java.io.File m_scenarioFile=null;
	private Object m_model=null;
	private java.util.Map<String,Object> m_properties=new java.util.HashMap<String,Object>();
	
	private static final Logger logger=Logger.getLogger(DefaultSimulationContext.class.getName());
	
	/**
	 * This constructor initializes the simulation context with the scenario
	 * file.
	 * 
	 * @param scenarioFile The scenario file
	 */
	public DefaultSimulationContext(java.io.File scenarioFile) {
		m_scenarioFile = scenarioFile;
	}
	
	/**
	 * This method sets the model associated with the context.
	 * 
	 * @param model The model
	 */
	public void setModel(Object model) {
		m_model = model;
	}
	
	/**
	 * This method returns the model associated with the context.
	 * 
	 * @return The model
	 */
	public Object getModel() {
		return(m_model);
	}
	
	/**
	 * This method returns the properties associated with the context.
	 * 
	 * @return The properties
	 */
	public java.util.Map<String, Object> getProperties() {
		return(m_properties);
	}
	
	/**
	 * This method returns an input stream associated with the
	 * resource identified by the path. If the path is a relative
	 * file path, then it will be considered relative to the
	 * scenario being simulated.
	 * 
	 * @param path The resource path
	 * @return The input stream
	 * @throws IOException Failed to get resource
	 */
	public java.io.InputStream getResource(String path) throws IOException {
		java.io.InputStream ret=null;
		
		if (path.startsWith("http:") || path.startsWith("https:")) {
			java.net.URL url=new java.net.URL(path);
			
			ret = url.openStream();
		} else {
			String baseFolder = m_scenarioFile.getParent();
			
			path = baseFolder+java.io.File.separatorChar+path;
			
			if (logger.isLoggable(java.util.logging.Level.FINE)) {
				logger.fine("Loading value from '"+path+"'");
			}
			
			ret = new java.io.FileInputStream(path);
		}

		return(ret);
	}

}
