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
package org.savara.scenario.util;

import java.util.logging.Logger;

import org.savara.common.util.XMLUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class MessageUtil {
	
	private static Logger logger=Logger.getLogger(MessageUtil.class.getName());

	public static boolean isValid(String paramValue, Object mesgValue) {
		boolean ret=false;
		
		if (mesgValue instanceof org.w3c.dom.Node) {			
			try {
				logger.info("Validating value="+XMLUtils.toText((org.w3c.dom.Node)mesgValue));

				org.w3c.dom.Node paramValueNode=XMLUtils.getNode(paramValue);
				
				if (paramValueNode != null) {
					ret = isValid(paramValueNode, (org.w3c.dom.Node)mesgValue);
				} else {
					logger.severe("Parameter value is not XML");
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else if (mesgValue instanceof String) {
			ret = paramValue.equals(mesgValue);
		}

		return(ret);
	}
	
	public static boolean isValid(org.w3c.dom.Node paramValue, org.w3c.dom.Node mesgValue) {
		boolean ret=false;
		
		if (paramValue instanceof org.w3c.dom.Attr && mesgValue instanceof org.w3c.dom.Attr) {
			ret = ((org.w3c.dom.Attr)paramValue).getValue().equals(
							((org.w3c.dom.Attr)mesgValue).getValue());
			
			logger.info("isValid Attribute: "+((org.w3c.dom.Attr)paramValue).getValue()+" = "+
					((org.w3c.dom.Attr)mesgValue).getValue()+" ? "+ret);
			
		} else if (paramValue instanceof org.w3c.dom.Element &&
					mesgValue instanceof org.w3c.dom.Element) {
			org.w3c.dom.Element paramElem=(org.w3c.dom.Element)paramValue;
			org.w3c.dom.Element mesgElem=(org.w3c.dom.Element)mesgValue;
			
			ret = true;
			
			// Iterate through parameter element to check that all its attributes are present
			// in the message value, with the same values
			NamedNodeMap map=paramElem.getAttributes();
			
			for (int i=0; ret && i < map.getLength(); i++) {
				org.w3c.dom.Attr paramAttr=(org.w3c.dom.Attr)map.item(i);
				
				// Check attribute is not a namespace definition
				if (!paramAttr.getName().startsWith("xmlns:") &&
						!paramAttr.getName().endsWith(":schemaLocation")) { // Should really check prefix
					org.w3c.dom.Attr mesgAttr=mesgElem.getAttributeNode(paramAttr.getName());
					
					if (mesgAttr == null) {
						logger.info("No attribute for '"+paramAttr.getName()+"'");
						ret = false;
					} else if (!isValid(paramAttr, mesgAttr)) {
						logger.info("Attribute '"+paramAttr.getName()+"' has different values");
						ret = false;
					}
				}
			}
			
			// Check the elements
			java.util.List<org.w3c.dom.Element> paramnl=getChildElements(paramElem);
			java.util.List<org.w3c.dom.Element> mesgnl=getChildElements(mesgElem);
			
			for (int i=0; ret == true && i < paramnl.size(); i++) {
				org.w3c.dom.Element pn=paramnl.get(i);
				
				boolean matched=false;
				
				for (int j=0; matched == false && j < mesgnl.size(); j++) {
					org.w3c.dom.Element mn=mesgnl.get(j);
					
					if (pn.getNodeName().equals(mn.getNodeName()) &&
									isValid(pn, mn)) {
						matched = true;
						
						mesgnl.remove(j);
						
						break;
					}
				}
				
				if (matched == false) {
					ret = false;
				}
			}
			
			if (mesgnl.size() > 0) {
				ret = false;
			}
		}
		
		return(ret);
	}
	
	protected static java.util.List<org.w3c.dom.Element> getChildElements(org.w3c.dom.Element elem) {
		java.util.List<org.w3c.dom.Element> ret=new java.util.Vector<org.w3c.dom.Element>();
		NodeList nl=elem.getChildNodes();
		
		for (int i=0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof org.w3c.dom.Element)
			ret.add((org.w3c.dom.Element)nl.item(i));
		}
		
		return(ret);
	}
}
