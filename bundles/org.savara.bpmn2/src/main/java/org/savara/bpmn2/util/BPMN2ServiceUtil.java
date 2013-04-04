/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpmn2.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.savara.bpmn2.model.ObjectFactory;
import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TChoreographyTask;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.model.TError;
import org.savara.bpmn2.model.TFlowElement;
import org.savara.bpmn2.model.TFlowNode;
import org.savara.bpmn2.model.TInterface;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TMessageFlow;
import org.savara.bpmn2.model.TOperation;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.model.TRootElement;
import org.savara.bpmn2.model.TSequenceFlow;
import org.savara.bpmn2.model.TStartEvent;

/**
 * This class provides capabilities associated with the service
 * (or interface) declarations within BPMN model.
 *
 */
public class BPMN2ServiceUtil {

	public static final String INTERFACE_ID_SUFFIX = "Interface";
	private static final Logger LOG=Logger.getLogger(BPMN2ServiceUtil.class.getName());
	
	/**
	 * This method identifies the service interfaces (and their operations) associated
	 * with the participants defined within the supplied BPMN model. This method only
	 * creates a single interface per participant. The supplied BPMN model is not modified
	 * by this operation, the service interface details can be applied to the definition
	 * (after making any necessary changes) using the merge method.
	 * 
	 * @param defns The BPMN model
	 * @return The map of participants to interfaces
	 */
	public static java.util.Map<TParticipant,TInterface> introspect(TDefinitions defns) {
		java.util.Map<TParticipant,TInterface> ret=new java.util.HashMap<TParticipant,TInterface>();

		for (JAXBElement<? extends TRootElement> elem : defns.getRootElement()) {
			if (elem.getDeclaredType() == TChoreography.class) {
				TChoreography choreo=(TChoreography)elem.getValue();
				
				// Find initial node
				TStartEvent startEvent=null;
				
				for (JAXBElement<? extends TFlowElement> jaxb : choreo.getFlowElement()) {
					if (jaxb.getValue().getClass() == TStartEvent.class) {
						if (startEvent != null) {
							LOG.severe("Multiple start events found in choreography");
						} else {
							startEvent = (TStartEvent)jaxb.getValue();
						}
					}
				}
				
				if (startEvent != null) {
					processNode(defns, startEvent, ret, new java.util.Vector<InteractionInfo>(),
								new ModelInfo(choreo.getParticipant(),
									choreo.getMessageFlow(), choreo.getFlowElement(),
									defns.getRootElement(), defns.getTargetNamespace()),
									new java.util.Vector<TFlowNode>());
				}
			}
		}
				
		return (ret);
	}
	
	protected static void processNode(TDefinitions defns, TFlowNode node, java.util.Map<TParticipant,TInterface> intfs,
			java.util.List<InteractionInfo> ii, ModelInfo modelInfo,
				java.util.List<TFlowNode> processedNodes) {
		
		if (!processedNodes.contains(node)) {
			processedNodes.add(node);
			
			// Check if node is an interaction
			if (node instanceof TChoreographyTask) {
				processChoreographyTask(defns, (TChoreographyTask)node, intfs, ii, modelInfo);
			}
			
			for (QName outgoing : node.getOutgoing()) {
				TSequenceFlow sf=(TSequenceFlow)modelInfo.getFlowElement(outgoing.getLocalPart());
				
				if (sf != null) {
					
					if (sf.getTargetRef() != null) {
						processNode(defns, (TFlowNode)sf.getTargetRef(), intfs,
								new java.util.Vector<InteractionInfo>(ii), modelInfo, processedNodes);
					} else if (LOG.isLoggable(Level.FINE)) {
						LOG.fine("Node's outgoing flow has no target ref: "+node);
					}
				}
			}
		}
	}
	
