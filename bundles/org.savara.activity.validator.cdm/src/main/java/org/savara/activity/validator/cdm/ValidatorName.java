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
 * This class represents a key for looking up a service validator.
 */
public class ValidatorName {

	/**
	 * This constructor is initialized with the details used to
	 * identify the validator.
	 * 
	 * @param role The role
	 */
	public ValidatorName(String role) {
		m_role = role;
		m_validate = false;
	}
	
	/**
	 * This constructor is initialized with the details used to
	 * identify the validator.
	 * 
	 * @param modelName The model name
	 * @param role The role
	 * @param validate Whether to validate, or simply record
	 */
	public ValidatorName(String modelName, String role) {
		m_modelName = modelName;
		m_role = role;
		m_validate = true;
	}
	
	/**
	 * This method returns the model name associated with the
	 * service validator.
	 * 
	 * @return The model name
	 */
	public String getModelName() {
		return(m_modelName);
	}
	
	/**
	 * This method returns the type associated with the model.
	 * If no model has been defined for the validator name,
	 * as in the case of a validator in record mode, then
	 * this method will return ValidatorName.NO_MODEL_TYPE.
	 * 
	 * @return The model type
	 */
	public String getModelType() {
		String ret=null;		
		int pos=0;
		
		if (m_modelName != null &&
					(pos=m_modelName.lastIndexOf('.')) != -1) {
			ret = m_modelName.substring(pos+1);
		} else {
			ret = NO_MODEL_TYPE;
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the role associated with the Service
	 * Validator.
	 * 
	 * @return The role
	 */
	public String getRole() {
		return(m_role);
	}
	
	/**
	 * This method determines whether the associated service
	 * validator is in validation or record mode.
	 * 
	 * @return Whether in validation or record mode
	 */
	public boolean isValidate() {
		return(m_validate);
	}
	
	public boolean equals(Object obj) {
		boolean ret=false;
		
		if (obj instanceof ValidatorName) {
			ret = (obj.toString().equals(toString()) &&
					((ValidatorName)obj).m_validate == m_validate);
		}

		return(ret);
	}
	
	public int hashCode() {
		return(toString().hashCode());
	}

	public String toString() {
		String ret=null;
		
		if (isValidate()) {
			ret = m_modelName+":"+m_role;
		} else {
			ret = m_role;
		}
		
		return(ret);
	}
	
	public static final String NO_MODEL_TYPE="NoModelType";
	
	private String m_modelName=null;
	private String m_role=null;
	private boolean m_validate=false;
}