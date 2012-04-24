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

import org.savara.bpmn2.model.BPMNEdge;
import org.savara.bpmn2.model.Point;

/**
 * This class represents a junction within a
 * BPMN process.
 *
 */
public class BoundaryEvent extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the junction activity.
	 * 
	 * @param node The junction node
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public BoundaryEvent(Object node,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation) {
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
	
	public void transitionTo(BPMNActivity toNode, String expression, Object parent) {
		Object ret=getModelFactory().createControlLink(getContainer(),
				getEndNode(), toNode.getStartNode(), expression);
		
		BPMNEdge edge=(BPMNEdge)getNotationFactory().createSequenceLink(getModelFactory(), ret, parent);
		
		if (edge.getWaypoint().size() > 1) {
			if (edge.getWaypoint().get(0).getY() == edge.getWaypoint().get(1).getY()) {
			
				Point p1=new Point();
				p1.setY(edge.getWaypoint().get(0).getY()+45);
				p1.setX(edge.getWaypoint().get(0).getX()-15);
				
				Point p2=new Point();
				p2.setY(edge.getWaypoint().get(0).getY()+45);
				p2.setX(edge.getWaypoint().get(1).getX()-45);
				
				Point p3=new Point();
				p3.setY(edge.getWaypoint().get(1).getY());
				p3.setX(edge.getWaypoint().get(1).getX()-45);
				
				edge.getWaypoint().add(1, p1);
				edge.getWaypoint().add(2, p2);
				edge.getWaypoint().add(3, p3);
			} else {
				Point p1=new Point();
				p1.setY(edge.getWaypoint().get(1).getY());
				p1.setX(edge.getWaypoint().get(0).getX()-15);
				
				edge.getWaypoint().add(1, p1);		
			}
		}
	}
	
	private Object m_node=null;
}
