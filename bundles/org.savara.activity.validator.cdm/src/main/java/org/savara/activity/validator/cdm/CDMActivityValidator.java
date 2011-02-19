/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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

import org.savara.activity.ActivityValidator;
import org.savara.activity.model.Activity;
import org.savara.activity.model.ExchangeType;
import org.savara.activity.model.InteractionActivity;
import org.savara.activity.model.ProtocolAnalysis;
import org.savara.common.util.XMLUtils;

public class CDMActivityValidator implements ActivityValidator {

	private ServiceValidatorManager m_serviceValidatorManager=new ServiceValidatorManager();

	public void setServiceValidatorManager(ServiceValidatorManager svm) {
		m_serviceValidatorManager = svm;
	}
	
	public void validate(Activity activity) {
		
		if (activity instanceof InteractionActivity) {
			InteractionActivity ia=(InteractionActivity)activity;
			java.util.List<ServiceValidator> validators=null;
			Endpoint endpoint=new Endpoint(ia.getDestinationType() != null ?
							ia.getDestinationType() : ia.getDestinationAddress());
			
			if (isOutputValidator(ia)) {
				validators = m_serviceValidatorManager.getOutputServiceValidators(endpoint);
			} else {
				validators = m_serviceValidatorManager.getInputServiceValidators(endpoint);
			}
			
			process(validators, ia);
			
			// Check whether a dynamic reply is expected
			if (isOutputValidator(ia)) {
				if (m_serviceValidatorManager.isOutputDynamicReplyTo(endpoint) &&
						ia.getReplyToAddress() != null) {
					
					// Register interest in the 'reply-to' endpoint
					Endpoint replyTo=new Endpoint(ia.getReplyToAddress());
					
					m_serviceValidatorManager.registerInputReplyToValidators(replyTo,
										validators);
				}
			} else if (m_serviceValidatorManager.isInputDynamicReplyTo(endpoint) &&
						ia.getReplyToAddress() != null) {
					
				// Register interest in the 'reply-to' endpoint
				Endpoint replyTo=new Endpoint(ia.getReplyToAddress());
				
				m_serviceValidatorManager.registerOutputReplyToValidators(replyTo,
									validators);
			}
		}
	}
	
	protected boolean isOutputValidator(InteractionActivity ia) {
		return((ia.getExchangeType() == ExchangeType.UNDEFINED && ia.isOutbound()) ||
					(ia.getExchangeType() != ExchangeType.UNDEFINED &&
						(ia.getExchangeType() == ExchangeType.REQUEST) == ia.isOutbound()));
	}
	
	public void process(java.util.List<ServiceValidator> validators, InteractionActivity ia) {
	
		if (validators != null && validators.size() > 0 && ia.getParameter().size() == 1) {

			for (int i=0; validators != null &&
						i < validators.size(); i++) {
				boolean validated=false;
				java.util.List<org.savara.activity.model.Context> contexts=null;
				
		        try {
		        	if (ia.isOutbound()) {
			        	contexts = validators.get(i).messageSent(ia.getParameter().get(0).getType(),
        						ia.getParameter().get(0).getValue());
		        	} else {
		        		contexts = validators.get(i).messageReceived(ia.getParameter().get(0).getType(),
        						ia.getParameter().get(0).getValue());
		        	}
		        	validated = true;
		        } catch(Exception t) {
		        	// Ignore
		        }
		        
		        ProtocolAnalysis pa=new ProtocolAnalysis();
		        pa.setProtocol(validators.get(i).getProtocolName());
		        pa.setRole(XMLUtils.getLocalname(validators.get(i).getValidatorName().getRole()));
		        pa.setExpected(validated);
		        
		        ia.getAnalysis().add(pa);
		        
		        if (contexts != null) {
		        	ia.getContext().addAll(contexts);
		        }
			}
		}
	}
}
