/*
 * Copyright 2005-6 Pi4 Technologies Ltd
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
 * 26 Jan 2007 : Initial version created by gary
 */
package org.savara.bpmn2.generation.process;

import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Interaction;

public interface BPMN2ModelFactory {

	public String getFileExtension();
	
	public void saveModel(String fileName, Object diagram)
						throws BPMN2GenerationException;
	
	public Object createDiagram();
	
	public Object createPool(Object diagram, String name);
	
	public Object createInitialNode(Object container);
	
	public Object createSimpleTask(Object container, Activity activity);
	
	public Object createDataBasedXORGateway(Object container);
	
	public Object createEventBasedXORGateway(Object container);
	
	public Object createANDGateway(Object container);
	
	public Object createFinalNode(Object container);
	
	public Object createControlLink(Object container,
			Object fromNode, Object toNode,
			String conditionalExpression);
	
	public Object createMessageLink(Object container,
			Object fromNode, Object toNode,	Interaction receive);
	
	public Object setLinkExpression(Object link, String expression);
	
	public boolean isDecision(Object node);
	
	/**
	 * This method determines if the supplied node is a join. This
	 * is true, if the node is a data or event based gateway,
	 * and as incoming edges. This is based on the fact that only
	 * a join gateway, at the point this method is invoked, would
	 * have incoming links, otherwise any other gateway would be
	 * assumed to be the initial gateway in a conditional grouping
	 * construct.
	 * 
	 * @param node
	 * @return Whether the node is a join
	 */
	public boolean isJoin(Object node);
	
	public boolean isTerminal(Object node);
	
	public void setLabel(Object entity, String label);
	
	public Object getSource(Object link);
	
	public void setSource(Object link, Object node);
	
	public Object getTarget(Object link);
	
	public void setTarget(Object link, Object node);
	
	public java.util.List<Object> getInboundControlLinks(Object node);
	
	public java.util.List<Object> getOutboundControlLinks(Object node);
	
	public java.util.List<Object> getInboundMessageLinks(Object node);
	
	public java.util.List<Object> getOutboundMessageLinks(Object node);
	
	public void delete(Object entity);
	
	public boolean isDeleted(Object entity);
	
}
