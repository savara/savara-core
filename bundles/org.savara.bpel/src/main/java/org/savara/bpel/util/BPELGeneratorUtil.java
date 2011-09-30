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

import org.savara.bpel.model.TPartnerLink;
import org.savara.bpel.model.TProcess;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.util.XMLUtils;
import org.savara.contract.model.Contract;
import org.savara.contract.model.Interface;
import org.savara.protocol.contract.generator.ContractGenerator;
import org.savara.protocol.contract.generator.ContractGeneratorFactory;
import org.savara.wsdl.util.WSDLGeneratorUtil;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;

/**
 * This class contains utility functions associated with the BPEL generator.
 */
public class BPELGeneratorUtil {
	
	private static final String USE_PEER2_PEER = "usePeer2Peer";
	private static final String XMLNS_PREFIX = "xmlns:";
	private static final String SERVICE_LABEL = "service";
	private static final String PROVIDE_LABEL = "provide";
	private static final String PARTNER_LINK_LABEL = "partnerLink";
	private static final String INVOKE_LABEL = "invoke";
	private static final String ACTIVE_LABEL = "active";
	private static final String PROCESS_LABEL = "process";
	private static final String DEPLOY_LABEL = "deploy";
	private static final String APACHE_ODE_NAMESPACE = "http://www.apache.org/ode/schemas/dd/2007/03";
	private static final String NAME_LABEL = "name";
	private static final String PROCESS_EVENTS_LABEL = "process-events";
	private static final String PORT_TYPE_LABEL = "portType";
	private static final String PLNK_ROLE = "plnk:role";
	private static final String PLNK_PARTNER_LINK_TYPE = "plnk:partnerLinkType";
	private static final String WSDL_DEFINITIONS = "wsdl:definitions";
	private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
	private static final String PLNKTYPE_NS = "http://docs.oasis-open.org/wsbpel/2.0/plnktype";
	private static final String XMLNS_WSDL = "xmlns:wsdl";
	private static final String XMLNS_PLNK = "xmlns:plnk";	
	private static final String WSDL_IMPORT = "wsdl:import";
	private static final String TARGET_NAMESPACE_LABEL = "targetNamespace";


