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
package org.savara.activity.validator.cdm;

/**
 * This interface is implemented by model specific loaders, to
 * load a ValidatorConfig for the particular model type.
 */
public interface ValidatorConfigLoader {

	/**
	 * This method determines if the validator config loader
	 * implementation supports the supplied model type.
	 *  
	 * @param modelType The model type
	 * @return Whether the loader supports the model type
	 */
	public boolean isSupported(String modelType);
	
	/**
	 * This method loads the validator config, contained within
	 * the supplied input stream representing a model of the
	 * supported type.
	 * 
	 * @param is The input stream
	 * @param modelPath The path to the model file
	 * @return The validator config
	 * @throws java.io.IOException Failed to load validator config
	 */
	public ValidatorConfig loadValidatorConfig(java.io.InputStream is,
					String modelPath) throws java.io.IOException;
	
}
