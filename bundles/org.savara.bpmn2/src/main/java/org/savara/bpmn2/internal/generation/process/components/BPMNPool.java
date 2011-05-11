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
package org.savara.bpmn2.internal.generation.process.components;

public class BPMNPool extends AbstractBPMNActivity {

	private boolean m_completed=false;
	private Object m_pool=null;
	private BPMNActivity m_initialState=null;
	private BPMNActivity m_finalState=null;
	
	/**
	 * This constructor initializes the pool state.
	 * 
	 * @param diagram The diagram
	 * @param name The pool name
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public BPMNPool(Object diagram, String name,
			BPMNActivity parent,
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
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
		
		m_pool = getModelFactory().createPool(diagram, name);
		
		/*
		m_activityModel = (org.eclipse.uml2.uml.Activity)
			servicePackage.createPackagedElement(null,
				UMLPackage.eINSTANCE.getActivity());
	
		m_activityModel.setName(getName(elem));
		*/

		// Create initial state
		m_initialState = new JunctionActivity(getModelFactory().createInitialNode(getContainer()),
				this, getModelFactory(), getNotationFactory());
		
		// Create final state
		m_finalState = new JunctionActivity(getModelFactory().createFinalNode(getContainer()),
				this, getModelFactory(), getNotationFactory());
		
	}
	
	/**
	 * This method returns the container associated with the
	 * activity.
	 * 
	 * @return The container
	 */
	public Object getContainer() {
		return(m_pool);
	}
		
	public Object getStartNode() {
		return(m_initialState.getStartNode());
	}
	
	public Object getEndNode() {
		return(m_finalState.getEndNode());
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(m_initialState);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(m_finalState);
	}
	
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			
			// Move the final state to the end of the list
			if (getChildStates().remove(m_finalState)) {
				getChildStates().add(m_finalState);
			}
			
			// Join the child state vertex with transitions
			transitionSequentialNodes();
			
			// Add padding
			setHeight(getHeight()+(VERTICAL_GAP*2));
			
			m_completed = true;
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
		getNotationFactory().createPool(getModelFactory(), m_pool,
				parent, getX(), getY(), getWidth(), getHeight());
		
		//m_initialState.draw(notation);
		//m_finalState.draw(notation);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
		}

		// Create diagram sequence flows
		java.util.List<Object> seqflows=getModelFactory().getControlLinks(getContainer());
		
		for (Object seqflow : seqflows) {
			getNotationFactory().createSequenceLink(getModelFactory(), seqflow, parent);
		}
		
	}
}
