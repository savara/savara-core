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
import org.pi4soa.service.behavior.While;
import org.savara.tools.bpmn.generation.BPMNGenerationException;

/**
 * This class represents a while repetition within a
 * UML state machine.
 *
 */
public class WhileActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the while state.
	 * 
	 * @param while The while
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public WhileActivity(While whileElem, BPMNActivity parent,
			org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model,
			org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation)
					throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(whileElem);		
	}

	/**
	 * This method performs the initialization of the 
	 * whiles state.
	 * 
	 * @param elem The while
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(While elem) throws BPMN2GenerationException {
		m_choiceElement = (elem.getParent() instanceof Choice);

		m_conditionalDescription = elem.getName();
		
		if (org.pi4soa.common.util.NamesUtil.isSet(elem.getExpression())) {
			m_conditionalExpression = elem.getExpression();
		}

		if (org.pi4soa.common.util.NamesUtil.isSet(elem.getReEvaluateExpression()) &&
				elem.getReEvaluateExpression().equals("true()") == false) {
			m_repeatExpression = elem.getReEvaluateExpression();
			
			if (isChoiceElement() && m_conditionalExpression != null) {
				m_repeatExpression += " and "+m_conditionalExpression;
			}
		} else if (isChoiceElement() && m_conditionalExpression != null) {
			m_repeatExpression = m_conditionalExpression;
		}
		
		// Get region
		//Activity region=getTopLevelActivity();
		
		if (isChoiceElement() == false) {
			
			// Create choice state
			/*
			ActivityNode choiceState = region.createNode(null,
					UMLPackage.eINSTANCE.getDecisionNode());
			choiceState.getInPartitions().add(getActivityPartition());
			
			m_initialChoiceState = new SimpleActivity(choiceState, this);
			*/
			Object choiceState=getModelFactory().createDataBasedXORGateway(getContainer());
			
			m_initialChoiceState = new JunctionActivity(choiceState, this,
					getModelFactory(), getNotationFactory());
		}
				
		if (m_repeatExpression != null) {
			/*
			ActivityNode endChoiceState = region.createNode(null,
					UMLPackage.eINSTANCE.getDecisionNode());
			endChoiceState.getInPartitions().add(getActivityPartition());
			
			m_endChoiceState = new SimpleActivity(endChoiceState, this);
			*/
			Object endChoiceState=getModelFactory().createDataBasedXORGateway(getContainer());
			
			m_endChoiceState = new JunctionActivity(endChoiceState, this,
					getModelFactory(), getNotationFactory());
		}

		// Create junction state
		/*
		ActivityNode junctionState = region.createNode(null,
				UMLPackage.eINSTANCE.getMergeNode());
		junctionState.getInPartitions().add(getActivityPartition());
		
		m_junctionState = new SimpleActivity(junctionState, this);
		*/
		Object junctionState=getModelFactory().createDataBasedXORGateway(getContainer());
		
		m_junctionState = new JunctionActivity(junctionState, this,
				getModelFactory(), getNotationFactory());
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
			
			// Move the end choice state to the end of the list
			if (m_endChoiceState != null &&
					getChildStates().remove(m_endChoiceState)) {
				getChildStates().add(m_endChoiceState);
			}
			
			// Move the junction state to the end of the list
			boolean junctionRemoved=getChildStates().remove(m_junctionState);
			
			// Join the child state vertex with transitions
			// Don't join the endChoice and junction states here
			transitionSequentialNodes();
			
			// Label the expression on the choice outgoing link
			/*
			java.util.List list=
				m_initialChoiceState.getEndNode().getOutgoings();
			if (list != null && list.size() > 0) {
				ActivityEdge t=(ActivityEdge)list.get(0);
				
				if (t != null && m_conditionalDescription != null) {
					String name=m_conditionalDescription.replace(' ', '_');
					t.setName(name);
					
					org.eclipse.uml2.uml.OpaqueExpression expr=
						(org.eclipse.uml2.uml.OpaqueExpression)
						t.createGuard("expression", null,
							UMLPackage.eINSTANCE.getOpaqueExpression());
						
					if (m_conditionalExpression != null) {
						expr.getBodies().add(m_conditionalExpression);
					} else {
						expr.getBodies().add("<non-observable>");
					}
				}
			}
			*/
			
			if (m_initialChoiceState != null) {
				java.util.List list=getModelFactory().getOutboundControlLinks(m_initialChoiceState.getEndNode());
	
				if (list.size() > 0) {
					Object outgoing=list.get(0);
	
					if (m_conditionalExpression != null) {
						getModelFactory().setLinkExpression(outgoing,
								m_conditionalExpression);
					} else {
						getModelFactory().setLinkExpression(outgoing,
								"<non-observable>");
					}
				}
			}
			
			// Junction is re-added after setting up the sequential transitions,
			// so that it is not sequentially transitioned - only transition
			// to the junction state based on an appropriate condition
			// evaluation
			BPMNActivity lastState=(BPMNActivity)getChildStates().
								get(getChildStates().size()-1);
			
			if (junctionRemoved) {
				getChildStates().add(m_junctionState);				
			}

			if (isChoiceElement() == false) {
				
				// Have direct transition from choice to junction
				// to represent the 'false' path
				/*
				ActivityEdge transition=
						m_junctionState.transitionFrom(m_initialChoiceState);
				//transition.setName("false");
			
				org.eclipse.uml2.uml.OpaqueExpression expr=
					(org.eclipse.uml2.uml.OpaqueExpression)
					transition.createGuard("expression", null,
						UMLPackage.eINSTANCE.getOpaqueExpression());
				
				expr.getBodies().add("false()");
				*/
				m_junctionState.transitionFrom(m_initialChoiceState,
						"false()");
			}
			
			if (m_repeatExpression != null) {
				// Transition from end choice state to start vertex
				// to represent the loop back
				/*
				ActivityEdge loopback=
					getStartState().transitionFrom(m_endChoiceState);
				
				if (loopback != null && m_repeatExpression != null) {
					org.eclipse.uml2.uml.OpaqueExpression expr=
						(org.eclipse.uml2.uml.OpaqueExpression)
						loopback.createGuard("expression", null,
							UMLPackage.eINSTANCE.getOpaqueExpression());
					
					expr.getBodies().add(m_repeatExpression);
				}
				*/
				
				getStartState().transitionFrom(m_endChoiceState,
						m_repeatExpression != null?
							m_repeatExpression:"<non-observable>");
				
				// Transition from end choice state to junction, to
				// represent the situation where the loopback does
				// not occur
				/*
				ActivityEdge whileEnd=
					m_junctionState.transitionFrom(m_endChoiceState);
				
				org.eclipse.uml2.uml.OpaqueExpression expr=
					(org.eclipse.uml2.uml.OpaqueExpression)
					whileEnd.createGuard("expression", null,
						UMLPackage.eINSTANCE.getOpaqueExpression());
				
				expr.getBodies().add("false()");
				*/
				m_junctionState.transitionFrom(m_endChoiceState,
						"false()");
			} else {
				// Establish transition from last state to initial choice
				//m_initialChoiceState.transitionFrom(lastState, null);
				getStartState().transitionFrom(lastState, null);
				
				if (isChoiceElement()) {
					m_junctionState.transitionFrom(lastState,
								"false()");
				}
			}
			
			if (isChoiceElement() == false) {
				int width=0;
				int height=0;
				
				// Calculate extra width
				width = m_initialChoiceState.getWidth()+HORIZONTAL_GAP+
						m_junctionState.getWidth()+HORIZONTAL_GAP;
				
				height = m_initialChoiceState.getHeight();
				
				if (height < m_junctionState.getHeight()) {
					height = m_junctionState.getHeight();
				}
				
				height += (VERTICAL_GAP*2);
				
				if (m_endChoiceState != null) {
					height += m_endChoiceState.getHeight()+VERTICAL_GAP;
				}
				
				setWidth(getWidth()+width);
				setHeight(getHeight()+height);
			} else {
				
				int width=0;
				int height=0;
				
				// Calculate extra width
				width = m_junctionState.getWidth()+HORIZONTAL_GAP;
				
				setWidth(getWidth()+width);
			}
			
			m_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int junctionY=y+getHeight()-VERTICAL_GAP;
		
		if (isChoiceElement()) {
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				if (act instanceof SequenceActivity) {
					act.calculatePosition(x, y);
				}
			}
			
			m_junctionState.calculatePosition(x+getWidth()-
					m_junctionState.getWidth(), junctionY-
					(m_junctionState.getHeight()/2));
			
			if (m_endChoiceState != null) {
				m_endChoiceState.calculatePosition(x+getWidth()-
					m_junctionState.getWidth()-HORIZONTAL_GAP-
					m_endChoiceState.getWidth(),
					junctionY-m_endChoiceState.getHeight()-
					(VERTICAL_GAP/2));
				//extraY = m_endChoiceState.getHeight()+VERTICAL_GAP;
			}
			
		} else {
			//int junctionY=y+(getHeight()/2);
			int extraY=0;
			
			m_initialChoiceState.calculatePosition(x, junctionY-
					(m_initialChoiceState.getHeight()/2));
			m_junctionState.calculatePosition(x+getWidth()-
					m_junctionState.getWidth(), junctionY-
					(m_junctionState.getHeight()/2));
			
			if (m_endChoiceState != null) {
				m_endChoiceState.calculatePosition(x+getWidth()-
					m_junctionState.getWidth()-HORIZONTAL_GAP-
					m_endChoiceState.getWidth(),
					junctionY-m_endChoiceState.getHeight()-
					(VERTICAL_GAP/2));
				//extraY = m_endChoiceState.getHeight()+VERTICAL_GAP;
			}
			
			SequenceActivity seq=null;
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				if (act instanceof SequenceActivity) {
					seq = (SequenceActivity)act;
					
					//act.calculatePosition(x+
					//		m_initialChoiceState.getWidth()+
					//		HORIZONTAL_GAP, y+extraY);
					int gap=(VERTICAL_GAP/4);
					
					if (m_endChoiceState != null) {
						gap = VERTICAL_GAP;
					}
					
					act.calculatePosition(x+
							m_initialChoiceState.getWidth()+
							HORIZONTAL_GAP, junctionY-
							act.getHeight()-gap);
				}
			}
			
			// If a normal expression, then move the end
			// node of the last child, to help with layout
			if (m_endChoiceState == null && seq.getChildStates().size() > 0) {
				BPMNActivity lastState=(BPMNActivity)seq.getEndState();
				
				lastState.calculatePosition(x+getWidth()-
						lastState.getWidth()-HORIZONTAL_GAP-
						m_junctionState.getWidth(),
						junctionY-lastState.getHeight()-
						(VERTICAL_GAP/2));
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

	/**
	 * This method returns the start node for the activites
	 * represented by this UML activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		Object ret=null;
		BPMNActivity state=getStartState();
		
		if (state != null) {
			ret = state.getStartNode();
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
			ret = m_initialChoiceState;
			
		} else if (getChildStates().size() > 0) {
			ret = (BPMNActivity)getChildStates().get(0);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(m_junctionState);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this UML activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(m_junctionState.getEndNode());
	}
	
	public boolean canDeleteEndNode() {
		return(false);
	}
	
	private boolean m_completed=false;
	private boolean m_choiceElement=false;
	
    private BPMNActivity m_initialChoiceState=null;
    private BPMNActivity m_endChoiceState=null;
    private BPMNActivity m_junctionState=null;
    private String m_conditionalDescription=null;
    private String m_conditionalExpression=null;
    private String m_repeatExpression=null;
}
