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
package org.savara.monitor.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.protocol.ProtocolId;
import org.scribble.protocol.monitor.model.Description;

public class DescriptionCache {

	private static final Logger LOG=Logger.getLogger(DescriptionCache.class.getName());
	
	private java.util.Map<ProtocolId, Description> m_descriptions=
					new java.util.HashMap<ProtocolId, Description>();
	
	/**
	 * This method returns the description associated with the supplied
	 * protocol id.
	 * 
	 * @param pid The protocol id
	 * @return The description, or null if not found
	 */
	public Description getDescription(ProtocolId pid) {
		return(m_descriptions.get(pid));
	}
	
	/**
	 * This method sets the monitorable description associated with
	 * the supplied protocol id.
	 * 
	 * @param pid The protocol id
	 * @param description The monitorable description
	 */
	public void setDescription(ProtocolId pid, Description description) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Set description for '"+pid+"' = "+description);
		}
		m_descriptions.put(pid, description);
	}
}
