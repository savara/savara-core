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

/**
 * This class represents the message exchange pattern.
 */
public abstract class MessageExchangePattern extends ContractObject {

	private String m_operation=null;
	private java.util.List<Type> m_types=
			new java.util.Vector<Type>();	
	
	/**
	 * The default constructor.
	 */
	public MessageExchangePattern() {
	}
	
	/**
	 * This method returns the optional operation.
	 * 
	 * @return The optional operation
	 */
	public String getOperation() {
		return(m_operation);
	}
	
	/**
	 * This method sets the operation.
	 * 
	 * @param operation The operation
	 */
	public void setOperation(String operation) {
		m_operation = operation;
	}
	
	/**
	 * This method returns the list of types. If
	 * no operation name is defined, then only one type
	 * should be defined.
	 * 
	 * @return The list of types
	 */
	public java.util.List<Type> getTypes() {
		return(m_types);
	}
}
