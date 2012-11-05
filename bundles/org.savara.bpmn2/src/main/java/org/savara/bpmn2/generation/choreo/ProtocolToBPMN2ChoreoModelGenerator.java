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
package org.savara.bpmn2.generation.choreo;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.savara.bpmn2.internal.generation.BPMN2GenerationException;
import org.savara.bpmn2.internal.generation.BPMN2ModelFactory;
import org.savara.bpmn2.internal.generation.BPMN2NotationFactory;
import org.savara.bpmn2.internal.generation.components.AbstractBPMNActivity;
import org.savara.bpmn2.internal.generation.components.BPMNActivity;
import org.savara.bpmn2.internal.generation.components.BPMNDiagram;
import org.savara.bpmn2.internal.generation.components.ChoiceActivity;
import org.savara.bpmn2.internal.generation.components.Choreography;
import org.savara.bpmn2.internal.generation.components.ChoreographyTask;
import org.savara.bpmn2.internal.generation.components.DoActivity;
import org.savara.bpmn2.internal.generation.components.DoBlockActivity;
import org.savara.bpmn2.internal.generation.components.ForkActivity;
import org.savara.bpmn2.internal.generation.components.JoinActivity;
import org.savara.bpmn2.internal.generation.components.ParallelActivity;
import org.savara.bpmn2.internal.generation.components.RepeatActivity;
import org.savara.bpmn2.internal.generation.components.RunActivity;
import org.savara.bpmn2.internal.generation.components.SequenceActivity;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.model.TError;
import org.savara.bpmn2.model.TImport;
import org.savara.bpmn2.model.TInterface;
import org.savara.bpmn2.model.TItemDefinition;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TOperation;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.model.TRootElement;
import org.savara.bpmn2.util.BPMN2ServiceUtil;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.resources.ResourceLocator;
import org.savara.protocol.model.Join;
import org.savara.protocol.model.Fork;
import org.savara.protocol.model.util.InteractionUtil;
import org.scribble.protocol.model.*;

/**
 * This class represents the Protocol to BPMN2 Choreography implementation of the model
 * generator interface.
 */
public class ProtocolToBPMN2ChoreoModelGenerator implements ModelGenerator {

	private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	private static final String BPMN_FILE_EXTENSION = ".bpmn";
	private boolean _consecutiveIds=false;
	private org.savara.bpmn2.model.ObjectFactory _objectFactory=new org.savara.bpmn2.model.ObjectFactory();
	
	private static final Logger logger=Logger.getLogger(ProtocolToBPMN2ChoreoModelGenerator.class.getName());
	
