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
package org.savara.activity.validator.cdm.pi4soa;

import org.savara.activity.validator.cdm.DefaultValidatorConfig;
import org.savara.activity.validator.cdm.ValidatorConfig;
import org.savara.activity.validator.cdm.ValidatorConfigLoader;

/**
 * This class provides the pi4soa implementation of the
 * validator config loader.
 */
public class Pi4SOAValidatorConfigLoader implements ValidatorConfigLoader {

	public static final String PI4SOA_MODEL_TYPE="cdm";
	
	/**
	 * This method determines if the validator config loader
	 * implementation supports the supplied model type.
	 *  
	 * @param modelType The model type
	 * @return Whether the loader supports the model type
	 */
	public boolean isSupported(String modelType) {
		return(PI4SOA_MODEL_TYPE.equals(modelType));
	}
	
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
					String modelPath) throws java.io.IOException {
		ValidatorConfig ret=null;
		
		org.pi4soa.cdl.Package cdlpack=
			org.pi4soa.service.util.DescriptionRetrievalUtil.instance().getCDLPackage(is);
		
		ValidatorConfigGenerator generator=new ValidatorConfigGenerator();
		
		org.w3c.dom.Element validator=
				generator.generate(cdlpack, modelPath);
		
		ret = new DefaultValidatorConfig(PI4SOA_MODEL_TYPE,
					validator);
		
		return(ret);
	}
}
