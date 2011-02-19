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
package org.savara.protocol.model.change;

import org.savara.contract.model.Contract;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.*;
import org.scribble.protocol.util.ProtocolModelUtil;

/**
 * Model change utilities.
 */
public class ModelChangeUtils {

	/**
	 * This method adds any contracts associated with roles defined in the
	 * supplied conversation.
	 * 
	 * @param context The context
	 * @param conv The conversation
	 * @param root Whether this is the root conversation
	 */
	//@SuppressWarnings("unchecked")
	//public static void addContracts(ModelChangeContext context, Protocol conv, boolean root) {
	public static void addContract(ModelChangeContext context, Role role, Contract contract) {
		
		java.util.Map<String,Contract> contracts=(java.util.Map<String,Contract>)
							context.getProperties().get(Contract.class.getName());
		
		if (contracts == null) {
			contracts = new java.util.HashMap<String, Contract>();
			
			context.getProperties().put(Contract.class.getName(), contracts);
		}
		
		if (role != null && contract != null) {
			addRoleContract(role, contract, contracts);
		}
	}
	
	// NOTE: Return all the roles defined in the scope of this conversation
	// Would be better to incrementally add contracts for roles, as defined
	// in their own scope, but need to refactor how blocks are dealt with
	// first - which is different in some cases, so not straightforward.
	protected static java.util.List<Role> getRoles(final Protocol conv) {
		final java.util.List<Role> roles=new java.util.Vector<Role>();
		
		conv.visit(new AbstractModelObjectVisitor() {

			public boolean process(ModelObject obj) {
				boolean ret=true;
				
				if (obj instanceof RoleList) {
					roles.addAll(((RoleList)obj).getRoles());
				} else if (obj instanceof Protocol &&
						obj != conv) {
					ret = false;
				}
				
				return(ret);
			}
		});
		
		return(roles);
	}
	
	/**
	 * This method initializes the contract associated with the supplied role.
	 * 
	 * @param role The role
	 * @param contract The contract
	 * @param contracts The map of roles to contracts
	 */
	protected static void addRoleContract(Role role, Contract contract, java.util.Map<String,Contract> contracts) {
		if (role != null && contract != null) {
			contracts.put(role.getName(), contract);
		}
	}

	/**
	 * This method removes the contract associated with the supplied role.
	 * 
	 * @param role The role
	 * @param contracts The map of roles to contracts
	 */
	protected static void removeRoleContract(Role role, java.util.Map<String,Contract> contracts) {
		if (role != null) {
			contracts.remove(role.getName());
		}
	}

	/**
	 * This method removes any contracts associated with roles defined in the
	 * supplied conversation.
	 * 
	 * @param context The context
	 * @param conv The conversation
	 * @param root Whether this is the root conversation
	 */
	@SuppressWarnings("unchecked")
	public static void removeContracts(ModelChangeContext context, Protocol conv, boolean root) {
		
		java.util.Map<String,Contract> contracts=(java.util.Map<String,Contract>)
							context.getProperties().get(Contract.class.getName());
		
		if (contracts != null) {
		
			if (conv.getRole() != null && root) {
				removeRoleContract(conv.getRole(), contracts);
			}
		
			// Get list of roles
			java.util.List<Role> roles=getRoles(conv);
			
			for (Role r : roles) {
				removeRoleContract(r, contracts);
			}
		}
	}
	
