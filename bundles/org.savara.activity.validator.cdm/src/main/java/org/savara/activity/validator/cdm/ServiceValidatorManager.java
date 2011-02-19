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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

/**
 * This class is responsible for managing the active
 * service validators, the mapping of endpoints to the service
 * validators and updating the configuration when changes
 * are detected.
 */
public class ServiceValidatorManager {

	/**
	 * Default constructor for the service validator manager.
	 */
	public ServiceValidatorManager() {
		initialize();
	}
	
	/**
	 * This method initializes the service validator manager on
	 * startup.
	 */
	protected void initialize() {
		
		java.net.URL url=
			ServiceValidatorManager.class.getClassLoader().getResource(CONFIG_FILE);
		
		if (url != null) {
			m_validatorConfigFile = new java.io.File(url.getFile());
			
			java.io.File[] files=m_validatorConfigFile.getParentFile().listFiles();
			
			for (int i=0; m_modelsDir == null && i < files.length; i++) {
				if (files[i].getName().equals("models") &&
						files[i].isDirectory()) {
					m_modelsDir = files[i];
				}
			}
			
			if (m_modelsDir != null) {
				ValidatorConfigChangeMonitor mon=
					new ValidatorConfigChangeMonitor();
				
				new Thread(mon).start();
			}
		}
	}
	
	/**
	 * This method closes the service validation manager.
	 */
	public void close() {
	}
	
	/**
	 * This method returns the list of service validators associated
	 * with the supplied input endpoint.
	 * 
	 * @param endpoint The input endpoint
	 * @return The list of service validators, or null if the endpoint
	 * 					is unknown
	 */
	public java.util.List<ServiceValidator> getInputServiceValidators(Endpoint endpoint) {
		java.util.List<ServiceValidator> ret=
					m_inputValidators.get(endpoint);
		
		if (ret == null) {
			ret = m_replyToManager.getInputServiceValidators(endpoint);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Return input validators '"+endpoint+"': "+ret);
		}

		return(ret);
	}
	
