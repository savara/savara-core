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
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.ProtocolModel;

/**
 * This class represents the state information associated with
 * the construction of a BPMN Activity model.
 *
 */
public class BPMNDiagram extends AbstractBPMNActivity {

	private Object m_diagram=null;
	private Object m_container=null;
    private java.util.Map<String,BPMNActivity> m_sendActivities=new java.util.HashMap<String,BPMNActivity>();
    private java.util.Map<String,BPMNActivity> m_receiveActivities=new java.util.HashMap<String,BPMNActivity>();
    
    private java.util.Map<String,BPMNPool> m_pools=new java.util.HashMap<String,BPMNPool>();
    private java.util.List<BPMNPool> m_duplicatePools=new java.util.Vector<BPMNPool>();
    
    private boolean m_sendReceiveAsControlLink=true;
    private boolean m_controlFromSendOnly=false;
    
	/**
	 * This is the constructor for the activity model.
	 * 
	 * @param choreoName The choreography name
	 * @param diagramName The diagram name
	 * @param parent The parent activity
	 * @param model The model factory
	 * @param folder The output folder
	 * @exception BPMN2GenerationException Failed to construct activity model
	 */
	public BPMNDiagram(String choreoName, String diagramName,
			BPMNActivity parent,
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation)
							throws BPMN2GenerationException {
		super(parent, model, notation);
		
		m_diagram = model.createDiagram();
	}
	
	/**
	 * This method returns the container associated with the
	 * activity.
	 * 
	 * @return The container
	 */
	public Object getContainer() {
		return(m_container);
	}
	
