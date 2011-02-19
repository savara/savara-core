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
 * Abstract base class representing the ServiceValidator.
 */
public abstract class AbstractServiceValidator implements ServiceValidator {

	private static final String MODELS_PATH = "models/";

	/**
	 * This is the constructor for the abstract
	 * service validator, initialised with the validator
	 * name.
	 * 
	 * @param name The validator name
	 */
	public AbstractServiceValidator(ValidatorName name) {
		m_name = name;
	}
	
	/**
	 * This method returns the validator name.
	 * 
	 * @return The validator name
	 */
	public ValidatorName getValidatorName() {
		return(m_name);
	}
	
	/**
	 * This method returns the input stream associated
	 * with the model. Once the input stream has been
	 * used, it is the caller's responsibility to close
	 * the stream.
	 * 
	 * @return The model's input stream, or null if
	 * 				not found
	 */
	protected java.io.InputStream getModel() {
		String filePath=MODELS_PATH+getValidatorName().getModelName();
		
		java.io.InputStream ret = AbstractServiceValidator.class.
				getClassLoader().getResourceAsStream(filePath);
		
		return(ret);
	}
	
	public String toString() {
		return("ServiceValidator["+getValidatorName()+"]");
	}
	
	private ValidatorName m_name=null;
}
