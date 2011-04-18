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

import org.pi4soa.common.util.NamesUtil;
import org.pi4soa.service.behavior.BehaviorDescription;
import org.pi4soa.service.behavior.BehaviorType;
import org.pi4soa.service.behavior.CompletionHandler;
import org.pi4soa.service.behavior.ExceptionHandler;
import org.savara.tools.bpmn.generation.BPMNGenerationException;

/**
 * This class represents the state information associated with
 * the construction of a BPMN Activity model.
 *
 */
public class BPMNDiagram extends AbstractBPMNActivity {

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
			org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model,
			org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation,
			String folder) throws BPMN2GenerationException {
		super(parent, model, notation);
		
		m_name = choreoName+"_"+diagramName;
		
		m_diagram = model.createDiagram();
		m_folder = folder;
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
	public static String getName(BehaviorType elem) {
		String ret=elem.getName();
		
		if (elem instanceof BehaviorDescription) {
			ret = ((BehaviorDescription)elem).getName();
			
			if (org.pi4soa.common.util.NamesUtil.isSet(
					((BehaviorDescription)elem).getParticipant()) &&
				ret.endsWith("_"+((BehaviorDescription)elem).getParticipant())) {
				
				ret = ret.substring(0, ret.length()-
						((BehaviorDescription)elem).getParticipant().length()-1);
			}

		} else if (elem instanceof CompletionHandler) {
			ret = getName(((CompletionHandler)elem).
					getEnclosingBehaviorDescription())+"["+
					ret+"]";

		} else if (elem instanceof ExceptionHandler) {
			String excType=((ExceptionHandler)elem).getExceptionType();
			if (NamesUtil.isSet(excType) == false) {
				excType = "default";
			}
			
			ret = getName(((ExceptionHandler)elem).
					getEnclosingBehaviorDescription())+"[exception="+
					excType+"]";
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
	protected boolean isRoot(BehaviorType elem) {
		boolean ret=false;
		
		if (elem instanceof BehaviorDescription &&
				((BehaviorDescription)elem).isRoot()) {
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
	public void registerSendActivity(org.pi4soa.service.behavior.Send send,
						BPMNActivity activity) {
		if (isPoolDuplicate(activity) == false) {
			m_sendActivities.put(send, activity);
		}
	}
		
	/**
	 * This method registers a receive activity against the BPMN representation.
	 * 
	 * @param recv The receive activity
	 * @param activity The BPMN representation
	 */
	public void registerReceiveActivity(org.pi4soa.service.behavior.Receive recv,
						BPMNActivity activity) {
		if (isPoolDuplicate(activity) == false) {
			m_receiveActivities.put(recv.getGlobalDescriptionURI(), activity);
		}
	}
	
	/**
	 * This method registers an initiating perform activity
	 * against the BPMN representation.
	 * 
	 * @param perform The perform activity
	 * @param activity The BPMN representation
	 */
	public void registerInitiatingPerform(org.pi4soa.service.behavior.Perform perform,
						BPMNActivity activity) {
		m_initiatingPerforms.put(perform.getGlobalDescriptionURI(), activity);
	}
		
	/**
	 * This method registers an initiated perform activity
	 * against the BPMN representation.
	 * 
	 * @param perform The perform activity
	 * @param activity The BPMN representation
	 */
	public void registerInitiatedPerform(org.pi4soa.service.behavior.Perform perform,
						BPMNActivity activity) {
		m_initiatedPerforms.put(perform.getGlobalDescriptionURI(), activity);
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
	public void completeModel() throws BPMN2GenerationException {
		java.util.Enumeration iter=m_sendActivities.keys();
		java.util.Vector messageLinks=new java.util.Vector();
		
		while (iter.hasMoreElements()) {
			org.pi4soa.service.behavior.Send send=
				(org.pi4soa.service.behavior.Send)iter.nextElement();

			BPMNActivity sendActivity=(BPMNActivity)
							m_sendActivities.get(send);
			ReceiveActivity receiveActivity=(ReceiveActivity)
							m_receiveActivities.get(send.getGlobalDescriptionURI());
			
			if (sendActivity != null && receiveActivity != null) {
				
				if (m_sendReceiveAsControlLink) {
				
					if (m_controlFromSendOnly) {
						// Break links to the receive node, as control will
						// be from the sending participant
						receiveActivity.breakLinks();
					}
					
					// Create control flow between the activities
					/*
					Activity activity=getTopLevelActivity();
					ActivityEdge ret=(ActivityEdge)
							activity.createEdge(null, UMLPackage.eINSTANCE.getControlFlow());
					
					ret.setSource(sendActivity.getEndNode());
					ret.setTarget(receiveActivity.getStartNode());
					
					ret.setName(send.getOperationName());
					*/
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
		
		// Process initiating and initiated performs
		/* GPB: COMMENT OUT PERFORM LINKS FOR NOW
		iter = m_initiatingPerforms.keys();
		
		while (iter.hasMoreElements()) {
			String key=(String)iter.nextElement();

			PerformActivity initiatingPerform=(PerformActivity)
					m_initiatingPerforms.get(key);
			PerformActivity initiatedPerform=(PerformActivity)
					m_initiatedPerforms.get(key);
			
			if (initiatingPerform != null && initiatedPerform != null) {
				
				if (m_performedAsControlLink) {
					
					if (m_controlFromInitiatingPerformOnly) {
						// Break links to the receive node, as control will
						// be from the initiating participant
						initiatedPerform.breakLinks();
					}
					
					// Create control flow between the activities
					Activity activity=getTopLevelActivity();
					
					ActivityEdge ret=(ActivityEdge)
							activity.createEdge(null, UMLPackage.eINSTANCE.getControlFlow());
					
					ret.setSource(initiatingPerform.getEndNode());
					ret.setTarget(initiatedPerform.getStartNode());
					
					// Check if the initiating and initiated performs have a link to the
					// same node, in which case remove the link from the initiating/
					checkForRedundantTargetLinks(initiatingPerform, initiatedPerform);
					
				}
				
				if (m_performedAsDependencyLink) {
					initiatingPerform.getEndNode().createDependency(
							initiatedPerform.getStartNode());
				}
			}
		}		
		*/
		
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
		
		getModelFactory().saveModel(m_folder+
				java.io.File.separator+m_name+"."+
				getModelFactory().getFileExtension(), m_diagram);
		
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
		for (int i=0; i < messageLinks.size(); i++) {
			Object mesglink=messageLinks.get(i);
			
			getNotationFactory().createMessageLink(getModelFactory(),
								mesglink, diagramNotation);
		}
		*/

		getNotationFactory().saveNotation(m_folder+
				java.io.File.separator+m_name+"."+
				getModelFactory().getFileExtension(), m_diagram,
				m_folder+java.io.File.separator+m_name+"."+
				getNotationFactory().getFileExtension(), diagramNotation);
	}
	
	protected void checkForRedundantTargetLinks(BPMNActivity source, BPMNActivity target) {
		
		// Check if the target and source activities have a link to the
		// same node, in which case remove the link from the source
		java.util.List outgoing1=getModelFactory().getOutboundControlLinks(target.getStartNode());
		
		for (int i=0; i < outgoing1.size(); i++) {
			Object ae1=outgoing1.get(i);
			
			java.util.List outgoing2=getModelFactory().getOutboundControlLinks(source.getStartNode());
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
		
	//private boolean m_completed=false;
	private String m_name=null;
	private Object m_diagram=null;
	private String m_folder=null;
	private Object m_container=null;
	//private java.util.Hashtable m_pools=new java.util.Hashtable();
    //private BPMNActivity m_initialState=null;
    //private BPMNActivity m_finalState=null;
    private java.util.Hashtable m_sendActivities=new java.util.Hashtable();
    private java.util.Hashtable m_receiveActivities=new java.util.Hashtable();
    private java.util.Hashtable m_initiatingPerforms=new java.util.Hashtable();
    private java.util.Hashtable m_initiatedPerforms=new java.util.Hashtable();
    
    private java.util.Hashtable m_pools=new java.util.Hashtable();
    private java.util.Vector m_duplicatePools=new java.util.Vector();
    
    private boolean m_sendReceiveAsControlLink=true;
    private boolean m_controlFromSendOnly=false;
    
    //private boolean m_performedAsControlLink=true;
    //private boolean m_performedAsDependencyLink=false;
    //private boolean m_controlFromInitiatingPerformOnly=false;
}
