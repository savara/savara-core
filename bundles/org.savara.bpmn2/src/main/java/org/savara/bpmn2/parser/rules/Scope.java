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
package org.savara.bpmn2.parser.rules;

import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.model.TFlowNode;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Parallel;
import org.scribble.protocol.model.Role;

public class Scope {
	
	private TDefinitions m_definitions=null;
	private Scope m_parent=null;
	private java.util.Map<String,Object> m_elements=new java.util.HashMap<String,Object>();
	private java.util.Map<String,Role> _roles=new java.util.HashMap<String,Role>();
	private java.util.Map<TFlowNode,Block> _joinBlocks=new java.util.HashMap<TFlowNode,Block>();
	private java.util.List<Parallel> _parallelReviewList=new java.util.Vector<Parallel>();

	public Scope(TDefinitions defns) {
		m_definitions = defns;
	}
	
	public Scope(Scope parent) {
		m_parent = parent;
	}
	
	/**
	 * This method returns the top level definitions associated with
	 * the BPMN2 model.
	 * 
	 * @return The definitions
	 */
	public TDefinitions getDefinitions() {
		if (m_parent != null) {
			return(m_parent.getDefinitions());
		}
		return(m_definitions);
	}
	
	/**
	 * This method returns the parent scope.
	 * 
	 * @return The parent scope
	 */
	public Scope getParent() {
		return(m_parent);
	}
	
	/**
	 * This method returns the object associated with the supplied id.
	 * 
	 * @param id The id
	 * @return The BPMN2 element, or null if not found
	 */
	public Object getBPMN2Element(String id) {
		if (m_elements.containsKey(id)) {
			return(m_elements.get(id));
		} else if (m_parent != null) {
			return(m_parent.getBPMN2Element(id));
		}
		return(null);
	}
	
	/**
	 * Register relevant information from the supplied object in
	 * the parser scope.
	 * 
	 * @param obj The object
	 */
	public void register(String id, Object obj) {
		m_elements.put(id, obj);
	}
	
	/**
	 * This method returns the role associated with the supplied
	 * name.
	 * 
	 * @param name The name
	 * @return The role, or null if not found
	 */
	public Role getRole(String name) {
		return(_roles.get(name));
	}
	
	public void registerRole(Role r) {
		_roles.put(r.getName(), r);
	}
	
	public java.util.Map<TFlowNode, Block> getJoinBlocks() {
		return (_joinBlocks);
	}
	
	public java.util.List<Parallel> getParallelReviewList() {
		return (_parallelReviewList);
	}
}
