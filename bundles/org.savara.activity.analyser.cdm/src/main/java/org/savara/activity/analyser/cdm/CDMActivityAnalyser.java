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
package org.savara.activity.analyser.cdm;

import java.io.Serializable;

import org.savara.activity.ActivityAnalyser;
import org.savara.activity.model.Activity;
import org.savara.activity.model.Analysis;
import org.savara.activity.model.ExchangeType;
import org.savara.activity.model.InteractionActivity;
import org.savara.activity.model.ProtocolAnalysis;
import org.savara.common.util.XMLUtils;

public class CDMActivityAnalyser implements ActivityAnalyser {

	private ServiceValidatorManager m_serviceValidatorManager=new ServiceValidatorManager();

	public void setServiceValidatorManager(ServiceValidatorManager svm) {
		m_serviceValidatorManager = svm;
	}
	
	public void analyse(Activity activity) {
		
		if (activity.getType() instanceof InteractionActivity) {
			InteractionActivity ia=(InteractionActivity)activity.getType();
			java.util.List<ServiceValidator> validators=null;
			Endpoint endpoint=new Endpoint(ia.getDestinationType() != null ?
							ia.getDestinationType() : ia.getDestinationAddress());
			
			if (isOutputValidator(ia)) {
				validators = m_serviceValidatorManager.getOutputServiceValidators(endpoint);
			} else {
				validators = m_serviceValidatorManager.getInputServiceValidators(endpoint);
			}
			
			process(validators, activity, ia);
			
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
		return((ia.getExchangeType() == ExchangeType.UNDEFINED && ia.getOutbound()) ||
					(ia.getExchangeType() != ExchangeType.UNDEFINED &&
						(ia.getExchangeType() == ExchangeType.REQUEST) == ia.getOutbound()));
	}
	
	protected boolean isInputValidator(InteractionActivity ia) {
		return((ia.getExchangeType() == ExchangeType.UNDEFINED && !ia.getOutbound()) ||
					(ia.getExchangeType() != ExchangeType.UNDEFINED &&
						(ia.getExchangeType() == ExchangeType.REQUEST) != ia.getOutbound()));
	}
	
	public void process(java.util.List<ServiceValidator> validators, Activity activity, InteractionActivity ia) {
	
		if (validators != null && validators.size() > 0 && ia.getMessage().size() == 1) {

			for (int i=0; validators != null &&
						i < validators.size(); i++) {
				boolean validated=false;
				java.util.List<org.savara.activity.model.Correlation> correlations=null;
				
		        try {
		        	if (ia.getOutbound()) {
		        		correlations = validators.get(i).messageSent(ia.getMessage().get(0).getType(),
        						(Serializable)ia.getMessage().get(0).getAny(),
        						activity.getCorrelation());
		        	} else {
		        		correlations = validators.get(i).messageReceived(ia.getMessage().get(0).getType(),
		        				(Serializable)ia.getMessage().get(0).getAny(),
        						activity.getCorrelation());
		        	}
		        	validated = true;
		        } catch(Exception t) {
		        	// Ignore
		        }
		        
		        ProtocolAnalysis pa=new ProtocolAnalysis();
		        pa.setProtocol(validators.get(i).getProtocolName());
		        pa.setRole(XMLUtils.getLocalname(validators.get(i).getValidatorName().getRole()));
		        pa.setExpected(validated);
		        
		        Analysis anal=new Analysis();
		        anal.setAny(pa);
		        activity.getAnalysis().add(anal);
		        
		        // Only associate derived correlation information if the activity event
		        // does not already include correlation details
		        if (correlations != null && activity.getCorrelation().size() == 0) {
		        	activity.getCorrelation().addAll(correlations);
		        }
			}
		}
	}
}
