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
import org.savara.bpel.model.TProcess;
import org.savara.bpel.model.TSequence;
import org.savara.bpel.model.TVariable;
import org.savara.bpel.util.ActivityUtil;
import org.savara.bpel.util.BPELInteractionUtil;
import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * This class represents a BPEL process.
 *  
 * @author gary
 */
public class ProcessParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TProcess);
	}
		
	public void parse(ParserContext context, Object component, List<Activity> activities,
						FeedbackHandler handler) {
		TProcess process=(TProcess)component;
		
		// Add variables to the context
		for (int i=0; i < process.getVariables().getVariable().size(); i++) {
			context.addVariable(process.getVariables().getVariable().get(i));
		}
		
		// Count number of invoke activities
		//int invokeCount=BPELInteractionUtil.countInvokes(process);
		
		// Check whether scope has been defined to represent
		// an interaction with one or more fault responses and
		// no event handlers
		TInvoke invoke=null;
		TActivity act=ActivityUtil.getActivity(process);
		
		if (//invokeCount == 1 &&
				process.getFaultHandlers() != null && (process.getFaultHandlers().getCatch().size() > 0 ||
						process.getFaultHandlers().getCatchAll() != null) &&
				(process.getEventHandlers() == null || (process.getEventHandlers().getOnEvent().size() == 0 &&
						process.getEventHandlers().getOnAlarm().size() == 0)) &&
				(invoke = BPELInteractionUtil.getInvoke(act)) != null) {

			InvokeParserRule.convertRequest(invoke, activities, context);
			
			// Create choice with normal response and fault paths
			org.scribble.protocol.model.Choice choice=new org.scribble.protocol.model.Choice();
			
			Block cb=new Block();
			
			InvokeParserRule.convertResponse(invoke, cb.getContents(), context);
			
			// Include remaining activities
			if (act instanceof TSequence) {
				for (int i=1; i < ((TSequence)act).getActivity().size(); i++) {
					context.parse(((TSequence)act).getActivity().get(i), cb.getContents(),
										handler);
				}
			}
			
			choice.getBlocks().add(cb);
			
			// Process fault handlers
			for (int i=0; i < process.getFaultHandlers().getCatch().size(); i++) {
				TCatch catchBlock=process.getFaultHandlers().getCatch().get(i);
				
				Block fcb=new Block();
				
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
				
				InvokeParserRule.convertFaultResponse(invoke,
						fcb.getContents(), catchBlock.getFaultVariable(),
									mesgType, context);
				
				TActivity cbact=ActivityUtil.getActivity(catchBlock);
				
				if (cbact != null) {
					context.parse(cbact, fcb.getContents(), handler);
				}
				
				choice.getBlocks().add(fcb);
				
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
			if (process.getFaultHandlers() != null &&
					(process.getFaultHandlers().getCatch().size() > 0 ||
							process.getFaultHandlers().getCatchAll() != null)) {
				org.scribble.protocol.model.Try te=
						new org.scribble.protocol.model.Try();
				//te.derivedFrom(this);
				//te.getBlock().derivedFrom(this);
				
				acts.add(te);
				
				acts = te.getBlock().getContents();
		
				for (int i=0; i < process.getFaultHandlers().getCatch().size(); i++) {
					TCatch catchPath=process.getFaultHandlers().getCatch().get(i);
					
					org.scribble.protocol.model.Catch cb=
						new org.scribble.protocol.model.Catch();
					//cb.derivedFrom(catchPath);				
					
					/* TODO: How to deal with catch based on type?
					 * 
					 *
					TypeReference tref=new TypeReference();
					tref.setName(XMLUtils.getLocalname(catchPath.getFaultName()));
					cb.setType(tref);
					*/
					
					TVariable faultVar=null;
					
					if (catchPath.getFaultVariable() != null) {
						faultVar = new TVariable();
						faultVar.setName(catchPath.getFaultVariable());
						faultVar.setMessageType(catchPath.getFaultMessageType());
						faultVar.setElement(catchPath.getFaultElement());			
						context.addVariable(faultVar);
					}
					
					TActivity cbact=ActivityUtil.getActivity(catchPath);
					
					if (cbact != null) {
						context.parse(cbact, cb.getBlock().getContents(), handler);
					}
					
					if (faultVar != null) {
						context.removeVariable(faultVar);
					}

					te.getCatches().add(cb);
				}
			}
			
			// Convert normal activities in scope
			if (act != null) {
				context.parse(act, acts, handler);
			}
		}
	}
}
