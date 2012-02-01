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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * This class represents the contract.
 */
public class Contract extends ContractObject {
	
	private String m_name=null;
	private String m_namespace=null;
	private java.util.Set<Interface> m_interfaces=
						new java.util.HashSet<Interface>();
	private java.util.Set<Namespace> m_namespaces=
			new java.util.HashSet<Namespace>();
	private java.util.Set<TypeDefinition> m_typeDefinitions=
		new java.util.HashSet<TypeDefinition>();
	
	/**
	 * The default constructor.
	 */
	public Contract() {
	}
	
	/**
	 * This method returns the name associated with the
	 * contract.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method sets the name of the contract.
	 * 
	 * @param name The name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * This method returns the namespace associated with the
	 * contract.
	 * 
	 * @return The namespace
	 */
	public String getNamespace() {
		return(m_namespace);
	}
	
	/**
	 * This method sets the namespace of the contract.
	 * 
	 * @param namespace The namespace
	 */
	public void setNamespace(String namespace) {
		m_namespace = namespace;
	}
	
	/**
	 * This method returns the list of interfaces.
	 * 
	 * @return The list of interfaces
	 */
	public java.util.Set<Interface> getInterfaces() {
		return(m_interfaces);
	}
	
	/**
	 * This method returns the list of type definitions.
	 * 
	 * @return The list of type definitions
	 */
	public java.util.Set<TypeDefinition> getTypeDefinitions() {
		return(m_typeDefinitions);
	}
	
	/**
	 * This method returns the type definition associated
	 * with the supplied name.
	 * 
	 * @param name The type definition name
	 * @return The type definition, or null if not found.
	 */
	public TypeDefinition getTypeDefinition(String name) {
		TypeDefinition ret=null;
		
		java.util.Iterator<TypeDefinition> iter=getTypeDefinitions().iterator();
		while (ret == null && iter.hasNext()) {
			ret = iter.next();
			
			if (ret.getName().equals(name) == false) {
				ret = null;
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the interface associated
	 * with the supplied name.
	 * 
	 * @oaram namespace The optional namespace
	 * @param name The interface name
	 * @return The interface, or null if not found.
	 */
	public Interface getInterface(String namespace, String name) {
		Interface ret=null;
		
		java.util.Iterator<Interface> iter=getInterfaces().iterator();
		while (ret == null && iter.hasNext()) {
			ret = iter.next();
			
			if (ret.getName().equals(name) == false ||
					(namespace != null && ret.getNamespace().equals(namespace) == false)) {
				ret = null;
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the list of namespaces.
	 * If a namespace is used in the contract that does
	 * not exist in this list, then a dynamic prefix should
	 * be created for the namespace.
	 * 
	 * @return The list of namespaces
	 */
	public java.util.Set<Namespace> getNamespaces() {
		return(m_namespaces);
	}
	
	/**
	 * This method returns the namespace associated with
	 * the specified URI.
	 * 
	 * @param uri The URI
	 * @return The namespace or null if not found
	 */
	public Namespace getNamespaceForURI(String uri) {
		Namespace ret=null;
		
		for (Namespace ns : m_namespaces) {
			if (ns.getURI() != null && ns.getURI().equals(uri)) {
				ret = ns;
				break;
			}
		}
		
		return(ret);
	}
	
	public String toString() {
		StringBuffer buf=new StringBuffer();
		
		buf.append("Contract {"+m_namespace+"}"+m_name+" {\r\n");
		
		// Sort namespaces, so output is consistent
		java.util.List<Namespace> nslist=new Vector<Namespace>(getNamespaces());
		
		Collections.sort(nslist, new Comparator<Namespace>() {

			public int compare(Namespace arg0, Namespace arg1) {
				return(arg0.getURI().compareTo(arg1.getURI()));
			}
		});
		
		for (Namespace ns : nslist) {
			buf.append(ns.toString());
		}
		
		// Sort type definitions, so output is consistent
		java.util.List<TypeDefinition> tdlist=new Vector<TypeDefinition>(getTypeDefinitions());
		
		Collections.sort(tdlist, new Comparator<TypeDefinition>() {

			public int compare(TypeDefinition arg0, TypeDefinition arg1) {
				return(arg0.getName().compareTo(arg1.getName()));
			}
		});
		
		for (TypeDefinition td : tdlist) {
			buf.append(td.toString());
		}
		
		// Sort interfaces, so output is consistent
		java.util.List<Interface> intflist=new Vector<Interface>(getInterfaces());
		
		Collections.sort(intflist, new Comparator<Interface>() {

			public int compare(Interface arg0, Interface arg1) {
				return(arg0.getName().compareTo(arg1.getName()));
			}
		});
		
		for (Interface intf : intflist) {
			buf.append(intf.toString());
		}
		
		buf.append("}\r\n");
		
		return(buf.toString());
	}
}
