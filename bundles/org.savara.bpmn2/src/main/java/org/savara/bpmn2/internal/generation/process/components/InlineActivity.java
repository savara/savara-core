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

import org.savara.bpmn2.model.TSubProcess;

/**
 * This class represents the BPMN activity node for a Perform activity.
 * 
 */
public abstract class InlineActivity extends AbstractBPMNActivity {

	private boolean m_completed=false;
	private BPMNActivity m_finalState=null;
	private TSubProcess m_subProcess=null;
	
	/**
	 * This constructor initializes the receive state.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public InlineActivity(BPMNActivity parent,
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
		super(parent, model, notation);
		
		initialize(parent);
	}
	
	protected void initialize(BPMNActivity parent) {
		
		m_subProcess = (TSubProcess)getModelFactory().createSubProcess(parent.getContainer());
		
		// Create initial state
		new JunctionActivity(getModelFactory().createInitialNode(getContainer()),
				this, getModelFactory(), getNotationFactory());
		
		// Create final state
		m_finalState = new JunctionActivity(getModelFactory().createFinalNode(getContainer()),
				this, getModelFactory(), getNotationFactory());
	}
	
	public TSubProcess getSubProcess() {
		return(m_subProcess);
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
			setWidth(getWidth()+(HORIZONTAL_GAP*2));
			
			m_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int curx=x+HORIZONTAL_GAP;
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
		getNotationFactory().createTask(getModelFactory(), m_subProcess,
				parent, getX(), getY(), getWidth(), getHeight());
		
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

	/**
	 * This method returns the container associated with the
	 * activity.
	 * 
	 * @return The container
	 */
	public Object getContainer() {
		return(m_subProcess);
	}
		
	public Object getStartNode() {
		return(m_subProcess);
	}
	
	public Object getEndNode() {
		return(m_subProcess);
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
}
