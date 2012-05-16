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
package org.savara.common.model.annotation;

/**
 * This class represents a named annotation with a set of named
 * properties.
 *
 */
public class Annotation implements org.scribble.common.model.Annotation {

	private String m_id=null;
	private String m_name=null;
	private java.util.Map<String,Object> m_properties=
				new java.util.HashMap<String, Object>();
	
	/**
	 * The constructor initialized with the annotation name.
	 * 
	 * @param name The name
	 */
	public Annotation(String name) {
		m_name = name;
	}
	
	/**
	 * The constructor initialized with the optional id and
	 * annotation name.
	 * 
	 * @param id The optional id
	 * @param name The name
	 */
	public Annotation(String id, String name) {
		m_id = id;
		m_name = name;
	}
	
	/**
	 * This is the copy constructor.
	 * 
	 * @param an The annotation to copy
	 */
	public Annotation(Annotation an) {
		m_id = an.m_id;
		m_name = an.m_name;
		m_properties.putAll(an.m_properties);
	}
	
	/**
	 * This method returns the optional id associated with the annotation.
	 * 
	 * @return
	 */
	public String getId() {
		return(m_id);
	}
	
	/**
	 * This method returns the name.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method sets the name.
	 * 
	 * @param name The name
	 */
	protected void setName(String name) {
		m_name = name;
	}
	
	/**
	 * This method returns the properties associated
	 * with this annotation.
	 * 
	 * @return The properties
	 */
	public java.util.Map<String,Object> getProperties() {
		return(m_properties);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		
		if (obj instanceof Annotation) {
			Annotation other=(Annotation)obj;
			
			if (getName().equals(other.getName()) &&
					getProperties().equals(other.getProperties())) {
				return (true);
			}
		}
		
		return (false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return(getName().hashCode());
	}
	
	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		buf.append(" ");
		buf.append(getName());
		
		if (getProperties().size() > 0) {
			buf.append("(");
			
			java.util.Set<String> props=getProperties().keySet();
			boolean f_first=true;
			for (String prop : props) {				
				Object val=getProperties().get(prop);
				if (val != null) {
					if (!f_first) {
						buf.append(",");
					}
					buf.append(prop);
					buf.append("=");
					buf.append(val.toString());
					f_first = false;
				}
			}
			
			buf.append(")");
		}
		buf.append(" ");
		
		return(buf.toString());
	}
}
