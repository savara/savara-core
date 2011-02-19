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
 * This interface represents the criteria to be used to identify
 * a protocol and role of interest.
 *
 */
public interface ProtocolCriteria {

	/**
	 * This method provides the destination endpoint address of the component that
	 * is the subject of the interaction.
	 * 
	 * If not specified, then the destination endpoint address must be defined.
	 * 
	 * @return The destination endpoint address, or null if not known
	 */
	public String getDestinationEndpointAddress();
	
	/**
	 * This method returns the destinationendpoint type of the component that is
	 * the subject of the interaction.
	 * 
	 * If not specified, then the destination endpoint type must be defined.
	 * 
	 * @return The destination enpdoint type, or null if not known
	 */
	public String getDestinationEndpointType();
	
	/**
	 * This method provides the endpoint address of the component that
	 * is the subject of the interaction.
	 * 
	 * If not specified, then the endpoint address must be defined.
	 * 
	 * @return The source endpoint address, or null if not known
	 */
	public String getSourceEndpointAddress();
	
	/**
	 * This method returns the source endpoint type of the component that is
	 * the subject of the interaction.
	 * 
	 * If not specified, then the source endpoint type must be defined.
	 * 
	 * @return The source endpoint type, or null if not known
	 */
	public String getSourceEndpointType();
	
	/**
	 * This method returns the direction of the message.
	 * 
	 * @return The direction
	 */
	public Direction getDirection();
	
	/**
	 * This enumerated class represents the direction of the message,
	 * whether it is 'inbound' (i.e. received) or 'outbound' 
	 * (i.e. sent).
	 *
	 */
	public enum Direction {
		Inbound,
		Outbound
	}
}
