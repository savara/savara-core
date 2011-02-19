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
package org.savara.protocol.model.util;

import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

/**
 * This class contains utility functions for dealing with Interactions.
 */
public class InteractionUtil {
	
	/**
	 * This method returns the name associated with the interaction.
	 * 
	 * @param interaction The interaction
	 * @return The name
	 */
	public static String getName(Interaction interaction) {
		String ret=null;
		
		if (isSend(interaction)) {
			ret = "Send";
		} else {
			ret = "Receive";
		}
		
		for (TypeReference ref : interaction.getMessageSignature().getTypeReferences()) {
			ret += "_"+ref.getName();
		}

		return(ret);
	}
	
	/**
	 * This method returns the name associated with the interaction.
	 * 
	 * @param when The when
	 * @return The name
	 */
	public static String getName(When when) {
		String ret=null;
		
		if (isSend(when)) {
			ret = "Send";
		} else {
			ret = "Receive";
		}
		
		for (TypeReference ref : when.getMessageSignature().getTypeReferences()) {
			ret += "_"+ref.getName();
		}

		return(ret);
	}
	
	/**
	 * This method determines whether the supplied interaction,
	 * within a located protocol, is a send.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a send
	 */
	public static boolean isSend(Interaction interaction) {
		Role role=null;
		
		if (interaction.enclosingProtocol() != null) {
			role = interaction.enclosingProtocol().getRole();
		}
		
		return(isSend(interaction, role));
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is a send.
	 * 
	 * @param interaction The interaction
	 * @param role The located role
	 * @return Whether the interaction is a send
	 */
	public static boolean isSend(Interaction interaction, Role role) {
		boolean ret=false;
		
		if (role != null && ((interaction.getFromRole() != null &&
				interaction.getFromRole().equals(
						role)) ||
			(interaction.getToRoles().size() > 0 &&
					interaction.getToRoles().contains(
						role) == false))) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is a send.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a send
	 */
	public static boolean isSend(When when) {		
		Role role=null;
		
		if (((Choice)when.getParent()).enclosingProtocol() != null) {
			role = ((Choice)when.getParent()).enclosingProtocol().getRole();
		}
		
		return(isSend(when, role));
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is a send.
	 * 
	 * @param interaction The interaction
	 * @param role The located role
	 * @return Whether the interaction is a send
	 */
	public static boolean isSend(When when, Role role) {
		boolean ret=false;
		
		if (role != null && ((((Choice)when.getParent()).getFromRole() != null &&
				((Choice)when.getParent()).getFromRole().equals(
						role)) ||
					(((Choice)when.getParent()).getToRole() != null &&
					((Choice)when.getParent()).getToRole().equals(
						role) == false))) {
			ret = true;
		}
		
		return(ret);
	}
	
	public static String getRequestLabel(Interaction interaction) {
		String ret=null;
		Annotation annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
							AnnotationDefinitions.CORRELATION);
		if (annotation != null) {
			ret = (String)annotation.getProperties().get(AnnotationDefinitions.REQUEST_PROPERTY);
		}
		return(ret);
	}
	
	public static String getReplyToLabel(Interaction interaction) {
		String ret=null;
		Annotation annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
				AnnotationDefinitions.CORRELATION);
		if (annotation != null) {
			ret = (String)annotation.getProperties().get(AnnotationDefinitions.REPLY_TO_PROPERTY);
		}
		return(ret);
	}
	
	public static String getRequestLabel(When interaction) {
		String ret=null;
		Annotation annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
				AnnotationDefinitions.CORRELATION);
		if (annotation != null) {
			ret = (String)annotation.getProperties().get(AnnotationDefinitions.REQUEST_PROPERTY);
		}
		return(ret);
	}
	
	public static String getReplyToLabel(When interaction) {
		String ret=null;
		Annotation annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
				AnnotationDefinitions.CORRELATION);
		if (annotation != null) {
			ret = (String)annotation.getProperties().get(AnnotationDefinitions.REPLY_TO_PROPERTY);
		}
		return(ret);
	}

	/**
	 * This method determines whether the supplied interaction
	 * is a request.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a request
	 */
	public static boolean isRequest(Interaction interaction) {
		boolean ret=false;

		if (getRequestLabel(interaction) != null ||
						getReplyToLabel(interaction) == null) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is a request.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a request
	 */
	public static boolean isRequest(When interaction) {
		boolean ret=false;

		if (getRequestLabel(interaction) != null ||
				getReplyToLabel(interaction) == null) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is a response.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a response
	 */
	public static boolean isResponse(Interaction interaction) {
		boolean ret=false;

		if (getReplyToLabel(interaction) != null) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is a response.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a response
	 */
	public static boolean isResponse(When interaction) {
		boolean ret=false;

		if (getReplyToLabel(interaction) != null) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method determines if the supplied interaction is a fault
	 * response.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a fault response
	 */
	public static boolean isFaultResponse(Interaction interaction) {
		boolean ret=false;
		
		if (isResponse(interaction) &&
				AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
						AnnotationDefinitions.FAULT) != null) {
			ret = true;
		}

		return(ret);
	}
	
	/**
	 * This method determines if the supplied interaction is a fault
	 * response.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a fault response
	 */
	public static boolean isFaultResponse(When interaction) {
		boolean ret=false;
		
		if (InteractionUtil.isResponse(interaction) &&
				AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
						AnnotationDefinitions.FAULT) != null) {
			ret = true;
		}

		return(ret);
	}
	
	/**
	 * This method returns the fault name associated with the supplied
	 * interaction.
	 * 
	 * @param interaction The interaction
	 * @return The fault name, or null if not found
	 */
	public static String getFaultName(Interaction interaction) {
		String ret=null;
		Annotation annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
							AnnotationDefinitions.FAULT);
		
		if (annotation != null) {
			ret = (String)annotation.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the fault name associated with the supplied
	 * interaction.
	 * 
	 * @param interaction The interaction
	 * @return The fault name, or null if not found
	 */
	public static String getFaultName(When interaction) {
		String ret=null;
		Annotation annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
				AnnotationDefinitions.FAULT);
		
		if (annotation != null) {
			ret = (String)annotation.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
		}
		
		return(ret);
	}
	
}
