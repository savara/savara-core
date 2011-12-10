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
 * This class represents the Join construct.
 * 
 */
public class Join extends CustomActivity {

	private Role _role=null;
	
	// TODO: Currently just support XOR and AND joins - but need expression support eventually
	private java.util.List<String> _labels=new java.util.Vector<String>();
	private boolean _xor=false;
	
	/**
	 * This method sets the role associated with the join.
	 * 
	 * @param role The role
	 */
	public void setRole(Role role) {
		_role = role;
	}
	
	/**
	 * This method returns the role associated with the join.
	 * 
	 * @return The role
	 */
	public Role getRole() {
		return (_role);
	}
	
	/**
	 * This method returns the list of labels being joined.
	 * 
	 * @return The list of join labels
	 */
	public java.util.List<String> getLabels() {
		return (_labels);
	}
	
	/**
	 * This method sets whether the join is mutually exclusive.
	 * 
	 * @param xor Whether mutually exclusive join
	 */
	public void setXOR(boolean xor) {
		_xor = xor;
	}
	
	/**
	 * This method returns whether the join is mutually exclusive.
	 * 
	 * @return Whether mutually exclusive join
	 */
	public boolean getXOR() {
		return (_xor);
	}
}
