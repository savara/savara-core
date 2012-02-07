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
package org.savara.protocol.model;

import org.scribble.protocol.model.CustomActivity;

/**
 * This class represents the Fork construct.
 * 
 * NOTE: This class is in experimental status. Once join/fork concept has become
 * stable it will be included in the scribble model.
 */
public class Fork extends CustomActivity {

	private String _label=null;
	
	/**
	 * This method sets the label associated with the fork.
	 * 
	 * @param label The label
	 */
	public void setLabel(String label) {
		_label = label;
	}
	
	/**
	 * This method returns the label associated with the fork.
	 * 
	 * @return The label
	 */
	public String getLabel() {
		return (_label);
	}
}
