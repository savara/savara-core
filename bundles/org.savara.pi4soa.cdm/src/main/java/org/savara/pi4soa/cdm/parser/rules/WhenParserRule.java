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
import org.scribble.protocol.model.*;

public class WhenParserRule implements ParserRule {

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
				cdlType instanceof org.pi4soa.cdl.When);
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
		//org.scribble.conversation.model.When ret=
		//		new org.scribble.conversation.model.When();
		org.pi4soa.cdl.When cdl=(org.pi4soa.cdl.When)cdlType;
		
		//ret.getProperties().put("CDLType",
		//		CDLTypeUtil.getURIFragment(cdl));

		Block block=new Block();
		
		context.pushState();
		
		// Set the expression if defined in the 'when'
		/*
		if (cdl.getExpression() != null) {
			XPathExpression exp=new XPathExpression();
		
			exp.setQuery(cdl.getExpression());
		
			block.setExpression(exp);
		}
		*/
				
		// Process all of the activities within the
		// choreography
		java.util.Iterator<org.pi4soa.cdl.Activity> actiter=
					cdl.getActivities().iterator();
		while (actiter.hasNext()) {
			org.pi4soa.cdl.Activity act=actiter.next();
			
			ParserRule rule=ParserRuleFactory.getConverter(
					org.scribble.protocol.model.Activity.class, act);
		
			if (rule != null) {
				org.scribble.protocol.model.Activity activity=
					(org.scribble.protocol.model.Activity)
					rule.parse(context,
							org.scribble.protocol.model.Activity.class, act);
				
				if (activity != null) {
					if (activity instanceof Block) {
						block.getContents().addAll(((Block)activity).getContents());
					} else {
						block.getContents().add(activity);
					}
				}
			}
		}
		
		// Get initiator role(s) for first activity in body and
		// associated with When
		//ret.getRoles().addAll(block.getInitiatorRoles());
		
		context.popState();
				
		//return(ret);
		return(block);
	}
}
