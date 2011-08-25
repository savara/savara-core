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
package org.savara.activity.analyser.cdm.pi4soa;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import org.savara.activity.analyser.cdm.AbstractServiceValidator;
import org.savara.activity.analyser.cdm.ValidatorName;
import org.savara.activity.model.Correlation;
import org.savara.activity.model.CorrelationKey;
import org.pi4soa.common.util.MessageUtil;
import org.pi4soa.service.ServiceException;
import org.pi4soa.service.behavior.*;
import org.pi4soa.service.monitor.*;

/**
 * This class implements the pi4soa service validator responsible for
 * validating a stream of ESB messages against a choreography model.
 */
public class Pi4SOAServiceValidator extends AbstractServiceValidator {

	private static final String CDM_MODEL_TYPE = "cdm";

	/**
	 * This is the constructor for the pi4soa service
	 * validator implementation.
	 * 
	 * @param name The validator name
	 * @throws Exception Failed to initialize service validator
	 */
	public Pi4SOAServiceValidator(ValidatorName name)
						throws Exception {
		super(name);
		
		update();
	}
	
	/**
	 * This method returns the model type associated with this
	 * service validator.
	 * 
	 * @return The model type
	 */
	public static String getModelType() {
		return CDM_MODEL_TYPE;
	}
	
	public String getProtocolName() {
		return(m_protocolName);
	}
	
