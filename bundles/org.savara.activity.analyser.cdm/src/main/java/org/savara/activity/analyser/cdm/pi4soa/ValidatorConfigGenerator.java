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

import java.util.Collections;
import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pi4soa.cdl.*;
import org.pi4soa.common.annotations.*;
import org.pi4soa.common.xml.XMLUtils;

/**
 * This class provides a generator for the JBossESB Validator
 * configuration.
 */
public class ValidatorConfigGenerator {

	private static final String VALIDATE_ATTR = "validate";
	private static final String ROLE_ATTR = "role";
	private static final String MODEL_ATTR = "model";
	private static final String SERVICE_ELEMENT = "service";
	private static final String VALIDATOR_ELEMENT = "validator";

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: ValidatorConfigGenerator cdmPath");
			System.exit(1);
		}
		
		try {
			java.io.FileInputStream is=new java.io.FileInputStream(args[0]);
			
			org.pi4soa.cdl.Package cdlpack=org.pi4soa.cdl.CDLManager.load(is);
			
			is.close();
			
			ValidatorConfigGenerator gen=new ValidatorConfigGenerator();
			
			org.w3c.dom.Element config=gen.generate(cdlpack, args[0]);
			
			System.out.println("CONFIG:\r\n"+XMLUtils.getText(config, true));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The default constructor.
	 */
	public ValidatorConfigGenerator() {
	}
	
	/**
	 * This method generates the validator configuration associated
	 * with the supplied choreography.
	 * 
	 * @param cdlpack The CDL package
	 * @param model The model (choreography file name)
	 * @return The validator config
	 */
	public org.w3c.dom.Element generate(org.pi4soa.cdl.Package cdlpack,
					String model) {
		org.w3c.dom.Element ret=null;
		
		try {
			ret = createValidatorConfig();
		
			// Create the new entries for the supplied choreography
			if (cdlpack != null) {
				createEntries(ret, model, cdlpack);
			} else {
				logger.error("Choreography not supplied for '"+model+"'");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if (logger.isDebugEnabled()) {
			try {
				logger.debug(XMLUtils.getText(ret, true));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method creates the initial template for the 
	 * validator config.
	 * 
	 * @return The initial validator config
	 * @throws Exception Failed to generate validator config
	 */
	protected org.w3c.dom.Element createValidatorConfig()
							throws Exception {
		org.w3c.dom.Element ret=null;
		
		org.w3c.dom.Document doc=
				javax.xml.parsers.DocumentBuilderFactory.
				newInstance().newDocumentBuilder().newDocument();
		
		ret = doc.createElement(VALIDATOR_ELEMENT);
		
		doc.appendChild(ret);
		
		return(ret);
	}
	
	/**
	 * This method creates the entries in the validator config.
	 * 
	 * @param validator The validator config
	 * @param model The model
	 * @param cdlpack The choreography description
	 */
	protected void createEntries(org.w3c.dom.Element validator,
						String model, org.pi4soa.cdl.Package cdlpack) {
		
		for (int i=0; i < cdlpack.getTypeDefinitions().getParticipantTypes().size(); i++) {
			org.pi4soa.cdl.ParticipantType ptype=
				cdlpack.getTypeDefinitions().getParticipantTypes().get(i);
			
			org.w3c.dom.Element service=
				validator.getOwnerDocument().createElement(SERVICE_ELEMENT);
		
			service.setAttribute(MODEL_ATTR, model);
			service.setAttribute(ROLE_ATTR, ptype.getName());
			service.setAttribute(VALIDATE_ATTR, Boolean.TRUE.toString());
			
			cdlpack.visit(new InputOutputAnalyser(ptype, service));

			if (service.getFirstChild() != null) {
				validator.appendChild(service);
			}
		}
	}
	
	private static Logger logger = Logger.getLogger(ValidatorConfigGenerator.class);

	public class InputOutputAnalyser extends DefaultCDLVisitor {
		
		private static final String DESTINATION_TYPE_ENDPOINT_ADDRESS = "endpoint address";
		private static final String TYPE_ATTR = "type";
		private static final String DYNAMIC_REPLY_TO_ATTR = "dynamicReplyTo";
		private static final String VALIDATOR_ANNOTATION = "validator";
		private static final String VALIDATOR_ELEMENT = "validator";
		private static final String DESTINATION_ELEMENT = "destination";
		private static final String NAME_ATTR = "name";
		private static final String EPR_ATTR = "epr";
		private static final String INPUT_ELEMENT = "input";
		private static final String OUTPUT_ELEMENT = "output";
		
		public InputOutputAnalyser(org.pi4soa.cdl.ParticipantType ptype,
							org.w3c.dom.Element service) {
			m_participantType = ptype;
			m_service = service;
			
			m_templateProcessor =
				org.pi4soa.common.annotations.AnnotationsManagerFactory.getAnnotationsManager().getTemplateProcessor(VALIDATOR_ANNOTATION);
		}
		
		/**
		 * This method inspects the supplied interaction.
		 * 
		 */
		public void interaction(Interaction interaction) {
			if (m_participantType.getRoleTypes().contains(interaction.getFromRoleType()) ||
					(interaction.getFromParticipant() != null &&
						Collections.disjoint(m_participantType.getRoleTypes(),
								interaction.getFromParticipant().getRoleTypes()) == false)) {
				
				for (int i=0; i < interaction.getExchangeDetails().size(); i++) {
					processExchangeDetails(interaction.getExchangeDetails().get(i), true);
				}
			} else if (m_participantType.getRoleTypes().contains(interaction.getToRoleType()) ||
					(interaction.getToParticipant() != null &&
							Collections.disjoint(m_participantType.getRoleTypes(),
									interaction.getToParticipant().getRoleTypes()) == false)) {
				for (int i=0; i < interaction.getExchangeDetails().size(); i++) {
					processExchangeDetails(interaction.getExchangeDetails().get(i), false);
				}
			}
		}
		
		/**
		 * This method checks the supplied exchange details to determine
		 * if there is a 'jbossesb' annotation containing information
		 * about a destination to be validated.
		 * 
		 * @param details The exchange details
		 * @param from Whether the 'from' details should be checked,
		 * 				otherwise the 'to' details will be checked
		 */
		protected void processExchangeDetails(ExchangeDetails details, boolean from) {

			for (int i=0; i < details.getSemanticAnnotations().size(); i++) {
				SemanticAnnotation sa=details.getSemanticAnnotations().get(i);
				org.w3c.dom.Element dest=null;
				
				if (sa.getAnnotation() != null && sa.getName() != null &&
						sa.getName().equals(VALIDATOR_ANNOTATION)) {
					try {
						// Transform the text representation to DOM
						DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
						fact.setNamespaceAware(true);
						
						DocumentBuilder builder=fact.newDocumentBuilder();
						org.w3c.dom.Document doc=
								builder.parse(new java.io.ByteArrayInputStream(
										sa.getAnnotation().getBytes()));
						
						if (doc.getDocumentElement() != null &&
								doc.getDocumentElement().getNodeName().equals(
											VALIDATOR_ELEMENT)) {
							org.w3c.dom.NodeList nl=
								doc.getDocumentElement().getElementsByTagName(DESTINATION_ELEMENT);
							
							if (nl.getLength() == 1) {
								dest = (org.w3c.dom.Element)nl.item(0);
							} else if (nl.getLength() > 1) {
								logger.error("Too many destination elements ("+
										nl.getLength()+") found");
							} else {
								logger.error("No destinations found");
							}
						}
						
					} catch(Exception e) {
						logger.error("Failed to load validator annotation", e);
					}
				}
				
				if (dest != null) {
					processDestination(details, dest, from);
				}
			}
		}
				
		/**
		 * This method processes the destination information
		 * associated with the exchange details.
		 * 
		 * @param details The exchange details
		 * @param dest The destination DOM element
		 * @param from Whether the 'from' or 'to' role should
		 * 					be considered
		 */
		protected void processDestination(ExchangeDetails details,
					org.w3c.dom.Element dest, boolean from) {
			String elemName=null;
			
			if (dest != null) {
				if (from) {
					if (details.getAction() == ExchangeActionType.REQUEST) {
						elemName = OUTPUT_ELEMENT;
					} else if (dest.getAttribute(TYPE_ATTR).trim().length() == 0 ||
							dest.getAttribute(TYPE_ATTR).equals(DESTINATION_TYPE_ENDPOINT_ADDRESS)) {
						elemName = INPUT_ELEMENT;
					}
				} else {
					if (details.getAction() == ExchangeActionType.REQUEST) {
						elemName = INPUT_ELEMENT;
					} else if (dest.getAttribute(TYPE_ATTR).trim().length() == 0 ||
							dest.getAttribute(TYPE_ATTR).equals(DESTINATION_TYPE_ENDPOINT_ADDRESS)) {
						elemName = OUTPUT_ELEMENT;
					}
				}
				
				if (elemName != null) {
					org.w3c.dom.Element elem=m_service.getOwnerDocument().createElement(elemName);
					
					// Parameter has been stored in a structured manner
					// to support use of templates and presentations,
					// so need to extract the value
					java.util.List<TemplateParameter> params=
						m_templateProcessor.getTemplateParameters(dest.getAttribute(NAME_ATTR));
					
					if (params != null && params.size() > 0) {
						elem.setAttribute(EPR_ATTR, params.get(0).getValue());
					}
					
					if (dest.hasAttribute(DYNAMIC_REPLY_TO_ATTR)) {
						params=m_templateProcessor.getTemplateParameters(dest.getAttribute(DYNAMIC_REPLY_TO_ATTR));
					
						if (params != null && params.size() > 0) {
					
							if (params.get(0).getValue().equalsIgnoreCase("true")) {
								elem.setAttribute(DYNAMIC_REPLY_TO_ATTR, "true");
							}
						}
					}
					
					if (elem.hasAttribute(EPR_ATTR)) {
						m_service.appendChild(elem);
					}
				}
			}
		}

		private ParticipantType m_participantType=null;
		private org.w3c.dom.Element m_service=null;
		private org.pi4soa.common.annotations.TemplateProcessor m_templateProcessor=null;
	}
}
