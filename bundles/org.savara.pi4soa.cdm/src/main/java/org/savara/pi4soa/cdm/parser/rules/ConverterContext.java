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

import org.pi4soa.cdl.Choreography;
import org.pi4soa.cdl.ExchangeDetails;
import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.model.*;

public interface ConverterContext {

	/**
	 * This method returns the feedback handler for reporting issues.
	 * 
	 * @return The feedback handler
	 */
	public FeedbackHandler getFeedbackHandler();
	
	/**
	 * This method returns the declaration associated
	 * with the supplied name.
	 * 
	 * @param name The name
	 * @return The declaration, or null if not found
	 */
	public Object getState(String name);
	
	/**
	 * This method sets the declaration associated with
	 * the supplied name.
	 * 
	 * @param name The name
	 * @param decl The declaration
	 */
	public void setState(String name, Object decl);
	
	/**
	 * This method pushes the scope, to clear the current
	 * state associated with a parent scope.
	 */
	public void pushScope();
	
	/**
	 * This method pops the scope associated with a parent
	 * conversation.
	 * 
	 */
	public void popScope();
	
	/**
	 * This method pushes the current state onto a stack,
	 * to create a local scope where further roles or
	 * variables may be declared.
	 */
	public void pushState();
	
	/**
	 * This method pops a previous state from the stack
	 * to clear any local declarations made within a
	 * local scope as part of a conversation definition.
	 */
	public void popState();
	
	/**
	 * This method adds a conversation to the context,
	 * for use when processing activities within its
	 * scope.
	 * 
	 * @param conv The conversation
	 */
	public void addProtocol(Protocol conv);
	
	/**
	 * This method removes the conversation from the
	 * context.
	 * 
	 * @param conv The conversation
	 */
	public void removeProtocol(Protocol conv);
	
	/**
	 * This method retrieves the conversation associated
	 * with the supplied model reference.
	 * 
	 * @param ref The model reference
	 * @return The conversation
	 */
	//public Protocol getProtocol(ModelReference ref);

	/**
	 * This method returns the list of Compose activities
	 * within the model.
	 * 
	 * @return The list of compose activities
	 */
	public java.util.List<Run> getComposeActivities();
	
	/**
	 * This method indicates that the supplied exchange
	 * details should be ignored. This can happen if the
	 * exchange details are used in a choice, and therefore
	 * should not also be converted as a normal activity.
	 * 
	 * @param ed The exchange details
	 */
	public void ignore(ExchangeDetails ed);
	
	/**
	 * This method determines whether the supplied exchange
	 * details should be ignored.
	 * 
	 * @param ed The exchange details
	 * @return Whether the exchange details should be ignored
	 */
	public boolean shouldIgnore(ExchangeDetails ed);
	
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
	public boolean shouldExpandChoreography(Choreography choreo);
	
}
