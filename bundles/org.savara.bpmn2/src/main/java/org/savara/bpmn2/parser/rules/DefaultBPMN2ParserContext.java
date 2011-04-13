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

import org.scribble.common.logging.Journal;
import org.scribble.protocol.ProtocolContext;

public class DefaultBPMN2ParserContext implements BPMN2ParserContext {

	private ProtocolContext m_protocolContext=null;
	private Journal m_journal=null;
	private Scope m_scope=null;
	
	public DefaultBPMN2ParserContext(ProtocolContext context, Journal journal,
							Scope scope) {
		m_protocolContext = context;
		m_journal = journal;
		m_scope = scope;
	}
	
	/**
	 * This method returns the protocol context.
	 * 
	 * @return The protocol context
	 */
	public ProtocolContext getProtocolContext() {
		return(m_protocolContext);
	}
	
	/**
	 * This method returns the journal for reporting issues.
	 * 
	 * @return The journal
	 */
	public Journal getJournal() {
		return(m_journal);
	}
	
	/**
	 * This method returns the current scope.
	 * 
	 * @return The scope
	 */
	public Scope getScope() {
		return(m_scope);
	}
	
	/**
	 * This method pushes the scope, to clear the current
	 * state associated with a parent scope.
	 */
	public void pushScope() {
		m_scope = new Scope(m_scope);
	}
	
	/**
	 * This method pops the scope associated with a parent
	 * conversation.
	 */
	public void popScope() {
		m_scope = m_scope.getParent();
	}

}
