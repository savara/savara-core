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
 * 24 Jul 2008 : Initial version created by gary
 */
package org.savara.protocol.model.change;

import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.*;

/**
 * This class provides a default implementation for the model
 * change context interface.
 */
public abstract class AbstractModelChangeContext implements ModelChangeContext {
	
	private ProtocolContext m_context=null;
	private FeedbackHandler m_feedbackHandler=null;
	
	private java.util.Stack<Scope> _scopes=new java.util.Stack<Scope>();

	/**
	 * This is the constructor for the model change context.
	 * 
	 * @param context The protocol context
	 * @param journal The journal
	 */
	public AbstractModelChangeContext(ProtocolContext context, FeedbackHandler journal) {
		m_context = context;
		m_feedbackHandler = journal;
		
		_scopes.add(new Scope());
	}
	
	/**
	 * This method returns the protocol context.
	 * 
	 * @return The protocol context
	 */
	public ProtocolContext getProtocolContext() {
		return(m_context);
	}
	
	/**
	 * This method returns the feedback handler.
	 * 
	 * @return The feedback handler
	 */
	public FeedbackHandler getFeedbackHandler() {
		return(m_feedbackHandler);
	}
	
	/**
	 * This method returns a list of model change rules appropriate
	 * for the notation being changed.
	 * 
	 * @return The list of model change rules
	 */
	public abstract java.util.List<ModelChangeRule> getRules();
	
	/**
	 * This method returns the current parent component.
	 * 
	 * @return The parent
	 */
	public Object getParent() {
		return(getScope().getParentComponent());
	}
	
	/**
	 * This method sets the new parent component.
	 * 
	 * @param parent The parent
	 */
	public void setParent(Object parent) {
		getScope().setParentComponent(parent);
	}
	
	/**
	 * This method adds a new model object, within a
	 * parent model object, with the details supplied in
	 * another model object. The supplied model object
	 * will usually be from a different model representation
	 * (e.g. due to a merge), so the details will be
	 * copied and placed in the representation associated
	 * with the supplied model and parent model object.<p>
	 * <p>
	 * If a reference model object is supplied, then the
	 * insertion will occur relative to it. If the reference
	 * object is a block, then it means that the insertion
	 * should occur at the end of the block. Otherwise the
	 * new model object should be inserted before the
	 * reference object, within the containing block. 
	 * 
	 * @param model The model being changed
	 * @param mobj The model object details to be inserted
	 * @param ref The optional reference model object
	 * @return Whether the change has been applied
	 */
	public boolean insert(ProtocolModel model, ModelObject mobj, ModelObject ref) {
		boolean ret=false;
		
		java.util.List<ModelChangeRule> rules=getRules();
		
		for (int i=0; ret == false && i < rules.size(); i++) {
			if (rules.get(i).isInsertSupported(this, model, mobj, ref)) {
				ret = rules.get(i).insert(this, model,
						mobj, ref);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method removes an existing model object, within a
	 * parent model object, with the details supplied in
	 * another model object.
	 * 
	 * @param model The model being changed
	 * @param mobj The model object details to be deleted
	 * @param position The position, where relevant
	 * @return Whether the change has been applied
	 */
	public boolean delete(ProtocolModel model, ModelObject mobj) {
		boolean ret=false;
		
		java.util.List<ModelChangeRule> rules=getRules();
		
		for (int i=0; ret == false && i < rules.size(); i++) {
			if (rules.get(i).isDeleteSupported(this, model, mobj)) {
				ret = rules.get(i).delete(this, model, mobj);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method modifies an existing model object, within a
	 * parent model object, with the details supplied in
	 * another model object.
	 * 
	 * @param model The model being changed
	 * @param fromObj The source model object
	 * @param toObj The model object to be updated
	 * @return Whether the change has been applied
	 */
	public boolean update(ProtocolModel model, ModelObject fromObj, ModelObject toObj) {
		boolean ret=false;
		
		java.util.List<ModelChangeRule> rules=getRules();
		
		for (int i=0; ret == false && i < rules.size(); i++) {
			if (rules.get(i).isUpdateSupported(this, model, fromObj, toObj)) {
				ret = rules.get(i).update(this, model,
						fromObj, toObj);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method returns a set of properties used during model change
	 * processing.
	 * 
	 * @return The properties
	 */
	public java.util.Map<String,Object> getProperties() {
		return(getScope().getProperties());
	}
	
	protected Scope getScope() {
		return(_scopes.peek());
	}
	
	public void pushScope() {
		_scopes.push(new Scope(getScope()));
	}
	
	public void popScope() {
		_scopes.pop();
	}
	
	public static class Scope {
		
		private Object _parent=null;
		private java.util.Map<String,Object> _properties=new java.util.HashMap<String,Object>();

		public Scope() {
		}
		
		public Scope(Scope scope) {
			_parent = scope.getParentComponent();
			_properties.putAll(scope.getProperties());
		}
		
		/**
		 * This method returns the current parent component.
		 * 
		 * @return The parent
		 */
		public Object getParentComponent() {
			return(_parent);
		}
		
		/**
		 * This method sets the new parent component.
		 * 
		 * @param parent The parent
		 */
		public void setParentComponent(Object parent) {
			_parent = parent;
		}
		
		/**
		 * This method returns a set of properties used during model change
		 * processing.
		 * 
		 * @return The properties
		 */
		public java.util.Map<String,Object> getProperties() {
			return(_properties);
		}

	}
}
