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
package org.savara.bpmn2.generation.components;

/**
 * This class represents a junction within a
 * BPMN process.
 *
 */
public class JunctionActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the junction activity.
	 * 
	 * @param node The junction node
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public JunctionActivity(Object node,
			BPMNActivity parent, org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model,
			org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation) {
		super(parent, model, notation);
		
		m_node = node;
		
	}
	
	/**
	 * This method returns the start node for the activites
	 * represented by this UML activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		return(m_node);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this UML activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(m_node);
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(this);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(this);
	}
	
	public int getWidth() {
		return(30);
	}
	
	public int getHeight() {
		return(30);
	}
	
	public void draw(Object parent) {
		
		if (getModelFactory().isDeleted(m_node) == false) {
			getNotationFactory().createJunction(getModelFactory(),
					m_node, parent, getX(), getY(), getWidth(), getHeight());
		}
	}
	
	private Object m_node=null;
}
