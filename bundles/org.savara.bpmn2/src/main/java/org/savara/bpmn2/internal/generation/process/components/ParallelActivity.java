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

import org.savara.bpmn2.generation.process.BPMN2GenerationException;
import org.scribble.protocol.model.Parallel;

/**
 * This class represents a parallel grouping of states within a
 * UML state machine.
 *
 */
public class ParallelActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the parallel state.
	 * 
	 * @param parallel The parallel
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ParallelActivity(Parallel parallel, BPMNActivity parent,
			org.savara.bpmn2.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.generation.process.BPMN2NotationFactory notation)
						throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(parallel);		
	}

	/**
	 * This method performs the initialization of the 
	 * parallel state.
	 * 
	 * @param elem The parallel
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(Parallel elem) throws BPMN2GenerationException {
		
		// Get region
		/*
		Activity region=getTopLevelActivity();
		
		// Create parallel state
		ActivityNode parallelState = region.createNode(null,
				UMLPackage.eINSTANCE.getForkNode());
		parallelState.getInPartitions().add(getActivityPartition());
		*/
		Object parallelState=getModelFactory().createANDGateway(getContainer());
		
		m_forkState = new JunctionActivity(parallelState, this,
				getModelFactory(), getNotationFactory());
		
		// Create join state
		/*
		ActivityNode joinState = region.createNode(null,
				UMLPackage.eINSTANCE.getJoinNode());
		joinState.getInPartitions().add(getActivityPartition());
		*/
		
		Object joinState=getModelFactory().createANDGateway(getContainer());
		
		m_joinState = new JunctionActivity(joinState, this,
				getModelFactory(), getNotationFactory());
	}
	
	/**
	 * This method indicates that the UML state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			int width=m_forkState.getWidth()+m_joinState.getWidth()+
							(2 * HORIZONTAL_GAP);
			int height=0;
			
			// Move the junction state to the end of the list
			if (getChildStates().remove(m_joinState)) {
				getChildStates().add(m_joinState);
			}
			
			// Join the child state vertex with transitions
			int maxwidth=0;

			for (int i=1; i < getChildStates().size()-1; i++) {
				BPMNActivity umls=(BPMNActivity)getChildStates().get(i);
				
				height += (umls.getHeight()+VERTICAL_GAP);
				
				if (umls.getWidth() > maxwidth) {
					maxwidth = umls.getWidth();
				}

				umls.transitionFrom(m_forkState, null);
				
				// Check if state is a junction
				Object endNode=umls.getEndNode();
				
				/* Do not remove endpoint junctions from
				 * parallel elements, as this can cause issues
				 * with layout
				if (getModelFactory().isJoin(endNode) || // instanceof org.eclipse.uml2.uml.MergeNode ||
						getModelFactory().isTerminal(endNode)) { // instanceof org.eclipse.uml2.uml.FlowFinalNode) {

					// Move the incoming transitions from the junction
					// to the next state
					java.util.List list=getModelFactory().getInboundControlLinks(endNode);
					for (int j=list.size()-1; j >= 0; j--) {
						Object transition=list.get(j);
						
						getModelFactory().setTarget(transition,
								m_joinState.getStartNode());
					}
					
					// Remove the junction
					getModelFactory().delete(endNode);
				} else {
				*/
					m_joinState.transitionFrom(umls, null);
				//}
			}
			
			width += maxwidth;
			
			if (height >= HORIZONTAL_GAP) {
				height -= HORIZONTAL_GAP;
			}
			
			if (height < m_forkState.getHeight()) {
				height = m_forkState.getHeight();
			}
			
			if (height < m_joinState.getHeight()) {
				height = m_joinState.getHeight();
			}
			
			setWidth(width);
			setHeight(height);

			adjustWidth(width);
			
			m_completed = true;
		}
	}
	
	/**
	 * This method returns the start node for the activites
	 * represented by this UML activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		return(m_forkState.getStartNode());
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this UML activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(m_joinState.getEndNode());
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(m_forkState);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(m_joinState);
	}
	
	public void adjustWidth(int width) {
		int extrawidth=m_forkState.getWidth()+m_joinState.getWidth()+
						(2 * HORIZONTAL_GAP);
		
		setWidth(width);
		
		// Adjust child widths
		for (int i=1; i < getChildStates().size()-1; i++) {
			BPMNActivity umls=(BPMNActivity)getChildStates().get(i);
			
			umls.adjustWidth(width-extrawidth);
		}
	}
	
	public void calculatePosition(int x, int y) {
		int cury=y;
		int midx=x+(getWidth()/2);
		int midy=y+(getHeight()/2);
		
		setX(x);
		setY(y);
		
		for (int i=1; i < getChildStates().size()-1; i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.calculatePosition(midx-(act.getWidth()/2), cury);
			//midy-(act.getHeight()/2));

			cury += (act.getHeight()+VERTICAL_GAP);
		}
		
		m_forkState.calculatePosition(x, midy);
		m_joinState.calculatePosition(x+getWidth()-
				m_joinState.getWidth(), midy);
	}
	
	public void draw(Object parent) {
		
		// Construct notation
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
		}
	}

	private boolean m_completed=false;
    private BPMNActivity m_forkState=null;
    private BPMNActivity m_joinState=null;
}
