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
import org.scribble.protocol.model.Role;

/**
 * This class represents the Sync construct.
 * 
 */
public class Sync extends CustomActivity {

	private Role _role=null;
	private String _label=null;
	
	/**
	 * This method sets the role associated with the sync.
	 * 
	 * @param role The role
	 */
	public void setRole(Role role) {
		_role = role;
	}
	
	/**
	 * This method returns the role associated with the sync.
	 * 
	 * @return The role
	 */
	public Role getRole() {
		return (_role);
	}
	
	/**
	 * This method sets the label associated with the sync.
	 * 
	 * @param label The label
	 */
	public void setLabel(String label) {
		_label = label;
	}
	
	/**
	 * This method returns the label associated with the sync.
	 * 
	 * @return The label
	 */
	public String getLabel() {
		return (_label);
	}
}
