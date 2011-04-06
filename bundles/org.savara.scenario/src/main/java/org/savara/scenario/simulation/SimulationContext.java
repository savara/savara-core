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

/**
 * This interface represents the context information used during the simulation
 * of a role against a model.
 *
 */
public interface SimulationContext {

	/**
	 * This method returns the model associated with the context.
	 * 
	 * @return The model
	 */
	public Object getModel();

	/**
	 * This method returns the properties associated with the context.
	 * 
	 * @return The properties
	 */
	public java.util.Map<String, Object> getProperties();
	
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
	public java.io.InputStream getResource(String path) throws IOException;
	
}
