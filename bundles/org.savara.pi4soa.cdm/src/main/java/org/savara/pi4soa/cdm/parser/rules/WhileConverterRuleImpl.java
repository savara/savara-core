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
 * 6 Jun 2008 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

public class WhileConverterRuleImpl implements ConverterRule {

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
				cdlType instanceof org.pi4soa.cdl.While);
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
		org.scribble.protocol.model.Repeat ret=
				new org.scribble.protocol.model.Repeat();
		org.pi4soa.cdl.While cdl=(org.pi4soa.cdl.While)cdlType;
		
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
				CDLTypeUtil.getURIFragment(cdl));
		ret.getAnnotations().add(scannotation);

		context.pushState();
		
		// Set the expression if defined in the 'while'
		/*
		if (cdl.getExpression() != null &&
				cdl.getExpression().trim().length() > 0) {
			XPathExpression exp=new XPathExpression();
		
			exp.setQuery(cdl.getExpression());
		
			ret.getBlock().setExpression(exp);
		}
		*/
				
		// Process all of the activities within the
		// choreography
		java.util.Iterator<org.pi4soa.cdl.Activity> actiter=
					cdl.getActivities().iterator();
		while (actiter.hasNext()) {
			org.pi4soa.cdl.Activity act=actiter.next();
			
			ConverterRule rule=ConverterRuleFactory.getConverter(
					org.scribble.protocol.model.Activity.class, act);
		
			if (rule != null) {
				org.scribble.protocol.model.Activity activity=
					(org.scribble.protocol.model.Activity)
					rule.convert(context,
							org.scribble.protocol.model.Activity.class, act);
				
				if (activity != null) {
					if (activity instanceof Block) {
						ret.getBlock().getContents().addAll(((Block)activity).getContents());
					} else {
						ret.getBlock().getContents().add(activity);
					}
				}
			}
		}
		
		// Get initiator role(s) for first activity in body and
		// associated with While
		//ret.getRoles().addAll(ret.getBlock().getInitiatorRoles());
		
		context.popState();
		
		return(ret);
	}
}
