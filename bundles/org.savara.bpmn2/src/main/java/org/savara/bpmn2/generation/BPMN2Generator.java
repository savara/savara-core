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
package org.savara.bpmn2.generation;

import java.util.logging.Logger;

import org.savara.bpel.BPELDefinitions;
import org.savara.bpmn2.generation.components.*;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.task.FeedbackHandler;
import org.savara.common.task.ResourceLocator;
import org.scribble.protocol.ProtocolDefinitions;

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
				targetType.equals(BPELDefinitions.BPEL_TYPE));
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
							ResourceLocator locator) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * This method generates the UML representation for the
	 * supplied choreography. If the optional participant
	 * is specified, then only the UML representation for
	 * that participant will be generated, otherwise all
	 * participants associated with the choreography will be
	 * generated.
	 * 
	 * @param choreography The choreography
	 * @param participants The optional participant names
	 * @param folder The folder where the model/diagrams will
	 * 					be stored
	 * @param config The UML configuration
	 * @exception BPMN2GenerationException Failed to generate
	 */
	public void generate(org.pi4soa.cdl.Package choreography,
					String[] participants, String folder,
					BPMN2Configuration config) throws BPMN2GenerationException {
	
		if (participants == null) {
			java.util.List pTypes=
				choreography.getTypeDefinitions().getParticipantTypes();
			
			participants = new String[pTypes.size()];
			
			for (int i=0; i < pTypes.size(); i++) {
				participants[i] = ((org.pi4soa.cdl.ParticipantType)pTypes.
							get(i)).getName();
			}
		}

		// Create the model
		org.savara.BPMN2ModelFactory.bpmn.generation.BPMNModelFactory model=null;
		org.savara.BPMN2NotationFactory.bpmn.generation.BPMNNotationFactory notation=null;
		
		try {
			model = new org.savara.tools.bpmn.generation.stp.STPBPMNModelFactoryImpl();
			notation = new org.savara.tools.bpmn.generation.gmf.GMFBPMNNotationFactoryImpl();
			
			generateModel(model, notation, choreography, config, folder);

		} catch(Exception e) {
			throw new BPMN2GenerationException("Failed to generate UML model", e);
		}
		
		/*
		if (diagrams.size() > 0) {
			Object diagram=diagrams.get(0);
			
			try {
				
				// Output the UML2 model to the supplied stream
				final org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl xmi =
					new org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl();
				xmi.getContents().add(diagram);
				
				xmi.doSave(ostream, xmi.getDefaultLoadOptions());
				
				ostream.close();
				
			} catch(Exception e) {
				throw new BPMNGenerationException("Failed to convert to XMI", e);
			}
		}
		*/
		// Just need to store as EMF based file
		/*
		try {
			java.io.ByteArrayOutputStream bs=new java.io.ByteArrayOutputStream();
			
			// Output the UML2 model to the supplied stream
			final org.eclipse.emf.ecore.xmi.impl.EMOFResourceImpl xmi =
				new org.eclipse.emf.ecore.xmi.impl.EMOFResourceImpl();
			xmi.getContents().add(model);
			
			xmi.doSave(bs, xmi.getDefaultLoadOptions());
			
			bs.close();
			
			UMLExportFormatter formatter=getExportFormatter(config.getExportFormat());
			if (formatter != null) {
				formatter.export(bs, ostream);
			} else {
				throw new UMLException("Unable to find UML export formatter for '"+
						config.getExportFormat()+"'");
			}
				
		} catch(UMLException UMLex) {
			throw UMLex;
			
		} catch(Exception e) {
			throw new UMLException("Failed to convert to XMI", e);
		}
		*/
	}
	
	/**
	 * This method generates the model information associated
	 * with the supplied choreography description.
	 * 
	 * @param model The model
	 * @param cdlpack The choreography description
	 * @param config The MDA configuration
	 * @return The list of diagrams
	 * @throws BPMN2GenerationException Failed to generate model
	 */
	public void generateModel(BPMN2ModelFactory model, 
			BPMN2NotationFactory notation, org.pi4soa.cdl.Package cdlpack,
					BPMN2Configuration config, String folder) throws BPMN2GenerationException {
		try {
			java.util.List participants=cdlpack.getTypeDefinitions().getParticipantTypes();
			
			BPMNModelVisitor visitor=
				new BPMNModelVisitor(org.pi4soa.common.xml.XMLUtils.getLocalname(
						cdlpack.getName()),
						model, notation, folder);
			
			for (int i=0; i < participants.size(); i++) {
				org.pi4soa.cdl.ParticipantType partType=
							(org.pi4soa.cdl.ParticipantType)
							cdlpack.getTypeDefinitions().getParticipantTypes().get(i);
					
				org.pi4soa.service.behavior.ServiceDescription sdesc=
					BehaviorProjection.projectServiceDescription(cdlpack,
							partType, null);
			
				visitor.setParticipant(partType.getName());
				
				sdesc.visit(visitor);
			}
			
			visitor.completeModels();
			
		} catch(Exception e) {
			throw new BPMN2GenerationException("Failed to generate BPMN model", e);
		}
	}
	
    // NOTE: Currently uses the Activity Model constructs
	public class BPMNModelVisitor implements BehaviorVisitor {
		
		/**
		 * The constructor the BPMN model visitor.
		 * 
		 */
		public BPMNModelVisitor(String choreoName,
				BPMN2ModelFactory model, BPMN2NotationFactory notation,
					String folder) {
			m_choreoName = choreoName;
			m_modelFactory = model;
			m_notationFactory = notation;
			m_folder = folder;
		}
		
		/**
		 * This method sets the name associated with the participant
		 * being projected.
		 * 
		 * @param participant The participant
		 */
		public void setParticipant(String participant) {
			m_participant = participant;
		}
		
		/**
		 * This method visits the abstract activity. This will be
		 * performed after performing the specific visit method
		 * for the real type, but before performing the visit
		 * on any other activities that may be contained within
		 * this activity type.
		 * 
		 * @param elem The activity type
		 */
		public void activityType(ActivityType elem) {
			
		}
		
		/**
		 * This method visits the assign activity.
		 * 
		 * @param elem The assign
		 */
		public void assign(Assign elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				new SimpleActivity(elem,
						umls, m_modelFactory, m_notationFactory);
			}
		}
		
		/**
		 * This method starts visiting the atomic unit element.
		 * 
		 * @param elem The atomic unit
		 */
		public void atomicUnitStart(AtomicUnit elem) {
			
		}
		
		/**
		 * This method ends visiting the atomic unit element.
		 * 
		 * @param elem The atomic unit
		 */
		public void atomicUnitEnd(AtomicUnit elem) {
			
		}
		
		/**
		 * This method starts visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public void behaviorDescriptionStart(BehaviorDescription elem) {
			
			try {
				BPMNDiagram diagram=getBPMNModel(elem, m_folder);
				
				String participant=m_participant;
				
				if (org.pi4soa.common.util.NamesUtil.isSet(elem.getParticipant())) {
					participant += " ["+elem.getParticipant()+"]";
				}
				
				BPMNPool pool=diagram.createPool(participant);
				
				//diagram.initialize(elem);
				
				pushBPMNActivity(pool);
			} catch(Exception e) {
				logger.severe("Failed to get state machine " +
						"for behavior '"+elem+"': "+e);
			}
		}
		
		/**
		 * This method ends visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public void behaviorDescriptionEnd(BehaviorDescription elem) {

			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}

			popUMLActivity();
		}
		
		/**
		 * This method starts visiting the choice element.
		 * 
		 * @param elem The choice
		 */
		public void choiceStart(Choice elem) {
			
			try {
				pushBPMNActivity(new ChoiceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create choice state: "+e);
			}
		}
		
		/**
		 * This method ends visiting the choice element.
		 * 
		 * @param elem The choice
		 */
		public void choiceEnd(Choice elem) {
			
			popUMLActivity();
		}
		
		/**
		 * This method starts visiting the completion handler element.
		 * 
		 * @param elem The completion handler
		 */
		public void completionHandlerStart(CompletionHandler elem) {
			
			/*
			try {
				BPMNActivity bdstate=getBPMNModel(elem, m_folder);
					
				pushBPMNActivity(bdstate);
			} catch(Exception e) {
				logger.severe("Failed to get state machine for " +
						"completion handler '"+elem+"': "+e);
			}
			*/
		}
		
		/**
		 * This method ends visiting the completion handler element.
		 * 
		 * @param elem The completion handler
		 */
		public void completionHandlerEnd(CompletionHandler elem) {
			
			/*
			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}

			popUMLActivity();
			*/
		}
		
		/**
		 * This method starts visiting the conditional element.
		 * 
		 * @param elem The conditional
		 */
		public void conditionalStart(Conditional elem) {
			
			try {
				pushBPMNActivity(new ConditionalActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
				pushBPMNActivity(new SequenceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create conditional state: "+e);
			}
		}
		
		/**
		 * This method ends visiting the conditional element.
		 * 
		 * @param elem The conditional
		 */
		public void conditionalEnd(Conditional elem) {
			
			popUMLActivity();
			popUMLActivity();
		}
		
		/**
		 * This method starts visiting the exception handler element.
		 * 
		 * @param elem The exception handler
		 */
		public void exceptionHandlerStart(ExceptionHandler elem) {
			
			/*
			try {
				BPMNActivity bdstate=getBPMNModel(elem);
					
				pushBPMNActivity(bdstate);
			} catch(Exception e) {
				logger.severe("Failed to get state machine for " +
						"exception handler '"+elem+"': "+e);
			}
			*/
		}
		
		/**
		 * This method ends visiting the exception handler element.
		 * 
		 * @param elem The exception handler
		 */
		public void exceptionHandlerEnd(ExceptionHandler elem) {
			
			/*
			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}

			popUMLActivity();
			*/
		}
		
		/**
		 * This method visits the finalize activity.
		 * 
		 * @param elem The finalize
		 */
		public void finalize(Finalize elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				SimpleActivity state=new SimpleActivity(elem,
						umls, m_modelFactory, m_notationFactory);
				
				/*
				try {
					StateMachineState subMachine=
						getStateMachineState(elem.getCompletionHandler());
					state.setSubStateMachine(subMachine);
				} catch(Exception e) {
					logger.severe("Failed to get state machine: "+e);
				}
				*/
			}
		}
		
		/**
		 * This method starts visiting the parallel element.
		 * 
		 * @param elem The parallel
		 */
		public void parallelStart(Parallel elem) {
			
			try {
				pushBPMNActivity(new ParallelActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create parallel state: "+e);
			}
		}
		
		/**
		 * This method ends visiting the parallel element.
		 * 
		 * @param elem The parallel
		 */
		public void parallelEnd(Parallel elem) {
			
			popUMLActivity();
		}
		
		/**
		 * This method visits the perform activity.
		 * 
		 * @param elem The perform
		 */
		public void perform(Perform elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				PerformActivity state=new PerformActivity(elem,
						umls, m_modelFactory, m_notationFactory);
				
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
			}
		}
		
		/**
		 * This method visits the raise exception activity.
		 * 
		 * @param elem The raise exception
		 */
		public void raiseException(RaiseException elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				new SimpleActivity(elem, umls, m_modelFactory, m_notationFactory);
			}
		}
		
		/**
		 * This method visits the receive activity.
		 * 
		 * @param elem The receive
		 */
		public void receive(Receive elem) {
			
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
		
		/**
		 * This method starts visiting the repetition type abstract element.
		 * 
		 * @param elem The repetition type
		 */
		public void repetitionTypeStart(RepetitionType elem) {
			
		}
		
		/**
		 * This method ends visiting the repetition type abstract element.
		 * 
		 * @param elem The repetition type
		 */
		public void repetitionTypeEnd(RepetitionType elem) {
			
		}
		
		/**
		 * This method visits the send activity.
		 * 
		 * @param elem The send
		 */
		public void send(Send elem) {
			
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
		
		/**
		 * This method starts visiting the sequence element.
		 * 
		 * @param elem The sequence
		 */
		public void sequenceStart(Sequence elem) {
			
			try {
				pushBPMNActivity(new SequenceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create sequence state: "+e);
			}
		}
		
		/**
		 * This method ends visiting the sequence element.
		 * 
		 * @param elem The sequence
		 */
		public void sequenceEnd(Sequence elem) {
			
			popUMLActivity();
		}
		
		/**
		 * This method starts visiting the service description element.
		 * 
		 * @param elem The service description
		 */
		public void serviceDescriptionStart(ServiceDescription elem) {
			
		}
		
		/**
		 * This method ends visiting the service description element.
		 * 
		 * @param elem The service description
		 */
		public void serviceDescriptionEnd(ServiceDescription elem) {
			
		}
		
		/**
		 * This method starts visiting the structural type abstract element.
		 * 
		 * @param elem The structural type
		 */
		public void structuralTypeStart(StructuralType elem) {
			
		}
		
		/**
		 * This method ends visiting the structural type abstract element.
		 * 
		 * @param elem The structural type
		 */
		public void structuralTypeEnd(StructuralType elem) {
			
			/*
			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}
			*/
		}
		
		/**
		 * This method starts visiting the timed unit element.
		 * 
		 * @param elem The timed unit
		 */
		public void timedUnitStart(TimedUnit elem) {
			
		}
		
		/**
		 * This method ends visiting the timed unit element.
		 * 
		 * @param elem The timed unit
		 */
		public void timedUnitEnd(TimedUnit elem) {
			
		}
		
		/**
		 * This method visits the unobservable activity.
		 * 
		 * @param elem The unobservable
		 */
		public void unobservable(Unobservable elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				new SimpleActivity(elem, umls, m_modelFactory, m_notationFactory);
			}
		}
		
		/**
		 * This method visits the variable declaration activity.
		 * 
		 * @param elem The variable declaration
		 */
		public void variableDeclaration(VariableDeclaration elem) {
			
		}
		
		/**
		 * This method starts visiting the when element.
		 * 
		 * @param elem The when
		 */
		public void whenStart(When elem) {
			
			try {
				pushBPMNActivity(new WhenActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
					
				pushBPMNActivity(new SequenceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
			} catch(Exception e) {
				logger.severe("Failed to create when Activity: "+e);
			}
		}
		
		/**
		 * This method ends visiting the when element.
		 * 
		 * @param elem The when
		 */
		public void whenEnd(When elem) {
			
			popUMLActivity();
			popUMLActivity();
		}

		/**
		 * This method starts visiting the while element.
		 * 
		 * @param elem The while
		 */
		public void whileStart(While elem) {
			
			try {
				pushBPMNActivity(new WhileActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
				pushBPMNActivity(new SequenceActivity(elem,
						getBPMNActivity(), m_modelFactory, m_notationFactory));
				
			} catch(Exception e) {
				logger.severe("Failed to create while Activity: "+e);
			}
		}
		
		/**
		 * This method ends visiting the while element.
		 * 
		 * @param elem The while
		 */
		public void whileEnd(While elem) {
			
			popUMLActivity();
			
			popUMLActivity();
		}
		
		/**
		 * This method returns a Activity machine Activity for the supplied
		 * behavior type. If one already exists, then it will be
		 * returned, otherwise one will be created.
		 * 
		 * @param elem The behavior type
		 * @return The Activity machine state
		 */
		protected BPMNDiagram getBPMNModel(
					BehaviorDescription elem, String folder) throws BPMN2GenerationException {
			String name=BPMNDiagram.getName(elem);
			
			BPMNDiagram ret=(BPMNDiagram)
						m_activityModels.get(name);
			
			if (ret == null) {
				ret = new BPMNDiagram(m_choreoName, name,
						null, m_modelFactory, m_notationFactory, folder);
				
				m_activityModels.put(name, ret);
			}
			
			// Create activity partition for service
			/*
			org.eclipse.uml2.uml.ActivityPartition partition = 
				(org.eclipse.uml2.uml.ActivityPartition)
				ret.getTopLevelActivity().createGroup(
						UMLPackage.eINSTANCE.getActivityPartition());
			partition.setName(org.pi4soa.common.xml.XMLUtils.getLocalname(
					elem.getServiceDescription().getName()));
			ret.setCurrentPartition(partition);
			*/
			
			return(ret);
		}
		
		/**
		 * This method pushes the supplied UML activity
		 * onto a stack.
		 * 
		 * @param sms The activity
		 */
		protected void pushBPMNActivity(BPMNActivity umls) {
			m_umlActivityStack.insertElementAt(umls, 0);
		}
		
		/**
		 * This method returns the UML activity found at the
		 * top of the stack.
		 * 
		 * @return The activity
		 */
		protected BPMNActivity getBPMNActivity() {
			BPMNActivity ret=null;
			
			if (m_umlActivityStack.size() > 0) {
				ret = (BPMNActivity)m_umlActivityStack.get(0);
			}
			
			return(ret);
		}
		
		/**
		 * This method returns the UML activity from the
		 * top of the stack.
		 *
		 */
		protected void popUMLActivity() {
			BPMNActivity umls=getBPMNActivity();
			
			if (umls != null) {
				umls.childrenComplete();
			}
			
			if (m_umlActivityStack.size() > 0) {
				m_umlActivityStack.remove(0);
			}
		}
		
		/**
		 * This method completes the construction of the activity
		 * models.
		 *
		 */
		public void completeModels() throws BPMN2GenerationException {
			java.util.Enumeration iter=m_activityModels.elements();
			
			while (iter.hasMoreElements()) {
				BPMNDiagram amodel=(BPMNDiagram)iter.nextElement();
				
				amodel.completeModel();
			}
		}
		
		private BPMN2ModelFactory m_modelFactory=null;
		private BPMN2NotationFactory m_notationFactory=null;
		private String m_folder=null;
		private String m_choreoName=null;
		private String m_participant=null;
	    private java.util.Vector m_umlActivityStack=new java.util.Vector();
	    private java.util.Hashtable m_activityModels=new java.util.Hashtable();
	}
}
