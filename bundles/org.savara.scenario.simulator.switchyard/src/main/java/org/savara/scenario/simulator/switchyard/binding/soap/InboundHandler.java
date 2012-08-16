/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
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
 
package org.savara.scenario.simulator.switchyard.binding.soap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.wsdl.Port;

import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.ServiceReference;
import org.switchyard.SynchronousInOutHandler;
import org.savara.common.util.XMLUtils;
import org.savara.scenario.simulator.switchyard.binding.soap.config.model.SOAPBindingModel;
import org.savara.scenario.simulator.switchyard.binding.soap.util.WSDLUtil;
import org.switchyard.deploy.BaseServiceHandler;
import org.switchyard.exception.DeliveryException;

/**
 * Inbound handler for binding.
 */
public class InboundHandler extends BaseServiceHandler {

    private static final Logger LOG = Logger.getLogger(InboundHandler.class.getName());

    private final SOAPBindingModel _config;
    
    private ServiceDomain _domain;
    private ServiceReference _service;
    private Port _wsdlPort;

    /**
     * Constructor.
     * @param config the configuration settings
     * @param domain the service domain
     */
    public InboundHandler(final SOAPBindingModel config, ServiceDomain domain) {
        _config = config;
        _domain = domain;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
    	
    	_service = _domain.getServiceReference(_config.getServiceName());
    	
        PortName portName = _config.getPort();
        
    	String wsdlLocation=_config.getWsdl();
    	
    	int index=wsdlLocation.indexOf('#');
    	if (index != -1) {
    		wsdlLocation = wsdlLocation.substring(0, index);
    	}
    	
        try {
        	javax.wsdl.Service wsdlService = WSDLUtil.getService(wsdlLocation,
        						portName);
        	
        	_wsdlPort = WSDLUtil.getPort(wsdlService, portName);
        	
        } catch (Exception e) {
        	LOG.severe("Failed to get WSDL: wsdl="+wsdlLocation);
        }
    }

    @Override
    public void handleFault(Exchange exchange) {
        // TODO: Why is this class an ExchangeHandler?  See SOAPActivator
        throw new IllegalStateException("Unexpected");
    }

    @Override
    public void handleMessage(Exchange exchange) throws HandlerException {
        // TODO: Why is this class an ExchangeHandler?  See SOAPActivator
        throw new IllegalStateException("Unexpected");
    }
    
    /**
     * This method determines if the supplied operation is supplied
     * by the service interface assocated with this inbound handler.
     * 
     * @param op The operation
     * @return
     */
    public boolean isOperation(String op) {
    	return (WSDLUtil.getOperationByName(_wsdlPort, op) != null);
    }

    /**
     * This method invokes the simulated service using the supplied
     * request message details.
     * 
     * @param operation The operation
     * @param mesg The message content
     */
    public String invoke(String operation, String mesg, QName type) {
    	
        try {
            SynchronousInOutHandler inOutHandler = new SynchronousInOutHandler();
            Exchange exchange = _service.createExchange(operation,
            						inOutHandler);
            
            Message req=exchange.createMessage();
            
            org.w3c.dom.Node bodyNode=XMLUtils.getNode(mesg);
            
            req.setContent(new DOMSource(bodyNode));
            
            exchange.send(req);
            
            if (!WSDLUtil.isOneWay(WSDLUtil.getOperationByName(_wsdlPort,
            					operation))) {
	            try {
	                exchange = inOutHandler.waitForOut(12000);
	            } catch (DeliveryException e) {
	            	e.printStackTrace();
	            }
	             
	            Message resp=exchange.getMessage();
            
	            if (resp == null) {
	                LOG.severe("No response received");
	            } else {
	            	return((String)resp.getContent(String.class));
	            }
            }
        } catch (Exception e) {
        	LOG.log(Level.SEVERE, "Failed to invoke", e);
        }
        
        return null;
    }
    
}