	protected static void processChoreographyTask(TDefinitions defns, TChoreographyTask node,
					java.util.Map<TParticipant,TInterface> intfs,
					java.util.List<InteractionInfo> ii, ModelInfo modelInfo) {
		InteractionInfo initiatingii=null;
		InteractionInfo respondingii=null;
		
		for (QName mflowQName : node.getMessageFlowRef()) {
			TMessageFlow mflow=modelInfo.getMessageFlow(mflowQName.getLocalPart());
			
			if (mflow != null) {
				TParticipant from=modelInfo.getParticipant(mflow.getSourceRef().getLocalPart());
				TParticipant to=modelInfo.getParticipant(mflow.getTargetRef().getLocalPart());
				TMessage mesg=modelInfo.getMessage(mflow.getMessageRef().getLocalPart());
				
				if (from != null && to != null && mesg != null) {
					if (mflow.getSourceRef().equals(node.getInitiatingParticipantRef())) {
						initiatingii = new InteractionInfo(from, to, mesg);
					} else {
						respondingii = new InteractionInfo(to, from, mesg);
					}
				}
			}
		}
		
		// Check if both initiating and responding interactions set
		if (initiatingii != null && respondingii != null) {
			// Assume matched pair
			storeMEP(defns, initiatingii, respondingii, intfs, modelInfo);
			
		} else if (initiatingii != null) {
			// Attempt to match with previous interaction
			InteractionInfo match=null;
			for (int i=ii.size()-1; match == null && i >= 0; i--) {
				InteractionInfo cur=ii.get(i);
				
				if (cur.getFrom().equals(initiatingii.getTo()) &&
						cur.getTo().equals(initiatingii.getFrom())) {
					match = cur;		
				}
			}
			
			if (match != null) {
				storeMEP(defns, match, initiatingii, intfs, modelInfo);
				
				ii.remove(match);
			} else {
			
				for (int i=ii.size()-1; match == null && i >= 0; i--) {
					InteractionInfo cur=ii.get(i);
					
					if (cur.getFrom().equals(initiatingii.getTo()) &&
							cur.getTo().equals(initiatingii.getFrom())) {
						match = cur;		
					}
				}
			
				// If previous interaction found with same from/to, then
				// record previous as one-way op
				if (match != null) {
					storeMEP(defns, match, null, intfs, modelInfo);
					
					ii.remove(match);
				}

				// Record new interaction
				ii.add(initiatingii);
			}
		} else if (respondingii != null) {
			// Error, can't have just a response??
			LOG.severe("Only a response interaction has been found for ChoreographyTask: "+node);
		} else {
			// Error no interactions found on choreography task
			LOG.severe("No interactions have been found for ChoreographyTask: "+node);
		}
	}
	
	protected static void storeMEP(TDefinitions defns, InteractionInfo req, InteractionInfo resp,
					java.util.Map<TParticipant,TInterface> intfs, ModelInfo modelInfo) {
		TParticipant participant=req.getTo();
		TInterface intf=intfs.get(participant);
		
		// Check if interface has been defined - if not, then create
		if (intf == null) {
			String intfName=getInterfaceName(participant);
			
			intf = new TInterface();
			intf.setId(intfName+INTERFACE_ID_SUFFIX);
			intf.setName(intfName);
			
			String ns=modelInfo.getTargetNamespace()+"/"+participant.getName();
			String prefix=findNamespacePrefix(defns, ns);
			
			QName impl=new QName(ns, participant.getName(), prefix);
			intf.setImplementationRef(impl);
			
			intfs.put(participant, intf);
		}
		
		// Find operation with request message type
		TOperation operation=null;
		
		for (TOperation op : intf.getOperation()) {
			if (op.getInMessageRef().getLocalPart().equals(req.getMessage().getId())) {
				operation = op;
				break;
			}
		}
		
		if (operation == null) {
			operation = new TOperation();
			
			String opname=getOperationName(req.getMessage());
			
			operation.setId(opname);
			operation.setName(opname);
			
			operation.setInMessageRef(new QName(modelInfo.getTargetNamespace(), req.getMessage().getId(), "tns"));
			
			intf.getOperation().add(operation);
		}
		
		if (resp != null) {
			// Check if Error exists for same ItemDefinition as the response Message
			TError err=modelInfo.getErrorForItemDefinition(resp.getMessage().getItemRef());
			
			if (err != null) {
				QName errQName=new QName(modelInfo.getTargetNamespace(), err.getId(), "tns");
				if (!operation.getErrorRef().contains(errQName)) {
					operation.getErrorRef().add(errQName);
				}
			} else {
				// Could be normal response, or fault without an error object
				
				// Check if response message already exists on operation
				if (operation.getOutMessageRef() == null) {
					operation.setOutMessageRef(new QName(modelInfo.getTargetNamespace(),
								resp.getMessage().getId(), "tns"));
					
				/* NOTE: Currently don't handle adding error objects, as this would
				 * modify the model. User has to ensure fault message types have an appropriate
				 * Error object.
				 *
				} else if (!operation.getOutMessageRef().getLocalPart().equals(resp.getMessage().getId())){
					boolean found=false;
									
					for (int i=0; !found && i < operation.getErrorRef().size(); i++) {
						found = operation.getErrorRef().get(i).getLocalPart().equals(
											resp.getMessage().getId());
					}
					
					if (!found) {
						err = new TError();
						err.setErrorCode(resp.getMessage().getName());
						err.setId(resp.getMessage().getName()+"Err");
						err.setStructureRef(resp.getMessage().getItemRef());
						modelInfo.addError(err);
						
						operation.getErrorRef().add(new QName(modelInfo.getTargetNamespace(), err.getId()));
					}
				*/
				}
			}
		}
	}
	
