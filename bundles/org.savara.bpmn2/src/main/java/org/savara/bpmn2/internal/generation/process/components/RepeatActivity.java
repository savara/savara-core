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

import org.savara.bpmn2.internal.generation.process.BPMN2GenerationException;
import org.savara.bpmn2.model.BPMNEdge;
import org.savara.bpmn2.model.Point;
import org.scribble.protocol.model.Repeat;

/**
 * This class represents a while repetition within a
 * state machine.
 *
 */
public class RepeatActivity extends AbstractBPMNActivity {

	private boolean m_completed=false;
	
    private BPMNActivity m_initialChoiceState=null;
    //private BPMNActivity m_endChoiceState=null;
    //private BPMNActivity m_junctionState=null;

    /**
	 * This constructor initializes the while state.
	 * 
	 * @param while The while
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public RepeatActivity(Repeat repeat, BPMNActivity parent,
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation)
					throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(repeat);		
	}

	/**
	 * This method performs the initialization of the 
	 * repeat's state.
	 * 
	 * @param elem The repeat
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(Repeat elem) throws BPMN2GenerationException {
		Object choiceState=getModelFactory().createDataBasedXORGateway(getContainer());
		
		m_initialChoiceState = new JunctionActivity(choiceState, this,
				getModelFactory(), getNotationFactory());
		
		/*
		Object endChoiceState=getModelFactory().createDataBasedXORGateway(getContainer());
		
		m_endChoiceState = new JunctionActivity(endChoiceState, this,
				getModelFactory(), getNotationFactory());

		Object junctionState=getModelFactory().createDataBasedXORGateway(getContainer());
		
		m_junctionState = new JunctionActivity(junctionState, this,
				getModelFactory(), getNotationFactory());
		*/
	}
	
	/**
	 * This method indicates that the UML state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			
			/*
			// Move the end choice state to the end of the list
			if (m_endChoiceState != null &&
					getChildStates().remove(m_endChoiceState)) {
				getChildStates().add(m_endChoiceState);
			}
			*/
			
			/*
			// Move the junction state to the end of the list
			boolean junctionRemoved=getChildStates().remove(m_junctionState);
			*/
			
			// Join the child state vertex with transitions
			// Don't join the endChoice and junction states here
			transitionSequentialNodes();
			
			if (m_initialChoiceState != null) {
				java.util.List<Object> list=
							getModelFactory().getOutboundControlLinks(m_initialChoiceState.getEndNode());
	
				if (list.size() > 0) {
					Object outgoing=list.get(0);
	
					//if (m_conditionalExpression != null) {
					//	getModelFactory().setLinkExpression(outgoing,
					//			m_conditionalExpression);
					//} else {
						getModelFactory().setLinkExpression(outgoing,
								"<non-observable>");
					//}
				}
			}
			
			// Junction is re-added after setting up the sequential transitions,
			// so that it is not sequentially transitioned - only transition
			// to the junction state based on an appropriate condition
			// evaluation
			//BPMNActivity lastState=(BPMNActivity)getChildStates().
			//					get(getChildStates().size()-1);
			
			/*
			if (junctionRemoved) {
				getChildStates().add(m_junctionState);				
			}

			m_junctionState.transitionFrom(m_initialChoiceState,
						"false()");
			
			
			getStartState().transitionFrom(m_endChoiceState,
						//m_repeatExpression != null?
							//m_repeatExpression:
							"<non-observable>");
				
			m_junctionState.transitionFrom(m_endChoiceState,
					"false()");
			*/
			
			int width=0;
			int height=0;
			
			// Calculate extra width
			width = m_initialChoiceState.getWidth()+HORIZONTAL_GAP; //+
					//m_junctionState.getWidth()+HORIZONTAL_GAP;
			
			height = m_initialChoiceState.getHeight();
			
			/*
			if (height < m_junctionState.getHeight()) {
				height = m_junctionState.getHeight();
			}
			*/
			
			height += (VERTICAL_GAP*2);
			
			/*
			if (m_endChoiceState != null) {
				height += m_endChoiceState.getHeight()+VERTICAL_GAP;
			}
			*/
			
			setWidth(getWidth()+width);
			setHeight(getHeight()+height);
		
			m_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int junctionY=y+getHeight()-VERTICAL_GAP;
		
		//int extraY=0;
		
		m_initialChoiceState.calculatePosition(x, junctionY-
				(m_initialChoiceState.getHeight()/2));
		
		/*
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
		*/
		
		//SequenceActivity seq=null;
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			if (act instanceof SequenceActivity) {
				//seq = (SequenceActivity)act;
				
				//act.calculatePosition(x+
				//		m_initialChoiceState.getWidth()+
				//		HORIZONTAL_GAP, y+extraY);
				int gap=(VERTICAL_GAP/4);
				
				/*
				if (m_endChoiceState != null) {
					gap = VERTICAL_GAP;
				}
				*/
				
				act.calculatePosition(x+
						m_initialChoiceState.getWidth()+
						HORIZONTAL_GAP, junctionY-
						act.getHeight()-gap);
			}
		}
		
		/*
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
		*/

	}
	
	public void draw(Object parent) {
		
		// Construct notation
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
			
			if (i > 0) {
				BPMNActivity prev=(BPMNActivity)getChildStates().get(i-1);
				
				prev.transitionTo(act, null, parent);
			}
		}
		
		if (getChildStates().size() > 0) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(getChildStates().size()-1);
			
			Object link=getModelFactory().createControlLink(getContainer(),
					act.getEndNode(), getStartNode(), null);
			
			BPMNEdge edge=(BPMNEdge)getNotationFactory().createSequenceLink(getModelFactory(), link, parent);

			Point p1=new Point();
			p1.setY(edge.getWaypoint().get(0).getY());
			p1.setX(edge.getWaypoint().get(0).getX()+40);
			
			Point p2=new Point();
			p2.setY(act.getY()-20);
			p2.setX(edge.getWaypoint().get(0).getX()+40);
			
			Point p3=new Point();
			p3.setY(act.getY()-20);
			p3.setX(edge.getWaypoint().get(1).getX()+22);
			
			edge.getWaypoint().get(1).setX(edge.getWaypoint().get(1).getX()+22);
			edge.getWaypoint().get(1).setY(edge.getWaypoint().get(1).getY()-22);
			
			edge.getWaypoint().add(1, p1);
			edge.getWaypoint().add(2, p2);
			edge.getWaypoint().add(3, p3);

		}
	}

	public void transitionTo(BPMNActivity toNode, String expression, Object parent) {
		Object ret=getModelFactory().createControlLink(getContainer(),
				getEndNode(), toNode.getStartNode(), expression);
		
		BPMNEdge edge=(BPMNEdge)getNotationFactory().createSequenceLink(getModelFactory(), ret, parent);
		
		Point p1=new Point();
		p1.setY(edge.getWaypoint().get(0).getY());
		p1.setX(edge.getWaypoint().get(1).getX()-40);
		
		Point p2=new Point();
		p2.setY(edge.getWaypoint().get(1).getY());
		p2.setX(edge.getWaypoint().get(1).getX()-40);
		
		edge.getWaypoint().add(1, p1);
		edge.getWaypoint().add(2, p2);
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
		return(m_initialChoiceState);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(m_initialChoiceState);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this UML activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(m_initialChoiceState.getEndNode());
	}
	
	public boolean canDeleteEndNode() {
		return(false);
	}
	
}
