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
package org.apache.tuscany.sca.binding.ws.impl;

import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.provider.BaseBindingImpl;
import org.w3c.dom.Element;

public class WebServiceBindingImpl extends BaseBindingImpl implements WebServiceBinding, org.apache.tuscany.sca.policy.PolicySubject {

    public static final QName TYPE = new QName(BaseBindingImpl.SCA11_NS, "binding.ws");

    private ExtensionType m_extensionType;
    private InterfaceContract bindingInterfaceContract;
    private List<PolicySet> m_policySetList=new java.util.Vector<PolicySet>();
    private List<Intent> m_intents=new java.util.Vector<Intent>();
    private Definition generatedWSDLDocument;
    private WSDLDefinition userSpecifiedWSDLDefinition;
    private Binding binding;
    private Element endPointReference;
    private String portName;
    private QName serviceName;
    private Port port;
    private Service service;

    public WebServiceBindingImpl() {
    }

    public QName getType() {
        return TYPE;
    }
    
	public ExtensionType getExtensionType() {
		return m_extensionType;
	}

    public InterfaceContract getBindingInterfaceContract() {
        return bindingInterfaceContract;
    }

    public void setBindingInterfaceContract(InterfaceContract bindingInterfaceContract) {
        this.bindingInterfaceContract = bindingInterfaceContract;
    }
    
    public Definition getGeneratedWSDLDocument() {
        return generatedWSDLDocument;
    }

    public void setGeneratedWSDLDocument(Definition definition) {
        this.generatedWSDLDocument = definition;
        //determineWSDLCharacteristics();
    }

    public WSDLDefinition getUserSpecifiedWSDLDefinition() {
        if (userSpecifiedWSDLDefinition == null) {
            Interface iface = bindingInterfaceContract.getInterface();
            if (iface instanceof WSDLInterface) {
                userSpecifiedWSDLDefinition = ((WSDLInterface) iface).getWsdlDefinition();
            }
        }
        return userSpecifiedWSDLDefinition;
    }

    public void setUserSpecifiedWSDLDefinition(WSDLDefinition wsdlDefinition) {
        this.userSpecifiedWSDLDefinition = wsdlDefinition;
    }

    public Binding getBinding() {
        if (binding == null) {
            if (getUserSpecifiedWSDLDefinition() != null && userSpecifiedWSDLDefinition.getBinding() != null) {
                binding = userSpecifiedWSDLDefinition.getBinding();
                //determineWSDLCharacteristics();
            }
        }
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
        //determineWSDLCharacteristics();
    }

    public Element getEndPointReference() {
        return endPointReference;
    }

    public void setEndPointReference(Element epr) {
        this.endPointReference = epr;
    }

    public String getPortName() {
        if (isUnresolved()) {
            return portName;
        } else if (port != null) {
            return port.getName();
        } else {
            return null;
        }
    }

    public void setPortName(String portName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.portName = portName;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setServiceName(QName serviceName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.serviceName = serviceName;
    }

    public Service getService() {
        return service;
    }

    public QName getServiceName() {
        if (isUnresolved()) {
            return serviceName;
        } else if (service != null) {
            return service.getQName();
        } else {
            return null;
        }
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

	public List<PolicySet> getPolicySets() {
		return m_policySetList;
	}

	public List<Intent> getRequiredIntents() {
		return m_intents;
	}

	public void setExtensionType(ExtensionType arg0) {
		m_extensionType = arg0;
	}

}
