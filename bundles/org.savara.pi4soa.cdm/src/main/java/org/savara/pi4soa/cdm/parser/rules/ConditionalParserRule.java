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

public class ConditionalParserRule implements ParserRule {

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
				cdlType instanceof org.pi4soa.cdl.Conditional);
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
		org.scribble.protocol.model.Choice ret=
				new org.scribble.protocol.model.Choice();
		org.pi4soa.cdl.Conditional cdl=(org.pi4soa.cdl.Conditional)cdlType;
		
		/*
		// Find exchange details for this path
		InteractionLocator locator=new InteractionLocator(cdl);
		
		cdl.visit(locator);
		
		// If interaction found, then make sure it is ignored when
		// processing the when path contents
		if (locator.getInteraction() != null &&
				locator.getInteraction().getExchangeDetails().size() > 0) {
			context.ignore(locator.getInteraction().getExchangeDetails().get(0));
		}
		*/

		//ret.getSource().setComponentURI(
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		Object uri=CDLTypeUtil.getURIFragment(cdl);
		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY, uri);
		ret.getAnnotations().add(scannotation);

		ret.getProperties().put(ModelProperties.URI, uri);
		
	
		Block block=new Block();
		ret.getPaths().add(block);
		
		/*
		if (locator.getInteraction() != null &&
				locator.getInteraction().getExchangeDetails().size() > 0) {
			
			scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

			scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
					CDLTypeUtil.getURIFragment(locator.getInteraction()));
			block.getAnnotations().add(scannotation);

			if (locator.getInteraction().getToRoleType() != null) {
				// TODO: Deal with interfaces that have multiple behaviours	
				Annotation annotation=new Annotation(AnnotationDefinitions.INTERFACE);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
								InteractionParserRule.getInterfaceName(locator.getInteraction()));
				block.getAnnotations().add(annotation);
			}
			
			// Get the first exchange details
			ExchangeDetails ed=locator.getInteraction().getExchangeDetails().get(0);
			
			if (ed != null) {
			
				MessageSignature ms=InteractionParserRule.createMessageSignature(ed,
										block);
			
				// TODO: Need to update/verify from/to roles on containing choice
			
				block.setMessageSignature(ms);
				
				ret.setFromRole(new Role(InteractionParserRule.getFromRole(context,
											ed)));
				
				ret.setToRole(new Role(InteractionParserRule.getToRole(context,
											ed)));
				
				if (ed.getAction() == ExchangeActionType.RESPOND) {
					Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
					annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY,
								CDMProtocolParserUtil.getLabel(ed));
					block.getAnnotations().add(annotation);
				} else {
					// Check if request has response/fault exchanges
					java.util.List<ExchangeDetails> resps=
							InteractionUtil.getResponseExchangeDetails(ed);
					
					if (resps != null && resps.size() > 0) {
						Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
						annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY,
									CDMProtocolParserUtil.getLabel(ed));
						block.getAnnotations().add(annotation);
					}
				}
			}
		}
		*/

		context.pushState();
		
		// Set the expression if defined in the 'conditional'
		if (cdl.getExpression() != null) {
			//XPathExpression exp=new XPathExpression();
		
			//exp.setQuery(cdl.getExpression());
		
			//block.setExpression(exp);
			//block.getProperties().put("Expression", cdl.getExpression());
			
			Annotation assertion=new Annotation(AnnotationDefinitions.ASSERTION);
			
			assertion.getProperties().put(AnnotationDefinitions.EXPRESSION_PROPERTY,
					cdl.getExpression());
			assertion.getProperties().put(AnnotationDefinitions.LANGUAGE_PROPERTY,
									"xpath");
			
			ret.getAnnotations().add(assertion);
		}
		
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
						block.add(activity);
					}
				}
			}
		}
		
		// Get initiator role(s) for first activity in body and
		// associated with If
		//ret.getRoles().addAll(block.getInitiatorRoles());
		
		// TODO: Need to (1) setup from/to roles and the interaction message that
		// triggers the conditional path, and (2) add a second path with just a label
		// to represent an optional bypass path.
		
		context.popState();
		
		return(ret);
	}
}
