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

import javax.xml.namespace.QName;

import org.savara.bpel.parser.rules.ParserContext;
import org.scribble.protocol.model.TypeReference;

public class TypeReferenceUtil {

	public static TypeReference createTypeReference(String type, ParserContext context) {
		TypeReference ret=new TypeReference();
		//ret.derivedFrom(this);
		
		// TODO: Need to set the namespace on the TypeReference
		if (type != null) {
			int index=-1;
			
			if (type.charAt(0) == '{') {
				// Assume a qname
				QName qname=QName.valueOf(type);
				
				ret.setName(qname.getLocalPart());
				//ret.setLocalpart(qname.getLocalPart());
				//ret.setNamespace(qname.getNamespaceURI());
				
			} else if ((index=type.indexOf(':')) == -1) {
				ret.setName(type);
				//ret.setLocalpart(type);
				
			} else {
				ret.setName(type.substring(index+1));
				//ret.setLocalpart(type.substring(index+1));
				
				/*
				String prefix=type.substring(0, index);
				String ns=context.getNamespace(prefix);
				
				if (ns != null) {
					ret.setNamespace(ns);
				} else {
					// TODO: Log error
				}
				*/
			}
		}
		
		return(ret);	
	}
	
}
