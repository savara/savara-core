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
package org.savara.bpmn2.internal.generation.process.components;

import org.scribble.protocol.model.Activity;

/**
 * This class represents a sequence of states within a
 * UML state machine.
 *
 */
public class SequenceActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the sequence state.
	 * 
	 * @param sequence The sequence
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public SequenceActivity(Activity sequence, BPMNActivity parent,
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
		super(parent, model, notation);
		
		//addChildState(new SimpleState(null, this));
		//addChildState(new SimpleState(null, this));
	}

	/**
	 * This method indicates that the UML state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			
			// Join the child state vertex with transitions
			/*
			for (int i=1; i < getChildStates().size(); i++) {
				UMLState v1=(UMLState)getChildStates().get(i-1);
				UMLState v2=(UMLState)getChildStates().get(i);
				
				v2.transitionFrom(v1);
			}
			*/
			transitionSequentialNodes();
			
			m_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int curx=x;
		int midy=y+(getHeight()/2);
		
		setX(x);
		setY(y);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.calculatePosition(curx, midy-(act.getHeight()/2));
			
			curx += (act.getWidth()+HORIZONTAL_GAP);
		}
	}
	
	/**
	 * This method returns the start node for the activites
	 * represented by this UML activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		Object ret=null;
		
		if (getChildStates().size() > 0) {
			BPMNActivity state=(BPMNActivity)getChildStates().get(0);
			ret = state.getStartNode();
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this UML activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		Object ret=null;
		int len=0;
		
		if ((len=getChildStates().size()) > 0) {
			BPMNActivity state=(BPMNActivity)getChildStates().get(len-1);
			ret = state.getEndNode();
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		BPMNActivity ret=null;
		
		if (getChildStates().size() > 0) {
			BPMNActivity state=(BPMNActivity)getChildStates().get(0);
			ret = state;
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		BPMNActivity ret=null;
		int len=0;
		
		if ((len=getChildStates().size()) > 0) {
			BPMNActivity state=(BPMNActivity)getChildStates().get(len-1);
			ret = state.getEndState();
		}
		
		return(ret);
	}
	
	public void adjustWidth(int width) {
		float percentChange=((float)width)/((float)getWidth());
		
		//setWidth(width);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			int cur=act.getWidth();
			int newWidth=(int)((float)cur * percentChange);
			
			act.adjustWidth(newWidth);
			
			int change=act.getWidth()-cur;
			
			setWidth(getWidth()+change);
		}
	}
	
	public void draw(Object parent) {
		
		// Construct notation
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
		}
	}
	
	private boolean m_completed=false;
}
