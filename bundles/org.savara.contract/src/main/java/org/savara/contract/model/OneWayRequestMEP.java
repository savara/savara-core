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
 * This class represents the one-way message exchange pattern.
 */
public class OneWayRequestMEP extends MessageExchangePattern {

	/**
	 * The default constructor.
	 */
	public OneWayRequestMEP() {
	}
	
	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		for (Annotation ann : getAnnotations()) {
			buf.append("\t\t[["+ann+"]]\r\n");
		}
		
		buf.append("\t\tOneWayRequestMEP "+getOperation()+"( ");
		
		for (Type t : getTypes()) {
			buf.append(t.toString()+" ");
		}
		
		buf.append(")\r\n");
		
		return(buf.toString());
	}
}
