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
package org.savara.monitor.impl;

import java.io.Serializable;

import org.savara.common.config.Configuration;
import org.savara.monitor.ConversationInstanceId;
import org.savara.monitor.SessionStore;
import org.savara.protocol.ProtocolId;

public class InMemorySessionStore implements SessionStore {

	private java.util.Map<ProtocolId,java.util.Map<ConversationInstanceId,java.io.Serializable>> m_sessions=
			new java.util.HashMap<ProtocolId,java.util.Map<ConversationInstanceId, java.io.Serializable>>();
	
	/**
	 * This method sets the configuration for use by the session
	 * store.
	 * 
	 *  @param config The configuration
	 *  @throws IOException Failed to initialize session store
	 */
	public void setConfiguration(Configuration config){
	}
	
	/**
	 * This method adds a new session.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @param session The session
	 */
	protected void addSession(ProtocolId pid, ConversationInstanceId cid, java.io.Serializable session) {
		java.util.Map<ConversationInstanceId,java.io.Serializable> sessions=
						m_sessions.get(pid);

		if (sessions == null) {
			sessions = new java.util.HashMap<ConversationInstanceId,java.io.Serializable>();
			m_sessions.put(pid, sessions);
		
		} else if (sessions.containsKey(cid)) {
			throw new IllegalArgumentException("Conversation instance id already in use");
		}
		
		sessions.put(cid, session);	
	}
	
	/**
	 * This method returns a new session associated with
	 * the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @return The session
	 * @throws IllegalArgumentException Protocol or conversation id is invalid
	 * @throws IOException Failed to create new session
	 */
	public java.io.Serializable create(ProtocolId pid, ConversationInstanceId cid, Serializable session) {
		if (pid == null) {
			throw new IllegalArgumentException("Protocol id not specified");
		} else if (cid == null) {
			throw new IllegalArgumentException("Conversation instance id not specified");
		}
		
		addSession(pid, cid, session);
		
		return(session);
	}
	
	/**
	 * This method returns an existing session associated with
	 * the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @return The session, or null if not found
	 * @throws IllegalArgumentException Protocol or conversation id is invalid
	 * @throws IOException Failed to retrieve session
	 */
	public java.io.Serializable find(ProtocolId pid, ConversationInstanceId cid) {
		java.io.Serializable ret=null;
		
		if (pid == null) {
			throw new IllegalArgumentException("Protocol id not specified");
		} else if (cid == null) {
			throw new IllegalArgumentException("Conversation instance id not specified");
		}

		java.util.Map<ConversationInstanceId,java.io.Serializable> sessions=
			m_sessions.get(pid);

		if (sessions != null) {
			ret = sessions.get(cid);
		}
		
		return(ret);
	}
	
	/**
	 * This method removes an existing session associated
	 * with the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @throws IllegalArgumentException Conversation instance id is invalid or unknown
	 * @throws java.io.IOException Failed to remove existing session
	 */
	public void remove(ProtocolId pid, ConversationInstanceId cid) {
		if (pid == null) {
			throw new IllegalArgumentException("Protocol id not specified");
		} else if (cid == null) {
			throw new IllegalArgumentException("Conversation instance id not specified");
		}

		java.util.Map<ConversationInstanceId,java.io.Serializable> sessions=
			m_sessions.get(pid);

		if (sessions == null || sessions.containsKey(cid) == false) {
			throw new IllegalArgumentException("Conversation instance id is unknown");
		}
		
		sessions.remove(cid);
		
		// Clear up sub map associated with the protocol id
		if (sessions.size() == 0) {
			m_sessions.remove(pid);
		}
	}
	
	/**
	 * This method updates an existing session associated
	 * with the supplied protocol and conversation instance id.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation instance id
	 * @param session The session
	 */
	public void update(ProtocolId pid, ConversationInstanceId cid, java.io.Serializable session) {
		if (pid == null) {
			throw new IllegalArgumentException("Protocol id not specified");
		} else if (cid == null) {
			throw new IllegalArgumentException("Conversation instance id not specified");
		}

		java.util.Map<ConversationInstanceId,java.io.Serializable> sessions=
			m_sessions.get(pid);

		if (sessions == null || sessions.containsKey(cid) == false) {
			throw new IllegalArgumentException("Conversation instance id unknown");
		}
		
		sessions.put(cid, session);
	}
	
	/**
	 * This method closes the session store.
	 * 
	 * @throws java.io.IOException Failed to close the session store
	 */
	public void close(){
	}

}
