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
package org.savara.bpmn2.generation.process;

import java.net.URI;
import java.util.logging.Logger;

import org.savara.bpmn2.internal.generation.process.BPMN2GenerationException;
import org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory;
import org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory;
import org.savara.bpmn2.internal.generation.process.components.*;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.resources.ResourceLocator;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.ProtocolDefinitions;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.DefaultVisitor;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Parallel;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.Run;

/**
 * This class provides the functionality for converting a
 * scribble protocol description into a BPMN2 representation.
 *
 */
public class BPMN2Generator implements ModelGenerator {

    private static Logger logger = Logger.getLogger(BPMN2Generator.class.getName());

	/*
	public static void main(String[] args) {
		
		// Check for optional parameters
		int pos=0;
		int num=args.length;
		String[] participant=null;
		
		for (; pos < num && args[pos].charAt(0) == '-'; pos++) {
			String option=args[pos].substring(1);
			if (option.equals("p")) {
				if (participant != null) {
					System.err.println("Cannot specify participant more than once");
					System.exit(1);
				} else if (pos+1 >= num) {
					System.err.println("Participant not defined");
					System.exit(1);
				} else {
					pos++;
					participant = new String[]{args[pos]};
				}
			} else {
				System.err.println("Unknown option "+args[pos]);
				System.err.println(USAGE_MESSAGE);
				System.exit(1);
			}
		}
		
		// Check parameters
		if (num - pos != 2) {
			System.err.println(USAGE_MESSAGE);
			System.exit(1);
		}
		
		String choreographyFile=args[pos];
		String outputFolder=args[pos+1];
		org.pi4soa.cdl.Package choreography=null;
		
		// Load the choreography
		try {
			choreography = CDLManager.load(choreographyFile);
			
		} catch(Exception e) {
			System.err.println("Failed to load choreography: "+e);
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			BPMNGenerator generator=new BPMNGenerator();
			
			generator.generate(choreography, participant, outputFolder,
					new DefaultBPMNConfiguration());
			
		} catch(Exception e) {
			System.err.println("Failed to generate UML: "+e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	*/
	
	/**
	 * This is the constructor for the UML generator.
	 * 
	 */
	public BPMN2Generator() {
	}
	

	/**
	 * This method determines whether the generator is appropriate for
	 * the specified source and target types.
	 * 
	 * @param sourceType The source type
	 * @param targetType The target type
	 * @return Whether the specified types are supported
	 */
	public boolean isSupported(String sourceType, String targetType) {
		return(sourceType.equals(ProtocolDefinitions.PROTOCOL_TYPE) &&
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
			
			org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model=null;
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation=null;
			
			BPMN2ModelVisitor visitor=
				new BPMN2ModelVisitor(pm.getProtocol().getName(),
						model, notation);
			
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
				generateProcess(pm, visitor, handler, locator);
			}
			
			visitor.completeModels();
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
			if (elem.getToRoles().contains(elem.enclosingProtocol().getRole())) {
				BPMNActivity umls=getBPMNActivity();
				if (umls != null) {
					ReceiveActivity sa=
						new ReceiveActivity(elem, umls, m_modelFactory, m_notationFactory);
					
					// Register the receive to enable links to be established
					// with an appropriate send
					BPMNDiagram amodel=umls.getBPMNDiagram();		
					amodel.registerReceiveActivity(elem, sa);
				}
			} else {
				
				BPMNActivity umls=getBPMNActivity();
				if (umls != null) {
					SimpleActivity sa=
						new SimpleActivity(elem, umls, m_modelFactory, m_notationFactory);
					
					// Register the send to enable links to be established
					// with an appropriate receive
					BPMNDiagram amodel=umls.getBPMNDiagram();		
					amodel.registerSendActivity(elem, sa);
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
