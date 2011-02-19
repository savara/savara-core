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
 * This class represents a type definition within
 */
public class TypeDefinition extends ContractObject {
	
	private String m_name=null;	
	private String m_dataType=null;
	private String m_location=null;
	private String m_typeSystem=null;
	
	/**
	 * The default constructor.
	 */
	public TypeDefinition() {
	}
	
	/**
	 * This method returns the name associated with the
	 * type.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method sets the name of the type.
	 * 
	 * @param name The name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * This method returns the data type associated with the
	 * type definition.
	 * 
	 * @return The data type
	 */
	public String getDataType() {
		return(m_dataType);
	}
	
	/**
	 * This method sets the data type of the type definition.
	 * 
	 * @param dataType The data type
	 */
	public void setDataType(String dataType) {
		m_dataType = dataType;
	}
	
	/**
	 * This method returns the location associated with the
	 * type definition.
	 * 
	 * @return The location
	 */
	public String getLocation() {
		return(m_location);
	}
	
	/**
	 * This method sets the location of the type definition.
	 * 
	 * @param location The location
	 */
	public void setLocation(String location) {
		m_location = location;
	}
	
	/**
	 * This method returns the type system associated with the
	 * type definition.
	 * 
	 * @return The type system
	 */
	public String getTypeSystem() {
		return(m_typeSystem);
	}
	
	/**
	 * This method sets the type system associated with the
	 * type definition.
	 * 
	 * @param typeSystem The type system
	 */
	public void setTypeSystem(String typeSystem) {
		m_typeSystem = typeSystem;
	}

	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		for (Annotation ann : getAnnotations()) {
			buf.append("\t[["+ann+"]]\r\n");
		}
		
		buf.append("\tTypeDef "+m_name+" type="+m_dataType+
				" location="+m_location+" typeSystem="+m_typeSystem+"\r\n");
		
		return(buf.toString());
	}
}
