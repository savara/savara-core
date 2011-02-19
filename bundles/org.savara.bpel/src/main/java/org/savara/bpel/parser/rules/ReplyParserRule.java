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

import org.savara.bpel.model.TReply;
import org.savara.bpel.model.TVariable;
import org.savara.bpel.util.BPELInteractionUtil;
import org.savara.bpel.util.PartnerLinkUtil;
import org.savara.bpel.util.TypeReferenceUtil;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.*;

/**
 * This class represents a BPEL reply activity.
 *  
 * @author gary
 */
public class ReplyParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TReply);
	}
		
	public void convert(ConversionContext context, Object component, List<Activity> activities,
									Journal journal) {
		TReply reply=(TReply)component;
		
		//getSource().setComponentURI(getURI());

		Interaction interaction=new Interaction();
		//interaction.derivedFrom(this);
		
		TVariable var=context.getVariable(reply.getVariable());
		
		String xmlType=BPELInteractionUtil.getXMLType(context.getProcess(), var.getMessageType(),
							context.getProtocolContext().getResourceLocator());

		TypeReference tref=TypeReferenceUtil.createTypeReference(xmlType, context);
		
		MessageSignature ms=new MessageSignature();
		//ms.derivedFrom(this);

		ms.setOperation(reply.getOperation());
		ms.getTypeReferences().add(tref);
		
		//if (context.getRole() != null) {
		//	interaction.setFromRole(new Role(context.getRole()));
		//}
		
		String toRole=PartnerLinkUtil.getServerPartnerRole(reply.getPartnerLink());
		
		if (toRole != null) {
			interaction.getToRoles().add(new Role(toRole));
		}
		
		interaction.setMessageSignature(ms);
		
		activities.add(interaction);
	}

}
