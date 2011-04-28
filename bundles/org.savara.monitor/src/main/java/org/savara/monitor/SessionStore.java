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
package org.savara.monitor;

import java.io.Serializable;

import org.savara.common.config.Configuration;
import org.savara.protocol.ProtocolId;

/**
 * This interface represents a session store responsible for
 * persisting information about conversation instances being
 * monitored against protocols.
 *
 */
public interface SessionStore {

	/**
	 * This method sets the configuration for use by the session
	 * store.
	 * 
	 *  @param config The configuration
	 *  @throws IOException Failed to setConfiguration session store
	 */
	public void setConfiguration(Configuration config);
	
	/**
	 * This method returns a new session associated with
	 * the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @param session, The session object
	 * @return The session
	 */
	public java.io.Serializable create(ProtocolId pid, ConversationInstanceId cid, Serializable session);
	
	/**
	 * This method returns an existing session associated with
	 * the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @return The session
	 */
	public java.io.Serializable find(ProtocolId pid, ConversationInstanceId cid);
	
	/**
	 * This method removes an existing session associated
	 * with the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 */
	public void remove(ProtocolId pid, ConversationInstanceId cid);
	
	/**
	 * This method updates an existing session associated
	 * with the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @param session The session
	 */
	public void update(ProtocolId pid, ConversationInstanceId cid,
					java.io.Serializable session);
	
	/**
	 * This method closes the session store.
	 */
	public void close();
	
}
