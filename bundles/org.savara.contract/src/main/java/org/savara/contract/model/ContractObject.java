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
 * Generic top level object inherited by all contract objects.
 *
 */
public class ContractObject {

	private java.util.Map<String,Object> m_properties=
				new java.util.HashMap<String, Object>();
	private java.util.List<Annotation> m_annotations=
				new java.util.Vector<Annotation>();

	/**
	 * This method returns the properties associated
	 * with this contract object.
	 * 
	 * @return The properties
	 */
	public java.util.Map<String,Object> getProperties() {
		return(m_properties);
	}
	
	/**
	 * This method returns the list of annotations associated
	 * with the contract object.
	 * 
	 * @return The annotations
	 */
	public java.util.List<Annotation> getAnnotations() {
		return(m_annotations);
	}
}
