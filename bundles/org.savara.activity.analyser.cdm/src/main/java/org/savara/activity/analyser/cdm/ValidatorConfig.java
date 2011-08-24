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

/**
 * This interface represents a configuration that will be used for
 * validation against a stream of ESB based messages.
 */
public interface ValidatorConfig {

	/**
	 * This method returns the type of the model associated
	 * with this validator configuration. This will
	 * general be based on the file extension of the model
	 * file.
	 * 
	 * @return The model type
	 */
	public String getModelType();
	
	/**
	 * This method returns the validator configuration
	 * associated with the model.
	 * 
	 * @return The validator model
	 */
	public org.w3c.dom.Element getConfiguration();
	
}