	/**
	 * This method creates a WSDL document containing the partner link
	 * types.
	 * 
	 * @param model The global protocol model
	 * @param role The role of the process
	 * @param localcm The local protocol model
	 * @param bpelProcess The BPEL process
	 * @param journal The feedback handler
	 * @return The WSDL document containing the partner link types
	 * @throws Exception Failed to generate the partner link types
	 */
	public static org.w3c.dom.Document generatePartnerLinkTypes(ProtocolModel model, Role role,
				ProtocolModel localcm, TProcess bpelProcess, FeedbackHandler journal) throws Exception {	
		org.w3c.dom.Document doc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();		
		org.w3c.dom.Element defn=doc.createElement(WSDL_DEFINITIONS);
		doc.appendChild(defn);
		
		defn.setAttribute(XMLNS_PLNK, PLNKTYPE_NS);
		defn.setAttribute(XMLNS_WSDL, WSDL_NS);
		
		defn.setAttribute(TARGET_NAMESPACE_LABEL, bpelProcess.getTargetNamespace());
		
		// Add import to associated wsdl
		String wsdlName=WSDLGeneratorUtil.getWSDLFileName(role, localcm.getProtocol().getName(), "");

		org.w3c.dom.Element imp=doc.createElement(WSDL_IMPORT);
		
		imp.setAttribute("namespace", bpelProcess.getTargetNamespace());
		imp.setAttribute("location", wsdlName);
		
		defn.appendChild(imp);					

		// Add imports for associated roles
		java.util.ListIterator<Role> roles=localcm.getProtocol().getRoles().listIterator();
		
		while (roles.hasNext()) {
			Role r=roles.next();
			
			ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
			Contract contract=null;
			
			if (cg != null) {
				contract=cg.generate(model.getProtocol(), null, r, journal);
			}
			
			if (contract != null) {
				boolean gen=false;
				
				java.util.Iterator<Interface> iter=contract.getInterfaces().iterator();
				
				while (gen == false && iter.hasNext()) {
					Interface intf=iter.next();
					
					if (intf.getMessageExchangePatterns().size() > 0) {
						gen = true;
					}
				}
				
				if (gen) {
					wsdlName = WSDLGeneratorUtil.getWSDLFileName(r, localcm.getProtocol().getName(), "");
		
					imp = doc.createElement(WSDL_IMPORT);
					
					imp.setAttribute("namespace", contract.getNamespace());
					imp.setAttribute("location", wsdlName);
					
					defn.appendChild(imp);
				}
			}
		}

		// Create partner link types
		java.util.Map<String, String> nsMap=new java.util.HashMap<String, String>();
		
		for (TPartnerLink pl : bpelProcess.getPartnerLinks().getPartnerLink()) {
			org.w3c.dom.Element plt=doc.createElement(PLNK_PARTNER_LINK_TYPE);
			
			plt.setAttribute(NAME_LABEL, pl.getPartnerLinkType().getLocalPart());
			
			if (pl.getPartnerRole() != null && pl.getPartnerRole().trim().length() > 0) {
				org.w3c.dom.Element plRole=doc.createElement(PLNK_ROLE);
				
				plt.appendChild(plRole);
				
				plRole.setAttribute(NAME_LABEL, pl.getPartnerRole());
				
				Role useRole=null;
				
				for (int i=0; useRole == null &&
							i < localcm.getProtocol().getRoles().size(); i++) {
					if (pl.getPartnerRole().startsWith(localcm.getProtocol().getRoles().get(i).getName())) {
						useRole = localcm.getProtocol().getRoles().get(i);
					}
				}
				
				ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
				Contract contract=null;
				
				if (cg != null && useRole != null) {
					contract=cg.generate(model.getProtocol(), null, useRole, journal);
				}
				
				if (contract != null) {
					Interface intf=null;
					
					if (pl.getMyRole() != null) {
						intf = contract.getInterface(pl.getMyRole());
					}
						
					if (intf == null && contract.getInterfaces().size() > 0) {
						intf = contract.getInterfaces().iterator().next();
					}
					
					if (intf != null) {
						String prefix=null;
						String portType=intf.getName();
						
						if (intf.getNamespace() != null) {
							prefix = XMLUtils.getPrefixForNamespace(intf.getNamespace(), nsMap);
							
							portType = prefix+":"+portType;
						}
						
						plRole.setAttribute(PORT_TYPE_LABEL, portType);
					}
				}
			}
			
			if (pl.getMyRole() != null && pl.getMyRole().trim().length() > 0) {
				org.w3c.dom.Element plRole=doc.createElement(PLNK_ROLE);
				
				plt.appendChild(plRole);
				
				plRole.setAttribute(NAME_LABEL, pl.getMyRole());
				
				ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
				Contract contract=null;
				
				if (cg != null) {
					contract=cg.generate(model.getProtocol(), null, role, journal);
				}
				
				if (contract != null) {
					Interface intf=null;
					
					if (pl.getMyRole() != null) {
						intf = contract.getInterface(pl.getMyRole());
					}
						
					if (intf == null && contract.getInterfaces().size() > 0) {
						intf = contract.getInterfaces().iterator().next();
					}
					
					if (intf != null) {
						String prefix=null;
						String portType=intf.getName();
						
						if (intf.getNamespace() != null) {
							prefix = XMLUtils.getPrefixForNamespace(intf.getNamespace(), nsMap);
							
							portType = prefix+":"+portType;
						}
						
						plRole.setAttribute(PORT_TYPE_LABEL, portType);
					}
				}
			}
			
			defn.appendChild(plt);
		}
		
		// Create remaining namespace/prefix mappings
		java.util.Iterator<String> iter=nsMap.keySet().iterator();
		while (iter.hasNext()) {
			String ns=iter.next();
			String prefix=nsMap.get(ns);
			
			defn.setAttribute(XMLNS_PREFIX+prefix, ns);
		}
		
		return (doc);
	}

