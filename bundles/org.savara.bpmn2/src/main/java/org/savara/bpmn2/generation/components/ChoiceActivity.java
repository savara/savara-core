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

import org.pi4soa.service.behavior.Choice;
import org.savara.tools.bpmn.generation.*;

/**
 * This class represents a selection of states within a
 * BPMN state machine.
 *
 */
public class ChoiceActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the choice state.
	 * 
	 * @param choice The choice
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ChoiceActivity(Choice choice, BPMNActivity parent,
			org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model,
			org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation)
						throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(choice);		
	}

	/**
	 * This method performs the initialization of the 
	 * choice state.
	 * 
	 * @param elem The choice
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(Choice elem) throws BPMN2GenerationException {
		
		// Get region
		//Activity region=getTopLevelActivity();
	
		// Determine if choice is event based, or data based
		boolean dataBased=true;
		
		java.util.List children=elem.getActivityTypes();
		for (int i=0; i < children.size(); i++) {
			org.pi4soa.service.behavior.ActivityType act=
				(org.pi4soa.service.behavior.ActivityType)children.get(i);
			
			if (act instanceof org.pi4soa.service.behavior.StructuralType) {
				org.pi4soa.service.behavior.StructuralType st=
					(org.pi4soa.service.behavior.StructuralType)act;
				
				if ((st.isConditionalGroupingConstruct() == false ||
						(st.isConditionalGroupingConstruct() &&
						st.isConditionObservable() == false)) &&
						st.isPredicateExtensionRequired() == false) {
					dataBased = false;
				}
				
				/*
				String expr=null;
				
				if (st instanceof org.pi4soa.service.behavior.Conditional) {
					expr = ((org.pi4soa.service.behavior.Conditional)st).getExpression();
				} else if (st instanceof org.pi4soa.service.behavior.While) {
					expr = ((org.pi4soa.service.behavior.While)st).getExpression();
				} else if (st instanceof org.pi4soa.service.behavior.When) {
					expr = ((org.pi4soa.service.behavior.When)st).getExpression();
				}
				
				if (expr != null && expr.trim().length() == 0) {
					expr = "";
				}
				
				m_expressions.add(expr);
				*/
			}
		}
		
		// If not data-based, then clear list of expressions
		/*
		if (dataBased == false) {
			m_expressions.clear();
		}
		*/
		
		// Create choice state
		Object choiceState=null;
		
		if (dataBased) {
			choiceState=getModelFactory().createDataBasedXORGateway(getContainer());
		} else {
			choiceState=getModelFactory().createEventBasedXORGateway(getContainer());
		}
		
			//region.createNode(null, UMLPackage.eINSTANCE.getDecisionNode());
		//choiceState.getInPartitions().add(getActivityPartition());
		
		m_choiceState = new JunctionActivity(choiceState, this,
				getModelFactory(), getNotationFactory());
		
		// Create junction state
		Object junctionState=null;
		
		if (dataBased) {
			junctionState=getModelFactory().createDataBasedXORGateway(getContainer());
		} else {
			junctionState=getModelFactory().createEventBasedXORGateway(getContainer());
		}

		//region.createNode(null, UMLPackage.eINSTANCE.getMergeNode());
		//junctionState.getInPartitions().add(getActivityPartition());

		m_junctionState = new JunctionActivity(junctionState, this,
				getModelFactory(), getNotationFactory());
	}
	
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			int width=m_choiceState.getWidth()+m_junctionState.getWidth()+
						(2 * HORIZONTAL_GAP);
			int height=0;
								
			// Move the junction state to the end of the list
			if (getChildStates().remove(m_junctionState)) {
				getChildStates().add(m_junctionState);
			}
			
			// Join the child state vertex with transitions
			int maxwidth=0;
			
			for (int i=1; i < getChildStates().size()-1; i++) {
				BPMNActivity umls=(BPMNActivity)getChildStates().get(i);
				
				height += umls.getHeight();
				
				if (i != 1) {
					height += VERTICAL_GAP;
				}
				
				if (umls.getWidth() > maxwidth) {
					maxwidth = umls.getWidth();
				}
				
				/*
				String expr=null;
				
				if (m_expressions.size() > 0) {
					expr = (String)m_expressions.get(i-1);
					
					if (expr != null && expr.length() == 0) {
						expr = null;
					}
				}
				*/
				
				umls.transitionFrom(m_choiceState, null);
				
				// Check if state is a junction
				Object endNode=umls.getEndNode();
				
				/*
				if (umls.getEndState().canDeleteEndNode() &&
						(getModelFactory().isJoin(endNode) || // instanceof org.eclipse.uml2.uml.MergeNode ||
						getModelFactory().isTerminal(endNode))) { // instanceof org.eclipse.uml2.uml.FlowFinalNode) {

					// Move the incoming transitions from the junction
					// to the next state
					java.util.List list=getModelFactory().getInboundControlLinks(endNode);
					for (int j=list.size()-1; j >= 0; j--) {
						Object transition=list.get(j);
						
						getModelFactory().setTarget(transition, m_junctionState.getStartNode());
						//transition.setTarget(m_junctionState.getStartNode());
					}
					
					// Remove the junction
					//endNode.destroy();
					getModelFactory().delete(endNode);
				} else {
				*/
					m_junctionState.transitionFrom(umls, null);
				//}
			}
			
			width += maxwidth;
			
			if (height < m_choiceState.getHeight()) {
				height = m_choiceState.getHeight();
			}
			
			if (height < m_junctionState.getHeight()) {
				height = m_junctionState.getHeight();
			}
			
			setWidth(width);
			setHeight(height);
			
			adjustWidth(width);
			
			m_completed = true;
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
		
		m_choiceState.calculatePosition(x, midy-(m_choiceState.getHeight()/2));
		m_junctionState.calculatePosition(x+getWidth()-
				m_junctionState.getWidth(),
				midy-(m_junctionState.getHeight()/2));
	}
	
	/**
	 * This method returns the start node for the activites
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		return(m_choiceState.getStartNode());
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(m_junctionState.getEndNode());
	}
		
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(m_choiceState);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(m_junctionState);
	}
	
	public void adjustWidth(int width) {
		
		int extrawidth=m_choiceState.getWidth()+m_junctionState.getWidth()+
						(2 * HORIZONTAL_GAP);
		
		setWidth(width);
		
		// Adjust child widths
		for (int i=1; i < getChildStates().size()-1; i++) {
			BPMNActivity umls=(BPMNActivity)getChildStates().get(i);
			
			umls.adjustWidth(width-extrawidth);
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
    private BPMNActivity m_choiceState=null;
    private BPMNActivity m_junctionState=null;
    //private java.util.Vector m_expressions=new java.util.Vector();
}
