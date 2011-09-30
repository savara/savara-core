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
package org.savara.switchyard.bpel.generator;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.savara.common.util.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SwitchyardBPELGenerator {
	
	private static final Logger logger=Logger.getLogger(SwitchyardBPELGenerator.class.getName());

	public SwitchyardBPELGenerator() {
	}
	
	public org.w3c.dom.Element createSwitchyardDescriptor(String name,
					org.w3c.dom.Element descriptor, java.util.Map<String,javax.wsdl.Definition> wsdls)
										throws Exception {
		org.w3c.dom.Element ret=null;
		
		StringBuffer composite=new StringBuffer();
		
		composite.append("<switchyard xmlns=\"urn:switchyard-config:switchyard:1.0\"\r\n");
		composite.append("\t\txmlns:swyd=\"urn:switchyard-config:switchyard:1.0\"\r\n");
		composite.append("\t\txmlns:trfm=\"urn:switchyard-config:transform:1.0\"\r\n");
		composite.append("\t\txmlns:bean=\"urn:switchyard-component-bean:config:1.0\"\r\n");
		composite.append("\t\txmlns:soap=\"urn:switchyard-component-soap:config:1.0\"\r\n");
		composite.append("\t\txmlns:bpel=\"http://docs.oasis-open.org/ns/opencsa/sca/200903\"\r\n");
		composite.append("\t\txmlns:sca=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\"\r\n");
		
		NamedNodeMap attrs=descriptor.getAttributes();
		
		// Initialize namespaces
		java.util.Map<String, String> namespaces=new java.util.HashMap<String,String>();
		
		for (int i=0; i < attrs.getLength(); i++) {
			Attr attr=(Attr)attrs.item(i);
			if (attr.getName().startsWith("xmlns:")) {
				namespaces.put(attr.getName().substring(6), attr.getValue());
				composite.append("\t\t"+attr.getName()+"=\""+attr.getValue()+"\"\r\n");
			}
		}
		
		composite.append("\t\ttargetNamespace=\""+descriptor.getAttribute("xmlns")+"\"\r\n");
		composite.append("\t\tname=\""+name+"\">\r\n");
		
		composite.append("\t<sca:composite name=\""+name+
				"\" targetNamespace=\""+descriptor.getAttribute("xmlns")+"\">\r\n");
		
		NodeList nl=descriptor.getChildNodes();
		int port=18001;
		
		for (int i=0; i < nl.getLength(); i++) {
			Node n=nl.item(i);
			
			if (n instanceof Element && XMLUtils.getLocalname(n.getNodeName()).equals("process")) {
				Element process=(Element)n;
				
				composite.append(generateServices(process, wsdls, namespaces, port++));
				
				composite.append(generateComponent(process, wsdls, namespaces));
				
				// TODO: Generate references for any 'invoke' entry that is not
				// satisfied by a service (process) provided by this deployment
				// descriptor
			}
		}
		
		composite.append("\t</sca:composite>\r\n");

		composite.append("</switchyard>\r\n");

		ret = (org.w3c.dom.Element)XMLUtils.getNode(composite.toString());
		
		return(ret);
	}
	
	/**
	 * This method generates a service element.
	 * 
	 * @param process The process details
	 * @param wsdls The set of wsdls
	 * @param namespaces The namespaces
	 * @param port The port number
	 * @return The service element
	 */
	protected String generateServices(Element process,
					java.util.Map<String,javax.wsdl.Definition> wsdls,
					java.util.Map<String, String> namespaces, int port) {
		StringBuffer ret=new StringBuffer();
		
		String name=getComponentName(process);
		
		ret.append("\t\t<sca:service name=\""+name+"\" promote=\""+name+"\">\r\n");
		ret.append("\t\t\t<soap:binding.soap>\r\n");

		NodeList nl=process.getElementsByTagName("provide");
		
		for (int i=0; i < nl.getLength(); i++) {
			Node n=nl.item(i);
			
			if (n instanceof Element) {
				NodeList services=((Element)n).getElementsByTagName("service");
				
				if (services.getLength() == 1 && services.item(0) instanceof Element) {
					Element service=(Element)services.item(0);
					
					String wsdlInterfacePath=getInterfacePath(service, wsdls, namespaces);
					
					ret.append("\t\t\t\t<soap:wsdl>"+wsdlInterfacePath+"</soap:wsdl>\r\n");
				}
			}
		}
		
		ret.append("\t\t\t\t<soap:serverPort>"+port+"</soap:serverPort>\r\n");
		ret.append("\t\t\t</soap:binding.soap>\r\n");
		
		ret.append("\t\t</sca:service>\r\n");
		
		return(ret.toString());
	}
	
	/**
	 * This method generates the component element.
	 * 
	 * @param process The process details
	 * @param wsdls The wsdls
	 * @param namespaces The namespaces
	 * @return The component element
	 */
	protected String generateComponent(Element process,
			java.util.Map<String,javax.wsdl.Definition> wsdls,
			java.util.Map<String, String> namespaces) {
		StringBuffer ret=new StringBuffer();
		
		ret.append("\t\t<sca:component name=\""+getComponentName(process)+"\">\r\n");
		
		ret.append("\t\t\t<bpel:implementation.bpel process=\""+process.getAttribute("name")+"\" />\r\n");
		
		NodeList nl=process.getElementsByTagName("provide");
		
		for (int i=0; i < nl.getLength(); i++) {
			Node n=nl.item(i);
			
			if (n instanceof Element) {
				NodeList services=((Element)n).getElementsByTagName("service");
				
				if (services.getLength() == 1 && services.item(0) instanceof Element) {
					Element service=(Element)services.item(0);
					
					// TODO: Need to access wsdl details for the service

					String wsdlInterface=getInterfaceDetails(service, wsdls, namespaces);
					
					ret.append("\t\t\t<sca:service name=\""+getServiceName(service)+"\" >\r\n");
					ret.append("\t\t\t\t<sca:interface.wsdl interface=\""+wsdlInterface+"\"/>\r\n");
					ret.append("\t\t\t</sca:service>\r\n");
				}
			}
		}
		
		nl = process.getElementsByTagName("invoke");
		
		for (int i=0; i < nl.getLength(); i++) {
			Node n=nl.item(i);
			
			if (n instanceof Element) {
				NodeList services=((Element)n).getElementsByTagName("service");
				
				if (services.getLength() == 1 && services.item(0) instanceof Element) {
					Element service=(Element)services.item(0);
					
					// TODO: Need to access wsdl details for the service

					String wsdlInterface=getInterfaceDetails(service, wsdls, namespaces);
					
					ret.append("\t\t\t<sca:reference name=\""+getServiceName(service)+"\" >\r\n");
					ret.append("\t\t\t\t<sca:interface.wsdl interface=\""+wsdlInterface+"\"/>\r\n");
					ret.append("\t\t\t</sca:reference>\r\n");
				}
			}
		}
		
		ret.append("\t\t</sca:component>\r\n");
		
		return(ret.toString());
	}
	
	/**
	 * The interface details associated with a wsdl interface.
	 * 
	 * @param service The service element
	 * @param wsdls The wsdls
	 * @param namespaces The namespaces
	 * @return The interface details
	 */
	protected String getInterfaceDetails(Element service,
			java.util.Map<String,javax.wsdl.Definition> wsdls,
			java.util.Map<String, String> namespaces) {
		String ret=null;
		
		// Identify the namespace and local part
		String namespace=null;
		String localPart=service.getAttribute("name");
		int index=localPart.indexOf(':');
		
		if (index != -1) {
			namespace = namespaces.get(localPart.substring(0, index));
			localPart = localPart.substring(index+1);
		}
		
		QName qname=new QName(namespace, localPart);
		
		for (String path : wsdls.keySet()) {
			javax.wsdl.Definition wsdl=wsdls.get(path);
			javax.wsdl.Service serv=wsdl.getService(qname);
			if (serv != null) {
				// Use port type associated with first port found
				javax.wsdl.Port port=(javax.wsdl.Port)
							serv.getPorts().values().iterator().next();
				
				if (port != null) {
					String portType=port.getBinding().getPortType().getQName().getLocalPart();
					ret = path+"#wsdl.porttype("+portType+")";
				} else {
					logger.severe("Unable to find port type associated with service '"+qname+"'");
				}
				break;
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the path to the wsdl definition associated
	 * with the supplied service.
	 * 
	 * @param service The service
	 * @param wsdls The wsdls
	 * @param namespaces The namespaces
	 * @return The wsdl definition path
	 */
	protected String getInterfacePath(Element service,
			java.util.Map<String,javax.wsdl.Definition> wsdls,
			java.util.Map<String, String> namespaces) {
		String ret=null;
		
		// Identify the namespace and local part
		String namespace=null;
		String localPart=service.getAttribute("name");
		int index=localPart.indexOf(':');
		
		if (index != -1) {
			namespace = namespaces.get(localPart.substring(0, index));
			localPart = localPart.substring(index+1);
		}
		
		QName qname=new QName(namespace, localPart);
		
		for (String path : wsdls.keySet()) {
			javax.wsdl.Definition wsdl=wsdls.get(path);
			javax.wsdl.Service serv=wsdl.getService(qname);
			if (serv != null) {
				ret = path;
				break;
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the name of the component associated with the
	 * BPEL process.
	 * 
	 * @param process The BPEL process
	 * @return The component name
	 */
	protected String getComponentName(Element process) {
		String ret=process.getAttribute("name");
		int index=ret.indexOf(':');
		
		if (index != -1) {
			ret = ret.substring(index+1);
		}
		return (ret);
	}
	
	/**
	 * This method returns the name of the service associated with the
	 * BPEL process.
	 * 
	 * @param process The BPEL process
	 * @return The service name
	 */
	protected String getServiceName(Element service) {
		String ret=service.getAttribute("name");
		int index=ret.indexOf(':');
		
		if (index != -1) {
			ret = ret.substring(index+1);
		}
		return (ret);
	}
}
