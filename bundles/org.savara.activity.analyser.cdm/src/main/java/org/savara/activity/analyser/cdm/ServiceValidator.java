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
package org.savara.activity.analyser.cdm;

import org.savara.activity.model.Context;

/**
 * This interface represents a service validator responsible for
 * validing a stream of ESB messages against a model.
 */
public interface ServiceValidator {

	/**
	 * This method returns the validator name.
	 * 
	 * @return The validator name
	 */
	public ValidatorName getValidatorName();
	
	/**
	 * The protocol name.
	 * 
	 * @return The protocol name
	 */
	public String getProtocolName();
	
	/**
	 * This method processes a sent message against a service
	 * behavioural description.
	 * 
	 * @param mesgType The optional message type
	 * @param msg The message
	 * @throws Exception Failed to process sent message 
	 */
	public java.util.List<Context> messageSent(String mesgType, java.io.Serializable msg) throws Exception;
	
	/**
	 * This method processes a received message against a service
	 * behavioural description.
	 * 
	 * @param mesgType The optional message type
	 * @param msg The message
	 * @throws Exception Failed to process received message 
	 */
	public java.util.List<Context> messageReceived(String mesgType, java.io.Serializable msg) throws Exception;
	
	/**
	 * This method is called to update the model associated
	 * with the service validator.
	 * 
	 * @throws Exception Failed to update the service validator
	 */
	public void update() throws Exception;

	/**
	 * This method closes the service validator.
	 * 
	 * @throws Exception Failed to close the service validator
	 */
	public void close() throws Exception;
	
}
