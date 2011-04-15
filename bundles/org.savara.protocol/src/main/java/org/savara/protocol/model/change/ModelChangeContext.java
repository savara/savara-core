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

import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.*;

/**
 * This interface represents the capabilities made available
 * to the model change rules.
 */
public interface ModelChangeContext {

	/**
	 * This method returns the protocol context.
	 * 
	 * @return The protocol context
	 */
	public ProtocolContext getProtocolContext();
	
	/**
	 * This method returns the feedback handler.
	 * 
	 * @return The feedback handler
	 */
	public FeedbackHandler getFeedbackHandler();
	
	/**
	 * This method returns the current parent component.
	 * 
	 * @return The parent
	 */
	public Object getParent();
	
	/**
	 * This method sets the new parent component.
	 * 
	 * @param parent The parent
	 */
	public void setParent(Object parent);
	
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
	public boolean insert(ProtocolModel model, ModelObject mobj, ModelObject ref);
	
	/**
	 * This method removes the supplied model object from the
	 * supplied model. The model object representation must
	 * contain the necessary model specific to remove the 
	 * object from the underlying model representation.
	 * 
	 * @param model The model being changed
	 * @param mobj The model object to be deleted
	 * @return Whether the change has been applied
	 */
	public boolean delete(ProtocolModel model, ModelObject mobj);
	
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
	public boolean update(ProtocolModel model, ModelObject fromObj, ModelObject toObj);
	
	/**
	 * This method returns a set of properties used during model change
	 * processing.
	 * 
	 * @return The properties
	 */
	public java.util.Map<String,Object> getProperties();
	
}
