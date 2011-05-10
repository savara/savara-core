/*
 * Copyright 2005-8 Pi4 Technologies Ltd
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
 * 24 Jul 2008 : Initial version created by gary
 */
package org.savara.bpmn2.generation.process;

import java.net.URI;
import java.util.logging.Logger;

import org.savara.bpmn2.internal.generation.process.BPMN2GenerationException;
import org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory;
import org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory;
import org.savara.bpmn2.internal.generation.process.components.BPMNActivity;
import org.savara.bpmn2.internal.generation.process.components.BPMNDiagram;
import org.savara.bpmn2.internal.generation.process.components.BPMNPool;
import org.savara.bpmn2.internal.generation.process.components.ChoiceActivity;
import org.savara.bpmn2.internal.generation.process.components.ParallelActivity;
import org.savara.bpmn2.internal.generation.process.components.ReceiveActivity;
import org.savara.bpmn2.internal.generation.process.components.RepeatActivity;
import org.savara.bpmn2.internal.generation.process.components.RunActivity;
import org.savara.bpmn2.internal.generation.process.components.SendActivity;
import org.savara.bpmn2.internal.generation.process.components.SequenceActivity;
import org.savara.bpmn2.internal.generation.process.components.SimpleActivity;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.resources.ResourceLocator;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.ProtocolDefinitions;
import org.scribble.protocol.model.*;

/**
 * This class represents the Protocol to BPEL implementation of the model
 * generator interface.
 */
public class ProtocolToBPMN2ProcessModelGenerator implements ModelGenerator {

	private boolean m_consecutiveIds=false;
	
	private static final Logger logger=Logger.getLogger(ProtocolToBPMN2ProcessModelGenerator.class.getName());
	
	/**
	 * This method determines whether consecutive ids should be used in the model and
	 * notation. If false (default), then random unique ids will be used.
	 * 
	 * @param b Whether to use consecutive ids
	 */
	public void setUseConsecutiveIds(boolean b) {
		m_consecutiveIds = b;
	}
	
	/**
	 * This method determines whether the generator is appropriate for
	 * the specified source and target types.
	 * 
	 * @param source The source
	 * @param targetType The target type
	 * @return Whether the specified types are supported
	 */
	public boolean isSupported(Object source, String targetType) {
		return(source instanceof ProtocolModel &&
				((ProtocolModel)source).isLocated() &&
				(targetType.equals("bpmn2") || targetType.equals("bpmn")));
	}

	/**
	 * This method generates the contents of the target
	 * model using information in the source model.
	 * 
	 * @param source The source model
	 * @param handler The feedback handler
	 * @param locator The resource locator
	 * @return The target model
	 */
	public Object generate(Object source, FeedbackHandler handler,
							final ResourceLocator locator) {
		Object ret=null;

		if (source instanceof ProtocolModel) {
			ProtocolModel pm=(ProtocolModel)source;
			
			TDefinitions defns=new TDefinitions();
			
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model=
				new org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory(defns);
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation=
					new org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory(model);
			
			model.setUseConsecutiveIds(m_consecutiveIds);
			notation.setUseConsecutiveIds(m_consecutiveIds);
			
			BPMN2ModelVisitor visitor=
				new BPMN2ModelVisitor(pm.getProtocol().getName(),
						model, notation);
			
			/*
			if (pm.getProtocol().getRole() == null) {
				// Global (choreography) model
				java.util.List<Role> roles=pm.getProtocol().getRoles();
				
				for (Role role : roles) {
					DefaultProtocolContext context=
							new DefaultProtocolContext(ProtocolServices.getParserManager(),
											new org.scribble.common.resource.ResourceLocator() {
						public URI getResourceURI(String uri) throws Exception {
							return(locator.getResourceURI(uri));
						}
					});
	
					ProtocolModel local=ProtocolServices.getProtocolProjector().project(pm,
									role, new JournalProxy(handler), context);
	
					if (local != null) {
						// TODO: SAVARA-167 - issue when projection is based on a sub-protocol
						if (AnnotationDefinitions.getAnnotation(local.getProtocol().getAnnotations(),
										AnnotationDefinitions.TYPE) == null &&
								AnnotationDefinitions.getAnnotation(pm.getProtocol().getAnnotations(),
												AnnotationDefinitions.TYPE) != null) {				
							AnnotationDefinitions.copyAnnotations(pm.getProtocol().getAnnotations(),
									local.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE);
						}
						
						generateProcess(local, visitor, handler, locator);
					}
				}
			} else {
			*/
				generateProcess(pm, visitor, handler, locator);
			//}
			
			visitor.completeModels();
			
			ret = defns;
		}
		
		return(ret);
	}
	