	/**
	 * This method generates the BPEL deployment descriptor.
	 * 
	 * @param model The global protocol model
	 * @param role The role being generated
	 * @param localcm The local protocol model
	 * @param bpelProcess The BPEL process
	 * @param journal The feedback handler
	 * @return The deployment descriptor
	 * @throws Exception Failed to generate the BPEL deployment descriptor
	 */
	public static org.w3c.dom.Document generateDeploymentDescriptor(ProtocolModel model, Role role, ProtocolModel localcm,
				TProcess bpelProcess, FeedbackHandler journal) throws Exception {	

		org.w3c.dom.Document doc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		org.w3c.dom.Element defn=doc.createElementNS(APACHE_ODE_NAMESPACE,
											DEPLOY_LABEL);
		doc.appendChild(defn);
		
		java.util.Map<String, String> nsMap=new java.util.HashMap<String, String>();
		
		// Create process element
		org.w3c.dom.Element proc=doc.createElement(PROCESS_LABEL);
		defn.appendChild(proc);

		String name=bpelProcess.getName();

		if (bpelProcess.getTargetNamespace() != null) {
			String prefix=XMLUtils.getPrefixForNamespace(bpelProcess.getTargetNamespace(), nsMap);

			name = prefix+":"+name;
		}

		proc.setAttribute(NAME_LABEL, name);
		
		org.w3c.dom.Element active=doc.createElement(ACTIVE_LABEL);
		proc.appendChild(active);
		
		org.w3c.dom.Text activeText=doc.createTextNode(Boolean.TRUE.toString());
		active.appendChild(activeText);
		
		org.w3c.dom.Element processEvents=doc.createElement(PROCESS_EVENTS_LABEL);
		processEvents.setAttribute("generate", "all");
		proc.appendChild(processEvents);
				
		// TODO: Need more info - possibly Contract should have interfaces based on
		// the relationship between two roles, as this may provide the necessary
		// information - but this requires the ability to have multiple interfaces
		// per role, which some other parts of the BPEL generation would currently
		// not handle.
		
		// Work through partner links for now
		for (TPartnerLink pl : bpelProcess.getPartnerLinks().getPartnerLink()) {
			if (pl.getPartnerRole() != null && pl.getPartnerRole().trim().length() > 0) {
				org.w3c.dom.Element invoke=doc.createElement(INVOKE_LABEL);
				
				invoke.setAttribute(PARTNER_LINK_LABEL, XMLUtils.getLocalname(pl.getName()));
				invoke.setAttribute(USE_PEER2_PEER, Boolean.FALSE.toString());
				
				org.w3c.dom.Element service=doc.createElement(SERVICE_LABEL);
				
				invoke.appendChild(service);
				
				proc.appendChild(invoke);
			}
		
			if (pl.getMyRole() != null && pl.getMyRole().trim().length() > 0) {
				org.w3c.dom.Element provide=doc.createElement(PROVIDE_LABEL);
				
				provide.setAttribute(PARTNER_LINK_LABEL, XMLUtils.getLocalname(pl.getName()));
				
				org.w3c.dom.Element service=doc.createElement(SERVICE_LABEL);
				
				provide.appendChild(service);
				
				proc.appendChild(provide);
			}
		}
		
		// Create remaining namespace/prefix mappings
		java.util.Iterator<String> iter=nsMap.keySet().iterator();
		while (iter.hasNext()) {
			String ns=iter.next();
			String prefix=nsMap.get(ns);
			
			defn.setAttribute(XMLNS_PREFIX+prefix, ns);
		}
		
		return (doc);
	}

}
