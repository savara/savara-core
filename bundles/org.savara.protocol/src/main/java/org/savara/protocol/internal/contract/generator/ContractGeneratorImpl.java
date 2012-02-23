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
package org.savara.protocol.internal.contract.generator;

import org.savara.common.logging.FeedbackHandler;
import org.savara.common.logging.MessageFormatter;
import org.savara.contract.model.Contract;
import org.savara.protocol.contract.generator.ContractGenerator;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Role;

/**
 * This class generates a contract from a protocol defining the behaviour of
 * multiple interacting roles.
 * 
 */
public class ContractGeneratorImpl implements ContractGenerator {

	/**
	 * This method generates a contract associated with a supplied located
	 * protocol.
	 * 
	 * @param cm The protocol
	 * @param handler The feedback handler
	 * @return The contract
	 */
	public Contract generate(Protocol cm, FeedbackHandler handler) throws IllegalArgumentException {
		if (cm == null) {
			throw new IllegalArgumentException(MessageFormatter.format(
					java.util.PropertyResourceBundle.getBundle(
							"org.savara.contract.Messages"), "SAVARA-CONTRACT-00001"));
			
		} else if (cm.getLocatedRole() == null) {
			throw new IllegalArgumentException(MessageFormatter.format(
					java.util.PropertyResourceBundle.getBundle(
							"org.savara.contract.Messages"), "SAVARA-CONTRACT-00002"));
		}

		return(generate(cm, null, cm.getLocatedRole(), handler));
	}

	/**
	 * This method generates a contract, derived from the supplied protocol model,
	 * associated with a supplied server role, optionally constrained by the supplied
	 * client roles. If the client role is not specified, then the contract associated with
	 * all relevant client roles will be generated.
	 * 
	 * @param cm The global or local (located) protocol
	 * @param clients The optional client roles
	 * @param server The server role
	 * @param journal The journal
	 * @return The contract
	 */
	public Contract generate(Protocol cm, java.util.Set<Role> clients, Role server, FeedbackHandler handler)
								throws IllegalArgumentException {
		// Check parameters
		if (cm == null) {
			throw new IllegalArgumentException(MessageFormatter.format(
					java.util.PropertyResourceBundle.getBundle(
					"org.savara.contract.Messages"), "SAVARA-CONTRACT-00001"));
			
		} else if (server == null) {
			throw new IllegalArgumentException(MessageFormatter.format(
					java.util.PropertyResourceBundle.getBundle(
					"org.savara.contract.Messages"), "SAVARA-CONTRACT-00003"));
		}
		
		ContractIntrospector introspector=new ContractIntrospector(cm, clients, server, handler);
		
		introspector.process();
		
		return(introspector.getContract());
	}
	
	
}
