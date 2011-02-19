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

import org.savara.protocol.ProtocolCriteria;

/**
 * This class represents a message to be monitored.
 *
 */
public class Message extends org.scribble.protocol.monitor.DefaultMessage
					implements ProtocolCriteria  {

	private String m_destinationEndpointAddress=null;
	private String m_destinationEndpointType=null;
	private String m_sourceEndpointAddress=null;
	private String m_sourceEndpointType=null;
	private ProtocolCriteria.Direction m_direction=Direction.Outbound;
	private java.util.List<String> m_values=new java.util.Vector<String>();
	
	/**
	 * This method returns the list of values associated with the message.
	 * 
	 * NOTE: The number of values must match the number of types.
	 * 
	 * @return The list of values
	 */
	public java.util.List<String> getValues() {
		return(m_values);
	}
	
	/**
	 * This method provides the destination endpoint address of the component that
	 * is the subject of the interaction.
	 * 
	 * If not specified, then the destination endpoint address must be defined.
	 * 
	 * @return The destination endpoint address, or null if not known
	 */
	public String getDestinationEndpointAddress() {
		return(m_destinationEndpointAddress);
	}
	
	/**
	 * This method sets the destination endpoint address.
	 * 
	 * @param address The destination endpoint address
	 */
	public void setDestinationEndpointAddress(String address) {
		m_destinationEndpointAddress = address;
	}
	
	/**
	 * This method returns the destination endpoint type of the component that is
	 * the subject of the interaction.
	 * 
	 * If not specified, then the destination endpoint type must be defined.
	 * 
	 * @return The destination endpoint type, or null if not known
	 */
	public String getDestinationEndpointType() {
		return(m_destinationEndpointType);
	}
	
	/**
	 * This method sets the destination endpoint type.
	 * 
	 * @param type The destination endpoint type
	 */
	public void setDestinationEndpointType(String type) {
		m_destinationEndpointType = type;
	}
	
	/**
	 * This method provides the source endpoint address of the component that
	 * is the subject of the interaction.
	 * 
	 * If not specified, then the source endpoint address must be defined.
	 * 
	 * @return The source endpoint address, or null if not known
	 */
	public String getSourceEndpointAddress() {
		return(m_sourceEndpointAddress);
	}
	
	/**
	 * This method sets the source endpoint address.
	 * 
	 * @param address The source endpoint address
	 */
	public void setSourceEndpointAddress(String address) {
		m_sourceEndpointAddress = address;
	}
	
	/**
	 * This method returns the source endpoint type of the component that is
	 * the subject of the interaction.
	 * 
	 * If not specified, then the source endpoint type must be defined.
	 * 
	 * @return The source endpoint type, or null if not known
	 */
	public String getSourceEndpointType() {
		return(m_sourceEndpointType);
	}
	
	/**
	 * This method sets the source endpoint type.
	 * 
	 * @param type The source endpoint type
	 */
	public void setSourceEndpointType(String type) {
		m_sourceEndpointType = type;
	}
	
	/**
	 * This method returns the direction of the message.
	 * 
	 * @return The direction
	 */
	public Direction getDirection() {
		return(m_direction);
	}
	
	/**
	 * This method sets the direction of the message.
	 * 
	 * @param direction The direction
	 */
	public void setDirection(Direction direction) {
		m_direction = direction;
	}
}
