/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * 6 Jun 2008 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import java.util.logging.Logger;

import org.pi4soa.cdl.Choreography;
import org.pi4soa.cdl.ExchangeDetails;
import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * The default implementation of the converter context.
 */
public class DefaultConverterContext implements ConverterContext {

	private FeedbackHandler m_feedbackHandler=null;
	
	/**
	 * Default constructor.
	 */
	public DefaultConverterContext(FeedbackHandler handler) {
		m_feedbackHandler = handler;
	}
	
	/**
	 * This method returns the feedback handler for reporting issues.
	 * 
	 * @return The feedback handler
	 */
	public FeedbackHandler getFeedbackHandler() {
		return(m_feedbackHandler);
	}
	
	/**
	 * This method returns the declaration associated
	 * with the supplied name.
	 * 
	 * @param name The name
	 * @return The declaration, or null if not found
	 */
	public Object getState(String name) {
		return(m_scope.getState(name));
	}
	
	/**
	 * This method sets the declaration associated with
	 * the supplied name.
	 * 
	 * @param name The name
	 * @param decl The declaration
	 */
	public void setState(String name, Object decl) {
		m_scope.setState(name, decl);
	}
	
	/**
	 * This method pushes the current state onto a stack.
	 */
	public void pushState() {
		m_scope.pushState();
	}
	
	/**
	 * This method pops the current state from the stack.
	 */
	public void popState() {
		m_scope.popState();
	}
		
	/**
	 * This method pushes the current scope onto a stack.
	 */
	public void pushScope() {
		m_scopeStack.add(0, m_scope);
		m_scope = new Scope();
	}
	
	/**
	 * This method pops the current scope from the stack.
	 */
	public void popScope() {
		if (m_scopeStack.size() > 0) {
			m_scope = m_scopeStack.remove(0);
		} else {
			logger.severe("No state entry to pop from stack");
		}
	}
	
	/**
	 * This method adds a conversation to the context,
	 * for use when processing activities within its
	 * scope.
	 * 
	 * @param conv The conversation
	 */
	public void addProtocol(Protocol conv) {
		m_protocols.add(conv);
	}
	
	/**
	 * This method removes the conversation from the
	 * context.
	 * 
	 * @param conv The conversation
	 */
	public void removeProtocol(Protocol conv) {
		m_protocols.remove(conv);
	}
	
	/**
	 * This method retrieves the conversation associated
	 * with the supplied model reference.
	 * 
	 * @param ref The model reference
	 * @return The conversation
	 */
	/*
	public Conversation getProtocol(ModelReference ref) {
		Conversation ret=null;
		
		for (int i=0; ret == null && i < m_protocols.size(); i++) {
			Conversation subconv=(Conversation)
							m_protocols.get(i);

			if (subconv.getLocatedName() != null &&
					subconv.getLocatedName().getName() != null &&
						ref.getAlias().equals(
					subconv.getLocatedName().getName()) &&
						((ref.getLocatedRole() == null &&
					subconv.getLocatedName().getRole() == null) ||
						((ref.getLocatedRole() != null &&
					subconv.getLocatedName().getRole() != null &&
						ref.getLocatedRole().equals(
					subconv.getLocatedName().getRole().getName()))))) {
			
				ret = subconv;
			}
		}

		return(ret);
	}
	*/

	/**
	 * This method returns the list of Compose activities
	 * within the model.
	 * 
	 * @return The list of compose activities
	 */
	public java.util.List<Run> getComposeActivities() {
		return(m_composeActivities);
	}
	
	/**
	 * This method indicates that the supplied exchange
	 * details should be ignored. This can happen if the
	 * exchange details are used in a choice, and therefore
	 * should not also be converted as a normal activity.
	 * 
	 * @param ed The exchange details
	 */
	public void ignore(ExchangeDetails ed) {
		m_ignore.add(ed);
	}
	
	/**
	 * This method determines whether the supplied exchange
	 * details should be ignored.
	 * 
	 * @param ed The exchange details
	 * @return Whether the exchange details should be ignored
	 */
	public boolean shouldIgnore(ExchangeDetails ed) {
		return(m_ignore.contains(ed));
	}
	
	/**
	 * This method determines whether the supplied choreography
	 * is associated with exchange details to be ignored. If
	 * so, then the choreography should be expanded, and the
	 * relevant exchange details ignored.
	 * 
	 * @param choreo The choreography to check
	 * @return Whether the choreography has an ignored exchange details,
	 * 				and should therefore be expanded
	 */
	public boolean shouldExpandChoreography(Choreography choreo) {
		boolean ret=false;
		
		for (ExchangeDetails ed : m_ignore) {
			if (ed.getEnclosingChoreography() == choreo) {
				ret = true;
				break;
			}
		}
		
		return(ret);
	}
	
	private static Logger logger = Logger.getLogger(DefaultConverterContext.class.getName());

	//private ModelReference m_source=null;
	private Scope m_scope=new Scope();
	private java.util.List<Scope> m_scopeStack=new java.util.Vector<Scope>();
	private java.util.List<Run> m_composeActivities=new java.util.Vector<Run>();
	private java.util.List<Protocol> m_protocols=new java.util.Vector<Protocol>();
	private java.util.List<ExchangeDetails> m_ignore=new java.util.Vector<ExchangeDetails>();
}
