/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.util;

import java.io.IOException;

import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.savara.bpel.model.TActivity;
import org.savara.bpel.model.TExtensibleElements;
import org.savara.bpel.model.TImport;
import org.savara.bpel.model.TInvoke;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.model.TScope;
import org.savara.bpel.model.TSequence;
import org.savara.common.util.XMLUtils;
import org.savara.common.task.ResourceLocator;

/**
 * This class contains utility functions for dealing with Interactions.
 */
public class BPELInteractionUtil {
	
	private static Log logger = LogFactory.getLog(BPELInteractionUtil.class);

	/**
	 * This method determines whether the supplied activity
	 * is either an invoke, or a sequence that has an
	 * invoke as its first element.
	 * 
	 * @param act The activity
	 * @return The invoke, or null if not found
	 */
	public static TInvoke getInvoke(TActivity act) {
		TInvoke ret=null;
		
		if (act instanceof TInvoke) {
			ret = (TInvoke)act;
		} else if (act instanceof TSequence &&
				((TSequence)act).getActivity().size() > 0 &&
				((TSequence)act).getActivity().get(0) instanceof TInvoke) {
			ret = (TInvoke)((TSequence)act).getActivity().get(0);
		}
		
		return(ret);
	}
	
	public static int countInvokes(TExtensibleElements scope) {
		org.w3c.dom.Element elem=null;
		
		try {
			elem = convertScopeToDOM(scope);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		int ret=0;
		
		if (elem != null) {
			ret = countInvokes(elem);
		}
		
		return(ret);
	}
	
	/**
	 * This method recursively counts the number of 'invoke' activities
	 * contained within a DOM element.
	 * 
	 * @param elem The current element
	 * @return The number of invokes contained within the element
	 */
	public static int countInvokes(org.w3c.dom.Element elem) {
		int ret=0;
		
		if (elem.getLocalName().equals("invoke")) {
			ret = 1;
		} else {
			org.w3c.dom.NodeList nl=elem.getChildNodes();
			
			for (int i=0; i < nl.getLength(); i++) {
				if (nl.item(i) instanceof org.w3c.dom.Element) {
					ret += countInvokes((org.w3c.dom.Element)nl.item(i));
				}
			}
		}
		
		return(ret);
	}	
	
	protected static org.w3c.dom.Element convertScopeToDOM(TExtensibleElements desc) throws IOException {
		org.w3c.dom.Element ret=null;
		 
		try {
			org.savara.bpel.model.ObjectFactory factory=
						new org.savara.bpel.model.ObjectFactory();
			
			JAXBContext context = JAXBContext.newInstance(desc.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
			
			if (desc instanceof TProcess) {
				marshaller.marshal(factory.createProcess((TProcess)desc), baos);
			} else if (desc instanceof TScope) {
				marshaller.marshal(factory.createScope((TScope)desc), baos);				
			}
			
			baos.close();
			
			ret = (org.w3c.dom.Element)XMLUtils.getNode(baos.toString());
			
		} catch(Exception e) {
			throw new IOException("Failed to serialize scope", e);
		}
		
		return(ret);
	}
	
	/**
	 * This method searches the defined WSDL definitions to identify
	 * the underlying XML element/type associated with the supplied
	 * WSDL message type.
	 * 
	 * @param wsdlMessageType The WSDL message type
	 * @param locator The resource locator
	 * @return The underlying XML element/type, or null if not found
	 */
	public static String getXMLType(TProcess process, QName wsdlMessageType, ResourceLocator locator) {
		String ret=null;
		
		String namespace=wsdlMessageType.getNamespaceURI();
		
		for (TImport imp : process.getImport()) {
			
			// Check if import relates to the correct namespace
			if (imp.getNamespace() != null &&
					imp.getNamespace().equals(namespace) &&
					imp.getLocation() != null &&
					imp.getLocation().endsWith(".wsdl")) {
				
				try {
					java.net.URI uri=
							locator.getResourceURI(imp.getLocation());
	
					if (uri != null) {
						ret = getXMLType(wsdlMessageType, uri);
						
						if (ret != null) {
							break;
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return(ret);
	}

	public static String getXMLType(QName wsdlMessageType, java.net.URI uri) {
		String ret=null;
		
		try {
			WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
			
			javax.wsdl.Definition defn=reader.readWSDL(uri.toString());
			
			if (defn != null) {
				javax.wsdl.Message mesg=defn.getMessage(wsdlMessageType);
				
				if (mesg != null && mesg.getParts().size() == 1) {
					javax.wsdl.Part part=(javax.wsdl.Part)
								mesg.getParts().values().iterator().next();
					
					if (part.getElementName() != null) {
						ret = part.getElementName().toString();
					} else if (part.getTypeName() != null) {
						ret = part.getTypeName().toString();
					}
				}
			}
			
		} catch(Exception e) {
			logger.error("Failed to read WSDL", e);
		}
		
		return(ret);
	}
	
}
