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

import org.pi4soa.service.behavior.When;
import org.savara.tools.bpmn.generation.BPMNGenerationException;

/**
 * This class represents a when activity within a
 * UML state machine.
 *
 */
public class WhenActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the when state.
	 * 
	 * @param when The when
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public WhenActivity(When when, BPMNActivity parent,
			org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model,
			org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation)
					throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(when);		
	}

	/**
	 * This method performs the initialization of the 
	 * when's state.
	 * 
	 * @param elem The when
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(When elem) throws BPMN2GenerationException {
		
		if (org.pi4soa.common.util.NamesUtil.isSet(elem.getExpression())) {
			m_conditionalExpression = elem.getExpression();
		}

		if (org.pi4soa.common.util.NamesUtil.isSet(elem.getReEvaluateExpression())) {
			m_repeatExpression = elem.getReEvaluateExpression();
		}
				
		// Get region
		/*
		Activity region=getTopLevelActivity();
		
		ActivityNode initialState = region.createNode(null,
				UMLPackage.eINSTANCE.getCallBehaviorAction());
		initialState.setName(UMLGenerationUtil.processName("When "+elem.getName()));
		initialState.getInPartitions().add(getActivityPartition());
		m_initialState = new SimpleActivity(initialState, this);
		*/

		m_initialState = new SimpleActivity(elem, this, getModelFactory(),
				getNotationFactory());

		// Only create end choice if there is a possibility of repeating
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

			// Create junction state
			Object junctionState=getModelFactory().createFinalNode(getContainer());
			
			m_junctionState = new JunctionActivity(junctionState, this,
					getModelFactory(), getNotationFactory());
		}
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
			boolean junctionRemoved=false;
			
			if (m_junctionState != null) {
				junctionRemoved = getChildStates().remove(m_junctionState);
			}

			// If no repeat condition, then include junction before
			// setting up sequential transitions
			if (junctionRemoved && m_repeatExpression == null) {
				getChildStates().add(m_junctionState);				
			}
			
			// Join the child state vertex with transitions
			// Don't join the endChoice and junction states here
			transitionSequentialNodes();
			
			/*
			java.util.List list=m_initialState.getEndNode().getOutgoings();
			
			if (list.size() > 0) {
				ActivityEdge transition=(ActivityEdge)list.get(0);
				
				org.eclipse.uml2.uml.OpaqueExpression expr=
					(org.eclipse.uml2.uml.OpaqueExpression)
					transition.createGuard("expression", null,
							UMLPackage.eINSTANCE.getOpaqueExpression());
				
				if (m_conditionalExpression != null) {
					expr.getBodies().add(m_conditionalExpression);
				} else {
					expr.getBodies().add("<non-observable>");				
				}
			}
			*/
			java.util.List list=getModelFactory().getOutboundControlLinks(m_initialState.getEndNode());

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
			
			// Junction is re-added after setting up the sequential transitions,
			// so that it is not sequentially transitioned - only transition
			// to the junction state based on an appropriate condition
			// evaluation
			if (junctionRemoved && m_repeatExpression != null) {
				getChildStates().add(m_junctionState);				
			}
			
			// Transition from end choice state to start vertex
			// to represent the loop back
			if (m_repeatExpression != null) {
				/*
				ActivityEdge loopback=
					m_initialState.transitionFrom(m_endChoiceState);
				
				org.eclipse.uml2.uml.OpaqueExpression expr=
					(org.eclipse.uml2.uml.OpaqueExpression)
					loopback.createGuard("expression", null,
						UMLPackage.eINSTANCE.getOpaqueExpression());
				
				expr.getBodies().add(m_repeatExpression);
				*/
				m_initialState.transitionFrom(m_endChoiceState,
							m_repeatExpression);				
								
				// Transition from end choice state to junction, to
				// represent the situation where the loopback does
				// not occur
				/*
				ActivityEdge whenEnd=
					m_junctionState.transitionFrom(m_endChoiceState);
	
				expr = (org.eclipse.uml2.uml.OpaqueExpression)
						whenEnd.createGuard("expression", null,
						UMLPackage.eINSTANCE.getOpaqueExpression());
				
				expr.getBodies().add("false()");
				*/
				m_junctionState.transitionFrom(m_endChoiceState,
						"false()");
			}
			
			int width=0;
			int height=0;
			
			// Calculate extra width
			if (m_junctionState != null) {
				width = m_junctionState.getWidth()+HORIZONTAL_GAP;
			
				height = m_initialState.getHeight();
				
				if (m_junctionState != null &&
						height < m_junctionState.getHeight()) {
					height = m_junctionState.getHeight();
				}
				
				height += (VERTICAL_GAP*2);
				
				if (m_endChoiceState != null) {
					height += m_endChoiceState.getHeight()+VERTICAL_GAP;
				}
				
				setWidth(getWidth()+width);
				setHeight(getHeight()+height);
			}

			m_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		
		if (m_junctionState == null) {
			int curx=x;
			int midy=y+(getHeight()/2);
			
			setX(x);
			setY(y);
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				act.calculatePosition(curx, midy-(act.getHeight()/2));
				
				curx += (act.getWidth()+HORIZONTAL_GAP);
			}
		} else {
		/*
		if (isChoiceElement()) {
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				if (act instanceof SequenceActivity) {
					act.calculatePosition(x, y);
				}
			}
			
		} else {
		*/
			int junctionY=y+getHeight()-VERTICAL_GAP;
			//int junctionY=y+(getHeight()/2);
			int extraY=0;
			
			m_initialState.calculatePosition(x, junctionY-
					(m_initialState.getHeight()/2));
			
			if (m_junctionState != null) {
				m_junctionState.calculatePosition(x+getWidth()-
						m_junctionState.getWidth(), junctionY-
						(m_junctionState.getHeight()/2));
			}
			
			if (m_endChoiceState != null) {
				m_endChoiceState.calculatePosition(x+getWidth()-
//						m_junctionState.getWidth(), y);
						m_junctionState.getWidth()-
						HORIZONTAL_GAP, junctionY-
						(m_junctionState.getHeight()/2));
				//extraY = m_endChoiceState.getHeight()+VERTICAL_GAP;
			}
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				
				if (act instanceof SequenceActivity) {
					act.calculatePosition(x+
								m_initialState.getWidth()+
								HORIZONTAL_GAP, y+extraY);
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
		
		if (getChildStates().size() > 0) {
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
		BPMNActivity ret=null;
		
		if (m_junctionState != null) {
			ret = m_junctionState;
		} else if (getChildStates().size() > 0) {
			BPMNActivity act=(BPMNActivity)
					getChildStates().get(getChildStates().size()-1);
			ret = act;
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
		
		if (m_junctionState != null) {
			ret = m_junctionState.getEndNode();
		} else if (getChildStates().size() > 0) {
			BPMNActivity act=(BPMNActivity)
					getChildStates().get(getChildStates().size()-1);
			ret = act.getEndNode();
		}
		
		return(ret);
	}
	
	private boolean m_completed=false;
	
    private BPMNActivity m_initialState=null;
    private BPMNActivity m_endChoiceState=null;
    private BPMNActivity m_junctionState=null;
    private String m_conditionalExpression=null;    
    private String m_repeatExpression=null;    
}
