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
package org.savara.protocol;

/**
 * This class represents a protocol id, used to identify the name
 * of the protocol used to monitor a conversation, and the role
 * being played by the endpoint.
 *
 */
public class ProtocolId {

	private String m_name=null;
	private String m_role=null;
	
	/**
	 * The constructor initialized with the name of the protocol
	 * and the role being played.
	 * 
	 * @param name The name
	 * @param role The role
	 */
	public ProtocolId(String name, String role) {
		m_name = name;
		m_role = role;
	}

	/**
	 * The name of the protocol.
	 * 
	 * @return The protocol name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * The role being monitored.
	 * 
	 * @return The role
	 */
	public String getRole() {
		return(m_role);
	}
	
	public int hashCode() {
		return(m_name.hashCode());
	}
	
	public boolean equals(Object obj) {
		boolean ret=false;
		
		if (obj instanceof ProtocolId &&
				m_name != null && ((ProtocolId)obj).m_name != null &&
				m_role != null && ((ProtocolId)obj).m_role != null &&
				((ProtocolId)obj).m_name.equals(m_name) &&
				((ProtocolId)obj).m_role.equals(m_role)) {
			ret = true;
		}
		
		return(ret);
	}
}
