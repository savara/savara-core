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
package org.savara.contract.model;

import org.savara.common.model.annotation.Annotation;

/**
 * This class represents an interface, as part of a contract.
 *
 */
public class Interface extends ContractObject {

	private String m_name=null;
	private String m_namespace=null;
	private java.util.List<MessageExchangePattern> m_messageExchangePatterns=
			new java.util.Vector<MessageExchangePattern>();

	/**
	 * The default constructor.
	 */
	public Interface() {
	}

	/**
	 * This method returns the name associated with the
	 * interface.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method sets the name of the interface.
	 * 
	 * @param name The name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * This method returns the namespace associated with the
	 * interface.
	 * 
	 * @return The namespace
	 */
	public String getNamespace() {
		return(m_namespace);
	}
	
	/**
	 * This method sets the namespace of the interface.
	 * 
	 * @param namespace The namespace
	 */
	public void setNamespace(String namespace) {
		m_namespace = namespace;
	}
	
	/**
	 * This method returns the list of message exchange patterns.
	 * 
	 * @return The list of message exchange patterns
	 */
	public java.util.List<MessageExchangePattern> getMessageExchangePatterns() {
		return(m_messageExchangePatterns);
	}
	
	/**
	 * This method retrieves an existing message exchange pattern,
	 * associated with the supplied operation name, if one exists.
	 * 
	 * @param op The operation
	 * @return The message exchange pattern, or null if not found
	 */
	public MessageExchangePattern getMessageExchangePatternForOperation(String op) {
		MessageExchangePattern ret=null;
		
		java.util.Iterator<MessageExchangePattern> iter=getMessageExchangePatterns().iterator();
		
		while (ret == null && iter.hasNext()) {
			ret = iter.next();
			
			if (op.equals(ret.getOperation()) == false) {
				ret = null;
			}
		}
		
		return(ret);
	}

	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		for (Annotation ann : getAnnotations()) {
			buf.append("\t[["+ann+"]]\r\n");
		}
		
		buf.append("\tInterface {"+m_namespace+"}"+m_name+" {\r\n");
		
		for (MessageExchangePattern mep : getMessageExchangePatterns()) {
			buf.append(mep.toString());
		}
		
		buf.append("\t}\r\n");
		
		return(buf.toString());
	}
}
