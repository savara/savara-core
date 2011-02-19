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
package org.savara.common.config;

/**
 * This class provides the default implementation of the
 * Configuration interface.
 *
 */
public class DefaultConfiguration implements Configuration {

	private java.util.Properties m_properties=new java.util.Properties();
	
	/**
	 * The default constructor.
	 */
	public DefaultConfiguration() {
	}
	
	/**
	 * This constructor provides the initial properties for the
	 * configuration.
	 * 
	 * @param props The properties
	 */
	public DefaultConfiguration(java.util.Properties props) {
		m_properties = props;
	}
	
	/**
	 * This method returns the property associated with the
	 * supplied name.
	 * 
	 * @param name The name
	 * @return The value, or null if not defined
	 */
	public String getProperty(String name) {
		return(m_properties.getProperty(name));
	}
}
