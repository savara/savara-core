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

import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;

/**
 * This class provides a factory for Service Validators.
 */
public class ServiceValidatorFactory {

	/**
	 * This method returns the Service Validator appropriate
	 * for the supplied validator name.
	 * 
	 * @param name The validator name
	 * @return The service validator
	 * @exception IOException Failed to create the service validator
	 */
	public static ServiceValidator getServiceValidator(ValidatorName name)
							throws Exception {
		ServiceValidator ret=null;
		
		String modelType=name.getModelType();
		
		if (modelType != null &&
				m_validatorClasses.containsKey(modelType)) {
			Class<?> cls=m_validatorClasses.get(modelType);

			Constructor<?> con=cls.getConstructor(
					new Class[]{ValidatorName.class});
			
			ret = (ServiceValidator)con.newInstance(new Object[]{name});
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Returning service validator for "+
							name+": "+ret);
		}

		return(ret);		
	}
	
	private static final Logger logger = Logger.getLogger(ServiceValidatorFactory.class);

	private static java.util.Map<String,Class<?>> m_validatorClasses=
					new java.util.HashMap<String,Class<?>>();
	
	static {
		m_validatorClasses.put(org.savara.activity.analyser.cdm.pi4soa.Pi4SOAServiceValidator.getModelType(),
				org.savara.activity.analyser.cdm.pi4soa.Pi4SOAServiceValidator.class);
	}
}
