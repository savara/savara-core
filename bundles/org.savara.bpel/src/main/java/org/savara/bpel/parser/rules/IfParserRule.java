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

import org.savara.bpel.model.TActivity;
import org.savara.bpel.model.TElseif;
import org.savara.bpel.model.TIf;
import org.savara.bpel.util.ActivityUtil;
import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * This class represents an 'if' grouping construct.
 *  
 */
public class IfParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TIf);
	}
		
	public void parse(ParserContext context, Object component, List<Activity> activities,
								FeedbackHandler handler) {
		TIf bpelElem=(TIf)component;
		
		//getSource().setComponentURI(getURI());
		
		org.scribble.protocol.model.Choice elem=
					new org.scribble.protocol.model.Choice();
		
		When cb=new When();
		
		// TODO: Convert the conditional expression
		
		TActivity act=ActivityUtil.getActivity(bpelElem);
		
		if (act != null) {
			context.parse(act, cb.getBlock().getContents(), handler);
			
			// Check if first activity is interaction
			if (cb.getBlock().getContents().size() > 0 &&
					cb.getBlock().getContents().get(0) instanceof Interaction) {
				Interaction interaction=(Interaction)cb.getBlock().getContents().get(0);
				
				cb.getBlock().getContents().remove(0);
				
				cb.derivedFrom(interaction);
				
				cb.setMessageSignature(interaction.getMessageSignature());
				
				elem.setFromRole(interaction.getFromRole());
				
				if (interaction.getToRoles().size() > 0) {
					elem.setToRole(interaction.getToRoles().get(0));
				}
			} else {
				handler.error("Main block of if does not contain an initial interaction", null);
			}
		}
		
		elem.getWhens().add(cb);
		
		// Convert 'else if' paths
		for (int i=0; i < bpelElem.getElseif().size(); i++) {
			TElseif elseIfElem=bpelElem.getElseif().get(i);
			
			cb = new When();
			
			context.parse(elseIfElem, cb.getBlock().getContents(), handler);
			
			setupWhenMs(elem, cb, handler);

			elem.getWhens().add(cb);
		}
		
		// Convert 'else' path
		if (bpelElem.getElse() != null) {
			cb = new When();
			
			context.parse(bpelElem.getElse(), cb.getBlock().getContents(), handler);
			
			setupWhenMs(elem, cb, handler);
			
			elem.getWhens().add(cb);
		}
		
		activities.add(elem);
	}
	
	protected void setupWhenMs(Choice elem, When cb, FeedbackHandler handler) {
		
		// Check if first activity is interaction
		if (cb.getBlock().getContents().size() > 0 &&
				cb.getBlock().getContents().get(0) instanceof Interaction) {
			Interaction interaction=(Interaction)cb.getBlock().getContents().get(0);
			
			cb.getBlock().getContents().remove(0);
			
			cb.derivedFrom(interaction);
			
			cb.setMessageSignature(interaction.getMessageSignature());
			
			// Verify from/to roles
			if (elem.getFromRole() != null) {
				if (interaction.getFromRole() != null) {
					if (elem.getFromRole().equals(interaction.getFromRole()) == false) {
						handler.error("ElseIf path has interaction with incompatible 'from' role", null);
					}
				} else {
					handler.error("ElseIf path does not contain a required 'from' role", null);
				}
			}
			elem.setFromRole(interaction.getFromRole());
			
			if (elem.getToRole() != null) {
				if (interaction.getToRoles().size() > 0) {
					if (elem.getToRole().equals(interaction.getToRoles().get(0)) == false) {
						handler.error("ElseIf path has interaction with incompatible 'to' role", null);
					}
				} else {
					handler.error("ElseIf path does not contain a required 'to' role", null);
				}
			}
		} else {
			handler.error("Main block of if does not contain an initial interaction", null);
		}
	}
}
