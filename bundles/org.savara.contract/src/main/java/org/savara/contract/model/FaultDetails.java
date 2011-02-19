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
 * This class represents the fault details.
 */
public class FaultDetails extends ContractObject {

	private String m_name=null;
	private java.util.List<Type> m_types=
						new java.util.Vector<Type>();	
	
	/**
	 * The default constructor.
	 */
	public FaultDetails() {
	}
	
	/**
	 * This method returns the name associated with the
	 * fault.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method sets the name of the fault.
	 * 
	 * @param name The name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * This method returns the list of types.
	 * 
	 * @return The list of types
	 */
	public java.util.List<Type> getTypes() {
		return(m_types);
	}
	
	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		buf.append("Fault "+getName()+"( ");
		
		for (Type t : getTypes()) {
			buf.append(t.toString()+" ");
		}
		
		buf.append(")");
		
		return(buf.toString());
	}

}
