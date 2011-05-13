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

import javax.xml.namespace.QName;

import org.savara.bpmn2.model.BPMNEdge;
import org.savara.bpmn2.model.Point;
import org.savara.bpmn2.model.TBoundaryEvent;
import org.savara.bpmn2.model.TSubProcess;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Try;

/**
 * This class represents the BPMN activity node for a Perform activity.
 * 
 */
public class TryActivity extends AbstractBPMNActivity {

	private boolean m_completed=false;
	
	private static final int CATCH_OFFSET=50;
	
	/**
	 * This constructor initializes the receive state.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public TryActivity(Activity act, BPMNActivity parent,
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
		super(parent, model, notation);
	}
	
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (m_completed == false) {
			int height=0;
			int maxwidth=0;
			
			for (int i=0; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				int width=act.getWidth();
				
				if (i > 0) {
					width += (getChildStates().size()-i) * CATCH_OFFSET;
				}
				
				if (width > maxwidth) {
					maxwidth = width;
				}
				
				height += act.getHeight()+VERTICAL_GAP;
			}
			
			// Add padding
			setHeight(height-VERTICAL_GAP);
			setWidth(maxwidth+(HORIZONTAL_GAP*2));
			
			m_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int curx=x+HORIZONTAL_GAP;
		int cury=y;
		
		setX(x);
		setY(y); //+VERTICAL_GAP);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			int newx=curx;
			
			if (i > 0) {
				newx += (getChildStates().size()-i) * CATCH_OFFSET;
			}
			
			act.calculatePosition(newx, cury);
			
			cury += act.getHeight()+VERTICAL_GAP;
		}
	}
	
	public void draw(Object parent) {
		TryBlockActivity tryblock=(TryBlockActivity)getChildStates().get(0);
		
		tryblock.draw(parent);
		
		for (int i=1; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
			
			if (i > 0) {
				TBoundaryEvent boundaryEvent=(TBoundaryEvent)getModelFactory().createBoundaryEvent(getContainer());
				
				boundaryEvent.setAttachedToRef(new QName(tryblock.getSubProcess().getId()));
				
				getNotationFactory().createEvent(getModelFactory(), boundaryEvent,
						parent, tryblock.getX()+((getChildStates().size()-i-1)*CATCH_OFFSET),
						tryblock.getY()+tryblock.getHeight()-15, 30, 30);
				
				// Draw throw event to this catch block
				Object link=getModelFactory().createControlLink(getContainer(),
						boundaryEvent, act.getStartNode(), null);
				
				BPMNEdge edge=(BPMNEdge)getNotationFactory().createSequenceLink(getModelFactory(), link, parent);

				edge.getWaypoint().get(0).setX(edge.getWaypoint().get(0).getX()-15);
				edge.getWaypoint().get(0).setY(edge.getWaypoint().get(0).getY()+15);
				
				Point p1=new Point();
				p1.setY(edge.getWaypoint().get(1).getY());
				p1.setX(edge.getWaypoint().get(0).getX());

				edge.getWaypoint().add(1, p1);
			}
		}
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
		
	public Object getStartNode() {
		BPMNActivity act=(BPMNActivity)getChildStates().get(0);
		return(act.getStartNode());
	}
	
	public Object getEndNode() {
		BPMNActivity act=(BPMNActivity)getChildStates().get(0);
		return(act.getEndNode());
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