	/**
	 * This method is called to update the model associated
	 * with the service validator.
	 * 
	 * @throws Exception Failed to update the service validator
	 */
	public void update() throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Update: "+getValidatorName());
		}
		
		java.io.InputStream is=getModel();
		
		if (is != null) {
			org.pi4soa.cdl.Package cdlpack=
				org.pi4soa.service.util.DescriptionRetrievalUtil.instance().getCDLPackage(is);
			
			if (cdlpack != null) {
				m_protocolName = new QName(cdlpack.getTargetNamespace(), cdlpack.getName()).toString();
			}
			
			try {
				is.close();
			} catch(Exception e) {
				logger.error("Failed to close model '"+
						getValidatorName()+"' input stream", e);
			}
		}
		
		// Get the endpoint description
		is=getModel();
		
		if (is != null) {
			org.pi4soa.service.behavior.ServiceDescription sdesc=
				org.pi4soa.service.util.DescriptionRetrievalUtil.instance().getServiceDescription(is,
								getValidatorName().getRole());
			
			try {
				is.close();
			} catch(Exception e) {
				logger.error("Failed to close model '"+
						getValidatorName()+"' input stream", e);
			}

	       	if (sdesc != null) {
				
				if (m_monitor == null) {
					
					if (logger.isDebugEnabled()) {
						logger.debug("Service monitor for '"+
									sdesc.getFullyQualifiedName()+
									"' being created");
					}

					try {
						// Use XML configuration, to enable alternative
						// runtime configuration to be specified by
						// including a pi4soa.xml file in the environment
						DefaultMonitorConfiguration conf=
							new XMLMonitorConfiguration();
						
						conf.setValidateBehaviour(getValidatorName().isValidate());

						m_monitor = ServiceMonitorFactory.getServiceMonitor(conf);
						
						// Register service description
						m_monitor.getConfiguration().getServiceRepository().
									addServiceDescription(sdesc);
						
						logger.debug("Created monitor for service description "+sdesc.getFullyQualifiedName());

					} catch(Exception e) {
						logger.error("Failed to initialize service monitor: "+e);
					}
				} else {
					
			    	synchronized(m_monitor) {
						// Service monitor already in use for the service
						// description, so update description
						try {
							// Clear previous version of the service description
							ServiceDescription[] sdescs=
								m_monitor.getConfiguration().getServiceRepository().getServiceDescriptions();
							
							for (int i=0; sdescs != null && i < sdescs.length; i++) {
								m_monitor.getConfiguration().getServiceRepository().
											removeServiceDescription(sdescs[i]);
							}
							
							logger.debug("Updating service description for "+sdesc.getFullyQualifiedName());
							
							m_monitor.getConfiguration().getServiceRepository().addServiceDescription(sdesc);
						} catch(Exception e) {
							logger.error("Failed to update service description '"+
											sdesc.getFullyQualifiedName()+"'", e);
						}
			    	}
				}
			} else {
				logger.error("Unable to obtain service description for validator '"+
								getValidatorName()+"'");
	       	}
		} else {
			logger.error("Unable to obtain model for validator '"+
							getValidatorName()+"'");
			
			throw new java.io.IOException("Failed to locate model '"+
					getValidatorName().getModelName()+"'");
		}
	}
	
	/**
	 * This method processes a sent message against a service
	 * behavioural description.
	 * 
	 * @param mesgType The optional message type
	 * @param msg The message
	 * @throws Exception Failed to process sent message 
	 */
	public java.util.List<Correlation> messageSent(String mesgType, java.io.Serializable msg,
				java.util.List<Correlation> correlations) throws Exception {
    	
    	if (msg == null) {
    		throw new ServiceException("Failed to obtain value from message: "+msg);
    	}
    	
    	if (mesgType == null) {
    		mesgType = MessageUtil.getMessageType(msg);
    	}
    	
    	org.pi4soa.service.Message mesg=
    			m_monitor.createMessage(mesgType,
    				null, null, msg, null, null);
    	mesg.setMessageIdentities(getIdentities(correlations));
   	
    	synchronized(m_monitor) {
    		m_monitor.messageSent(mesg);
    	}
    	
    	return(getCorrelations(mesg));
	}
	
	/**
	 * This method processes a received message against a service
	 * behavioural description.
	 * 
	 * @param mesgType The optional message type
	 * @param msg The message
	 * @throws Exception Failed to process received message 
	 */
	public java.util.List<Correlation> messageReceived(String mesgType, java.io.Serializable msg,
					java.util.List<Correlation> correlations) throws Exception {
   	
    	if (msg == null) {
    		//throw new ServiceException("Failed to obtain value from message: "+msg);
    	}
    	
    	if (mesgType == null) {
    		mesgType = MessageUtil.getMessageType(msg);
    	}

    	org.pi4soa.service.Message mesg=
    			m_monitor.createMessage(mesgType,
    				null, null, msg, null, null);
    	mesg.setMessageIdentities(getIdentities(correlations));
    	
    	synchronized(m_monitor) {
    		m_monitor.messageReceived(mesg); 
    	}
    	
    	return(getCorrelations(mesg));
	}
	
	protected java.util.List<org.pi4soa.service.Identity> getIdentities(java.util.List<Correlation> correlations) {
		java.util.List<org.pi4soa.service.Identity> ret=null;
		
		if (correlations != null && correlations.size() > 0 &&
							correlations.get(0).getKey().size() > 0) {
			String[] names=new String[correlations.get(0).getKey().size()];
			Object[] values=new String[correlations.get(0).getKey().size()];
			String name="";
			for (int i=0; i < names.length; i++) {
				if (i > 0) {
					name += ":";
				}
				name += correlations.get(0).getKey().get(i).getName();
				names[i] = correlations.get(0).getKey().get(i).getName();
				values[i] = correlations.get(0).getKey().get(i).getValue();
			}
			ret = new java.util.Vector<org.pi4soa.service.Identity>();
			ret.add(new org.pi4soa.service.Identity(name, names, values));
		}
		
		return(ret);
	}
	
	protected java.util.List<Correlation> getCorrelations(org.pi4soa.service.Message mesg) {
    	java.util.List<Correlation> ret=new java.util.Vector<Correlation>();
    	
     	for (org.pi4soa.service.Identity id : mesg.getMessageIdentities()) {
     		Correlation correlation=new Correlation();
     		
     		for (int i=0; i < id.getTokens().length; i++) {
     			CorrelationKey key=new CorrelationKey();
     			key.setName(id.getTokens()[i]);
     			key.setValue(id.getValues()[i].toString());
     			correlation.getKey().add(key);
     		}
     		
    		ret.add(correlation);
    	}
    	
    	return(ret);
	}
	
	/**
	 * This method closes the service validator.
	 * 
	 * @throws Exception Failed to close the service validator
	 */
	public void close() throws Exception {
		m_monitor.close();
	}

	private static final Logger logger = Logger.getLogger(Pi4SOAServiceValidator.class);

	private org.pi4soa.service.monitor.ServiceMonitor m_monitor=null;
	private String m_protocolName=null;
}
