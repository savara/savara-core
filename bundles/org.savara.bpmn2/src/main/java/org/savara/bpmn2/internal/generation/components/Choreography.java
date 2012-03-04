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
 * 29 Jan 2007 : Initial version created by gary
 */
package org.savara.bpmn2.internal.generation.components;

public class Choreography extends AbstractBPMNActivity {

	private boolean _completed=false;
	private Object _choreography=null;
	private BPMNActivity _initialState=null;
	private BPMNActivity _finalState=null;
	
	/**
	 * This constructor initializes the choreography state.
	 * 
	 * @param diagram The diagram
	 * @param name The choreography name
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public Choreography(Object diagram, String name,
			BPMNActivity parent,
			org.savara.bpmn2.internal.generation.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation) {
		super(parent, model, notation);
		
		initialize(diagram, name);
	}
	
	/**
	 * This method performs the initialization of the 
	 * activity model.
	 * 
	 * @param diagram The diagram
	 * @param name The pool name
	 */
	public void initialize(Object diagram, String name) {
		
		_choreography = getModelFactory().createChoreography(diagram, name);
		
		// Create initial state
		_initialState = new JunctionActivity(getModelFactory().createInitialNode(getContainer()),
				this, getModelFactory(), getNotationFactory());
		
		// Create final state
		_finalState = new JunctionActivity(getModelFactory().createFinalNode(getContainer()),
				this, getModelFactory(), getNotationFactory());
		
	}
	
	/**
	 * This method returns the container associated with the
	 * activity.
	 * 
	 * @return The container
	 */
	public Object getContainer() {
		return(_choreography);
	}
		
	public Object getStartNode() {
		return(_initialState.getStartNode());
	}
	
	public Object getEndNode() {
		return(_finalState.getEndNode());
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(_initialState);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(_finalState);
	}
	
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (_completed == false) {
			
			// Move the final state to the end of the list
			if (getChildStates().remove(_finalState)) {
				getChildStates().add(_finalState);
			}
			
			// Join the child state vertex with transitions
			transitionSequentialNodes();
			
			// Add padding
			setHeight(getHeight()+(VERTICAL_GAP*2));
			setWidth(getWidth()+(HORIZONTAL_GAP*2));
			
			_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int curx=HORIZONTAL_GAP;
		int midy=(getHeight()/2);
		
		setX(x);
		setY(y); //+VERTICAL_GAP);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.calculatePosition(curx, y + (midy-(act.getHeight()/2)));
			
			curx += (act.getWidth()+HORIZONTAL_GAP);
		}
	}
	
	public void draw(Object parent) {
		
		// Construct notation
		//getNotationFactory().createChoreography(getModelFactory(), _choreography,
		//		parent, getX(), getY(), getWidth(), getHeight());
		
		//m_initialState.draw(notation);
		//m_finalState.draw(notation);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
			
			if (i > 0) {
				BPMNActivity prev=(BPMNActivity)getChildStates().get(i-1);
				
				prev.transitionTo(act, null, parent);
			}
		}

		// Create diagram sequence flows
		/*
		java.util.List<Object> seqflows=getModelFactory().getControlLinks(getContainer());
		
		for (Object seqflow : seqflows) {
			getNotationFactory().createSequenceLink(getModelFactory(), seqflow, parent);
		}
		*/
		
	}
}
