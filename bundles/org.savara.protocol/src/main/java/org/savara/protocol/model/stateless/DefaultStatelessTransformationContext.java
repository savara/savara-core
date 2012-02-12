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
 * This class provides the default implementation of the
 * stateless transformation context.
 */
public class DefaultStatelessTransformationContext 
					implements StatelessTransformationContext {
	
	private boolean _messageBased=false;
	private org.scribble.protocol.model.Choice _choice=null;
	private boolean m_allowNewPaths=true;
	private Block m_lastPath=null;
	private java.util.List<TransformState> m_stack=new java.util.Vector<TransformState>();
	private java.util.List<org.scribble.protocol.model.Role> m_roleList=null;
	private static java.util.List<StatelessTransformationRule> RULES=
				new java.util.Vector<StatelessTransformationRule>();

	static {
		RULES.add(new BlockStatelessTransformationRule());
		RULES.add(new ChoiceStatelessTransformationRule());
		RULES.add(new InteractionStatelessTransformationRule());
		RULES.add(new MessageSignatureStatelessTransformationRule());
		RULES.add(new ParallelStatelessTransformationRule());
		RULES.add(new ProtocolModelStatelessTransformationRule());
		RULES.add(new ProtocolStatelessTransformationRule());
		RULES.add(new RepeatStatelessTransformationRule());
		RULES.add(new RoleStatelessTransformationRule());
		RULES.add(new RunStatelessTransformationRule());
		RULES.add(new TypeReferenceStatelessTransformationRule());
	}

	/**
	 * This is the constructor for the default context.
	 * 
	 * @param messageBased Whether the transformation is message based
	 * 						versus RPC based
	 */
	public DefaultStatelessTransformationContext(boolean messageBased) {
		_messageBased = messageBased;
	}
	
	/**
	 * This method determines whether the transformation should be
	 * message based, as opposed to RPC (request/response) based.
	 * 
	 * @return Whether the transformation is message based
	 */
	public boolean isMessageBased() {
		return (_messageBased);
	}
	
	/**
	 * This method transforms the supplied model object.
	 * 
	 * @param src The source block
	 * @param target The target block
	 * @return Whether the block was fully transformed
	 */
	public boolean transform(Block src, Block target) {
		boolean ret=false;
		boolean f_processed=false;
		
		for (int i=0; f_processed == false &&
						i < RULES.size(); i++) {
			if (RULES.get(i).isSupported(src)) {
				ret = RULES.get(i).transform(this, src, target);
				f_processed = true;
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method transforms the supplied model object.
	 * 
	 * @param modelObject The model object
	 * @return The transformed model object
	 */
	public ModelObject transform(ModelObject modelObject) {
		ModelObject ret=null;
		
		for (int i=0; ret == null &&
							i < RULES.size(); i++) {
			if (RULES.get(i).isSupported(modelObject)) {
				ret = RULES.get(i).transform(this, modelObject);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method returns a new path from the top
	 * level stateless choice construct, or null if 
	 * new stateless paths should not be processed at
	 * this time.
	 * 
	 * @return The new path, or null if new  paths
	 * 				should not be processed
	 */
	public org.scribble.protocol.model.Block createNewPath() {
		org.scribble.protocol.model.Block ret=null;
		
		if (_choice != null && m_allowNewPaths) {
			if (m_lastPath != null && m_lastPath.getContents().size() == 0) {
				ret = m_lastPath;
			} else {
				ret = new Block();
				
				_choice.getPaths().add(ret);
				
				m_lastPath = ret;
			}
		} else {
			// Clear the last path
			m_lastPath = null;
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the current stateless path. If
	 * no path is returned, then it is not currently possible
	 * to record the stateless behaviour.
	 * 
	 * @return The current path, or null if no path associated
	 */
	public Block getCurrentPath() {
		return(m_lastPath);
	}
	
	/**
	 * This method indicates that new paths are allowed.
	 */
	public void allowNewPaths() {
		m_allowNewPaths = true;
	}
	
	/**
	 * This method indicates that new paths should not be
	 * created.
	 */
	public void disallowNewPaths() {
		m_allowNewPaths = false;
	}
	
	/**
	 * This method sets the choice behaviour representing
	 * the top level mutually exclusive construct used to
	 * model the different stateless paths.
	 * 
	 * @param choice The choice
	 */
	protected void setMultiPathBehaviour(org.scribble.protocol.model.Choice choice) {
		_choice = choice;
	}
	
	/**
	 * This method returns the list of roles that
	 * are defined at the top level, associated
	 * with the stateless definition.
	 * 
	 * @return The role list
	 */
	public java.util.List<Role> getRoleList() {
		return(m_roleList);
	}

	/**
	 * This method sets the role list.
	 * 
	 * @param roleList The role list
	 */
	protected void setRoleList(java.util.List<Role> roleList) {
		m_roleList = roleList;
	}
	
	/**
	 * This method pushes the supplied state on to
	 * the top of the stack.
	 * 
	 *@param state The state
	 */
	public void push(TransformState state) {
		m_stack.add(0, state);
	}
	
	/**
	 * This method returns the state at the top of the
	 * stack.
	 * 
	 * @return The state on top of the stack, or null
	 * 				if the stack is empty
	 */
	public TransformState pop() {
		TransformState ret=null;
		
		if (m_stack.size() > 0) {
			ret = m_stack.remove(0);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the stack, with the first
	 * element being the top of the stack.
	 * 
	 * @return The stack
	 */
	public java.util.List<TransformState> getStack() {
		return(m_stack);
	}
}
