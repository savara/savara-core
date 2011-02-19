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
package org.savara.contract.model;

/**
 * This class represents the namespace.
 */
public class Namespace extends ContractObject {

	private String m_prefix=null;
	private String m_uri=null;
	private String m_schemaLocation=null;
	
	/**
	 * The default constructor.
	 */
	public Namespace() {
	}
	
	/**
	 * This method returns the prefix associated with the
	 * prefix.
	 * 
	 * @return The name
	 */
	public String getPrefix() {
		return(m_prefix);
	}
	
	/**
	 * This method sets the prefix of the namespace.
	 * 
	 * @param prefix The prefix
	 */
	public void setPrefix(String prefix) {
		m_prefix = prefix;
	}
	
	/**
	 * This method returns the URI associated with the
	 * namespace.
	 * 
	 * @return The URI
	 */
	public String getURI() {
		return(m_uri);
	}
	
	/**
	 * This method sets the URI of the namespace.
	 * 
	 * @param uri The URI
	 */
	public void setURI(String uri) {
		m_uri = uri;
	}
	
	/**
	 * This method returns the schema location associated with the
	 * namespace.
	 * 
	 * @return The URI
	 */
	public String getSchemaLocation() {
		return(m_schemaLocation);
	}
	
	/**
	 * This method sets the schema location of the namespace.
	 * 
	 * @param schemaLocation The optional schema location
	 */
	public void setSchemaLocation(String schemaLocation) {
		m_schemaLocation = schemaLocation;
	}

	public String toString() {
		return("\tNamespace prefix="+m_prefix+
				" uri="+m_uri+" schemaLocation="+m_schemaLocation+"\r\n");
	}
}
