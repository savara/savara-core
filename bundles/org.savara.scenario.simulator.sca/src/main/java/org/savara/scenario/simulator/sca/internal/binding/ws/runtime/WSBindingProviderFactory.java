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

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;
import org.savara.scenario.simulator.sca.internal.MessageStore;
import org.savara.scenario.simulator.sca.internal.ServiceStore;
import org.savara.scenario.simulator.sca.internal.binding.ws.WSBinding;

public class WSBindingProviderFactory implements BindingProviderFactory<WSBinding> {

	private static ServiceStore m_serviceStore=null;
	private static MessageStore m_messageStore=null;
	private static ExtensionPointRegistry m_registry=null;
	
    public WSBindingProviderFactory(ExtensionPointRegistry extensionPoints) {
    	m_registry = extensionPoints;
    }

    public static void setServiceStore(ServiceStore sstore) {
    	m_serviceStore = sstore;
    }
    
    public static void setMessageStore(MessageStore mstore) {
    	m_messageStore = mstore;
    }
    
    public static ExtensionPointRegistry getRegistry() {
    	return(m_registry);
    }
    
    public Class<WSBinding> getModelType() {
        return WSBinding.class;
    }

    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeEndpointReference endpoint) {
    	// SAVARA-234 - currently responses returned as XSD types rather than elements
    	// Tried using WSDL contract to see whether that would help, but causes a type mismatch
    	// on the request
    	//InterfaceContract interfaceContract = endpoint.getGeneratedWSDLContract(endpoint.getComponentReferenceInterfaceContract());
        
    	//InterfaceContract interfaceContract = endpoint.getComponentReferenceInterfaceContract();
        //interfaceContract.getInterface().resetDataBinding(DOMDataBinding.NAME);
        return new WSReferenceBindingProvider(endpoint, m_serviceStore, m_messageStore);
    }

    public ServiceBindingProvider createServiceBindingProvider(RuntimeEndpoint endpoint) {
    	// SAVARA-234 - currently responses returned as XSD types rather than elements
    	// Tried using WSDL contract to see whether that would help, but causes a type mismatch
    	// on the request
    	//InterfaceContract interfaceContract = endpoint.getGeneratedWSDLContract(endpoint.getComponentServiceInterfaceContract());

    	//InterfaceContract interfaceContract = endpoint.getComponentServiceInterfaceContract();
        //interfaceContract.getInterface().resetDataBinding(DOMDataBinding.NAME);
        return new WSServiceBindingProvider(endpoint, m_serviceStore, m_messageStore);
    }

}