	protected void generateProcess(ProtocolModel local, BPMN2ModelVisitor visitor,
					FeedbackHandler handler, ResourceLocator locator) {
		local.visit(visitor);
	}
	
	public class BPMN2ModelVisitor extends DefaultVisitor {
		
		private BPMN2ModelFactory m_modelFactory=null;
		private BPMN2NotationFactory m_notationFactory=null;
		private String m_choreoName=null;
	    private java.util.List<BPMNActivity> m_bpmnActivityStack=new java.util.ArrayList<BPMNActivity>();
	    private java.util.Map<String,BPMNDiagram> m_activityModels=
	    				new java.util.HashMap<String,BPMNDiagram>();

	    /**
		 * The constructor the BPMN model visitor.
		 * 
		 */
		public BPMN2ModelVisitor(String choreoName,
				BPMN2ModelFactory model, BPMN2NotationFactory notation) {
			m_choreoName = choreoName;
			m_modelFactory = model;
			m_notationFactory = notation;
		}
		
		/**
		 * This method starts visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public boolean start(Protocol elem) {
			
			try {
				BPMNDiagram diagram=getBPMNModel(elem);
				
				BPMNPool pool=diagram.createPool(elem.getRole().getName());
				
				//diagram.initialize(elem);
				
				pushBPMNActivity(pool);
			} catch(Exception e) {
				logger.severe("Failed to get state machine " +
						"for behavior '"+elem+"': "+e);
			}
			
			return(true);
		}
		
		/**
		 * This method ends visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public void end(Protocol elem) {

			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}

			popBPMNActivity();
		}
		
		/**
		 * This method starts visiting the choice element.
		 * 
		 * @param elem The choice
		 */
		public boolean start(Choice elem) {
			
			try {
				pushBPMNActivity(new ChoiceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create choice state: "+e);
			}
			
			return(true);
		}
		
		/**
		 * This method ends visiting the choice element.
		 * 
		 * @param elem The choice
		 */
		public void end(Choice elem) {
			
			popBPMNActivity();
		}
		
		/**
		 * This method indicates the start of a
		 * when block.
		 * 
		 * @param elem The when block
		 * @return Whether to process the contents
		 */
		public boolean start(When elem) {
			
			return(true);
		}
		
		/**
		 * This method indicates the end of a
		 * when block.
		 * 
		 * @param elem The when block
		 */
		public void end(When elem) {
		}

		/**
		 * This method starts visiting the parallel element.
		 * 
		 * @param elem The parallel
		 */
		public boolean start(Parallel elem) {
			
			try {
				pushBPMNActivity(new ParallelActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create parallel state: "+e);
			}
			
			return(true);
		}
		
		/**
		 * This method ends visiting the parallel element.
		 * 
		 * @param elem The parallel
		 */
		public void end(Parallel elem) {
			
			popBPMNActivity();
		}
		
		/**
		 * This method visits the perform activity.
		 * 
		 * @param elem The perform
		 */
		public boolean start(Run elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				RunActivity state=new RunActivity(elem,
						umls, m_modelFactory, m_notationFactory);
				
				/* TODO: See if possible to determine who is the initiating party
				 * in the performed protocol, to establish a link
				 *
				if (elem instanceof LookaheadElement) {
					LookaheadElement lookahead=(LookaheadElement)elem;
					boolean f_send=false;
					boolean f_receive=false;
					
					java.util.Iterator iter=lookahead.getPreConditions().iterator();
					while (iter.hasNext()) {
						Predicate pred=(Predicate)iter.next();
						
						if (pred.isMessagePredicate()) {
							
							if (pred.isReceiveLookaheadPredicate()) {
								f_receive = true;
							} else {
								f_send = true;
							}
						}
					}
					
					if (f_send && !f_receive) {
						umls.getBPMNDiagram().registerInitiatingPerform(elem, state);
					} else if (!f_send && f_receive) {
						umls.getBPMNDiagram().registerInitiatedPerform(elem, state);
					}
				}
				*/
			}
			
			return(true);
		}
		
		/**
		 * This method visits the receive activity.
		 * 
		 * @param elem The receive
		 */
		public void accept(Interaction elem) {
			
			// Check if a receive
			if (elem.getFromRole() == null ||
					elem.getFromRole().equals(elem.enclosingProtocol().getRole())) {
				
				BPMNActivity umls=getBPMNActivity();
				if (umls != null) {
					SimpleActivity sa=
						new SendActivity(elem, umls, m_modelFactory, m_notationFactory);
					
					// Register the send to enable links to be established
					// with an appropriate receive
					BPMNDiagram amodel=umls.getBPMNDiagram();		
					amodel.registerSendActivity(elem, sa);
				}
			} else {
				BPMNActivity umls=getBPMNActivity();
				if (umls != null) {
					ReceiveActivity sa=
						new ReceiveActivity(elem, umls, m_modelFactory, m_notationFactory);
					
					// Register the receive to enable links to be established
					// with an appropriate send
					BPMNDiagram amodel=umls.getBPMNDiagram();		
					amodel.registerReceiveActivity(elem, sa);
				}
			}
		}
		
		/**
		 * This method starts visiting the sequence element.
		 * 
		 * @param elem The sequence
		 */
		public boolean start(Block elem) {
			
			try {
				pushBPMNActivity(new SequenceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
				if (elem.getParent() instanceof When &&
						elem.getParent().getParent() instanceof Choice) {
					When parent=(When)elem.getParent();
					Choice choice=(Choice)elem.getParent().getParent();
					
					Interaction interaction=new Interaction();
					interaction.getAnnotations().addAll(parent.getAnnotations());
					
					interaction.setFromRole(choice.getFromRole());
					
					if (choice.getToRole() != null) {
						interaction.getToRoles().add(choice.getToRole());
					}
					
					interaction.setMessageSignature(new MessageSignature(parent.getMessageSignature()));
		
					// Check if a receive
					if (choice.getToRole() == null ||
								choice.getToRole().equals(elem.enclosingProtocol().getRole())) {
						BPMNActivity umls=getBPMNActivity();
						
						if (umls != null) {
							ReceiveActivity sa=
								new ReceiveActivity(interaction, umls, m_modelFactory, m_notationFactory);
							
							// Register the receive to enable links to be established
							// with an appropriate send
							BPMNDiagram amodel=umls.getBPMNDiagram();		
							amodel.registerReceiveActivity(interaction, sa);
						}
					} else {
						
						BPMNActivity umls=getBPMNActivity();
						if (umls != null) {
							SimpleActivity sa=
								new SendActivity(interaction, umls, m_modelFactory, m_notationFactory);
							
							// Register the send to enable links to be established
							// with an appropriate receive
							BPMNDiagram amodel=umls.getBPMNDiagram();		
							amodel.registerSendActivity(interaction, sa);
						}
					}

				}
				
			} catch(Exception e) {
				logger.severe("Failed to create sequence state: "+e);
			}
			
			return(true);
		}
		
		/**
		 * This method ends visiting the sequence element.
		 * 
		 * @param elem The sequence
		 */
		public void end(Block elem) {
			
			popBPMNActivity();
		}
		
		/**
		 * This method starts visiting the while element.
		 * 
		 * @param elem The while
		 */
		public boolean start(Repeat elem) {
			
			try {
				pushBPMNActivity(new RepeatActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
				pushBPMNActivity(new SequenceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create while Activity: "+e);
			}
			
			return(true);
		}
		
		/**
		 * This method ends visiting the while element.
		 * 
		 * @param elem The while
		 */
		public void end(Repeat elem) {
			
			popBPMNActivity();
			
			popBPMNActivity();
		}
		
		protected BPMNDiagram getBPMNModel(Protocol elem) throws BPMN2GenerationException {
			String name=BPMNDiagram.getName(elem);
			
			BPMNDiagram ret=(BPMNDiagram)
						m_activityModels.get(name);
			
			if (ret == null) {
				ret = new BPMNDiagram(m_choreoName, name,
						null, m_modelFactory, m_notationFactory);
				
				m_activityModels.put(name, ret);
			}
			
			return(ret);
		}
		
		/**
		 * This method pushes the supplied UML activity
		 * onto a stack.
		 * 
		 * @param act The activity
		 */
		protected void pushBPMNActivity(BPMNActivity act) {
			m_bpmnActivityStack.add(0, act);
		}
		
		/**
		 * This method returns the UML activity found at the
		 * top of the stack.
		 * 
		 * @return The activity
		 */
		protected BPMNActivity getBPMNActivity() {
			BPMNActivity ret=null;
			
			if (m_bpmnActivityStack.size() > 0) {
				ret = (BPMNActivity)m_bpmnActivityStack.get(0);
			}
			
			return(ret);
		}
		
		/**
		 * This method returns the UML activity from the
		 * top of the stack.
		 *
		 */
		protected void popBPMNActivity() {
			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}
			
			if (m_bpmnActivityStack.size() > 0) {
				m_bpmnActivityStack.remove(0);
			}
		}
		
		/**
		 * This method completes the construction of the activity
		 * models.
		 *
		 */
		public void completeModels() {
			java.util.Iterator<BPMNDiagram> iter=m_activityModels.values().iterator();
			
			while (iter.hasNext()) {
				BPMNDiagram amodel=iter.next();
				
				amodel.completeModel();
			}
		}
	}
}
