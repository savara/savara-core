/*
 * Copyright 2005-7 Pi4 Technologies Ltd
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
 * Jan 25, 2007 : Initial version created by gary
 */
package org.savara.bpmn2.internal.generation.components;

import org.savara.bpmn2.model.TReceiveTask;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Interaction;

/**
 * This class represents the UML activity node for a Receive activity.
 * 
 */
public class ReceiveActivity extends SimpleActivity {
	
	private Interaction m_receive=null;

	/**
	 * This constructor initializes the receive state.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ReceiveActivity(Interaction act,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation) {
		super(act, parent, model, notation);
		
		m_receive = act;
	}
	
	protected Object createNode(Activity act) {
		return(getModelFactory().createReceiveTask(getContainer(), act));
	}
	
	/**
	 * This method returns the behavioral receive activity.
	 * 
	 * @return The receive activity
	 */
	public Interaction getReceive() {
		return(m_receive);
	}

	/**
	 * This method returns the receive task.
	 * 
	 * @return The send task
	 */
	public TReceiveTask getReceiveTask() {
		return((TReceiveTask)getNode());
	}

	/**
	 * This method causes the receive activity to break any
	 * existing control links, including removing preceeding
	 * nodes that only have these links as their outgoing
	 * links.
	 *
	 */
	public void breakLinks() {
		Object node=getStartNode();
		
		java.util.List<Object> list=getModelFactory().getInboundControlLinks(node);
		
		for (int i=list.size()-1; i >= 0; i--) {
			Object edge=list.get(i);
			
			tidyUpEdge(edge);
		}
 	}
	
	/**
	 * This method tidies up an activity edge, recursively being
	 * called if its source node only has a single incoming
	 * activity edge.
	 * 
	 * @param edge The activity edge
	 */
	protected void tidyUpEdge(Object edge) {
		
		// Check if source node has only a single incoming link
		// and if so, then remove the node
		Object sourceNode=getModelFactory().getSource(edge);
		
		getModelFactory().delete(edge);
		
		// If has less than 2 incoming links and no
		// outgoing link, then tidyup node and links recursively
		if (getModelFactory().getInboundControlLinks(sourceNode).size() <= 1 &&
				getModelFactory().getOutboundControlLinks(sourceNode).size() == 0 &&
				(getModelFactory().isDecision(sourceNode) ||	// was decision node
					getModelFactory().isJoin(sourceNode))) {	// was merge node
			if (getModelFactory().getInboundControlLinks(sourceNode).size() == 1) {
				Object actedge=getModelFactory().
						getInboundControlLinks(sourceNode).get(0);
				
				tidyUpEdge(actedge);
			}
			
			getModelFactory().delete(sourceNode);
		}
	}
}
