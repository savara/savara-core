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
package org.savara.protocol.util;

import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.ModelProperties;
import org.scribble.protocol.model.ProtocolModel;

/**
 * This class defines a set of protocol related utility functions.
 */
public final class ProtocolUtils {
		
	/**
	 * This method calculates the start and end position of a supplied DOM element, within
	 * the supplied text contents, and sets the values on the supplied ModelObject.
	 * 
	 * @param obj The ModelObject to be initialized
	 * @param contents The text contents
	 * @param elem The DOM element to be located in the text
	 */
	public static void setStartAndEndPosition(ModelObject obj, String contents, org.w3c.dom.Element elem) {

		if (contents != null) {
			org.w3c.dom.NodeList nl=elem.getOwnerDocument().getElementsByTagName(elem.getNodeName());
			int elempos=-1;
			
			for (int i=0; elempos == -1 && i < nl.getLength(); i++) {
				if (nl.item(i) == elem) {
					elempos = i;
				}
			}
			
			if (elempos != -1) {
				int startpos=-1;
					
				for (int i=0; i <= elempos; i++) {
					int val1=contents.indexOf("<"+elem.getNodeName()+">", startpos+1);
					int val2=contents.indexOf("<"+elem.getNodeName()+" ", startpos+1);
					
					if (val1 == -1 && val2 != -1) {
						startpos = val2;
					} else if (val1 != -1 && val2 == -1) {
						startpos = val1;
					} else if (val1 == -1 && val2 == -1) {
						// TODO: Error condition
						break;
					} else if (val1 > val2) {
						startpos = val2;
					} else {
						startpos = val1;
					}
				}
				
				if (startpos != -1) {
					//obj.getSource().setStartPosition(startpos);
					obj.getProperties().put(ModelProperties.START_LOCATION, startpos);
					
					// Check if single node
					int p1=contents.indexOf('>', startpos);
					
					if (p1 != -1 && contents.charAt(p1-1) == '/') {
						//obj.getSource().setEndPosition(p1);
						obj.getProperties().put(ModelProperties.END_LOCATION, p1);
					} else {
					
						org.w3c.dom.NodeList enl=elem.getElementsByTagName(elem.getNodeName());
						
						int endpos=startpos;
						String nodetxt="</"+elem.getNodeName()+">";
						
						for (int i=0; endpos != -1 && i <= enl.getLength(); i++) {
							endpos = contents.indexOf(nodetxt, endpos+1);
						}
						
						if (endpos != -1) {
							//obj.getSource().setEndPosition(endpos+nodetxt.length()-1);
							obj.getProperties().put(ModelProperties.END_LOCATION, endpos+nodetxt.length()-1);
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method finds a prefix associated with a supplied namespace using the
	 * type annotation information associated with a protocol model.
	 * 
	 * @param model The model
	 * @param namespace The namespace
	 * @return The prefix, or null if not found
	 */
	public static String getNamespacePrefix(ProtocolModel model, String namespace) {
		Annotation annotation=null;
		
		if (namespace == null || namespace.trim().length() == 0) {
			return("");
		}
		
		if (model != null && model.getProtocol() != null) {
			annotation = AnnotationDefinitions.getAnnotationWithProperty(
					model.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE,
					AnnotationDefinitions.NAMESPACE_PROPERTY, namespace);	
		}
		
		return(annotation == null ? null :
				(String)annotation.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
	}
}
