/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.parser.rules;

import java.util.List;

import javax.xml.namespace.QName;

import org.savara.bpel.model.TCatch;
import org.savara.bpel.model.TInvoke;
import org.savara.bpel.model.TScope;
import org.savara.bpel.model.TVariable;
import org.savara.bpel.util.BPELInteractionUtil;
import org.savara.bpel.util.PartnerLinkUtil;
import org.savara.bpel.util.TypeReferenceUtil;
import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * This class represents an unsupported (or custom) action within
 * the conversation based ESB service descriptor.
 *  
 */
public class InvokeParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TInvoke);
	}
		
	public void convert(ConversionContext context, Object component, List<Activity> activities,
								FeedbackHandler handler) {
		TInvoke invoke=(TInvoke)component;
		
		//getSource().setComponentURI(getURI());
		
		convertRequest(invoke, activities, context);
		
		// Check if invoke is contained within a scope that defines
		// fault handlers. If so, then generate choice to throw
		// fault exceptions.
		TScope scope=context.getScope();
		
		if (scope != null && scope.getFaultHandlers() != null &&
						(scope.getFaultHandlers().getCatch().size() > 0 ||
						scope.getFaultHandlers().getCatchAll() != null)) {
			org.scribble.protocol.model.Choice te=
					new org.scribble.protocol.model.Choice();
			//te.derivedFrom(this);
			
			activities.add(te);
			
			org.scribble.protocol.model.When when=
						new org.scribble.protocol.model.When();
			te.getWhens().add(when);
			
			activities = when.getBlock().getContents();
			
			for (int i=0; i < scope.getFaultHandlers().getCatch().size(); i++) {
				TCatch catchBlock=scope.getFaultHandlers().getCatch().get(i);

				org.scribble.protocol.model.When fcb=
					new org.scribble.protocol.model.When();
				
				QName mesgType=catchBlock.getFaultMessageType();
				
				if (mesgType == null) {
					mesgType = catchBlock.getFaultElement();
				}
				
				TVariable faultVar=null;
				
				if (catchBlock.getFaultVariable() != null) {
					faultVar = new TVariable();
					faultVar.setName(catchBlock.getFaultVariable());
					faultVar.setMessageType(catchBlock.getFaultMessageType());
					faultVar.setElement(catchBlock.getFaultElement());			
					context.addVariable(faultVar);
				}
				
				convertFaultResponse(invoke, fcb.getBlock().getContents(), catchBlock.getFaultVariable(),
									mesgType, context);
				
				org.scribble.protocol.model.Raise raise=
							new org.scribble.protocol.model.Raise();
				
				TypeReference tref=new TypeReference();
				tref.setName(catchBlock.getFaultName().getLocalPart());
				raise.setType(tref);
				
				fcb.getBlock().add(raise);
				
				te.getWhens().add(fcb);
				
				if (faultVar != null) {
					context.removeVariable(faultVar);
				}
			}
		}
		
		if (invoke.getOutputVariable() != null) {
			
			convertResponse(invoke, activities, context);
		}
	}
	
	protected static void convertRequest(TInvoke invoke, java.util.List<Activity> activities,
			ConversionContext context) {
		// Create interaction for request
		Interaction interaction=new Interaction();
		//interaction.derivedFrom(this);
		
		TVariable var=context.getVariable(invoke.getInputVariable());
		
		String xmlType=BPELInteractionUtil.getXMLType(context.getProcess(), var.getMessageType(),
								context.getResourceLocator());

		TypeReference tref=TypeReferenceUtil.createTypeReference(xmlType, context);
				
		MessageSignature ms=new MessageSignature();
		ms.setOperation(invoke.getOperation());
		ms.getTypeReferences().add(tref);
		
		//if (context.getRole() != null) {
		//	interaction.setFromRole(new Role(context.getRole()));
		//}
		
		String toRole=PartnerLinkUtil.getClientPartnerRole(invoke.getPartnerLink());
		
		if (toRole != null) {
			interaction.getToRoles().add(new Role(toRole));
		}
		
		interaction.setMessageSignature(ms);
		
		activities.add(interaction);
	}

	protected static void convertResponse(TInvoke invoke, java.util.List<Activity> activities,
			ConversionContext context) {
		
		// Create interaction for request
		Interaction interaction=new Interaction();
		//interaction.derivedFrom(this);
		
		TVariable var=context.getVariable(invoke.getOutputVariable());
		
		String xmlType=BPELInteractionUtil.getXMLType(context.getProcess(), var.getMessageType(),
						context.getResourceLocator());

		TypeReference tref=TypeReferenceUtil.createTypeReference(xmlType, context);
		
		MessageSignature ms=new MessageSignature();
		ms.setOperation(invoke.getOperation());
		ms.getTypeReferences().add(tref);
		
		String fromRole=PartnerLinkUtil.getClientPartnerRole(invoke.getPartnerLink());
		
		if (fromRole != null) {
			interaction.setFromRole(new Role(fromRole));
		}
		
		//if (context.getRole() != null) {
		//	interaction.getToRoles().add(new Role(context.getRole()));
		//}
		
		interaction.setMessageSignature(ms);
		
		activities.add(interaction);
	}
	
	protected static void convertFaultResponse(TInvoke invoke, java.util.List<Activity> activities,
			String faultVar, QName faultMesgType, ConversionContext context) {
		
		// Create interaction for request
		Interaction interaction=new Interaction();
		//interaction.derivedFrom(this);
		
		TypeReference tref=null;
		
		// TODO: Not sure if fault variable is supposed to be declared
		// in catch scope, or reused from outer scope?? If declared,
		// then without the message type/element, may be difficult to
		// define the message signature.
		
		TVariable var=context.getVariable(faultVar);
		
		if (var != null) {
			String xmlType=BPELInteractionUtil.getXMLType(context.getProcess(), var.getMessageType(),
							context.getResourceLocator());

			tref = TypeReferenceUtil.createTypeReference(xmlType, context);
			//tref.setLocalpart(var.getMessageType());
		} else if (faultMesgType != null) {
			tref = TypeReferenceUtil.createTypeReference(faultMesgType.getLocalPart(), context);
			//tref.setLocalpart(faultMesgType);
		}
		
		MessageSignature ms=new MessageSignature();
		ms.setOperation(invoke.getOperation());
		ms.getTypeReferences().add(tref);
		
		String fromRole=PartnerLinkUtil.getClientPartnerRole(invoke.getPartnerLink());
		
		if (fromRole != null) {
			interaction.setFromRole(new Role(fromRole));
		}
		
		//if (context.getRole() != null) {
		//	interaction.getToRoles().add(new Role(context.getRole()));
		//}
		
		interaction.setMessageSignature(ms);
		
		activities.add(interaction);
	}
}
