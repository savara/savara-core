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
package org.savara.bpmn2.internal.parser.choreo.rules;

import javax.xml.namespace.QName;

import org.savara.bpmn2.model.TChoreographyTask;
import org.savara.bpmn2.model.TInterface;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TMessageFlow;
import org.savara.bpmn2.model.TOperation;
import org.savara.bpmn2.model.TParticipant;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.ParameterDefinition;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.TypeReference;

public class TChoreographyTaskParserRule implements BPMN2ParserRule {

	/**
	 * This method determines whether the rule supports the
	 * supplied BPMN2 model element.
	 * 
	 * @param elem The element
	 * @return Whether the rule parses the supplied element
	 */
	public boolean isSupported(Object elem) {
		return(elem.getClass() == TChoreographyTask.class);
	}

	/**
	 * This method parses the supplied element against the supplied
	 * context.
	 * 
	 * @param context The context
	 * @param elem The element
	 * @param container The container into which converted objects should be placed
	 */
	public void parse(BPMN2ParserContext context, Object elem, Block container) {
		TChoreographyTask task=(TChoreographyTask)elem;
		
		// TODO: May need to check ordering, as first message flow from
		// choreo task needs to be from the initiating participant
		
		for (QName mflowQName : task.getMessageFlowRef()) {
			TMessageFlow mflow=(TMessageFlow)
					context.getScope().getBPMN2Element(mflowQName.getLocalPart());
			
			parseMessageFlow(context, mflow, container);
		}
	}
	
	/**
	 * This method parsers the message flow.
	 * 
	 * @param context The context
	 * @param mflow The message flow
	 * @param container The container
	 */
	protected void parseMessageFlow(BPMN2ParserContext context, TMessageFlow mflow, Block container) {
		
		// Check if initiating participant is known
		TParticipant source=(TParticipant)context.getScope().getBPMN2Element(mflow.getSourceRef().getLocalPart());
		TParticipant target=(TParticipant)context.getScope().getBPMN2Element(mflow.getTargetRef().getLocalPart());
		
		// Create interaction
		Interaction interaction=new Interaction();
		
		// Get initiating role
		Role initiatingRole=context.getScope().getRole(source.getName());
		
		if (initiatingRole == null) {
			// Create role
			initiatingRole = new Role(source.getName());
			
			// Add to choreography parameter list
			context.getScope().registerRole(initiatingRole);
			
			ParameterDefinition pd=new ParameterDefinition();
			pd.setName(source.getName());
			
			container.getEnclosingProtocol().getParameterDefinitions().add(pd);
		}
		
		interaction.setFromRole(new Role(initiatingRole));
		
		// Define 'to' role
		Role otherRole=context.getScope().getRole(target.getName());
			
		if (otherRole == null) {
			// Need to introduce role
			otherRole = new Role(target.getName());
			
			// Add to scope
			context.getScope().registerRole(otherRole);

			Introduces intros=context.getScope().getIntroduces().get(initiatingRole);
					
			if (intros == null) {
				intros = new Introduces();
				intros.setIntroducer(initiatingRole);			
				
				context.getScope().getIntroduces().put(initiatingRole, intros);
			}

			intros.getIntroducedRoles().add(otherRole);
		}
		
		interaction.getToRoles().add(new Role(otherRole));
		
		if (mflow.getMessageRef() != null) {
			// Define message signature
			TMessage mesg=(TMessage)context.getScope().getBPMN2Element(mflow.getMessageRef().getLocalPart());
			
			MessageSignature msig=new MessageSignature();
			TypeReference tref=new TypeReference(mesg.getName());
			msig.getTypeReferences().add(tref);
			
			interaction.setMessageSignature(msig);
		}
		
		// Check for correlation information
		checkForCorrelation(context, interaction, source, target, mflow);

		// Add to containing block
		container.add(interaction);
	}
	
	protected void checkForCorrelation(BPMN2ParserContext context, Interaction interaction, TParticipant source,
			TParticipant target, TMessageFlow mflow) {
		Annotation annotation=null;

		// Check if request
		for (QName qname : target.getInterfaceRef()) {
			TInterface intf=(TInterface)context.getScope().getBPMN2Element(qname.getLocalPart());
			
			if (intf != null) {
				for (TOperation op : intf.getOperation()) {
					if (op.getInMessageRef() != null &&
							op.getInMessageRef().equals(mflow.getMessageRef())) {
						annotation=new Annotation(AnnotationDefinitions.CORRELATION);
						annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY,
									op.getName());
						
						interaction.getMessageSignature().setOperation(op.getName());
						break;
					}
				}
				if (annotation != null) {
					break;
				}
			}
		}
		
		if (annotation == null) {
			// Check if response
			for (QName qname : source.getInterfaceRef()) {
				TInterface intf=(TInterface)context.getScope().getBPMN2Element(qname.getLocalPart());
				
				if (intf != null) {
					for (TOperation op : intf.getOperation()) {
						if (op.getOutMessageRef() != null &&
								op.getOutMessageRef().equals(mflow.getMessageRef())) {
							annotation=new Annotation(AnnotationDefinitions.CORRELATION);
							annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY,
										op.getName());
							interaction.getMessageSignature().setOperation(op.getName());
							break;
						} else if (op.getErrorRef().contains(mflow.getMessageRef())) {
							annotation=new Annotation(AnnotationDefinitions.CORRELATION);
							annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY,
										op.getName());
							
							Annotation intfann=new Annotation(AnnotationDefinitions.FAULT);
							intfann.getProperties().put(AnnotationDefinitions.NAME_PROPERTY, mflow.getName());
							interaction.getAnnotations().add(intfann);
							interaction.getMessageSignature().setOperation(op.getName());
							break;
						}
					}
				}
				if (annotation != null) {
					break;
				}
			}
		}
			
		if (annotation != null) {
			interaction.getAnnotations().add(annotation);
		}
	}
}
