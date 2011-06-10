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
package org.apache.tuscany.sca.binding.ws;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.provider.BaseBindingImpl;
import org.w3c.dom.Element;

public interface WebServiceBinding extends Binding {

    public static final QName TYPE = new QName(BaseBindingImpl.SCA11_NS, "binding.ws");

    public QName getType();

    public InterfaceContract getBindingInterfaceContract();

    public void setBindingInterfaceContract(InterfaceContract bindingInterfaceContract);
    
    /**
     * Returns the generated WSDL definitions document.
     * @return the generated WSDL definitions document
     */
    Definition getGeneratedWSDLDocument();

    /**
     * Sets the generated WSDL definitions document. The WSDL is generated
     * from the component implementation
     * 
     * @param definition the generated WSDL definitions document
     */
    void setGeneratedWSDLDocument(Definition definition);
   
    /**
     * Returns the WSDL definition that was specified by the
     * user either via and interface.wsdl or via a wsdlElement 
     * on the binding. This may be empty if no WSDL was specified
     * explicitly in which case the generated WSDL should contain
     * a full WSDL description
     * 
     * @return the WSDL definition
     */
    WSDLDefinition getUserSpecifiedWSDLDefinition();

    /**
     * Sets the WSDL definition if one was specified by the user in the
     * composite file either via and interface.wsdl or via a wsdlElement 
     * on the binding
     * 
     * @param wsdlDefinition the WSDL definition
     */
    void setUserSpecifiedWSDLDefinition(WSDLDefinition wsdlDefinition);

    /**
     * Returns the WSDL binding.
     * @return the WSDL binding
     */
    javax.wsdl.Binding getBinding();

    /**
     * Sets the WSDL binding
     * @param binding the WSDL binding
     */
    void setBinding(javax.wsdl.Binding binding);
    
    Element getEndPointReference();

    void setEndPointReference(Element element);

    /**
     * Returns the name of the WSDL service.
     *
     * @return the name of the WSDL service
     */
    QName getServiceName();

    /**
     * Sets the name of the WSDL service.
     *
     * @param serviceName the name of the WSDL service
     */
    void setServiceName(QName serviceName);

    /**
     * Returns the name of the WSDL port.
     *
     * @return the name of the WSDL port
     */
    String getPortName();

    /**
     * Sets the name of the WSDL port.
     *
     * @param portName the name of the WSDL port
     */
    void setPortName(String portName);

    /**
     * Returns the WSDL port
     * @return the WSDL port
     */
    Port getPort();

    /**
     * Sets the WSDL port
     * @param port the WSDL port
     */
    void setPort(Port port);

    /**
     * Returns the WSDL service
     * @return the WSDL service
     */
    Service getService();

    /**
     * Sets the WSDL service.
     * @param service the WSDL service
     */
    void setService(Service service);

}
