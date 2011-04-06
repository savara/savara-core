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

/**
 * This class represents the simulation model information.
 *
 */
public class SimulationModel {

	private String m_path=null;
	private byte[] m_contents=null;
	
	/**
	 * This constructor initializes the simulation model information.
	 * 
	 * @param path The path
	 * @param is The contents
	 * @throws Exception Failed to initialize the simulation model info
	 */
	public SimulationModel(String path, java.io.InputStream is) throws Exception {
		m_path = path;
		
		if (is != null) {
			m_contents = new byte[is.available()];
			is.read(m_contents);
		}
	}
	
	/**
	 * This method returns the name of the model.
	 * 
	 * @return The model
	 */
	public String getName() {
		return(m_path);
	}
	
	/**
	 * This method provides the contents as an input stream.
	 * 
	 * @return The contents, or null if not available
	 */
	public java.io.InputStream getContents() {
		java.io.InputStream ret=null;
		
		if (m_contents != null) {
			ret = new java.io.ByteArrayInputStream(m_contents);
		}
		
		return(ret);
	}
}
