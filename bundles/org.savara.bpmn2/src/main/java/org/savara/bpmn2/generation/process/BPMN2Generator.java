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

import java.util.logging.Logger;

import org.savara.bpmn2.internal.generation.process.BPMN2GenerationException;
import org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory;
import org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory;
import org.savara.bpmn2.internal.generation.process.components.*;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.resources.ResourceLocator;
import org.scribble.protocol.ProtocolDefinitions;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.DefaultVisitor;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Parallel;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Run;
import org.scribble.protocol.model.Visitor;

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
		org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model=null;
		org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation=null;
		
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
			
			BPMN2ModelVisitor visitor=
				new BPMN2ModelVisitor(org.pi4soa.common.xml.XMLUtils.getLocalname(
						cdlpack.getName()),
						model, notation, folder);
			
			for (int i=0; i < participants.size(); i++) {
				org.pi4soa.cdl.ParticipantType partType=
							(org.pi4soa.cdl.ParticipantType)
							cdlpack.getTypeDefinitions().getParticipantTypes().get(i);
					
				org.pi4soa.service.behavior.ServiceDescription sdesc=
					BehaviorProjection.projectServiceDescription(cdlpack,
							partType, null);
			
				visitor.setRole(partType.getName());
				
				sdesc.visit(visitor);
			}
			
			visitor.completeModels();
			
		} catch(Exception e) {
			throw new BPMN2GenerationException("Failed to generate BPMN model", e);
		}
	}
	
	public class BPMN2ModelVisitor extends DefaultVisitor {
			
		private BPMN2ModelFactory m_modelFactory=null;
		private BPMN2NotationFactory m_notationFactory=null;
		private String m_choreoName=null;
		private String m_role=null;
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
		 * This method sets the name associated with the role
		 * being projected.
		 * 
		 * @param role The role
		 */
		public void setRole(String role) {
			m_role = role;
		}
		
		/**
		 * This method starts visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public boolean start(Protocol elem) {
			
			try {
				BPMNDiagram diagram=getBPMNModel(elem);
				
				String role=m_role;
				
				if (elem.getRole() != null) {
					role += " ["+elem.getRole().getName()+"]";
				}
				
				BPMNPool pool=diagram.createPool(role);
				
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
		public void completeModels() throws BPMN2GenerationException {
			java.util.Iterator<BPMNDiagram> iter=m_activityModels.values().iterator();
			
			while (iter.hasNext()) {
				BPMNDiagram amodel=iter.next();
				
				amodel.completeModel();
			}
		}
	}
}