	/**
	 * This method determines whether consecutive ids should be used in the model and
	 * notation. If false (default), then random unique ids will be used.
	 * 
	 * @param b Whether to use consecutive ids
	 */
	public void setUseConsecutiveIds(boolean b) {
		_consecutiveIds = b;
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
				!((ProtocolModel)source).isLocated() &&
				(targetType.equals("bpmn2") || targetType.equals("bpmn")));
	}

	/**
	 * {@inheritDoc}
	 */
	public java.util.Map<String,Object> generate(Object source, FeedbackHandler handler,
							ResourceLocator locator) {
		java.util.Map<String,Object> ret=new java.util.HashMap<String,Object>();

		if (source instanceof ProtocolModel) {
			ProtocolModel pm=(ProtocolModel)source;
			
			processProtocol(pm, pm.getProtocol(), ret, handler, locator);
		}
		
		return(ret);
	}
	
	protected String getProtocolName(Protocol p) {
		String ret=p.getName();
		
		if (p.getParent() instanceof Protocol) {
			ret = getProtocolName((Protocol)p.getParent())+"_"+ret;
		}
		
		return(ret);
	}
	
	/**
	 * This method processes a protocol to derive the BPMN2 choreography model for the
	 * top level or nested protocols.
	 * 
	 * @param pm The protocol model
	 * @param p The protocol (top level or nested)
	 * @param modelMap The model map
	 * @param handler The handler
	 * @param locator The resource locator
	 * @return The model file
	 */
	protected String processProtocol(ProtocolModel pm, Protocol p, java.util.Map<String,Object> modelMap,
						FeedbackHandler handler, ResourceLocator locator) {
		TDefinitions defns=new TDefinitions();

		// Set an id on the definition - not strictly required,
		// although the BPMN2 modeler currently seems to want
		// it
		defns.setId("id-"+pm.getProtocol().getName());				
		
		org.savara.bpmn2.internal.generation.BPMN2ModelFactory model=
				new org.savara.bpmn2.internal.generation.BPMN2ModelFactory(defns);
		org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation=
				new org.savara.bpmn2.internal.generation.BPMN2NotationFactory(model);
			
		model.setUseConsecutiveIds(_consecutiveIds);
		notation.setUseConsecutiveIds(_consecutiveIds);
			
		// Find namespace for role
		initNamespace(defns, pm, p);
		
		initImports(defns, pm);
		
		initMessages(defns, pm);
		
		String modelName=getProtocolName(p);
		
		BPMN2ModelVisitor visitor=
			new BPMN2ModelVisitor(modelName, p, model, notation);
		
		generateChoreography(p, visitor, handler, locator);
		
		visitor.completeModels();
		
		// Define interfaces for the choreography
		java.util.Map<TParticipant,TInterface> intfs=
				BPMN2ServiceUtil.introspect(defns);

		if (intfs.size() > 0) {
			BPMN2ServiceUtil.merge(defns, intfs);
		} else if (logger.isLoggable(Level.FINE)) {
			logger.fine("No interfaces detected in generated BPMN2 choreography");
		}
		
		String ret=modelName+BPMN_FILE_EXTENSION;
		
		modelMap.put(ret, defns);
		
		// Check if nested protocols have been defined
		for (Protocol nested : p.getNestedProtocols()) {
			
			String nestedModelFile=processProtocol(pm, nested, modelMap, handler, locator);
			
			TDefinitions nestedDefn=(TDefinitions)modelMap.get(nestedModelFile);
			
			// Define import for nested protocol
			TImport imp=new TImport();
			
			imp.setImportType("http://www.omg.org/spec/BPMN/20100524/MODEL");
			imp.setLocation(nestedModelFile);
			imp.setNamespace(nestedDefn.getTargetNamespace());
			
			defns.getImport().add(imp);
		}
		
		return(ret);
	}
	
	protected void initNamespace(TDefinitions defns, ProtocolModel pm, Protocol p) {
		Annotation ann=AnnotationDefinitions.getAnnotation(p.getAnnotations(),
							AnnotationDefinitions.PROTOCOL);
		
		if (ann != null) {
			defns.setTargetNamespace((String)ann.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY));
		}
	}
	
	protected void initImports(TDefinitions defns, ProtocolModel pm) {
		java.util.List<Annotation> anns=AnnotationDefinitions.getAnnotations(pm.getProtocol().getAnnotations(),
						AnnotationDefinitions.TYPE);
		
		for (Annotation ann : anns) {
			String ns=(String)ann.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY);
			String loc=(String)ann.getProperties().get(AnnotationDefinitions.LOCATION_PROPERTY);
			
			if (!ns.equals(XSD_NAMESPACE)) {
				// Add import
				TImport imp=new TImport();
				imp.setImportType(XSD_NAMESPACE); // Assume xsd for now
				imp.setLocation(loc);
				imp.setNamespace(ns);
				
				defns.getImport().add(imp);
			}
		}
	}
	
	protected void initMessages(TDefinitions defns, ProtocolModel pm) {

		for (ImportList il : pm.getImports()) {

			if (il instanceof TypeImportList) {
				TypeImportList til=(TypeImportList)il;
				
				for (TypeImport ti : til.getTypeImports()) {
					TItemDefinition itemDef=new TItemDefinition();
					itemDef.setId("ITEM"+ti.getName());
					itemDef.setStructureRef(QName.valueOf(ti.getDataType().getDetails()));
					defns.getRootElement().add(_objectFactory.createItemDefinition(itemDef));
					
					TMessage mesg=new TMessage();
					mesg.setId("ID"+ti.getName());
					mesg.setName(ti.getName());
					mesg.setItemRef(new QName(defns.getTargetNamespace(),itemDef.getId()));
					defns.getRootElement().add(_objectFactory.createMessage(mesg));
				}
			}
		}
	}
	
	protected TInterface getInterface(TDefinitions defns, Role role) {
		TInterface ret=null;
		String intfName=role.getName();
		
		for (JAXBElement<? extends TRootElement> rootElem : defns.getRootElement()) {
			if (rootElem.getValue() instanceof TInterface &&
					((TInterface)rootElem.getValue()).getName().equals(intfName)) {
				ret = (TInterface)rootElem.getValue();
				break;
			}
		}
		
		if (ret == null) {
			ret = new TInterface();
			ret.setId(intfName+BPMN2ServiceUtil.INTERFACE_ID_SUFFIX);
			ret.setName(intfName);
			
			defns.getRootElement().add(_objectFactory.createInterface(ret));
		}
		
		return(ret);
	}
	
	protected void generateChoreography(Protocol p, BPMN2ModelVisitor visitor,
					FeedbackHandler handler, ResourceLocator locator) {
		p.visit(visitor);
	}
	
	public class BPMN2ModelVisitor extends DefaultVisitor {
		
		private BPMN2ModelFactory _modelFactory=null;
		private BPMN2NotationFactory _notationFactory=null;
		private String _choreoName=null;
		private Protocol _protocol=null;
	    private java.util.List<BPMNActivity> _bpmnActivityStack=new java.util.ArrayList<BPMNActivity>();
	    private java.util.Map<String,BPMNDiagram> _activityModels=
	    				new java.util.HashMap<String,BPMNDiagram>();

	    /**
		 * The constructor the BPMN model visitor.
		 * 
		 */
		public BPMN2ModelVisitor(String choreoName, Protocol p,
				BPMN2ModelFactory model, BPMN2NotationFactory notation) {
			_choreoName = choreoName;
			_protocol = p;
			_modelFactory = model;
			_notationFactory = notation;
		}
		
		/**
		 * This method starts visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public boolean start(Protocol elem) {
			
			if (elem == _protocol) {
				try {
					BPMNDiagram diagram=getBPMNModel(elem);
					
					Choreography choreo=diagram.createChoreography(_choreoName);
					
					pushBPMNActivity(choreo);
				} catch(Exception e) {
					logger.severe("Failed to get state machine " +
							"for behavior '"+elem+"': "+e);
				}
				
				return (true);
			}
			
			return (false);
		}
		
		/**
		 * This method ends visiting the behavior description element.
		 * 
		 * @param elem The behavior description
		 */
		public void end(Protocol elem) {

			if (elem == _protocol) {
				BPMNActivity umls=getBPMNActivity();
				
				if (umls != null) {
					umls.childrenComplete();
				}
	
				popBPMNActivity();
			}
		}
		
		/**
		 * This method starts visiting the choice element.
		 * 
		 * @param elem The choice
		 */
		public boolean start(Choice elem) {
			
			try {
				pushBPMNActivity(new ChoiceActivity(elem,
						getBPMNActivity(), _modelFactory, _notationFactory));
				
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
						getBPMNActivity(), _modelFactory, _notationFactory));
				
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
		public void accept(Run elem) {
			//AbstractBPMNActivity state=null;
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				new RunActivity(elem, umls, _modelFactory, _notationFactory);					
			}
		}
		
		/**
		 * This method indicates the start of a
		 * global escape.
		 * 
		 * @param elem The global escape
		 * @return Whether to process the contents
		 */
		public boolean start(Do elem) {
			AbstractBPMNActivity state=null;
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				state = new DoActivity(elem,
					umls, _modelFactory, _notationFactory);
			
				pushBPMNActivity(state);
				
				DoBlockActivity inline=new DoBlockActivity(state, _modelFactory, _notationFactory);
				
				pushBPMNActivity(inline);
			}
			
			return(true);
		}
		
		/**
		 * This method indicates the end of a
		 * try escape.
		 * 
		 * @param elem The global escape
		 */
		public void end(Do elem) {
			
			if (getBPMNActivity() instanceof DoBlockActivity) {				
				popBPMNActivity();
			}
			
			popBPMNActivity();
		}
		
		/**
		 * This method indicates the start of a
		 * catch block.
		 * 
		 * @param elem The catch block
		 * @return Whether to process the contents
		 */
		public boolean start(Interrupt elem) {
			if (getBPMNActivity() instanceof DoBlockActivity) {				
				popBPMNActivity();
			}
			
			return(true);
		}
		
		/**
		 * This method indicates the end of a
		 * catch block.
		 * 
		 * @param elem The catch block
		 */
		public void end(Interrupt elem) {
			//popBPMNActivity();
		}

		/**
		 * This method visits the receive activity.
		 * 
		 * @param elem The receive
		 */
		public void accept(Interaction elem) {
			
			BPMNActivity umls=getBPMNActivity();
			if (umls != null) {
				new ChoreographyTask(elem, umls, _modelFactory, _notationFactory);
			}	
		}
		
		/**
		 * This method starts visiting the sequence element.
		 * 
		 * @param elem The sequence
		 */
		public boolean start(Block elem) {
			
			try {
				pushBPMNActivity(new SequenceActivity(getBPMNActivity(), _modelFactory, _notationFactory));
				
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
						getBPMNActivity(), _modelFactory, _notationFactory));
				
				pushBPMNActivity(new SequenceActivity(getBPMNActivity(), _modelFactory, _notationFactory));
				
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
		
		/**
		 * {@inheritDoc}
		 */
		public void accept(CustomActivity act) {
			
			if (act instanceof Fork) {
				BPMNActivity umls=getBPMNActivity();
				if (umls != null) {
					new ForkActivity((Fork)act, umls, _modelFactory, _notationFactory);
				}
			} else if (act instanceof Join) {
				BPMNActivity umls=getBPMNActivity();
				if (umls != null) {
					new JoinActivity((Join)act, umls, _modelFactory, _notationFactory);
				}
			}
		}
		
		protected BPMNDiagram getBPMNModel(Protocol elem) throws BPMN2GenerationException {
			String name=elem.getName();
			
			BPMNDiagram ret=(BPMNDiagram)
						_activityModels.get(name);
			
			if (ret == null) {
				ret = new BPMNDiagram(_choreoName, name,
						null, _modelFactory, _notationFactory);
				
				_activityModels.put(name, ret);
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
			_bpmnActivityStack.add(0, act);
		}
		
		/**
		 * This method returns the UML activity found at the
		 * top of the stack.
		 * 
		 * @return The activity
		 */
		protected BPMNActivity getBPMNActivity() {
			BPMNActivity ret=null;
			
			if (_bpmnActivityStack.size() > 0) {
				ret = (BPMNActivity)_bpmnActivityStack.get(0);
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
			
			if (_bpmnActivityStack.size() > 0) {
				_bpmnActivityStack.remove(0);
			}
		}
		
		/**
		 * This method completes the construction of the activity
		 * models.
		 *
		 */
		public void completeModels() {
			java.util.Iterator<BPMNDiagram> iter=_activityModels.values().iterator();
			
			while (iter.hasNext()) {
				BPMNDiagram amodel=iter.next();
				
				amodel.completeModel();
			}
		}
		
		/**
		 * This method establishes the associated operation details,
		 * if not defined, and returns a reference to it.
		 * 
		 * @param interaction The interaction
		 * @return The operation reference
		 */
		protected QName getOperationReference(Interaction interaction, QName messageRef) {
			QName ret=null;
			
			// Check that interaction has an operation name
			if (interaction.getMessageSignature().getOperation() == null) {
				return(null);
			}
			
			// Find interface
			Role serverRole=null;
			
			if (InteractionUtil.isRequest(interaction)) {
				if (interaction.getToRoles().size() > 0) {
					serverRole = interaction.getToRoles().get(0);
				}
				
				if (serverRole == null) {
					serverRole = interaction.getEnclosingProtocol().getLocatedRole();
				}
			} else {
				serverRole = interaction.getFromRole();
				
				if (serverRole == null) {
					serverRole = interaction.getEnclosingProtocol().getLocatedRole();
				}
			}
			
			if (serverRole == null) {
				// TODO: REPORT ERROR
				return (null);
			}
			
			TInterface intf=getInterface(_modelFactory.getDefinitions(), serverRole);
			
			if (intf == null) {
				// TODO: REPORT ERROR
				return (null);
			}
			
			// Find operation
			TOperation op=null;
			
			for (TOperation curop : intf.getOperation()) {
				if (interaction.getMessageSignature().getOperation() != null &&
						curop.getName().equals(interaction.getMessageSignature().getOperation())) {
					op = curop;
					break;
				}
			}
			
			if (op == null) {
				op = new TOperation();
				op.setName(interaction.getMessageSignature().getOperation());
				op.setId("OP_"+serverRole.getName()+"_"+op.getName());
				
				intf.getOperation().add(op);
			}
			
			ret = new QName(_modelFactory.getDefinitions().getTargetNamespace(), op.getId());
			
			if (InteractionUtil.isRequest(interaction)) {
				if (op.getInMessageRef() == null) {
					op.setInMessageRef(messageRef);
				} else if (!op.getInMessageRef().equals(messageRef)) {
					// TODO: Report mismatch
				}
			} else if (InteractionUtil.isFaultResponse(interaction)) {
				String faultName=InteractionUtil.getFaultName(interaction);
				TError error=null;
				
				// Check for Error definition with associated fault name and message ref
				for (JAXBElement<? extends TRootElement> rootElem :
							_modelFactory.getDefinitions().getRootElement()) {
					if (rootElem.getValue() instanceof TError) {
						TError cur=(TError)rootElem.getValue();
						
						// Check if error has same fault name
						// TODO: may need to also check same item def??
						if (cur.getErrorCode().equals(faultName)) {
							error = cur;
							break;
						}
					}
				}
				
				if (error != null) {
					QName qname=new QName(_modelFactory.getDefinitions().getTargetNamespace(), error.getId());
					
					if (!op.getErrorRef().contains(qname)) {
						op.getErrorRef().add(qname);
					}
				} else {
					// TODO: Report unable to find Error for fault name
				}
			} else {
				// Normal response
				if (op.getOutMessageRef() == null) {
					op.setOutMessageRef(messageRef);
				} else if (!op.getOutMessageRef().equals(messageRef)) {
					// TODO: Report mismatch
				}
			}
			
			return(ret);
		}
	}
}
