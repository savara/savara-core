/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.bpmn2.internal.generation.components;

import org.savara.bpmn2.internal.generation.BPMN2GenerationException;
import org.savara.bpmn2.model.TServiceTask;
import org.scribble.protocol.model.Interaction;

/**
 * This class represents a selection of states within a
 * BPMN state machine.
 *
 */
public class ServiceInvocationActivity extends AbstractBPMNActivity {
	
	private boolean _completed=false;
    private ServiceTaskActivity _serviceTaskState=null;
    private BPMNActivity _junctionState=null;
    private java.util.Map<BPMNActivity, BoundaryEvent> _faults=
    					new java.util.HashMap<BPMNActivity, BoundaryEvent>();

	/**
	 * This constructor initializes the choice state.
	 * 
	 * @param choice The choice
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ServiceInvocationActivity(Interaction request, BPMNActivity parent,
			org.savara.bpmn2.internal.generation.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation)
						throws BPMN2GenerationException {
		super(parent, model, notation);
		
		initialize(request);		
	}

	/**
	 * This method performs the initialization of the 
	 * choice state.
	 * 
	 * @param elem The choice
	 * @throws BPMN2GenerationException Failed to initialize
	 */
	protected void initialize(Interaction req) throws BPMN2GenerationException {
		
		// Create choice state
		_serviceTaskState = new ServiceTaskActivity(req, this,
				getModelFactory(), getNotationFactory());
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void addChildState(BPMNActivity child) {
		// Only create a junction if a sequence being created, which indicates a choice
		// following the initial send
		if (_junctionState == null && child instanceof SequenceActivity) {
			// Create junction state
			Object junctionState=getModelFactory().createEventBasedXORGateway(getContainer());

			_junctionState = new JunctionActivity(junctionState, this,
					getModelFactory(), getNotationFactory());
		}
		
		super.addChildState(child);
	}

	public TServiceTask getServiceTask() {
		return(_serviceTaskState.getServiceTask());
	}
	
	public void register(BPMNActivity act, BoundaryEvent be) {
		_faults.put(act, be);
	}
	
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
		
		if (_completed == false) {
			
			// Move the junction state to the end of the list
			if (getChildStates().remove(_junctionState)) {
				getChildStates().add(_junctionState);
			}
			
			if (_junctionState == null) {
				
				java.util.List<BPMNActivity> children=new java.util.Vector<BPMNActivity>();
				
				while (getChildStates().size() > 1) {
					BPMNActivity act=(BPMNActivity)getChildStates().get(1);
					
					// Move to the parent of the service invocation activity
					getChildStates().remove(1);
					
					children.add(act);
				}

				if (children.size() > 0) {
					promote(children, (AbstractBPMNActivity)getParent(), this);					
				}
				
				setWidth(_serviceTaskState.getWidth());
				setHeight(_serviceTaskState.getHeight());
				
			} else {
				int width=_serviceTaskState.getWidth()+_junctionState.getWidth()+
							(2 * HORIZONTAL_GAP);
				int height=0;
									
				// Join the child state vertex with transitions
				int maxwidth=0;
				
				BPMNActivity normalPath=null;
				
				for (int i=1; i < getChildStates().size()-1; i++) {
					BPMNActivity umls=(BPMNActivity)getChildStates().get(i);
					
					if (!_faults.containsKey(umls) && i > 1) {
						normalPath = umls;
					}
					
					height += umls.getHeight();
					
					if (i != 1) {
						height += VERTICAL_GAP;
					}
					
					if (umls.getWidth() > maxwidth) {
						maxwidth = umls.getWidth();
					}
				}
				
				if (normalPath != null) {
					// Move normal path to be first
					getChildStates().remove(normalPath);
					getChildStates().add(1, normalPath);
				}
				
				width += maxwidth;
				
				if (height < _serviceTaskState.getHeight()) {
					height = _serviceTaskState.getHeight();
				}
				
				if (height < _junctionState.getHeight()) {
					height = _junctionState.getHeight();
				}
				
				setWidth(width);
				setHeight(height);
				
				adjustWidth(width);
			}
			
			_completed = true;
		}
	}
	
	public void calculatePosition(int x, int y) {
		int cury=y;
		int midy=y+(getHeight()/2);
		
		setX(x);
		setY(y);
		
		int boundaryCount=0;
		
		for (int i=1; i < getChildStates().size()-1; i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.calculatePosition(x+_serviceTaskState.getWidth()+HORIZONTAL_GAP, cury);

			cury += (act.getHeight()+VERTICAL_GAP);
			
			BoundaryEvent be=_faults.get(act);
			
			if (be != null) {
				be.calculatePosition(x+(30*boundaryCount++), midy+(_serviceTaskState.getHeight()/2)-15);
			}
		}
		
		_serviceTaskState.calculatePosition(x, midy-(_serviceTaskState.getHeight()/2));
		
		if (_junctionState != null) {
			_junctionState.calculatePosition(x+getWidth()-
					_junctionState.getWidth(),
					midy-(_junctionState.getHeight()/2));
		}
	}
	
	/**
	 * This method returns the start node for the activites
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		return(_serviceTaskState.getStartNode());
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(_junctionState == null ? _serviceTaskState.getEndNode() : _junctionState.getEndNode());
	}
		
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(_serviceTaskState);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(_junctionState == null ? _serviceTaskState : _junctionState);
	}
	
	public void adjustWidth(int width) {
		
		if (_junctionState == null) {
			setWidth(width);
		} else {
			int extrawidth=_serviceTaskState.getWidth()+_junctionState.getWidth()+
							(2 * HORIZONTAL_GAP);
			
			setWidth(width);
			
			// Adjust child widths
			for (int i=1; i < getChildStates().size()-1; i++) {
				BPMNActivity umls=(BPMNActivity)getChildStates().get(i);
				
				umls.adjustWidth(width-extrawidth);
			}
		}
	}
	
	public void draw(Object parent) {
		
		// Construct notation
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			act.draw(parent);
		}
	
		if (_junctionState != null) {
			// Construct sequence links
			for (int i=1; i < getChildStates().size(); i++) {
				BPMNActivity act=(BPMNActivity)getChildStates().get(i);
				if (act != _junctionState) {
					BoundaryEvent be=_faults.get(act);
					
					if (be != null) {
						be.draw(parent);
						
						if (act.getStartState() != null) {
							be.transitionTo(act, null, parent);
						} else {
							be.transitionTo(_junctionState, null, parent);
						}
					} else {
						if (act.getStartState() != null) {
							getStartState().transitionTo(act, null, parent);
						} else {
							getStartState().transitionTo(_junctionState, null, parent);
						}
					}
					
					if (act.getEndState() != null) {
						act.getEndState().transitionTo(_junctionState, null, parent);
					}
				}
			}
		}
	}
}