	/**
	 * This method determines whether the supplied endpoint will be
	 * associated with a dynamic reply-to endpoint.
	 *  
	 * @param endpoint The endpoint
	 * @return Whether the endpoint has a dynamic reply-to
	 */
	public boolean isInputDynamicReplyTo(Endpoint endpoint) {
		boolean ret=m_inputDynaReplyTos.contains(endpoint);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Is input endpoint '"+endpoint+
					"' a dynamic reply-to: "+ret);
		}

		return(ret);
	}
	
	/**
	 * This method returns the list of service validators associated
	 * with the supplied output endpoint.
	 * 
	 * @param endpoint The output endpoint
	 * @return The list of service validators, or null if the endpoint
	 * 					is unknown
	 */
	public java.util.List<ServiceValidator> getOutputServiceValidators(Endpoint endpoint) {
		java.util.List<ServiceValidator> ret=
					m_outputValidators.get(endpoint);

		if (ret == null) {
			ret = m_replyToManager.getOutputServiceValidators(endpoint);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Return output validators '"+endpoint+"': "+ret);
		}

		return(ret);
	}
	
	/**
	 * This method determines whether the supplied endpoint will be
	 * associated with a dynamic reply-to endpoint.
	 *  
	 * @param endpoint The endpoint
	 * @return Whether the endpoint has a dynamic reply-to
	 */
	public boolean isOutputDynamicReplyTo(Endpoint endpoint) {
		boolean ret=m_outputDynaReplyTos.contains(endpoint);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Is output endpoint '"+endpoint+
					"' a dynamic reply-to: "+ret);
		}

		return(ret);
	}
	
	/**
	 * This method registers a list of service validators against
	 * a dynamic 'reply-to' endpoint.
	 * 
	 * @param endpoint The endpoint
	 * @param validators The list of service validators
	 */
	public void registerInputReplyToValidators(Endpoint endpoint,
						java.util.List<ServiceValidator> validators) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Register input reply-to '"+endpoint+"': "+validators);
		}
		
		m_replyToManager.registerInputDynamicReplyTo(endpoint, validators);
	}
	
	/**
	 * This method registers a list of service validators against
	 * a dynamic 'reply-to' endpoint.
	 * 
	 * @param endpoint The endpoint
	 * @param validators The list of service validators
	 */
	public void registerOutputReplyToValidators(Endpoint endpoint,
						java.util.List<ServiceValidator> validators) {
		if (logger.isDebugEnabled()) {
			logger.debug("Register output reply-to '"+endpoint+"': "+validators);
		}
		
		m_replyToManager.registerOutputDynamicReplyTo(endpoint, validators);
	}
	
	/**
	 * This method returns a ServiceValidator associated with the
	 * supplied validator name.
	 * 
	 * @param name The validator name
	 * @return The service validator
	 * @throws Exception Failed to create service validator
	 */
	public ServiceValidator createServiceValidator(ValidatorName name)
							throws Exception {
		ServiceValidator ret=null;
		
		synchronized(m_serviceValidators) {
			ret = m_serviceValidators.get(name);
			
			// If does not exist, then create, else update
			// the existing service validator
			if (ret == null) {
				ret = ServiceValidatorFactory.getServiceValidator(name);
				
				m_serviceValidators.put(name, ret);
			} else {
				
				// Update the description
				ret.update();
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Created Service Validator '"+name+"': "+ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method removes the service validator associated with
	 * the supplied validator name.
	 * 
	 * @param name The validator name
	 * @throws Exception Failed to remove the service validator
	 */
	protected void removeServiceValidator(ValidatorName name)
							throws Exception {
		synchronized(m_serviceValidators) {
			ServiceValidator validator=(ServiceValidator)
					m_serviceValidators.remove(name);

			// Check if validator should be removed from
			// input/output endpoint mappings
			java.util.Iterator<Endpoint> iter=m_inputValidators.keySet().iterator();
			
			while (iter.hasNext()) {
				Endpoint ep=iter.next();
				java.util.List<ServiceValidator> list=
						m_inputValidators.get(ep);
				
				if (list.contains(validator)) {
					list.remove(validator);
					
					if (list.size() == 0) {
						logger.error("Input validator list, associated with endpoint '"+
								ep+"' contains no entries after removal of validator '"+
								name+"' - this Endpoint should have previously been removed");
					}
				}
			}
			
			iter=m_outputValidators.keySet().iterator();
			
			while (iter.hasNext()) {
				Endpoint ep=iter.next();
				java.util.List<ServiceValidator> list=
						m_outputValidators.get(ep);
				
				if (list.contains(validator)) {
					list.remove(validator);
					
					if (list.size() == 0) {
						logger.error("Output validator list, associated with endpoint '"+
								ep+"' contains no entries after removal of validator '"+
								name+"' - this Endpoint should have previously been removed");
					}
				}
			}
			
			if (validator != null) {
				validator.close();
			}
		}
	}
	
	/**
	 * This method returns the set of currently configured
	 * service validator names.
	 * 
	 * @return The set of validator names
	 */
	protected java.util.Set<ValidatorName> getServiceValidatorNames() {
		java.util.Set<ValidatorName> ret=new java.util.HashSet<ValidatorName>();
		
		ret.addAll(m_serviceValidators.keySet());
		
		return(ret);
	}

	/**
	 * This method updates the configuration of the service
	 * validators and endpoint mappings.
	 */
	protected void updateConfigurations() {
		java.util.Set<ValidatorName> existingValidatorNames=
					getServiceValidatorNames();
		
		java.util.Set<Endpoint> existingInputEndpoints=
			new java.util.HashSet<Endpoint>(m_inputValidators.keySet());

		java.util.Set<Endpoint> existingOutputEndpoints=
			new java.util.HashSet<Endpoint>(m_outputValidators.keySet());

		java.util.Set<Endpoint> existingInputDynaReplyTos=
			new java.util.HashSet<Endpoint>(m_inputDynaReplyTos);

		java.util.Set<Endpoint> existingOutputDynaReplyTos=
			new java.util.HashSet<Endpoint>(m_outputDynaReplyTos);

		java.io.InputStream is=ServiceValidatorManager.class.getClassLoader().
					getResourceAsStream(CONFIG_FILE);

		if (logger.isDebugEnabled()) {
			logger.debug("ValidationFilter: config="+CONFIG_FILE+" is="+is);
		}

		try {
			DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);

			DocumentBuilder builder=fact.newDocumentBuilder();
			org.w3c.dom.Document doc=builder.parse(is);
			
			org.w3c.dom.Element config=doc.getDocumentElement();
			
			if (config.getNodeName().equals(VALIDATOR_NODE)) {
				
				// Old 'active' attribute is now 'mode'
				String active=config.getAttribute(ACTIVE_ATTR);
				
				if (active != null && active.equalsIgnoreCase("true")) {
					m_managedMode = true;
				}
				
				String mode=config.getAttribute(MODE_ATTR);
				
				if (mode != null) {
					if (mode.equals("manage")) {
						m_managedMode = true;
					} else if (mode.equals("monitor")) {
						m_managedMode = false; // will override active=true
					} else {
						logger.error("Unknown mode '"+mode+"' - setting to 'monitor'");
						m_managedMode = false;
					}
				}
					
				if (m_managedMode) {
					logger.debug("Setting validators in 'manage' mode");
				} else {
					logger.debug("Setting validators in 'monitor' mode");
				}
				
				// Check for dynamic replyTo endpoint timeout
				String timeoutValue=config.getAttribute(REPLY_TO_TIMEOUT_ATTR);
				
				if (timeoutValue != null) {
					try {
						long tv=Long.parseLong(timeoutValue);
						
						m_replyToManager.setReplyToTimeout(tv);
					} catch(Exception e) {
						logger.error("Unable to set 'reply to' timeout", e);
					}
				}
			}
			
			updateConfiguration(config, existingValidatorNames,
					existingInputEndpoints, existingOutputEndpoints,
					existingInputDynaReplyTos, existingOutputDynaReplyTos);
			
			// Work through choreography files in the models directory
			java.io.File[] files=m_modelsDir.listFiles();
			
			for (int i=0; i < files.length; i++) {
				
				// Check if a ValidatorModel exists for the
				// file
				try {
					ValidatorConfig vm=
						ValidatorConfigFactory.getValidatorConfig(files[i]);
					
					if (vm != null) {
						org.w3c.dom.Element vConfig=vm.getConfiguration();
						
						if (vConfig != null) {
							updateConfiguration(vConfig,
									existingValidatorNames,
									existingInputEndpoints,
									existingOutputEndpoints,
									existingInputDynaReplyTos,
									existingOutputDynaReplyTos);
						} else {
							logger.error("Failed to obtain configuration for model '"+
									files[i].getName()+"'");
						}
					}
				} catch(Exception e) {
					logger.error("Failed to update configuration for model '"+
							files[i].getName()+"'", e);
				}
			}
			
			// Any remaining input keys need to be removed
			java.util.Iterator<Endpoint> iter=
							existingInputEndpoints.iterator();
			
			while (iter.hasNext()) {
				Endpoint key=iter.next();
				m_inputValidators.remove(key);
			}
			
			iter = existingInputDynaReplyTos.iterator();

			while (iter.hasNext()) {
				Endpoint key=iter.next();
				m_inputDynaReplyTos.remove(key);
			}

			// Any remaining output keys need to be removed
			iter = existingOutputEndpoints.iterator();
			
			while (iter.hasNext()) {
				Endpoint key=iter.next();
				m_outputValidators.remove(key);
			}
			
			iter = existingOutputDynaReplyTos.iterator();

			while (iter.hasNext()) {
				Endpoint key=iter.next();
				m_outputDynaReplyTos.remove(key);
			}

			// Any remaining service description names need to
			// have their associated monitors removed
			java.util.Iterator<ValidatorName> sviter=
				existingValidatorNames.iterator();
			
			while (sviter.hasNext()) {
				ValidatorName svkey=sviter.next();
				
				try {
					logger.debug("Removing service validator: "+svkey);
					removeServiceValidator(svkey);
				} catch(Exception e) {
					logger.error(
							"Failed to remove service validator: "+svkey, e);
				}
			}
		} catch(Exception e) {		
			logger.error("Failed to update configuration from input stream", e);
		}
	}
	
	/**
	 * This method performs the update of the configuration of
	 * service monitors, based on the information in the validator
	 * configuration XML file, and the choreographies defined in the
	 * models folder.
	 * 
	 * @param config The configuration
	 * @param existingValidatorNames The list of validator names
	 * @param existingInputEndpoints The list of input endpoints
	 * @param existingOutputEndpoints The list of output endpoints
	 * @param existingInputDynaReplyTos The set of input dynamic replyTo endpoints
	 * @param existingOutputDynaReplyTos The set of output dynamic replyTo endpoints
	 */
	protected void updateConfiguration(org.w3c.dom.Element config,
			java.util.Set<ValidatorName> existingValidatorNames,
			java.util.Set<Endpoint> existingInputEndpoints,
			java.util.Set<Endpoint> existingOutputEndpoints,
			java.util.Set<Endpoint> existingInputDynaReplyTos,
			java.util.Set<Endpoint> existingOutputDynaReplyTos) {
		logger.debug("Update Service Validator Configuration");
		
		if (config != null) {
			try {				
				if (config.getNodeName().equals(VALIDATOR_NODE)) {
					String active=config.getAttribute(ACTIVE_ATTR);
					
					if (active != null && active.equalsIgnoreCase("true")) {
						
						logger.debug("Setting validator into active mode");
						m_managedMode = true;
					}
				}
				
				org.w3c.dom.NodeList services=config.getElementsByTagName(SERVICE_NODE);
				
				if (logger.isDebugEnabled()) {
					if (services != null) {
						logger.debug("ServiceValidationManager: services="+services.getLength());
					} else {
						logger.debug("ServiceValidationManager: services null");
					}
				}

				for (int i=0; i < services.getLength(); i++) {
		       		ServiceValidator sm=null;
					
					// Create service monitor for service
					String modelName=
						((org.w3c.dom.Element)services.item(i)).getAttribute(MODEL_ATTR);
					String role=
						((org.w3c.dom.Element)services.item(i)).getAttribute(ROLE_ATTR);
					String validate=
						((org.w3c.dom.Element)services.item(i)).getAttribute(VALIDATE_ATTR);

					// Check for old attribute names, for
					// backward compatibility - only support
					// for a limited time
					if (role == null) {
						role = ((org.w3c.dom.Element)services.item(i)).getAttribute("participantType");
					}
					
					if (modelName == null) {
						modelName = ((org.w3c.dom.Element)services.item(i)).getAttribute("cdmFilePath");
						
						if (modelName != null) {
							// Remove path
							int pos=modelName.lastIndexOf('/');
							if (pos != -1) {
								modelName = modelName.substring(pos+1);
							}
						}
					}
					
					if (logger.isDebugEnabled()) {
						logger.debug("Initialize service validator for: model="+
								modelName+" role="+role+" validate="+validate);
					}
					
					if (role != null) {
						boolean f_validate=(validate != null && validate.equalsIgnoreCase("true"));
						
						if (modelName != null ||
								f_validate == false) {
							ValidatorName name=null;
							
							if (f_validate) {
								name = new ValidatorName(modelName, role);
							} else {
								name = new ValidatorName(role);
								
							}
							
							try {
								// Obtain the service container
					       		sm = createServiceValidator(name);
					       		
					       		if (logger.isDebugEnabled()) {
					    			logger.debug("Service validator for '"+modelName+
					       					"' and role '"+role+"' = "+sm);
					       		}
					       		
					       		// Remove validator name from list
					       		existingValidatorNames.remove(sm.getValidatorName());
							} catch(Exception e) {
								logger.error(
										"Failed to create service validator '"+
										name+"'", e);
							}
						} else {
							logger.error("Model name must be specified in validation mode");
						}
					} else {
				   		logger.error("Role must be specified");
					}
					
					if (sm != null) {
						// Map inputs to service
						org.w3c.dom.NodeList inputs=((org.w3c.dom.Element)services.item(i)).getElementsByTagName(INPUT_NODE);
						
						for (int j=0; j < inputs.getLength(); j++) {
							String epr=((org.w3c.dom.Element)inputs.item(j)).getAttribute(EPR_ATTR);
							
							if (epr != null) {
								Endpoint endpoint=new Endpoint(epr);
								
								if (logger.isDebugEnabled()) {
									logger.debug("Storing input endpoint '"+
										endpoint+"' against validator: "+sm);									
								}
								
								// Check if existing list
								java.util.List<ServiceValidator> svs=
											m_inputValidators.get(endpoint);
					
								if (svs == null) {
									svs = new java.util.Vector<ServiceValidator>();
									m_inputValidators.put(endpoint, svs);
								}
								
								if (svs.contains(sm) == false) {
									svs.add(sm);
								}
								
								existingInputEndpoints.remove(endpoint);
								
								// Check if dynamic replyTo
								String dynamicReplyTo=((org.w3c.dom.Element)inputs.item(j)).
												getAttribute(DYNAMIC_REPLY_TO_ATTR);
								
								if (dynamicReplyTo != null && dynamicReplyTo.equalsIgnoreCase("true")) {

									if (logger.isDebugEnabled()) {
										logger.debug("Input endpoint '"+
											endpoint+"' has dynamic replyTo destination");									
									}
									
									m_inputDynaReplyTos.add(endpoint);
									
									existingInputDynaReplyTos.remove(endpoint);
								}		
							}
						}
	
						// Map outputs to service
						org.w3c.dom.NodeList outputs=((org.w3c.dom.Element)services.item(i)).getElementsByTagName(OUTPUT_NODE);
						
						for (int j=0; j < outputs.getLength(); j++) {
							String epr=((org.w3c.dom.Element)outputs.item(j)).getAttribute(EPR_ATTR);
							
							if (epr != null) {
								Endpoint endpoint=new Endpoint(epr);

								if (logger.isDebugEnabled()) {
									logger.debug("Storing output endpoint '"+
										endpoint+"' against validator: "+sm);									
								}
								
								// Check if existing list
								java.util.List<ServiceValidator> svs=
											m_outputValidators.get(endpoint);
					
								if (svs == null) {
									svs = new java.util.Vector<ServiceValidator>();
									m_outputValidators.put(endpoint, svs);
								}
								
								if (svs.contains(sm) == false) {
									svs.add(sm);
								}
								
								existingOutputEndpoints.remove(endpoint);
								
								// Check if dynamic replyTo
								String dynamicReplyTo=((org.w3c.dom.Element)outputs.item(j)).
											getAttribute(DYNAMIC_REPLY_TO_ATTR);
				
								if (dynamicReplyTo != null && dynamicReplyTo.equalsIgnoreCase("true")) {

									if (logger.isDebugEnabled()) {
										logger.debug("Output endpoint '"+
											endpoint+"' has dynamic replyTo destination");									
									}
									
									m_outputDynaReplyTos.add(endpoint);
									
									existingOutputDynaReplyTos.remove(endpoint);
								}		
							}
						}
					}
				}
			} catch(Exception e) {
				logger.error("Failed to load validator config", e);
			}
		}
	}

	/**
	 * This method returns whether the service validator configuration
	 * is in 'manage' mode. If yes, then invalid messages will
	 * be blocked from being delivered to their destination.
	 * 
	 * @return Whether the service validator is in 'manage' mode
	 */
	public boolean isManagedMode() {
		return(m_managedMode);
	}
	
	private static final Logger logger = Logger.getLogger(ServiceValidatorManager.class);

	private static final String EPR_ATTR = "epr";
	private static final String DYNAMIC_REPLY_TO_ATTR = "dynamicReplyTo";

	private static final String OUTPUT_NODE = "output";
	private static final String INPUT_NODE = "input";
	private static final String SERVICE_NODE = "service";
	private static final String ACTIVE_ATTR = "active";
	private static final String MODE_ATTR = "mode";
	private static final String REPLY_TO_TIMEOUT_ATTR = "replyToTimeout";
	private static final String VALIDATOR_NODE = "validator";

	private static final String CONFIG_FILE = "validator-config.xml";
	private static final String MODEL_ATTR="model";
	private static final String ROLE_ATTR="role";
	private static final String VALIDATE_ATTR="validate";
	
	private static ServiceValidatorManager m_instance=null;
	private java.io.File m_validatorConfigFile=null;
	private java.io.File m_modelsDir=null;
	private java.util.Map<ValidatorName,ServiceValidator> m_serviceValidators=
					new java.util.HashMap<ValidatorName,ServiceValidator>();
	private java.util.Map<Endpoint,java.util.List<ServiceValidator>> m_inputValidators=new java.util.Hashtable<Endpoint,java.util.List<ServiceValidator>>();
	private java.util.Map<Endpoint,java.util.List<ServiceValidator>> m_outputValidators=new java.util.Hashtable<Endpoint,java.util.List<ServiceValidator>>();
	private java.util.Set<Endpoint> m_inputDynaReplyTos=new java.util.HashSet<Endpoint>();
	private java.util.Set<Endpoint> m_outputDynaReplyTos=new java.util.HashSet<Endpoint>();
	private boolean m_managedMode=false;
	private DynamicReplyToEndpointManager m_replyToManager=new DynamicReplyToEndpointManager();
	
	/**
	 * This class is responsible for monitoring the models folder,
	 * within the overlord validator ESB bundle, to determine when
	 * the configuration has changed.
	 */
	public class ValidatorConfigChangeMonitor implements java.lang.Runnable {
		
		public ValidatorConfigChangeMonitor() {
			// Do initial check for updates, so monitors
			// initialized before returning from construct,
			// as remainder of checks will be in a separate
			// thread - so we need to ensure that the monitors
			// are configured before the first message is
			// passed through the filter.
			checkForUpdates();
		}
		
		public void run() {
			
			while (true) {
				checkForUpdates();
				
				try {
					synchronized(ValidatorConfigChangeMonitor.this) {
						wait(30000);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		protected void checkForUpdates() {
			logger.debug("Checking for config updates");
			
			// Get last update time
			long lastUpdate=getLastUpdate();
			
			if (lastUpdate > m_lastUpdate) {
				
				updateConfigurations();
				
				m_lastUpdate = lastUpdate;
			}			
		}
		
		protected long getLastUpdate() {
			long ret=0;
			
			if (m_validatorConfigFile != null) {
				ret = m_validatorConfigFile.lastModified();
			}
			
			if (m_modelsDir != null) {
				java.io.File[] files=m_modelsDir.listFiles();
				
				for (int i=0; i < files.length; i++) {
					if (files[i].getName().endsWith(".cdm") &&
							ret < files[i].lastModified()) {
						ret = files[i].lastModified();
					}
				}
				
				if (ret < m_modelsDir.lastModified()) {
					ret = m_modelsDir.lastModified();
				}
			}
			
			return(ret);
		}
		
		private long m_lastUpdate=0;
	}
	
	public class DynamicReplyToEndpointManager extends Thread {
		
		/**
		 * The default constructor
		 */
		public DynamicReplyToEndpointManager() {
			setDaemon(true);
			
			start();
		}
		
		/**
		 * The run method is responsible for ensuring the dynamic
		 * 'reply-to' endpoints are cleaned up periodically.
		 */
		public void run() {
			
			while(true) {
				
				try {
					synchronized(this) {
						wait(m_replyToTimeout);
					}
				} catch(Exception e) {
					logger.error("Failed to wait");
				}
				
				// Shift main entries to an emptied 'pending delete'
				// map
				synchronized(m_inputs) {
					if (logger.isDebugEnabled()) {
						java.util.Iterator<Endpoint> iter=
								m_inputsPendingDelete.keySet().iterator();
						while (iter.hasNext()) {
							Endpoint endpoint=iter.next();
							java.util.List<ServiceValidator> validators=
										m_inputsPendingDelete.get(endpoint);
							logger.debug("Deleting input (reply-to) validators '"+
									endpoint+"': "+validators);
						}
					}
					
					m_inputsPendingDelete.clear();				
					m_inputsPendingDelete.putAll(m_inputs);
					m_inputs.clear();
				}
				
				synchronized(m_outputs) {
					if (logger.isDebugEnabled()) {
						java.util.Iterator<Endpoint> iter=
								m_outputsPendingDelete.keySet().iterator();
						while (iter.hasNext()) {
							Endpoint endpoint=iter.next();
							java.util.List<ServiceValidator> validators=
								m_outputsPendingDelete.get(endpoint);
							logger.debug("Deleting output (reply-to) validators '"+
									endpoint+"': "+validators);
						}
					}

					m_outputsPendingDelete.clear();				
					m_outputsPendingDelete.putAll(m_outputs);
					m_outputs.clear();
				}
			}
		}
		
		/**
		 * This method returns the list of service validators
		 * associated with the dynamic reply-to endpoint.
		 * 
		 * @param endpoint The input endpoint
		 * @return The list of service validators, or null if
		 * 			not found
		 */
		public java.util.List<ServiceValidator> getInputServiceValidators(Endpoint endpoint) {
			java.util.List<ServiceValidator> ret=null;
		
			synchronized(m_inputs) {
				ret = m_inputs.get(endpoint);
				
				if (ret == null) {
					ret = m_inputsPendingDelete.get(endpoint);
				}
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Return input (reply-to) validators '"+endpoint+"': "+ret);
			}
			
			return(ret);
		}

		/**
		 * This method returns the list of service validators
		 * associated with the dynamic reply-to endpoint.
		 * 
		 * @param endpoint The output endpoint
		 * @return The list of service validators, or null if
		 * 			not found
		 */
		public java.util.List<ServiceValidator> getOutputServiceValidators(Endpoint endpoint) {
			java.util.List<ServiceValidator> ret=null;
		
			synchronized(m_outputs) {
				ret = m_outputs.get(endpoint);
				
				if (ret == null) {
					ret = m_outputsPendingDelete.get(endpoint);
				}
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Return output (reply-to) validators '"+endpoint+"': "+ret);
			}
			
			return(ret);
		}

		/**
		 * This method registers a list of service validators against
		 * a dynamic 'reply-to' endpoint.
		 * 
		 * @param endpoint The endpoint
		 * @param validators The list of service validators
		 */
		public void registerInputDynamicReplyTo(Endpoint endpoint,
							java.util.List<ServiceValidator> validators) {

			synchronized(m_inputs) {
				m_inputs.put(endpoint, validators);
				
				// May not be necessary, as entry would not be
				// used - but could save memory?
				m_inputsPendingDelete.remove(endpoint);
			}
		}
		
		/**
		 * This method registers a list of service validators against
		 * a dynamic 'reply-to' endpoint.
		 * 
		 * @param endpoint The endpoint
		 * @param validators The list of service validators
		 */
		public void registerOutputDynamicReplyTo(Endpoint endpoint,
							java.util.List<ServiceValidator> validators) {

			synchronized(m_outputs) {
				m_outputs.put(endpoint, validators);
				
				// May not be necessary, as entry would not be
				// used - but could save memory?
				m_outputsPendingDelete.remove(endpoint);
			}
		}
		
		/**
		 * This method sets the timeout period for purging the
		 * cache of reply-to endpoints.
		 * 
		 * @param timeoutValue The timeout value
		 */
		public void setReplyToTimeout(long timeoutValue) {
			m_replyToTimeout = timeoutValue;
		}
		
		private java.util.Map<Endpoint,java.util.List<ServiceValidator>> m_inputs=
			new java.util.HashMap<Endpoint,java.util.List<ServiceValidator>>();
		private java.util.Map<Endpoint,java.util.List<ServiceValidator>> m_outputs=
			new java.util.HashMap<Endpoint,java.util.List<ServiceValidator>>();
		private java.util.Map<Endpoint,java.util.List<ServiceValidator>> m_inputsPendingDelete=
			new java.util.HashMap<Endpoint,java.util.List<ServiceValidator>>();
		private java.util.Map<Endpoint,java.util.List<ServiceValidator>> m_outputsPendingDelete=
			new java.util.HashMap<Endpoint,java.util.List<ServiceValidator>>();
		private long m_replyToTimeout=10000;
	}
}
