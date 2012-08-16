/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2010-2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more 
 * details. You should have received a copy of the GNU Lesser General Public 
 * License, v.2.1 along with this distribution; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  
 * 02110-1301, USA.
 */

package org.savara.scenario.simulator.switchyard.binding.soap.deploy;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.savara.scenario.simulator.switchyard.binding.soap.InboundHandler;
import org.savara.scenario.simulator.switchyard.binding.soap.OutboundHandler;
import org.savara.scenario.simulator.switchyard.binding.soap.config.model.SOAPBindingModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.deploy.BaseActivator;
import org.switchyard.deploy.ServiceHandler;


/**
 * SOAP Activator.
 */
public class SOAPActivator extends BaseActivator {
	
	private static final Logger LOG=Logger.getLogger(SOAPActivator.class.getName());

    private static final String SOAP_TYPE = "soap";
    private Configuration _environment;
    
    private InboundHandler _inboundHandler=null;
    private java.util.Map<String,OutboundHandler> _outboundHandler=
    				new java.util.HashMap<String, OutboundHandler>();
    
    /**
     * Creates a new activator for SOAP endpoints.
     */
    public SOAPActivator() {
        super(SOAP_TYPE);
    }
    
    /**
     * This method returns the inbound handler.
     * 
     * @return The inbound handler
     */
    public InboundHandler getInboundHandler() {
    	return (_inboundHandler);
    }
    
    /**
     * This method returns the outbound handler for the
     * specified type.
     * 
     * @param type The type
     * @return The outbound handler, or null if not found
     */
    public OutboundHandler getOutboundHandler(String type) {
    	return (_outboundHandler.get(type));
    }

    /**
     * This method returns the map of service types to outbound
     * handler.
     *  
     * @return The outbound handler map
     */
    public java.util.Map<String,OutboundHandler> getOutboundHandlers() {
    	return (_outboundHandler);
    }
    
    @Override
    public ServiceHandler activateBinding(QName name, BindingModel config) {
        SOAPBindingModel binding = (SOAPBindingModel)config;
        binding.setEnvironment(_environment);
        
        if (binding.isServiceBinding()) {
        	if (_inboundHandler != null) {
        		LOG.severe("Inbound handler already set");
        	}
        	_inboundHandler = new InboundHandler(binding, getServiceDomain());
            return _inboundHandler;
        } else {
        	if (_outboundHandler.containsKey(name.getLocalPart())) {
        		LOG.severe("Outbound handler for '"+name.getLocalPart()+"' already set");
        	}
        	
        	OutboundHandler oh=new OutboundHandler(binding);
            _outboundHandler.put(name.getLocalPart(), oh);
            return oh;
        }
    }
    
    
    
    @Override
    public void deactivateBinding(QName name, ServiceHandler handler) {
        // Nothing to do here
    }

    /**
     * Set the Environment configuration for the activator.
     * @param config The global environment configuration.
     */
    public void setEnvironment(Configuration config) {
        _environment = config;
    }
}
