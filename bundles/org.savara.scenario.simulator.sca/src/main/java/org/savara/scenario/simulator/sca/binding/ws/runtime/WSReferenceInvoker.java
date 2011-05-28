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
package org.savara.scenario.simulator.sca.binding.ws.runtime;

import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;

public class WSReferenceInvoker implements Invoker {
    
    protected Operation operation;
    protected EndpointReference endpoint;

    public WSReferenceInvoker(Operation operation, EndpointReference endpoint) {
        this.operation = operation;
        this.endpoint = endpoint;
    }

    public Message invoke(Message msg) {
        try {

            //return doInvoke(msg);
        	
    		org.apache.tuscany.sca.core.invocation.impl.MessageImpl resp=new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();

    		resp.setOperation(operation);
    		resp.setBody("hello");
    		
    		return(resp);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Message doInvoke(Message msg) {
        // Add some code here to make an invocation over the Sample binding protocol
        // For this sample we'll just get it from the static stash
        WSServiceInvoker fi = WSServiceStore.getService(endpoint.getBinding().getURI());
        return fi.invokeService(msg);
    }
}