	/**
	 * This method sets the container associated with the
	 * BPMN model.
	 * 
	 * @param container The container
	 */
	public BPMNPool createPool(String participant) {
		BPMNPool ret=new BPMNPool(m_diagram, participant,
					this, getModelFactory(), getNotationFactory());
		
		if (m_pools.containsKey(participant)) {
			m_duplicatePools.add(ret);
		} else {
			m_pools.put(participant, ret);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the state machine name to be used for the
	 * supplied behavior type.
	 * 
	 * @param elem The behavior type
	 * @return The name
	 */
	public static String getName(Activity elem) {
		String ret="<unknown>";
		
		if (elem instanceof Protocol) {
			ret = ((Protocol)elem).getName();
			
			/* Causes multiple diagrams to be created
			if (((Protocol)elem).getRole() != null) {
				ret += "_"+((Protocol)elem).getRole().getName();
			}
			*/
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the supplied behavior type
	 * should result in a root entry/exit point from the
	 * state machine.
	 * 
	 * @param elem The behavior type
	 * @return Whether it is a root
	 */
	protected boolean isRoot(Activity elem) {
		boolean ret=false;
		
		if (elem instanceof Protocol &&
				((Protocol)elem).getParent() instanceof ProtocolModel) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the containing ActivityModel.
	 * 
	 * @return The activity model
	 */
	public BPMNDiagram getBPMNDiagram() {
		return(this);
	}
	
	/**
	 * This method returns the start node for the activites
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		//return(m_initialState.getStartNode());
		return(null);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this BPMN activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		//return(m_finalState.getEndNode());
		return(null);
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(null);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(null);
	}
	
	/**
	 * This method registers a send activity against the BPMN representation.
	 * 
	 * @param send The send activity
	 * @param activity The BPMN representation
	 */
	public void registerSendActivity(Interaction send,
						BPMNActivity activity) {
		if (isPoolDuplicate(activity) == false) {
			Annotation ann=AnnotationDefinitions.getAnnotation(send.getAnnotations(),
					AnnotationDefinitions.SOURCE_COMPONENT);
			
			if (ann != null && ann.getProperties().containsKey(AnnotationDefinitions.ID_PROPERTY)) {
				m_sendActivities.put((String)ann.getProperties().get(AnnotationDefinitions.ID_PROPERTY),
										activity);
			}
		}
	}
		
	/**
	 * This method registers a receive activity against the BPMN representation.
	 * 
	 * @param recv The receive activity
	 * @param activity The BPMN representation
	 */
	public void registerReceiveActivity(Interaction recv,
						BPMNActivity activity) {
		if (isPoolDuplicate(activity) == false) {
			Annotation ann=AnnotationDefinitions.getAnnotation(recv.getAnnotations(),
					AnnotationDefinitions.SOURCE_COMPONENT);
			
			if (ann != null && ann.getProperties().containsKey(AnnotationDefinitions.ID_PROPERTY)) {
				m_receiveActivities.put((String)ann.getProperties().get(AnnotationDefinitions.ID_PROPERTY),
										activity);
			}
		}
	}
	
	/**
	 * This method determines if the supplied activity is
	 * associated with a duplicate pool that will be deleted.
	 * 
	 * @param activity The activity
	 * @return Whether the activity is associated with a
	 * 					duplicate pool
	 */
	protected boolean isPoolDuplicate(BPMNActivity activity) {
		boolean ret=false;
		
		while (activity != null && (activity instanceof BPMNPool)
							== false) {
			activity = activity.getParent();
		}
		
		if (activity != null) {
			ret = m_duplicatePools.contains(activity);
		}
		
		return(ret);
	}
	
	/**
	 * This method completes the generation of the model.
	 *
	 */
	public void completeModel() {
		java.util.Iterator<String> iter=m_sendActivities.keySet().iterator();
		java.util.List<Object> messageLinks=new java.util.ArrayList<Object>();
		
		while (iter.hasNext()) {
			String sendId=iter.next();
			BPMNActivity sendActivity=(BPMNActivity)
							m_sendActivities.get(sendId);
			ReceiveActivity receiveActivity=(ReceiveActivity)
							m_receiveActivities.get(sendId);
			
			if (sendActivity != null && receiveActivity != null) {
				
				if (m_sendReceiveAsControlLink) {
				
					if (m_controlFromSendOnly) {
						// Break links to the receive node, as control will
						// be from the sending participant
						receiveActivity.breakLinks();
					}
					
					// Create control flow between the activities
					messageLinks.add(getModelFactory().
							createMessageLink(m_diagram,
							sendActivity.getEndNode(),
							receiveActivity.getStartNode(),
							receiveActivity.getReceive()));
					
					// Check if the receive and send activities have a link to the
					// same node, in which case remove the link from the send
					checkForRedundantTargetLinks(sendActivity, receiveActivity);
				}
			}
		}
		
		// Delete duplicate pools
		for (int i=0; i < m_duplicatePools.size(); i++) {
			BPMNPool pool=(BPMNPool)m_duplicatePools.get(i);
			getModelFactory().delete(pool.getContainer());
			getChildStates().remove(pool);
		}
		
		// Draw diagram
		int cury=0;
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNPool pool=(BPMNPool)getChildStates().get(i);
			
			pool.calculatePosition(0, cury);
			
			cury += (pool.getHeight()+100);
		}
		
		//getModelFactory().saveModel(m_folder+
		//		java.io.File.separator+m_name+"."+
		//		getModelFactory().getFileExtension(), m_diagram);
		
		// Construct notation
		Object diagramNotation=getNotationFactory().createDiagram(getModelFactory(), m_diagram,
					getX(), getY(), getWidth(), getHeight());
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNPool pool=(BPMNPool)getChildStates().get(i);

			pool.draw(diagramNotation);	
		}
		
		/* GPB: 25/4/08
		 * Don't generate message links, as diagram infers them
		 * anyway - and in the latest version the link positions
		 * the label incorrectly. If using the default message
		 * link is an issue, then need to investigate how to
		 * get label positioned correctly.
		 * 
		for (int i=0; i < messageLinks.size(); i++) {
			Object mesglink=messageLinks.get(i);
			
			getNotationFactory().createMessageLink(getModelFactory(),
								mesglink, diagramNotation);
		}
		*/

		//getNotationFactory().saveNotation(m_folder+
		//		java.io.File.separator+m_name+"."+
		//		getModelFactory().getFileExtension(), m_diagram,
		//		m_folder+java.io.File.separator+m_name+"."+
		//		getNotationFactory().getFileExtension(), diagramNotation);
	}
	
	protected void checkForRedundantTargetLinks(BPMNActivity source, BPMNActivity target) {
		
		// Check if the target and source activities have a link to the
		// same node, in which case remove the link from the source
		java.util.List<Object> outgoing1=getModelFactory().getOutboundControlLinks(target.getStartNode());
		
		for (int i=0; i < outgoing1.size(); i++) {
			Object ae1=outgoing1.get(i);
			
			java.util.List<Object> outgoing2=getModelFactory().getOutboundControlLinks(source.getStartNode());
			boolean f_found=false;
			
			for (int j=0; f_found==false && j < outgoing2.size(); j++){ 
				Object ae2=outgoing2.get(j);
				
				if (getModelFactory().getTarget(ae1) == 
							getModelFactory().getTarget(ae2)) {
					f_found = true;
					
					getModelFactory().delete(ae2);
				}
			}
		}
	}
		
}
