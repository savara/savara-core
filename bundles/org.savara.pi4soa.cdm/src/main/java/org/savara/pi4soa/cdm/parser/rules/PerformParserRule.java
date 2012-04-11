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
 * 27 Jun 2008 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

public class PerformParserRule implements ParserRule {

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
				cdlType instanceof org.pi4soa.cdl.Perform);
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
	public ModelObject parse(ParserContext context,
			Class<?> scribbleType, CDLType cdlType) {
		org.pi4soa.cdl.Perform cdl=(org.pi4soa.cdl.Perform)cdlType;
		ModelObject ret=null;
		
		// Check if context contains an interaction from the performed choreography
		// that is to be ignored - this would indicate that the perform is contained
		// within a choice
		/* TODO: Check if this is required, as using more liberal choice
		if (context.shouldExpandChoreography(cdl.getChoreography())) {
			ret = new Block();
			
			context.pushScope();
			
			// Define roles
			//ProtocolConverterRuleImpl.defineRoles(context, cdl.getChoreography(), (Block)ret);
			java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(cdl.getChoreography());
			
			for (Role r : roles) {
				context.setState(r.getName(), r);
			}
			
			roles = CDMProtocolParserUtil.getRoleDeclarations(cdl.getChoreography());
			
			if (roles.size() > 0) {
				Introduces rl=new Introduces();
				for (Role r : roles) {
					rl.getRoles().add(r);
					context.setState(r.getName(), r);
				}
				((Block)ret).add(rl);
			}
			
			// Convert sub-activities
			ProtocolParserRule.convertActivities(context, cdl.getChoreography().getActivities(), (Block)ret);
		
			context.popScope();
			
		} else {
		*/
			org.scribble.protocol.model.Run run=new org.scribble.protocol.model.Run();
			
			if (cdl.getWaitForCompletion() != Boolean.TRUE) {
				// TODO: Need to consider how to deal with async perform
				// possibly when split is supported
			}
			
			Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

			Object uri=CDLTypeUtil.getURIFragment(cdl);
			
			scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
					uri);
			run.getAnnotations().add(scannotation);
			
			run.getProperties().put(ModelProperties.URI, uri);
	
			ProtocolReference ref=new ProtocolReference();
			ref.setName(cdl.getChoreography().getName());
			
			run.setProtocolReference(ref);
			
			// Generate roles
			java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(cdl.getChoreography());
			
			for (Role r : roles) {
				Parameter p=new Parameter();
				p.setName(r.getName());
				run.getParameters().add(p);
			}
			
			// Convert binding parameters
			java.util.List<BindDetails> bds=cdl.getBindDetails();
			
			for (int i=0; i < bds.size(); i++) {
				
				// Only convert if a variable, not a channel
				// TODO: Need to also convert explicit channels
				
				/* TODO SAVARA-160 Don't convert variables for now, as protocol does not
				 * use them. However when assertions are defined, they may be
				 * useful, and also if participants are bound. 
				 *
				if (bds.get(i).getThisVariable() instanceof org.pi4soa.cdl.Variable) {
					Parameter db=new Parameter();
					
					Object state=context.getState(bds.get(i).getThisVariable().getName());
					
					if (state instanceof Role) {
						db.setName(((Role)state).getName());
						//db.setBoundName(bds.get(i).getFreeVariable().getName());
					
						run.getParameters().add(db);
					} else {
						logger.finer("State not found for '"+bds.get(i).getThisVariable().getName()+"'");
					}
				}
				*/
			}
			
			ret = run;
		//}
				
		return(ret);
	}
	
	//private static Logger logger = Logger.getLogger("org.savara.pi4soa.cdm.parser.rules");
}
