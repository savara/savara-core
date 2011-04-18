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
 * This interface represents the generic capabilities of a state
 * managing component in the CDL to BPMN transformation.
 *
 */
public interface BPMNActivity {

	/**
	 * This method returns the parent BPMN activity.
	 * 
	 * @return The parent
	 */
	public BPMNActivity getParent();
	
	/**
	 * This method returns the container object associated
	 * with this activity.
	 * 
	 * @return The container
	 */
	public Object getContainer();
		
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete();
	
	/**
	 * This method returns the BPMN top level Activity.
	 * 
	 * @return The top level activity
	 */
	//public Activity getTopLevelActivity();
	
	/**
	 * This method returns the containing ActivityModel.
	 * 
	 * @return The activity model
	 */
	public BPMNDiagram getBPMNDiagram();
	
	/**
	 * This method returns the BPMN Activity Partition.
	 * 
	 * @return The activity model
	 */
	//public ActivityPartition getActivityPartition();
	
	/**
	 * This method returns the start node for the activites
	 * represented by this UML activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode();
	
	/**
	 * This method returns the end node for the activities
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode();
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState();
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState();
	
	/**
	 * This method causes a transition to be established from
	 * the supplied BPMN state to the current state, applying
	 * any relevant information (such as event triggers
	 * or conditios) to the transition as appropriate.
	 * 
	 * @param fromNode The source node
	 * @param expression The optional conditional expression
	 * @return The edge
	 */
	public Object transitionFrom(BPMNActivity fromNode, String expression);
	
	public int getX();
	
	public void setX(int x);

	public int getY();
	
	public void setY(int y);

	public int getWidth();
	
	public void setWidth(int width);

	public int getHeight();
	
	public void setHeight(int height);
	
	public void adjustWidth(int width);
	
	public void calculatePosition(int x, int y);
	
	public void draw(Object parent);
	
	public boolean canDeleteEndNode();
	
}