	protected static String findNamespacePrefix(TDefinitions defns, String namespace) {
		String ret=null;
		
		for (QName qname : defns.getOtherAttributes().keySet()) {
			String value=defns.getOtherAttributes().get(qname);
			
			if (value.equals(namespace) && qname.getLocalPart().startsWith("xmlns:")) {
				ret = qname.getLocalPart().substring(6);
			}
		}
		
		if (ret == null) {
			int i=1;
			
			while (ret == null) {
				ret = "intf"+(i++);
				
				QName qname=new QName(null, "xmlns:"+ret);
				
				if (defns.getOtherAttributes().containsKey(qname)) {
					ret = null;
				}
			}
			
			// Add namespace prefix declaration
			defns.getOtherAttributes().put(new QName(null, "xmlns:"+ret), namespace);
		}
		
		return (ret);
	}
	
	/**
	 * This method returns the default interface name associated
	 * with the supplied participant.
	 * 
	 * @param participant The participant
	 * @return The interface name
	 */
	public static String getInterfaceName(TParticipant participant) {
		return(participant.getName());
	}
	
	/**
	 * This method derives an operation name from the supplied message.
	 * 
	 * @param message The message
	 * @return The operation name
	 */
	public static String getOperationName(TMessage message) {
		String ret=Character.toLowerCase(message.getName().charAt(0))+
						message.getName().substring(1);
		
		ret = ret.replaceAll("Request", "");
		ret = ret.replaceAll("Response", "");
		ret = ret.replaceAll("Req", "");
		ret = ret.replaceAll("Resp", "");
		
		return(ret);
	}
		
	/**
	 * This method merges the supplied service interface information into the supplied
	 * BPMN model. If no interfaces exist in the model, then the supplied interfaces will be
	 * added. If interfaces already exist, then it will attempt to merge the operations
	 * into the existing one or more interfaces defined per participant.
	 * 
	 * @param defns The BPMN model
	 * @param interfaces The map of participants to interfaces
	 */
	public static void merge(TDefinitions defns, java.util.Map<TParticipant,TInterface> interfaces) {
		ObjectFactory factory=new ObjectFactory();
		
		java.util.List<TParticipant> participants=
					new java.util.Vector<TParticipant>(interfaces.keySet());
		
		Collections.sort(participants, new Comparator<TParticipant>() {
			public int compare(TParticipant o1, TParticipant o2) {
				return(o1.getName().compareTo(o2.getName()));
			}
		});
		
		ModelInfo modelInfo=new ModelInfo(null, null, null, defns.getRootElement(),
							defns.getTargetNamespace());
		
		for (TParticipant participant : participants) {
			TInterface intf=interfaces.get(participant);
			
			// Check whether participant already has an interface
			if (participant.getInterfaceRef().size() == 0) {
				// Add interface to model and reference it from participant
				defns.getRootElement().add(factory.createInterface(intf));
				
				participant.getInterfaceRef().add(
						new QName(modelInfo.getTargetNamespace(), intf.getId(), "tns"));
			} else {				
				for (TOperation op : intf.getOperation()) {
					// Find operation
					TOperation other=null;
					
					for (QName qname : participant.getInterfaceRef()) {
						TInterface otherintf=(TInterface)
									modelInfo.getRootElement(qname.getLocalPart());
						
						if (otherintf != null) {
							for (TOperation otherOp : otherintf.getOperation()) {
								if (op.getName().equals(otherOp.getName())) {
									other = otherOp;
									break;
								}
							}
							
							if (other != null) {
								break;
							}
						}
					}
					
					if (other == null) {
						// Install operation on first interface
						TInterface otherintf=(TInterface)
								modelInfo.getRootElement(participant.getInterfaceRef().
											get(0).getLocalPart());
						otherintf.getOperation().add(op);
					} else {
						// Merge operations
						mergeOperation(op, other);
					}
				}
			}
		}
	}
	
