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

import org.savara.bpel.model.TActivity;
import org.savara.bpel.model.TCatch;
import org.savara.bpel.model.TInvoke;
import org.savara.bpel.model.TScope;
import org.savara.bpel.model.TSequence;
import org.savara.bpel.model.TVariable;
import org.savara.bpel.util.ActivityUtil;
import org.savara.bpel.util.BPELInteractionUtil;
import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * This class represents a scope grouping activity.
 *  
 * @author gary
 */
public class ScopeParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TScope);
	}
		
	public void convert(ConversionContext context, Object component, List<Activity> activities,
							FeedbackHandler handler) {
		TScope scope=(TScope)component;
		
		//getSource().setComponentURI(getURI());
		
		// Add variables to the context
		if (scope.getVariables() != null) {
			for (int i=0; i < scope.getVariables().getVariable().size(); i++) {
				context.addVariable(scope.getVariables().getVariable().get(i));
			}
		}
		
		// Count number of invoke activities
		//int invokeCount=BPELInteractionUtil.countInvokes(scope);

		// Check whether scope has been defined to represent
		// an interaction with one or more fault responses and
		// no event handlers
		TInvoke invoke=null;
		
		if (//invokeCount == 1 &&
				scope.getFaultHandlers() != null && (scope.getFaultHandlers().getCatch().size() > 0 ||
						scope.getFaultHandlers().getCatchAll() != null) &&
				(scope.getEventHandlers() == null || (scope.getEventHandlers().getOnEvent().size() == 0 &&
						scope.getEventHandlers().getOnAlarm().size() == 0)) &&
				(invoke = BPELInteractionUtil.getInvoke(ActivityUtil.getActivity(scope))) != null) {

			InvokeParserRule.convertRequest(invoke, activities, context);
			
			// Create choice with normal response and fault paths
			org.scribble.protocol.model.Choice choice=new org.scribble.protocol.model.Choice();
			
			When cb=new When();
			
			InvokeParserRule.convertResponse(invoke, cb.getBlock().getContents(), context);
			
			if (cb.getBlock().getContents().size() > 0) {
				Interaction resp=(Interaction)cb.getBlock().getContents().get(0);
				cb.getBlock().getContents().remove(resp);
				
				cb.setMessageSignature(resp.getMessageSignature());
				
				choice.setFromRole(resp.getFromRole());
			}
			
			// Include remaining activities
			if (scope.getSequence() != null) {
				for (int i=1; i < ((TSequence)scope.getSequence()).getActivity().size(); i++) {
					context.convert(((TSequence)scope.getSequence()).getActivity().get(i),
									cb.getBlock().getContents(), handler);
				}
			}
			
			choice.getWhens().add(cb);
			
			// Process fault handlers
			for (int i=0; i < scope.getFaultHandlers().getCatch().size(); i++) {
				TCatch catchBlock=scope.getFaultHandlers().getCatch().get(i);
				
				When fcb=new When();
				
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
				
				InvokeParserRule.convertFaultResponse(invoke, fcb.getBlock().getContents(),
							catchBlock.getFaultVariable(), mesgType, context);

				if (fcb.getBlock().getContents().size() > 0) {
					Interaction resp=(Interaction)fcb.getBlock().getContents().get(0);
					fcb.getBlock().getContents().remove(resp);
					
					fcb.setMessageSignature(resp.getMessageSignature());
					
					// Validate from role
					if (resp.getFromRole() != null &&
							choice.getFromRole() != null &&
							resp.getFromRole().equals(choice.getFromRole()) == false) {
						handler.error("Fault handler 'from role' not same as normal response", null);
					}
				}
				
				TActivity act=ActivityUtil.getActivity(catchBlock);
				
				if (act != null) {
					context.convert(act, fcb.getBlock().getContents(), handler);
				}
				
				choice.getWhens().add(fcb);
				
				if (faultVar != null) {
					context.removeVariable(faultVar);
				}
			}
			
			activities.add(choice);
		} else {
			// Store in local var, in case try/catch block needs to be
			// added
			java.util.List<Activity> acts=activities;
			
			// Check if try/catch block is required
			if (scope.getFaultHandlers() != null &&
					(scope.getFaultHandlers().getCatch().size() > 0 ||
							scope.getFaultHandlers().getCatchAll() != null)) {
				org.scribble.protocol.model.Try te=
						new org.scribble.protocol.model.Try();
				//te.derivedFrom(this);
				//te.getBlock().derivedFrom(this);
				
				acts.add(te);
				
				acts = te.getBlock().getContents();
		
				for (int i=0; i < scope.getFaultHandlers().getCatch().size(); i++) {
					TCatch catchPath=scope.getFaultHandlers().getCatch().get(i);
					
					org.scribble.protocol.model.Catch cb=
						new org.scribble.protocol.model.Catch();
					//cb.derivedFrom(catchPath);
					
					TypeReference tref=new TypeReference();
					tref.setName(catchPath.getFaultName().getLocalPart());
					
					//cb.setType(tref);
					
					// TODO: Need to do a search for first interaction in the block,
					// and treat that as the triggering condition
					
					TVariable faultVar=null;
					
					if (catchPath.getFaultVariable() != null) {
						faultVar = new TVariable();
						faultVar.setName(catchPath.getFaultVariable());
						faultVar.setMessageType(catchPath.getFaultMessageType());
						faultVar.setElement(catchPath.getFaultElement());			
						context.addVariable(faultVar);
					}
					
					TActivity act=ActivityUtil.getActivity(catchPath);
					
					if (act != null) {
						context.convert(act, cb.getBlock().getContents(), handler);
					}
					
					if (faultVar != null) {
						context.removeVariable(faultVar);
					}

					te.getCatches().add(cb);
				}
			}
			
			// Convert normal activities in scope
			TActivity act=ActivityUtil.getActivity(scope);
			
			if (act != null) {
				context.convert(act, acts, handler);
			}
		}
	}
}
