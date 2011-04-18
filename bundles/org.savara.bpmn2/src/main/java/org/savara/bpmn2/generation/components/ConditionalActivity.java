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
import org.pi4soa.service.behavior.Conditional;
import org.savara.tools.bpmn.generation.BPMNGenerationException;

/**
 * This class represents a conditional decision within a
 * BPMN state machine.
 *
 */
public class ConditionalActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the conditional state.
	 * 
	 * @param conditional The conditional
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ConditionalActivity(Conditional conditional,
					BPMNActivity parent,
					org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model,
					org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation)
						throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(conditional);		
	}

	/**
	 * This method performs the initialization of the 
	 * conditional state.
	 * 
	 * @param elem The conditional
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(Conditional elem) throws BPMN2GenerationException {
		m_choiceElement = (elem.getParent() instanceof Choice);
		
		// TODO: condition expression causes problem when used as
		// name
		m_conditionalDescription = elem.getName();
		
		if (org.pi4soa.common.util.NamesUtil.isSet(elem.getExpression())) {
			m_conditionalExpression = elem.getExpression();
		}
		
		if (isChoiceElement() == false) {
			
			// Get region
			/*
			Activity region=getTopLevelActivity();
			
			// Create choice state
			ActivityNode choiceState = region.createNode(null,
						UMLPackage.eINSTANCE.getDecisionNode());
			choiceState.getInPartitions().add(getActivityPartition());
			*/
			Object choiceState=getModelFactory().createDataBasedXORGateway(getContainer());
			
			m_choiceState = new JunctionActivity(choiceState, this,
					getModelFactory(), getNotationFactory());
			
			// Create junction state
			/*
			ActivityNode junctionState = region.createNode(null,
					UMLPackage.eINSTANCE.getMergeNode());
			junctionState.getInPartitions().add(getActivityPartition());
			 */
			Object junctionState=getModelFactory().createDataBasedXORGateway(getContainer());
			
			m_junctionState = new JunctionActivity(junctionState, this,
					getModelFactory(), getNotationFactory());
		}
	}
	
	/**
	 * This method determines whether the conditional is a
	 * choice element.
	 * 
	 * @return Whether a choice element
	 */
	protected boolean isChoiceElement() {
		return(m_choiceElement);
	}
	
	/**
	 * This method indicates that the UML state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			
			if (isChoiceElement() == false) {
				
				// Move the junction state to the end of the list
				if (getChildStates().remove(m_junctionState)) {
					getChildStates().add(m_junctionState);
				}
			}
			
			// Join the child state vertex with transitions
			transitionSequentialNodes();
			
			if (isChoiceElement() == false) {
				
				// Place the condition on the outgoing transition
				// from the choice state, before adding the alternate
				// 'false' path
				java.util.List list=getModelFactory().getOutboundControlLinks(m_choiceState.getEndNode());

				if (list.size() > 0) {
					Object outgoing=list.get(0);

					/*
					org.eclipse.uml2.uml.OpaqueExpression expr=
						(org.eclipse.uml2.uml.OpaqueExpression)
						outgoing.createGuard("expression", null,
							UMLPackage.eINSTANCE.getOpaqueExpression());
					*/
					
					if (m_conditionalExpression != null) {
						//expr.getBodies().add(m_conditionalExpression);
						getModelFactory().setLinkExpression(outgoing,
								m_conditionalExpression);
					} else {
						//expr.getBodies().add("<non-observable>");								
						getModelFactory().setLinkExpression(outgoing,
								"<non-observable>");
					}
				}
				
				// Have direct transition from choice to junction
				// to represent the 'false' path
				Object transition=
						m_junctionState.transitionFrom(m_choiceState,
								"false()");
				//transition.setName("false");
				
				/*
				org.eclipse.uml2.uml.OpaqueExpression expr=
					(org.eclipse.uml2.uml.OpaqueExpression)
					transition.createGuard("expression", null,
						UMLPackage.eINSTANCE.getOpaqueExpression());
					
				expr.getBodies().add("false()");
				*/
			}
						
			if (isChoiceElement() == false) {
				int width=0;
				int height=0;
				
				// Calculate extra width
				/*
				width = m_junctionState.getWidth()+
						(HORIZONTAL_GAP*2)+
						m_choiceState.getWidth();
				*/
				
				height = m_choiceState.getHeight();
				
				if (height < m_junctionState.getHeight()) {
					height = m_junctionState.getHeight();
				}
				
				height += (VERTICAL_GAP*2);
				
				setWidth(getWidth()+width);
				setHeight(getHeight()+height);
			}
			
			m_completed = true;
		}
	}
	
	/**
	 * This method causes a transition to be established from
	 * the supplied BPMN state to the current state, applying
	 * any relevant information (such as event triggers
	 * or conditions) to the transition as appropriate.
	 * 
	 * @param fromVertex The source vertex
	 * @return The transition
	 */
	public Object transitionFrom(BPMNActivity fromVertex,
			String conditionalExpression) {
		Object ret=null;
		
		if (isChoiceElement()) {
			String expr=m_conditionalExpression;
			
			if (expr == null) {
				expr = "<non-observable>";
			}
			
			ret = super.transitionFrom(fromVertex, expr);
		} else {
			ret = super.transitionFrom(fromVertex, conditionalExpression);
		}

		if (ret != null && m_conditionalDescription != null) {
			String name=m_conditionalDescription.replace(' ', '_');
			getModelFactory().setLabel(ret, name);
		}
		
		return(ret);
	}

	/**
	 * This method returns the start node for the activites
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		Object ret=null;
		
		if (isChoiceElement() == false) {
			ret = m_choiceState.getStartNode();
			
		} else if (getChildStates().size() > 0) {
			BPMNActivity state=(BPMNActivity)getChildStates().get(0);
			ret = state.getStartNode();
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		Object ret=null;
		int len=0;
		
		if (isChoiceElement() == false) {
			ret = m_junctionState.getEndNode();
			
		} else if ((len=getChildStates().size()) > 0) {
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
		
		if (isChoiceElement() == false) {
			ret = m_choiceState;
			
		} else if (getChildStates().size() > 0) {
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
		
		if (isChoiceElement() == false) {
			ret = m_junctionState;
			
		} else if ((len=getChildStates().size()) > 0) {
			BPMNActivity state=(BPMNActivity)getChildStates().get(len-1);
			ret = state;
		}
		
		return(ret);
	}
	
	public void calculatePosition(int x, int y) {
		if (isChoiceElement()) {
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				if (act instanceof SequenceActivity) {
					act.calculatePosition(x, y);
				}
			}
			
		} else {
			int junctionY=y+getHeight()-VERTICAL_GAP;
			//int junctionY=y+(getHeight()/2);
			
			m_choiceState.calculatePosition(x, junctionY-
					(m_choiceState.getHeight()/2));
			m_junctionState.calculatePosition(x+getWidth()-
					m_junctionState.getWidth(), junctionY-
					(m_junctionState.getHeight()/2));
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				if (act instanceof SequenceActivity) {
					act.calculatePosition(x+
								m_choiceState.getWidth()+
								HORIZONTAL_GAP, y);
				}
			}
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
	private boolean m_choiceElement=false;
	
    private BPMNActivity m_choiceState=null;
    private BPMNActivity m_junctionState=null;
    private String m_conditionalDescription=null;
    private String m_conditionalExpression=null;
}
