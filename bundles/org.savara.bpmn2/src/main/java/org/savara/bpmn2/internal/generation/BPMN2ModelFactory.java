/*
 * Copyright 2005-6 Pi4 Technologies Ltd
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
 * 26 Jan 2007 : Initial version created by gary
 */
package org.savara.bpmn2.internal.generation;

import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.savara.bpmn2.model.ObjectFactory;
import org.savara.bpmn2.model.TBaseElement;
import org.savara.bpmn2.model.TBoundaryEvent;
import org.savara.bpmn2.model.TCallActivity;
import org.savara.bpmn2.model.TCallChoreography;
import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TChoreographyTask;
import org.savara.bpmn2.model.TCollaboration;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.model.TEndEvent;
import org.savara.bpmn2.model.TError;
import org.savara.bpmn2.model.TExclusiveGateway;
import org.savara.bpmn2.model.TFlowElement;
import org.savara.bpmn2.model.TFlowNode;
import org.savara.bpmn2.model.TGateway;
import org.savara.bpmn2.model.TInclusiveGateway;
import org.savara.bpmn2.model.TIntermediateCatchEvent;
import org.savara.bpmn2.model.TIntermediateThrowEvent;
import org.savara.bpmn2.model.TLinkEventDefinition;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TMessageFlow;
import org.savara.bpmn2.model.TParallelGateway;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.model.TProcess;
import org.savara.bpmn2.model.TReceiveTask;
import org.savara.bpmn2.model.TRootElement;
import org.savara.bpmn2.model.TSendTask;
import org.savara.bpmn2.model.TSequenceFlow;
import org.savara.bpmn2.model.TServiceTask;
import org.savara.bpmn2.model.TStartEvent;
import org.savara.bpmn2.model.TSubChoreography;
import org.savara.bpmn2.model.TSubProcess;
import org.savara.bpmn2.model.TTask;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.model.Join;
import org.savara.protocol.model.Fork;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.Run;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.util.InteractionUtil;

public class BPMN2ModelFactory {
	
	private TDefinitions m_definitions=null;
	private TCollaboration m_collaboration=null;
	private TChoreography _choreography=null;
	private ObjectFactory m_factory=new ObjectFactory();
	private boolean m_consecutiveIds=false;
	private int m_id=1;
	
	public BPMN2ModelFactory(TDefinitions defns) {
		m_definitions = defns;
	}
	
	public void setUseConsecutiveIds(boolean b) {
		m_consecutiveIds = b;
	}
	
	public String createId() {
		if (m_consecutiveIds) {
			return("MID"+(m_id++));
		}
		return("id-"+UUID.randomUUID().toString());
	}

	public Object createDiagram() {
		return(m_definitions);
	}
	
	public TDefinitions getDefinitions() {
		return(m_definitions);
	}
	
	public TCollaboration getCollaboration() {
		return(m_collaboration);
	}
	
	public Object createPool(Object diagram, String name) {

		// Check if collaboration has already been defined - if not create it
		if (m_collaboration == null) {
			// Create collaboration
			m_collaboration = new TCollaboration();
			m_collaboration.setId(createId());
			
			m_definitions.getRootElement().add(m_factory.createCollaboration(m_collaboration));
		}
		
		// Create process
		TProcess process=new TProcess();
		process.setId(createId());
		
		process.setName(name);
		
		m_definitions.getRootElement().add(m_factory.createProcess(process));
		
		// Create participant in collaboration and point to process
		TParticipant participant=new TParticipant();
		participant.setId(createId());
		participant.setName(name);
		
		participant.setProcessRef(new QName(m_definitions.getTargetNamespace(),
							process.getId()));
		
		m_collaboration.getParticipant().add(participant);

		return(process);
	}
	
	public Object createChoreography(Object diagram, String name) {
		TChoreography choreo=new TChoreography();
		choreo.setId(createId());
		
		choreo.setName(name);
		
		m_definitions.getRootElement().add(m_factory.createChoreography(choreo));
		
		_choreography = choreo;
		
		return(choreo);
	}
	
