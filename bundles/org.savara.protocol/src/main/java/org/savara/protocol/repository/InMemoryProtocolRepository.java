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
package org.savara.protocol.repository;

import org.savara.protocol.ProtocolCriteria;
import org.savara.protocol.ProtocolId;
import org.scribble.protocol.model.ProtocolModel;

/**
 * This is the in-memory implementation of the protocol repository.
 *
 */
public class InMemoryProtocolRepository implements ProtocolRepository {

	private java.util.Map<ProtocolId,ProtocolModel> m_protocolModels=
						new java.util.HashMap<ProtocolId,ProtocolModel>();
	
	public InMemoryProtocolRepository() {
	}

	/**
	 * This method adds a protocol model to the repository.
	 * 
	 * @param pid The protocol id
	 * @param pm The protocol model
	 */
	public void addProtocol(ProtocolId pid, ProtocolModel pm) {
		m_protocolModels.put(pid, pm);
	}
	
	/**
	 * This method returns the protocol model associated with
	 * the supplied protocol id (name and role).
	 * 
	 * @param pid The protocol id
	 * @return The protocol model, or null if not found
	 */
	public ProtocolModel getProtocol(ProtocolId pid) {
		return(m_protocolModels.get(pid));
	}
	
	/**
	 * This method determines which protocols, identified by their protocol ids,
	 * would be interested in an endpoint address, whether or not the message is inbound,
	 * and finally the message details itself.
	 * 
	 * @param criteria The message criteria
	 * @return The list of protocol ids interested in the message criteria
	 */
	public java.util.List<ProtocolId> getProtocols(ProtocolCriteria criteria) {
		java.util.List<ProtocolId> ret=new java.util.Vector<ProtocolId>();
		
		return(ret);
	}
}
