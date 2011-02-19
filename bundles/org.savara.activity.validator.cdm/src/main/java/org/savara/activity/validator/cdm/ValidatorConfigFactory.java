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

import org.apache.log4j.Logger;

/**
 * This class provides a factory for ValidatorConfig objects
 * based on supplied model file details.
 */
public class ValidatorConfigFactory {

	/**
	 * This method retrieves a validator config associated with
	 * the supplied file. If the model associated with the
	 * file is not supported, then a null will be returned.
	 * 
	 * @param file The file containing the model
	 * @return The validator config, or null if an unsupported type
	 * @exception IOException Failed to load supported model type
	 */
	public static ValidatorConfig getValidatorConfig(java.io.File file)
							throws java.io.IOException {
		ValidatorConfig ret=null;
		
		String modelType=null;
		
		if (file != null && (modelType=getModelType(file.getName())) != null) {
			ValidatorConfigLoader loader=null;
			
			for (int i=0; loader == null &&
					i < m_loaders.size(); i++) {
				
				if (m_loaders.get(i).isSupported(modelType)) {
					loader = m_loaders.get(i);
				}
			}
			
			if (loader != null) {
				java.io.FileInputStream fis=new java.io.FileInputStream(file);
				
				ret = loader.loadValidatorConfig(fis, file.getName());
				
				fis.close();
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Returning validator config for "+
							file.getName()+": "+ret);
		}

		return(ret);		
	}
	
	/**
	 * This method determines the model type associated with the
	 * supplied model filename.
	 * 
	 * @param name The model filename
	 * @return The model type, or null if not known
	 */
	protected static String getModelType(String name) {
		String ret=null;
		int pos=name.lastIndexOf('.');
		
		if (pos != -1) {
			ret = name.substring(pos+1);
		}
		
		return(ret);
	}
	
	private static final Logger logger = Logger.getLogger(ValidatorConfigFactory.class);

	private static java.util.List<ValidatorConfigLoader> m_loaders=
					new java.util.Vector<ValidatorConfigLoader>();
	
	static {
		m_loaders.add(new org.savara.activity.validator.cdm.pi4soa.Pi4SOAValidatorConfigLoader());
	}
}
