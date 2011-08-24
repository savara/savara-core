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
package org.savara.activity.analyser.cdm;

/**
 * This class represents an endpoint being monitored by one
 * or more service validator.
 */
public class Endpoint {

	/**
	 * This is the constructor for the endpoint, supplying
	 * the destination associated with the endpoint.
	 * 
	 * @param destination The destination
	 */
	public Endpoint(String destination) {
		m_destination = destination;
	}
	
	/**
	 * This method returns the destination for the endpoint.
	 * 
	 * @return The destination
	 */
	public String getDestination() {
		return(m_destination);
	}
	
	/**
	 * This method returns the list of service validators
	 * associated with this endpoint.
	 * 
	 * @return The list of service validators for this endpoint
	 */
	public java.util.List<ServiceValidator> getServiceValidators() {
		return(m_validators);
	}
	
	public boolean equals(Object obj) {
		boolean ret=false;
		
		if (obj instanceof Endpoint) {
			Endpoint ep=(Endpoint)obj;
			
			if (ep.getDestination() != null &&
					ep.getDestination().equals(m_destination)) {
				ret = true;
			}
		}
		
		return(ret);
	}
	
	public int hashCode() {
		int ret=0;
		
		if (m_destination != null) {
			ret = m_destination.hashCode();
		}
		
		return(ret);
	}
	
	public String toString() {
		return("Endpoint["+m_destination+"]");
	}
	
	private String m_destination=null;
	private java.util.List<ServiceValidator> m_validators=
					new java.util.Vector<ServiceValidator>();
}