	protected static void mergeOperation(TOperation newOp, TOperation existingOp) {
		
		// If request message already set, then confirm that the request message
		// types are the same - otherwise copy
		boolean mergeResponse=false;
		
		if (existingOp.getInMessageRef() == null) {
			existingOp.setInMessageRef(newOp.getInMessageRef());
			mergeResponse = true;
		} else if (newOp.getInMessageRef() != null) {
			if (newOp.getInMessageRef().equals(existingOp.getInMessageRef())) {
				mergeResponse = true;
			} else {
				LOG.severe("Incompatible request message type for operation '"+
							existingOp.getName()+"'");
			}
		} else {
			mergeResponse = true;
		}
		
		if (mergeResponse) {
			// Check response
			if (existingOp.getOutMessageRef() == null) {
				existingOp.setOutMessageRef(newOp.getOutMessageRef());
			} else if (newOp.getOutMessageRef() != null &&
					!newOp.getOutMessageRef().equals(existingOp.getOutMessageRef())) {
				LOG.severe("Incompatible response message type for operation '"+
								existingOp.getName()+"'");
			}
			
			// Check error messages
			for (QName errQName : newOp.getErrorRef()) {
				if (!existingOp.getErrorRef().contains(errQName)) {
					existingOp.getErrorRef().add(errQName);
				}
			}
		}
	}
	
	/**
	 * This class is a wrapper for the pieces of information that
	 * constitute an interaction.
	 *
	 */
	protected static class InteractionInfo {
		
		private TParticipant _from=null;
		private TParticipant _to=null;
		private TMessage _message=null;
		
		public InteractionInfo(TParticipant from, TParticipant to, TMessage message) {
			_from = from;
			_to = to;
			_message = message;
		}
		
		public TParticipant getFrom() {
			return (_from);
		}
		
		public TParticipant getTo() {
			return (_to);
		}
		
		public TMessage getMessage() {
			return (_message);
		}
	}
	
	protected static class ModelInfo {
		
		private List<TParticipant> _participants=null;
		private List<TMessageFlow> _messageFlows=null;
		private List<JAXBElement<? extends TFlowElement>> _flowElements;
		private List<JAXBElement<? extends TRootElement>> _rootElements;
		private String _targetNamespace=null;
		private ObjectFactory _factory=new ObjectFactory();
		
		public ModelInfo(List<TParticipant> participants, List<TMessageFlow> messageFlows,
				List<JAXBElement<? extends TFlowElement>> flowElements,
				List<JAXBElement<? extends TRootElement>> rootElements,
				String targetNamespace) {
			_participants = participants;
			_messageFlows = messageFlows;
			_flowElements = flowElements;
			_rootElements = rootElements;
			_targetNamespace = targetNamespace;
		}
		
		public String getTargetNamespace() {
			return (_targetNamespace);
		}
		
		public void addError(TError err) {
			_rootElements.add(_factory.createError(err));
		}

		public TError getErrorForItemDefinition(QName itemRef) {
			TError ret=null;
			
			for (JAXBElement<? extends TRootElement> fejaxb : _rootElements) {
				TRootElement fe=fejaxb.getValue();
				if (fe instanceof TError && ((TError)fe).getStructureRef().equals(itemRef)) {
					ret = (TError)fe;
					break;
				}
			}
			
			return(ret);
		}

		public TParticipant getParticipant(String id) {
			TParticipant ret=null;
			
			for (TParticipant part : _participants) {
				if (part.getId().equals(id)) {
					ret = part;
					break;
				}
			}
			
			return (ret);
		}
		
		public TMessageFlow getMessageFlow(String id) {
			TMessageFlow ret=null;
			
			for (TMessageFlow mflow : _messageFlows) {
				if (mflow.getId().equals(id)) {
					ret = mflow;
					break;
				}
			}
			
			return (ret);
		}
		
		public TMessage getMessage(String id) {
			return((TMessage)getRootElement(id));
		}
		
		public TRootElement getRootElement(String id) {
			TRootElement ret=null;
			
			for (JAXBElement<? extends TRootElement> fejaxb : _rootElements) {
				TRootElement fe=fejaxb.getValue();
				if (fe.getId().equals(id)) {
					ret = fe;
					break;
				}
			}
			
			return (ret);
		}
		
		public TFlowElement getFlowElement(String id) {
			TFlowElement ret=null;
			
			for (JAXBElement<? extends TFlowElement> fejaxb : _flowElements) {
				TFlowElement fe=fejaxb.getValue();
				if (fe.getId().equals(id)) {
					ret = fe;
					break;
				}
			}
			
			return (ret);
		}

		public TError getError(String id) {
			return((TError)getRootElement(id));
		}
		
	}
}
