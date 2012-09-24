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

import javax.xml.transform.dom.DOMSource;

import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.savara.common.util.XMLUtils;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.simulator.switchyard.binding.soap.config.model.SOAPBindingModel;
import org.savara.scenario.simulator.switchyard.internal.MessageStore;
import org.switchyard.deploy.BaseServiceHandler;

/**
 * Handle outbound invocations.
 */
public class OutboundHandler extends BaseServiceHandler {

    private static final Logger LOG = Logger.getLogger(OutboundHandler.class.getName());

    private MessageStore _messageStore=null;
    private SOAPBindingModel _config=null;
    
    /**
     * Constructor.
     * 
     * @param The binding configuration
     */
    public OutboundHandler(SOAPBindingModel config) {
    	_config = config;
    }
    
    /**
     * This method returns the binding configuration.
     * 
     * @return The binding configuration
     */
    public SOAPBindingModel getConfig() {
    	return (_config);
    }
    
    /**
     * This method sets the message store used by the simulator.
     * 
     * @param ms The message store
     */
    public void setMessageStore(MessageStore ms) {
    	_messageStore = ms;
    }

    /**
     * Start lifecycle.
     */
    public void start() {
    }

    /**
     * Stop lifecycle.
     */
    public void stop() {
    }

    /**
     * The handler method that invokes the actual Webservice when the
     * component is used as a WS consumer.
     * @param exchange the Exchange
     * @throws HandlerException handler exception
     */
    @Override
    public void handleMessage(final Exchange exchange) throws HandlerException {
    	
    	String content=exchange.getMessage().getContent(String.class);
    	
    	String op=exchange.getContract().getProviderOperation().getName();
    
    	if (LOG.isLoggable(Level.FINEST)) {
    		LOG.fine("Handle outbound message: "+content);
    	}
    	
        try {
        	_messageStore.waitForSendEvent(op, content);
        	
        	if (exchange.getContract().getProviderOperation().getExchangePattern()
        					== ExchangePattern.IN_OUT) {
	        	ReceiveEvent respEvent = _messageStore.waitForReceiveEvent(op);
	        	
	        	if (LOG.isLoggable(Level.FINEST)) {
	        		LOG.fine("Response receive event: "+respEvent);
	        	}
	        	
	    		// Associate response with the exchange
	        	Message respMessage=exchange.createMessage();
	        	
	        	if (respEvent != null) {
		        	String resp=_messageStore.getMessageContent(
		        			respEvent.getParameter().get(0).getValue());
		        	
		            org.w3c.dom.Node bodyNode=XMLUtils.getNode((String)resp);
		            
		            respMessage.setContent(new DOMSource(bodyNode));
		        	
		            if (respEvent.getFaultName() != null
		            		&& respEvent.getFaultName().trim().length() > 0) {
			        	exchange.sendFault(respMessage);
		            } else {
			        	exchange.send(respMessage);
		            }
	        	} else {
	        		throw new HandlerException("Expected response, but got no receive event");
	        	}
        	}
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
