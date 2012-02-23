/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.scenario.simulator.protocol.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.monitor.DefaultMonitorContext;
import org.scribble.protocol.monitor.Result;
import org.scribble.protocol.monitor.Session;
import org.scribble.protocol.monitor.model.Annotation;
import org.scribble.protocol.monitor.model.MessageNode;

/**
 * This class extends the default monitor context, provided by
 * Scribble, to also verify fault names that may be defined.
 *
 */
public class MonitorContextImpl extends DefaultMonitorContext {

	private static final Logger LOG=Logger.getLogger(MonitorContextImpl.class.getName());
	
    /**
     * {@inheritDoc}
     */
	@Override
    public Result validate(Session session, MessageNode mesgNode,
    					org.scribble.protocol.monitor.Message mesg) {
        Result ret=super.validate(session, mesgNode, mesg);
        
        // Only check for fault names if the result is currently considered valid
        if (ret != null && ret.isValid() && mesg instanceof Message) {
    		String mesgFaultName=((Message)mesg).getFault();
        	
        	// Check if fault defined for message node
        	String nodeFaultName=null;
        	
    		java.util.List<Annotation> annotations=mesgNode.getAnnotation();
    		
    		for (Annotation ann : annotations) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("Checking annotation ID="+ann.getId()+" VALUE="+ann.getValue());        			
				}
    			if (ann.getValue().startsWith(" "+AnnotationDefinitions.FAULT+"(")) {
    				
    				// Extract name field
    				int startpos=ann.getValue().indexOf("name=");
    				int endpos=ann.getValue().indexOf(")");
    				
    				if (startpos != -1 && endpos != -1) {
    					nodeFaultName = ann.getValue().substring(startpos+5, endpos);
    	    			if (LOG.isLoggable(Level.FINEST)) {
    	    				LOG.finest("Extracted fault name '"+nodeFaultName+
    	    						"' from node annotation");        			
    	    			}
    				}
    				break;
    			}
    		}
    		
    		if (nodeFaultName == null) {
    			nodeFaultName = "";
    		}

			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Node fault name '"+nodeFaultName+"'");        			
			}

        	if (nodeFaultName.length() > 0) {
        		// Check if message has fault defined
    			if (LOG.isLoggable(Level.FINEST)) {
    				LOG.finest("Compare message fault name '"+mesgFaultName+
    						"' against node '"+nodeFaultName+"'");        			
    			}

        		if (mesgFaultName == null) {       			
        			if (LOG.isLoggable(Level.FINEST)) {
        				LOG.finest("Message node had fault '"+nodeFaultName+
        						"', but message had no fault");
        			}
        			ret = Result.INVALID;
        		} else if (!mesgFaultName.equals(nodeFaultName)) {
        			if (LOG.isLoggable(Level.FINEST)) {
        				LOG.finest("Message node had fault '"+nodeFaultName+
        						"' not compatible with message fault '"+mesgFaultName+"'");
        			}
        			ret = Result.INVALID;
        		}
        	} else if (mesgFaultName != null && mesgFaultName.trim().length() > 0) {
    			if (LOG.isLoggable(Level.FINEST)) {
    				LOG.finest("Message had fault '"+mesgFaultName+
    						"', but node had no fault");
    			}
        		ret = Result.INVALID;
        	}
        }
        
        return(ret);
	}
}
