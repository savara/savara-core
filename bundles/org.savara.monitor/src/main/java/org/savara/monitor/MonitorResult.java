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
package org.savara.monitor;

import org.savara.protocol.ProtocolId;

/**
 * This class represents a result from monitoring a message against a
 * protocol.
 *
 */
public class MonitorResult {

	private ProtocolId _protocolId=null;
	private ConversationId _conversationId=null;
	private boolean m_valid=false;
	private String m_reason=null;
	private java.util.Map<String,Object> m_properties=null;
	
	/**
	 * This is the result constructor.
	 * 
	 * @param pid The protocol id
	 * @param cid The conversation id
	 * @param valid Whether the result was valid
	 * @param reason Reason for result
	 * @param props The properties
	 */
	public MonitorResult(ProtocolId pid, ConversationId cid,
			boolean valid, String reason, java.util.Map<String,Object> props) {
		_protocolId = pid;
		_conversationId = cid;
		m_valid = valid;
		m_reason = reason;
		m_properties = props;
	}
	
	/**
	 * This method returns the protocol id.
	 * 
	 * @return The protocol id
	 */
	public ProtocolId getProtocolId() {
		return(_protocolId);
	}
	
	/**
	 * This method returns the conversation id.
	 * 
	 * @return The conversation id
	 */
	public ConversationId getConversationId() {
		return(_conversationId);
	}
	
	/**
	 * This method indicates whether the result is valid.
	 * 
	 * @return Whether the result is valid
	 */
	public boolean isValid() {
		return(m_valid);
	}
	
	/**
	 * This method provides an optional reason for the result.
	 * 
	 * @return The reason, or null if not specified
	 */
	public String getReason() {
		return(m_reason);
	}
	
	/**
	 * This method returns the property map.
	 * 
	 * @return The property map
	 */
	public java.util.Map<String,Object> getProperties() {
		return(m_properties);
	}
	
	public String toString() {
		return("MonitorResult[pid="+getProtocolId()+" cid="+getConversationId()+
				" valid="+isValid()+" reason="+
				getReason()+" props="+getProperties()+"]");
	}
}
