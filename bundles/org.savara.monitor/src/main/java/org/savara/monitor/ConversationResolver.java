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

import org.scribble.protocol.monitor.model.Description;

/**
 * This interface represents the component responsible for deriving
 * the conversation instance id from a message.
 *
 */
public interface ConversationResolver {

	/**
	 * This method derives the conversation id from the supplied protocol
	 * description and message.
	 * 
	 * @param protocol The protocol
	 * @param message The message
	 * @return The conversation id, or null if could not be resolved
	 */
	public ConversationId getConversationId(Description protocol, Message message);
	
}