	/**
	 * This method returns the contract associated with the supplied role.
	 * 
	 * @param contezt The context
	 * @param roleName The role name
	 * @return The contract, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public static Contract getContract(ModelChangeContext context, Role role) {
		Contract ret=null;
		
		if (context.getProperties().containsKey(Contract.class.getName())) {
			java.util.Map<String,Contract> contracts=
					(java.util.Map<String,Contract>)
					context.getProperties().get(Contract.class.getName());
			
			ret = contracts.get(role.getName());
			
			/*
			if (ret == null) {
				ret = (Contract)role.getProperties().get(Contract.class.getName());
			}
			*/
		}
		
		return(ret);
	}
	
	/**
	 * This method updates the role mapping based on the supplied list of
	 * declaration bindings.
	 * 
	 * @param context The context
	 * @param run The run construct
	 */
	@SuppressWarnings("unchecked")
	public static void pushRoleContractMapping(ModelChangeContext context,
							Run run, Journal journal) {

		if (context.getProperties().containsKey(Contract.class.getName())) {
			java.util.Map<String,Contract> contracts=
					(java.util.Map<String,Contract>)
					context.getProperties().get(Contract.class.getName());

			if (run.enclosingProtocol().getRole() != null &&
					run.getProtocol().getRole() != null) {
				Contract c=contracts.remove(run.enclosingProtocol().getRole().getName());
				
				if (c != null) {
					contracts.put(run.getProtocol().getRole().getName(), c);
				}
			}
		
			// Store protocol against mapped role
			Protocol defn=run.getProtocol();
			
			if (defn == null) {
				// Check if protocol import defined for protocol
				ProtocolImport pi=ProtocolModelUtil.getProtocolImport(run.getModel(),
								run.getProtocolReference());
				
				if (pi == null) {
					journal.error("Referenced protocol '"+run.getProtocolReference().getName()+
								"' not found within model or in import statements", run.getProperties());
				} else {
					ProtocolModel pm=context.getProtocolContext().getProtocolModel(pi, journal);
					
					if (pm != null) {
						defn = pm.getProtocol();
					} else {
						journal.error("Referenced protocol '"+run.getProtocolReference().getName()+
								"' could not be loaded from location '"+pi.getLocation()+"'",
								run.getProperties());
					}
				}
			}

			if (defn != null) {
				
				if (defn.getParameterDefinitions().size() ==
							run.getParameters().size()) {
					for (int i=0; i < run.getParameters().size(); i++) {
						Parameter p=run.getParameters().get(i);
						ParameterDefinition pd=defn.getParameterDefinitions().get(i);
						
						Contract c=contracts.remove(p.getName());
						
						if (c != null) {
							contracts.put(pd.getName(), c);
						}
					}
				} else {
					journal.error("Referenced protocol '"+run.getProtocolReference().getName()+
							"' is expecting a different number of parameters",
							run.getProperties());
				}
			}
		}
	}

	/**
	 * This method resets the role mapping based on the supplied list of
	 * declaration bindings.
	 * 
	 * @param context The context
	 * @param run The run construct
	 */
	@SuppressWarnings("unchecked")
	public static void popRoleContractMapping(ModelChangeContext context,
									Run run, Journal journal) {

		if (context.getProperties().containsKey(Contract.class.getName())) {
			java.util.Map<String,Contract> contracts=
					(java.util.Map<String,Contract>)
					context.getProperties().get(Contract.class.getName());

			if (run.enclosingProtocol().getRole() != null &&
					run.getProtocol().getRole() != null) {
				Contract c=contracts.remove(run.getProtocol().getRole().getName());
				
				if (c != null) {
					contracts.put(run.enclosingProtocol().getRole().getName(), c);
				}
			}
			
			// Store protocol against mapped role
			Protocol defn=run.getProtocol();
			
			if (defn == null) {
				// Check if protocol import defined for protocol
				ProtocolImport pi=ProtocolModelUtil.getProtocolImport(run.getModel(),
								run.getProtocolReference());
				
				if (pi == null) {
					journal.error("Referenced protocol '"+run.getProtocolReference().getName()+
								"' not found within model or in import statements", run.getProperties());
				} else {
					ProtocolModel pm=context.getProtocolContext().getProtocolModel(pi, journal);
					
					if (pm != null) {
						defn = pm.getProtocol();
					} else {
						journal.error("Referenced protocol '"+run.getProtocolReference().getName()+
								"' could not be loaded from location '"+pi.getLocation()+"'",
								run.getProperties());
					}
				}
			}

			if (defn != null) {
				
				if (defn.getParameterDefinitions().size() ==
							run.getParameters().size()) {
					for (int i=0; i < run.getParameters().size(); i++) {
						Parameter p=run.getParameters().get(i);
						ParameterDefinition pd=defn.getParameterDefinitions().get(i);
						
						Contract c=contracts.remove(pd.getName());
						
						if (c != null) {
							contracts.put(p.getName(), c);
						}
					}
				} else {
					journal.error("Referenced protocol '"+run.getProtocolReference().getName()+
							"' is expecting a different number of parameters",
							run.getProperties());
				}
			}
		}
	}
}
