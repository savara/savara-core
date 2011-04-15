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
import org.savara.bpel.model.TOnMessage;
import org.savara.bpel.model.TPick;
import org.savara.bpel.model.TVariable;
import org.savara.bpel.util.ActivityUtil;
import org.savara.bpel.util.BPELInteractionUtil;
import org.savara.bpel.util.PartnerLinkUtil;
import org.savara.bpel.util.TypeReferenceUtil;
import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * This class represents a pick grouping activity.
 *  
 * @author gary
 */
public class PickParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TPick);
	}
		
	public void parse(ParserContext context, Object component, List<Activity> activities,
								FeedbackHandler handler) {
		TPick pick=(TPick)component;
		
		//getSource().setComponentURI(getURI());
		
		org.scribble.protocol.model.Choice elem=
					new org.scribble.protocol.model.Choice();
		
		Role fromRole=null;
		
		// Convert 'onMessage' paths
		for (int i=0; i < pick.getOnMessage().size(); i++) {
			TOnMessage onMessageElem=pick.getOnMessage().get(i);
			
			When cb = new When();
			
			context.parse(onMessageElem, cb.getBlock().getContents(), handler);
			
			String fromRoleName=PartnerLinkUtil.getServerPartnerRole(onMessageElem.getPartnerLink());
			
			if (i == 0) {
				// Obtain from/to roles
				if (fromRoleName != null) {
					fromRole = new Role(fromRoleName);
				}
			} else {
				// TODO: Check 'from role' for other paths
				if (fromRoleName != null &&
						fromRole != null &&
						fromRoleName.equals(fromRole.getName()) == false) {
					handler.error("Pick path has different from roles", null);
				}
			}
			
			elem.setFromRole(fromRole);

			TVariable var=context.getVariable(onMessageElem.getVariable());
			
			String xmlType=BPELInteractionUtil.getXMLType(context.getProcess(), var.getMessageType(),
							context.getResourceLocator());

			TypeReference tref=TypeReferenceUtil.createTypeReference(xmlType, context);
			
			MessageSignature ms=new MessageSignature();
			ms.setOperation(onMessageElem.getOperation());
			ms.getTypeReferences().add(tref);
			
			cb.setMessageSignature(ms);
			
			// Process the contained activities
			TActivity act=ActivityUtil.getActivity(onMessageElem);
			
			if (act != null) {
				context.parse(act, cb.getBlock().getContents(), handler);
			}

			elem.getWhens().add(cb);
		}
		
		// TODO: If alarms defined, then model these using a
		// try/catch with interrupt?
		
		activities.add(elem);
	}
}
