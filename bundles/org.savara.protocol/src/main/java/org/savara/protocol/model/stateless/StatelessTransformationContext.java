/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 * Copyright 2012 Red Hat, Inc.
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
 * 13 Dec 2008 : Initial version created by gary
 * Feb 2012 : Update based on scribble v2
 */
package org.savara.protocol.model.stateless;

import org.scribble.protocol.model.*;

/**
 * This interface provides the context for the stateless
 * transformation rules.
 */
public interface StatelessTransformationContext {

	/**
	 * This method determines whether the transformation should be
	 * message based, as opposed to RPC (request/response) based.
	 * 
	 * @return Whether the transformation is message based
	 */
	public boolean isMessageBased();
	
	/**
	 * This method transforms the supplied block.
	 * 
	 * @param src The source block
	 * @param target The target block
	 * @return Whether the block was fully transformed
	 */
	public boolean transform(Block src, Block target);
	
	/**
	 * This method transforms the supplied model object.
	 * 
	 * @param modelObject The model object
	 * @return The transformed model object
	 */
	public ModelObject transform(ModelObject modelObject);
	
	/**
	 * This method returns a new path from the top
	 * level stateless choice construct, or null if 
	 * new stateless paths should not be processed at
	 * this time.
	 * 
	 * @return The new path, or null if new  paths
	 * 				should not be processed
	 */
	public Block createNewPath();
	
	/**
	 * This method returns the current stateless path. If
	 * no path is returned, then it is not currently possible
	 * to record the stateless behaviour.
	 * 
	 * @return The current path, or null if no path associated
	 */
	public Block getCurrentPath();
	
	/**
	 * This method indicates that new paths are allowed.
	 */
	public void allowNewPaths();
	
	/**
	 * This method indicates that new paths should not be
	 * created.
	 */
	public void disallowNewPaths();

	/**
	 * This method returns the list of roles that
	 * are defined at the top level, associated
	 * with the stateless definition.
	 * 
	 * @return The role list
	 */
	public java.util.List<Role> getRoleList();
	
	/**
	 * This method pushes the supplied state on to
	 * the top of the stack.
	 * 
	 *@param state The state
	 */
	public void push(TransformState state);
	
	/**
	 * This method returns the state at the top of the
	 * stack.
	 * 
	 * @return The state on top of the stack, or null
	 * 				if the stack is empty
	 */
	public TransformState pop();
	
	/**
	 * This method returns the stack, with the first
	 * element being the top of the stack.
	 * 
	 * @return The stack
	 */
	public java.util.List<TransformState> getStack();
	
	// TODO: Will need support for name mapping, as
	// (for example) roles in a sub-conversation,
	// when moved to be accessible at the top level
	// in a path, will need to use the role names
	// defined at the top level.
}
