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
package org.savara.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.protocol.model.util.InteractionUtil;
import org.scribble.protocol.monitor.Message;
import org.scribble.protocol.monitor.Result;
import org.scribble.protocol.monitor.Session;
import org.scribble.protocol.monitor.model.MessageNode;
import org.scribble.protocol.monitor.model.MessageType;

/**
 * This class extends the default monitor context, provided by
 * Scribble, to also verify fault names that may be defined.
 *
 */
public class DefautMonitorContext extends org.scribble.protocol.monitor.DefaultMonitorContext {

	private static final Logger LOG=Logger.getLogger(DefautMonitorContext.class.getName());
	
    /**
     * {@inheritDoc}
     */
	@Override
    public Result validate(Session session, MessageNode mesgNode, Message mesg) {
        // Do direct comparison for now, but could also check for derived
        // types
        Result ret=Result.NOT_HANDLED;
        
        if (mesgNode.getOperator() != null
                && mesg.getOperator() != null) {
        	String op=mesg.getOperator();
        	
        	if (mesg instanceof org.savara.monitor.Message
        			&& ((org.savara.monitor.Message)mesg).getFault() != null) {
        		op = InteractionUtil.getOperator(op, ((org.savara.monitor.Message)mesg).getFault());
        	}
        	
            if (mesgNode.getOperator().equals(op)) {
            	ret = Result.VALID;
            }
        }
        
        if (ret.isValid() && mesgNode.getMessageType().size() > 0) {
            
            if (mesgNode.getMessageType().size() == mesg.getTypes().size()) {
                ret = Result.VALID;
                
                // If message type defined on message node, then compare against it
                for (int i=0; i < mesgNode.getMessageType().size(); i++) {
                    MessageType mt=mesgNode.getMessageType().get(i);
                    
                    if (mt.getValue() == null || !mt.getValue().equals(mesg.getTypes().get(i))) {
                        ret = Result.NOT_HANDLED;
                        break;
                    }
                }
            } else if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("Number of message types different ("+mesgNode.getMessageType().size()+" : "
                            +mesg.getTypes().size()+")");
                
                ret = Result.INVALID;
            }
        }
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Session ("+session+") validate message '"+mesg+"' against node "
                        +mesgNode+" ret = "+ret);
        }
        
        return (ret);
	}
}
