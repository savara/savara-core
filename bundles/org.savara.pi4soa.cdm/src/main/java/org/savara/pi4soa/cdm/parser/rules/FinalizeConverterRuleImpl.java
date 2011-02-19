/*
 * Copyright 2005-8 Pi4 Technologies Ltd
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
 * 1 Jun 2009 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

public class FinalizeConverterRuleImpl implements ConverterRule {

	/**
	 * This method determines whether the rule can be applied
	 * to the supplied CDL type.
	 * 
	 * @param scribbleType The Scribble target type
	 * @param cdlType The CDL type
	 * @return Whether the rule is appropriate to convert
	 * 					the CDL type
	 */
	public boolean isSupported(Class<?> scribbleType,
						CDLType cdlType) {
		return(scribbleType == org.scribble.protocol.model.Activity.class &&
				cdlType instanceof org.pi4soa.cdl.Finalize);
	}
	
	/**
	 * This method converts the supplied CDL type into a
	 * Scribble model object.
	 * 
	 * @param context The converters context
	 * @param scribbleType The Scribble target type
	 * @param cdlType The CDL type to be converted
	 * @return The converted Scribble model object
	 */
	public ModelObject convert(ConverterContext context,
			Class<?> scribbleType, CDLType cdlType) {
		org.scribble.protocol.model.Run ret=
					new org.scribble.protocol.model.Run();
		org.pi4soa.cdl.Finalize cdl=(org.pi4soa.cdl.Finalize)cdlType;
		
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
				CDLTypeUtil.getURIFragment(cdl));
		ret.getAnnotations().add(scannotation);

		ProtocolReference ref=new ProtocolReference();
		ref.setName(cdl.getChoreography().getName()+"_"+cdl.getFinalizer().getName());
		
		ret.setProtocolReference(ref);
		
		// Find conversation related to reference, and use
		// as inner definition initially - to help with
		// subsequent processing (e.g. locating initiator
		// roles). The inner definition will be cleared when
		// the model is fully converted.

		/* TODO: Is this required for Scribble v2
		Protocol prot=context.getProtocol(ref);
		
		if (prot != null) {
			ret.setInlineDefinition(prot);
			context.getComposeActivities().add(ret);
		
			// Bind roles
			java.util.List<Role> roles=prot.getRoles();
			
			for (int i=0; i < roles.size(); i++) {
				Role role=roles.get(i);
				
				Object decl=context.getState(role.getName());
				
				if (decl instanceof Role) {
					ret.getBindings().add(new DeclarationBinding(((Role)decl).getName(), role.getName()));
				}
			}
		}
		*/
				
		return(ret);
	}
}
