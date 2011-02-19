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

import org.savara.protocol.ProtocolId;
import org.savara.protocol.repository.ProtocolRepository;
import org.savara.protocol.ProtocolUnknownException;

/**
 * This interface represents a behaviour monitor, comparing a stream of messages
 * against the expected behaviour associated with an endpoint protocol.
 *
 * Use cases:
 * 1) Where a component directly uses the monitor, and knows the protocol id
 *    and/or the conversation id in advance.
 * 2) Where used to analyse an activity stream, from within an application or
 *    server, where the events may related to multiple conversations and/or
 *    protocols.
 */
public interface Monitor {

	/**
	 * This method sets the protocol repository to use when
	 * monitoring.
	 * 
	 * @param rep The protocol repository
	 */
	public void setProtocolRepository(ProtocolRepository rep);
	
	/**
	 * This method sets the session store to use when
	 * monitoring.
	 * 
	 * @param store The session store
	 */
	public void setSessionStore(SessionStore store);
	
	/**
	 * This method is used to indicate that a message has been
	 * sent or received, and should be monitored against the
	 * specified protocol id and optional conversation instance
	 * id.
	 * 
	 * If the protocol id is not specified, then the first
	 * relevant protocol will be used. If none are found, then
	 * a null result will be returned.
	 * 
	 * If the conversation instance id is not explicitly
	 * specified, then the protocol monitor will be responsible
	 * for deriving the appropriate value.
	 * 
	 * @param pid The optional protocol id
	 * @param cid The optional conversation instance id
	 * @param mesg The message
	 * @return The monitor result, or null if a suitable protocol was not found
	 * @throws ProtocolUnknownException Unknown protocol name or role
	 * @throws IOException Failed to create or retrieve session
	 */
	public MonitorResult process(ProtocolId pid, ConversationInstanceId cid, Message mesg)
							throws ProtocolUnknownException,
									java.io.IOException;
	
}