	public Object createInitialNode(Object container) {
		TStartEvent startEvent=new TStartEvent();
		startEvent.setId(createId());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createStartEvent(startEvent));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createStartEvent(startEvent));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createStartEvent(startEvent));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createStartEvent(startEvent));
		}
		
		return(startEvent);
	}
	
	public Object createSimpleTask(Object container, Activity activity) {
		TTask task=new TTask();
		task.setId(createId());
		
		task.setName("task: "+activity);
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createTask(task));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createTask(task));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createTask(task));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createTask(task));
		}

		return(task);
	}
	
	public Object createCallActivity(Object container, Run run) {
		Object ret=null;
		
		if (container instanceof TProcess || container instanceof TSubProcess) {
			TCallActivity task=new TCallActivity();
			
			task.setName("Call: "+run.getProtocolReference().getName()+"_"+
					run.getProtocolReference().getRole());

			if (container instanceof TProcess) {
				((TProcess)container).getFlowElement().add(m_factory.createCallActivity((TCallActivity)task));
			} else if (container instanceof TSubProcess) {
				((TSubProcess)container).getFlowElement().add(m_factory.createCallActivity((TCallActivity)task));
			}

			task.setId(createId());
			
			ret = task;
		} else if (container instanceof TChoreography || container instanceof TSubChoreography) {
			TCallChoreography task=new TCallChoreography();
			
			task.setName("Call: "+run.getProtocolReference().getName()+"_"+
					run.getProtocolReference().getRole());

			if (container instanceof TChoreography) {
				((TChoreography)container).getFlowElement().add(m_factory.createCallChoreography((TCallChoreography)task));
			} else if (container instanceof TSubChoreography) {
				((TChoreography)container).getFlowElement().add(m_factory.createCallChoreography((TCallChoreography)task));
			}

			task.setId(createId());
			
			ret = task;
		}

		return (ret);
	}
	
	public Object createSubProcess(Object container) {
		TSubProcess task=new TSubProcess();
			
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createSubProcess((TSubProcess)task));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createSubProcess((TSubProcess)task));
		}

		task.setId(createId());

		return(task);
	}
	
	public Object createForkTask(Object container, Activity activity) {
		Fork sync=(Fork)activity;
		
		TIntermediateThrowEvent event=new TIntermediateThrowEvent();
		TLinkEventDefinition led=new TLinkEventDefinition();
		
		led.setName(sync.getLabel());
		event.getEventDefinition().add(m_factory.createLinkEventDefinition(led));

		event.setId(createId());
		
		event.setName(sync.getLabel());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createEvent(event));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createEvent(event));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createEvent(event));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createEvent(event));
		}

		return(event);
	}
	
	public Object createJoinTask(Object container, Activity activity) {
		Join join=(Join)activity;
		TGateway gw=null;
		
		if (join.getXOR()) {
			gw = new TExclusiveGateway();
			if (container instanceof TProcess) {
				((TProcess)container).getFlowElement().add(m_factory.createExclusiveGateway((TExclusiveGateway)gw));
			} else if (container instanceof TSubProcess) {
				((TSubProcess)container).getFlowElement().add(m_factory.createExclusiveGateway((TExclusiveGateway)gw));
			} else if (container instanceof TChoreography) {
				((TChoreography)container).getFlowElement().add(m_factory.createExclusiveGateway((TExclusiveGateway)gw));
			} else if (container instanceof TSubChoreography) {
				((TSubChoreography)container).getFlowElement().add(m_factory.createExclusiveGateway((TExclusiveGateway)gw));
			}
		} else {
			gw = new TParallelGateway();
			if (container instanceof TProcess) {
				((TProcess)container).getFlowElement().add(m_factory.createParallelGateway((TParallelGateway)gw));
			} else if (container instanceof TSubProcess) {
				((TSubProcess)container).getFlowElement().add(m_factory.createParallelGateway((TParallelGateway)gw));
			} else if (container instanceof TChoreography) {
				((TChoreography)container).getFlowElement().add(m_factory.createParallelGateway((TParallelGateway)gw));
			} else if (container instanceof TSubChoreography) {
				((TSubChoreography)container).getFlowElement().add(m_factory.createParallelGateway((TParallelGateway)gw));
			}
		}
		
		gw.setId(createId());
		
		return(gw);
	}
	
	public Object createLinkTarget(Object container, String label) {
		TIntermediateCatchEvent linkTarget=new TIntermediateCatchEvent();
		TLinkEventDefinition led=new TLinkEventDefinition();
		
		led.setName(label);
		linkTarget.getEventDefinition().add(m_factory.createLinkEventDefinition(led));
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createIntermediateCatchEvent(linkTarget));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createIntermediateCatchEvent(linkTarget));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createIntermediateCatchEvent(linkTarget));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createIntermediateCatchEvent(linkTarget));
		}
		
		linkTarget.setId(createId());
		
		linkTarget.setName(label);

		return(linkTarget);
	}
	
	public TParticipant getParticipant(String name) {
		TParticipant participant=null;
		java.util.List<TParticipant> participants=(_choreography != null ?
					_choreography.getParticipant() : m_collaboration.getParticipant());
		
		for (TParticipant p : participants) {
			if (p.getName().equals(name)) {
				participant = p;
				break;
			}
		}
		
		if (participant == null) {
			// Create participant in collaboration and point to process
			participant = new TParticipant();
			participant.setId(createId());
			participant.setName(name);
			
			participants.add(participant);
		}
		
		return(participant);
	}
	
	/**
	 * This method establishes the associated message details,
	 * if not defined, and returns a reference to it.
	 * 
	 * @param interaction The interaction
	 * @return The message reference
	 */
	protected QName getMessageReference(Interaction interaction) {
		QName ret=null;
		
		if (interaction.getMessageSignature() == null ||
				interaction.getMessageSignature().getTypeReferences().size() == 0) {
			return(null);
		}
		
		// Check for Message definition
		for (JAXBElement<? extends TRootElement> rootElem :
						getDefinitions().getRootElement()) {
			if (rootElem.getValue() instanceof TMessage) {
				TMessage mesg=(TMessage)rootElem.getValue();
				
				if (mesg.getName().equals(interaction.getMessageSignature().
								getTypeReferences().get(0).getName())) {
					ret = new QName(getDefinitions().getTargetNamespace(),
										mesg.getId());
					
					// Check if fault, and if so, that an error has been defined
					if (org.savara.protocol.model.util.InteractionUtil.isFaultResponse(interaction)) {
						String faultName=org.savara.protocol.model.util.InteractionUtil.getFaultName(interaction);
						boolean found=false;
						
						for (JAXBElement<? extends TRootElement> subRootElem :
										getDefinitions().getRootElement()) {
							
							if (subRootElem.getValue() instanceof TError &&
									((TError)subRootElem.getValue()).getStructureRef().equals(
											mesg.getItemRef()) &&
									((TError)subRootElem.getValue()).getName().equals(faultName)) {
								found = true;
							}
						}
						
						if (!found) {
							TError error=new TError();
							error.setId("ERR"+faultName);
							error.setName(faultName);
							error.setErrorCode(faultName);
							error.setStructureRef(mesg.getItemRef());
							getDefinitions().getRootElement().add(
									m_factory.createError(error));
						}
					}
					
					break;
				}
			}
		}
		
		return(ret);
	}
	
	public Object createChoreographyTask(Object container, Interaction interaction) {
		TChoreographyTask task=new TChoreographyTask();
		task.setId(createId());
		
		task.setName(interaction.getMessageSignature().toString());
		
		Role fromRole=interaction.getFromRole();
		TParticipant fromParticipant=getParticipant(fromRole.getName());
		
		Role toRole=interaction.getToRoles().get(0);
		TParticipant toParticipant=getParticipant(toRole.getName());
		
		task.getParticipantRef().add(new QName(m_definitions.getTargetNamespace(),
								fromParticipant.getId()));
		task.getParticipantRef().add(new QName(m_definitions.getTargetNamespace(),
								toParticipant.getId()));
		task.setInitiatingParticipantRef(new QName(m_definitions.getTargetNamespace(),
								fromParticipant.getId()));
		
		// Create message flow
		TMessageFlow mf=new TMessageFlow();
		mf.setId(createId());
		
		mf.setSourceRef(new QName(m_definitions.getTargetNamespace(),
							fromParticipant.getId()));
		mf.setTargetRef(new QName(m_definitions.getTargetNamespace(),
				toParticipant.getId()));
		
		QName mesg=getMessageReference(interaction);
		if (mesg != null) {
			mf.setMessageRef(mesg);
		}
		
		_choreography.getMessageFlow().add(mf);
		
		task.getMessageFlowRef().add(new QName(m_definitions.getTargetNamespace(),
								mf.getId()));
		
		if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createChoreographyTask(task));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createChoreographyTask(task));
		}
		
		// If interaction represents a fault, then check if the associated error has been defined
		Annotation ann=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
						AnnotationDefinitions.FAULT);
		if (ann != null) {
			
		}

		return(task);
	}

	public Object createServiceTask(Object container, Activity activity) {
		TServiceTask task=new TServiceTask();
		task.setId(createId());
		
		task.setName("Service: "+InteractionUtil.getMessageSignature(activity)+
				" to "+InteractionUtil.getToRole(activity));
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createTask(task));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createTask(task));
		}

		return(task);
	}
	
	public Object createSendTask(Object container, Activity activity) {
		TSendTask task=new TSendTask();
		task.setId(createId());
		
		task.setName("Send: "+InteractionUtil.getMessageSignature(activity)+
				" to "+InteractionUtil.getToRole(activity));
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createTask(task));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createTask(task));
		}

		return(task);
	}
	
	public Object createReceiveTask(Object container, Activity activity) {
		TReceiveTask task=new TReceiveTask();
		task.setId(createId());
		
		task.setName("Receive: "+InteractionUtil.getMessageSignature(activity)+
				" from "+InteractionUtil.getFromRole(activity));
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createTask(task));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createTask(task));
		}

		return(task);
	}
	
	public Object createEventBasedXORGateway(Object container) {
		TExclusiveGateway gateway=new TExclusiveGateway();
		gateway.setId(createId());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createExclusiveGateway(gateway));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createExclusiveGateway(gateway));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createExclusiveGateway(gateway));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createExclusiveGateway(gateway));
		}

		return(gateway);
	}
	
	public Object createANDGateway(Object container) {
		TParallelGateway gateway=new TParallelGateway();
		gateway.setId(createId());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createParallelGateway(gateway));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createParallelGateway(gateway));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createParallelGateway(gateway));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createParallelGateway(gateway));
		}

		return(gateway);
	}
	
	public Object createDataBasedXORGateway(Object container) {
		TInclusiveGateway gateway=new TInclusiveGateway();
		gateway.setId(createId());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createInclusiveGateway(gateway));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createInclusiveGateway(gateway));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createInclusiveGateway(gateway));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createInclusiveGateway(gateway));
		}

		return(gateway);
	}
	
	public Object createFinalNode(Object container) {
		TEndEvent endEvent=new TEndEvent();
		endEvent.setId(createId());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createEndEvent(endEvent));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createEndEvent(endEvent));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createEndEvent(endEvent));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createEndEvent(endEvent));
		}
		
		return(endEvent);
	}
	
	public Object createBoundaryEvent(Object container) {
		TBoundaryEvent event=new TBoundaryEvent();
		event.setId(createId());
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createBoundaryEvent(event));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createBoundaryEvent(event));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createBoundaryEvent(event));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createBoundaryEvent(event));
		}
		
		return(event);
	}
	
	public Object createControlLink(Object container,
			Object fromNode, Object toNode,
			String conditionalExpression) {
		TSequenceFlow link=new TSequenceFlow();
		link.setId(createId());
		
		link.setSourceRef(fromNode);
		link.setTargetRef(toNode);
		
		link.setName(conditionalExpression);
		
		if (fromNode instanceof TFlowNode) {
			((TFlowNode)fromNode).getOutgoing().add(new QName(getDefinitions().getTargetNamespace(),
								link.getId()));
		}
		
		if (toNode instanceof TFlowNode) {
			((TFlowNode)toNode).getIncoming().add(new QName(getDefinitions().getTargetNamespace(),
								link.getId()));
		}
		
		if (container instanceof TProcess) {
			((TProcess)container).getFlowElement().add(m_factory.createSequenceFlow(link));
		} else if (container instanceof TSubProcess) {
			((TSubProcess)container).getFlowElement().add(m_factory.createSequenceFlow(link));
		} else if (container instanceof TChoreography) {
			((TChoreography)container).getFlowElement().add(m_factory.createSequenceFlow(link));
		} else if (container instanceof TSubChoreography) {
			((TSubChoreography)container).getFlowElement().add(m_factory.createSequenceFlow(link));
		}
		
		return(link);
	}
	
	public Object createMessageLink(Object container,
			Object fromNode, Object toNode,	Interaction receive) {
		TMessageFlow link=new TMessageFlow();
		
		link.setId(createId());
		
		if (fromNode instanceof TBaseElement) {
			link.setSourceRef(new QName(getDefinitions().getTargetNamespace(),
								((TBaseElement)fromNode).getId()));
		}
		
		if (toNode instanceof TBaseElement) {
			link.setTargetRef(new QName(getDefinitions().getTargetNamespace(),
								((TBaseElement)toNode).getId()));
		}
		
		// TODO: Define message and message ref
		link.setName(receive.toString());
		
		m_collaboration.getMessageFlow().add(link);

		return(link);
	}
	
	public Object setLinkExpression(Object link, String expression) {
		
		if (link instanceof TSequenceFlow) {
			((TSequenceFlow)link).setName(expression);
		}
		
		return(link);
	}
	
	public boolean isDecision(Object node) {
		return(false);
	}
	
	/**
	 * This method determines if the supplied node is a join. This
	 * is true, if the node is a data or event based gateway,
	 * and as incoming edges. This is based on the fact that only
	 * a join gateway, at the point this method is invoked, would
	 * have incoming links, otherwise any other gateway would be
	 * assumed to be the initial gateway in a conditional grouping
	 * construct.
	 * 
	 * @param node
	 * @return Whether the node is a join
	 */
	public boolean isJoin(Object node) {
		boolean ret=false;
		
		if (node instanceof TGateway) {
			TGateway gw=(TGateway)node;
		
			ret = gw.getIncoming().size() > 1;
		}
		
		return(ret);
	}
	
	public boolean isTerminal(Object node) {
		boolean ret=false;
		
		if (node instanceof TEndEvent) {
			ret = true;
		}
		
		return(ret);
	}
	
	public void setLabel(Object entity, String label) {
		if (entity instanceof TFlowElement) {
			((TFlowElement)entity).setName(label);
		}
	}
	
	public Object getSource(Object link) {
		Object ret=null;
		
		if (link instanceof TSequenceFlow) {
			ret = ((TSequenceFlow)link).getSourceRef();
		}
		
		return(ret);
	}
	
	public void setSource(Object link, Object node) {
		if (link instanceof TSequenceFlow) {
			((TSequenceFlow)link).setSourceRef(node);
		}
	}
	
	public Object getTarget(Object link) {
		Object ret=null;
		
		if (link instanceof TSequenceFlow) {
			ret = ((TSequenceFlow)link).getTargetRef();
		}
		
		return(ret);
	}
	
	public void setTarget(Object link, Object node) {
		if (link instanceof TSequenceFlow) {
			((TSequenceFlow)link).setTargetRef(node);
		}
	}
	
	public java.util.List<Object> getControlLinks(Object node) {
		java.util.List<Object> ret=new java.util.Vector<Object>();
		
		java.util.List<JAXBElement<? extends TFlowElement>> list=null;
		
		if (node instanceof TProcess) {
			list = ((TProcess)node).getFlowElement();
		} else if (node instanceof TSubProcess) {
			list = ((TSubProcess)node).getFlowElement();
		} else if (node instanceof TChoreography) {
			list = ((TChoreography)node).getFlowElement();
		} else if (node instanceof TSubChoreography) {
			list = ((TSubChoreography)node).getFlowElement();
		}
		
		if (list != null) {
			for (JAXBElement<? extends TFlowElement> jbfe : list) {
				TFlowElement fe=jbfe.getValue();
				
				if (fe instanceof TSequenceFlow) {
					ret.add(fe);
				}
			}
			
		}
		
		return(ret);
	}
	
	public java.util.List<Object> getInboundControlLinks(Object node) {
		java.util.List<Object> ret=new java.util.Vector<Object>();
		
		/*
		if (node instanceof TFlowNode) {
			for (QName qname : ((TFlowNode)node).getIncoming()) {
				
			}
			
		}
		*/
		
		return(ret);
	}
	
	public java.util.List<Object> getOutboundControlLinks(Object node) {
		java.util.List<Object> ret=new java.util.Vector<Object>();
		
		/*
		if (node instanceof TFlowNode) {
			for (QName qname : ((TFlowNode)node).getOutgoing()) {
				
			}
			
		}
		*/
		
		return(ret);
	}
	
	public java.util.List<Object> getInboundMessageLinks(Object node) {
		java.util.List<Object> ret=new java.util.Vector<Object>();
		
		if (node instanceof TBaseElement) {
			QName id=new QName(getDefinitions().getTargetNamespace(),((TBaseElement)node).getId());
			
			for (TMessageFlow mf : m_collaboration.getMessageFlow()) {
				if (mf.getTargetRef() != null &&
						mf.getTargetRef().equals(id)) {
					ret.add(mf);
				}
			}
			
		}
		
		return(ret);
	}
	
	public java.util.List<Object> getOutboundMessageLinks(Object node) {
		java.util.List<Object> ret=new java.util.Vector<Object>();
		
		if (node instanceof TBaseElement) {
			QName id=new QName(getDefinitions().getTargetNamespace(),((TBaseElement)node).getId());
			
			for (TMessageFlow mf : m_collaboration.getMessageFlow()) {
				if (mf.getSourceRef() != null &&
						mf.getSourceRef().equals(id)) {
					ret.add(mf);
				}
			}
			
		}
		
		return(ret);
	}
	
	public void delete(Object entity) {
		// TODO: DELETE
	}
	
	public boolean isDeleted(Object entity) {
		// TODO: DELETE
		return(false);
	}
	
}
