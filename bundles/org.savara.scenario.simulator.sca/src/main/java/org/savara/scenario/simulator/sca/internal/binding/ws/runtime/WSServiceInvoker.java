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
package org.savara.scenario.simulator.sca.internal.binding.ws.runtime;

import java.util.logging.Logger;

import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.impl.TransformationContextImpl;
import org.apache.tuscany.sca.databinding.jaxb.JAXB2Node;
import org.apache.tuscany.sca.databinding.jaxb.Node2JAXB;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.savara.scenario.simulator.sca.internal.ServiceInvoker;

public class WSServiceInvoker implements ServiceInvoker {

	private static Logger logger=Logger.getLogger(WSServiceInvoker.class.getName());
	
    private RuntimeEndpoint wire;
    
    public WSServiceInvoker(RuntimeEndpoint wire) {
        this.wire = wire;
    }

    public RuntimeEndpoint getEndpoint() {
    	return(wire);
    }
    
    /**
     * Send the request down the wire to invoke the service 
     */
    public Message invoke(Message msg) {
        return wire.invoke(msg);
    }
    
	protected Object transformRequestValue(Object source, Operation op, DataType<?> dtype) {
		Object ret=source;
		
		if (source instanceof org.w3c.dom.Node) {
			logger.info("GPB: Transform "+source+" of type "+dtype);
			
			Node2JAXB transformer=new Node2JAXB(WSBindingProviderFactory.getRegistry());
			
			TransformationContext context=new TransformationContextImpl();
			context.setTargetDataType(dtype);
			context.setTargetOperation(op);
			
			ret = transformer.transform((org.w3c.dom.Node)source, context);
			
			logger.info("GPB: INTO "+ret);
		}
		
		return(ret);
	}
	
}
