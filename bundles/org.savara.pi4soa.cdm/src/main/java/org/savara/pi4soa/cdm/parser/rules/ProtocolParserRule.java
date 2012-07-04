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
import org.pi4soa.common.util.NamesUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

public class ProtocolParserRule implements ParserRule {

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
		return(scribbleType == Protocol.class &&
				cdlType instanceof Choreography);
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
		Protocol ret=new Protocol();
		Choreography choreo=(Choreography)cdlType;
		
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		Object uri=CDLTypeUtil.getURIFragment(choreo);
		
		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
				uri);
		ret.getAnnotations().add(scannotation);
			
		ret.getProperties().put(ModelProperties.URI, uri);

		context.pushScope();
		
		//LocatedName modelName=new LocatedName();
		//modelName.setName(choreo.getName());
		
		ret.setName(choreo.getName());
		
		// Define roles
		//defineRoles(context, choreo, ret.getBlock());
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(choreo);
		
		for (Role r : roles) {
			ParameterDefinition pd=new ParameterDefinition();
			pd.setName(r.getName());
			ret.getParameterDefinitions().add(pd);
			
			context.setState(r.getName(), r);
			
			if (choreo.getRoot() == Boolean.TRUE) {
				
				// Associate Namespace annotation with protocol
				Annotation annotation=AnnotationDefinitions.getAnnotation(r.getAnnotations(),
							AnnotationDefinitions.INTERFACE);
				
				if (annotation != null) {
					Annotation pa=new Annotation(AnnotationDefinitions.INTERFACE);
					pa.getProperties().putAll(annotation.getProperties());
					pa.getProperties().put(AnnotationDefinitions.ROLE_PROPERTY, r.getName());
					ret.getAnnotations().add(pa);
				}
			}
		}

		java.util.List<Introduces> declared=CDMProtocolParserUtil.getRoleDeclarations(choreo);
		
		if (declared.size() > 0) {
			for (Introduces intro : declared) {
				
				for (Role r : intro.getIntroducedRoles()) {
					//rl.getRoles().add(r);
					
					context.setState(r.getName(), r);
					
					// Associate Namespace annotation with protocol
					Annotation annotation=AnnotationDefinitions.getAnnotation(r.getAnnotations(),
								AnnotationDefinitions.INTERFACE);
					
					if (annotation != null) {
						Annotation pa=new Annotation(AnnotationDefinitions.INTERFACE);
						pa.getProperties().putAll(annotation.getProperties());
						pa.getProperties().put(AnnotationDefinitions.ROLE_PROPERTY, r.getName());
						ret.getAnnotations().add(pa);
					}
				}
				
				ret.getBlock().add(intro);
			}
		}
		
		// Check if root, then need to project other sibling choreos
		if (choreo.getRoot() == Boolean.TRUE) {
			
			java.util.Iterator<Choreography> citer=
				choreo.getPackage().getChoreographies().iterator();
	
			while (citer.hasNext()) {
				Choreography subchoreo=citer.next();
				
				if (subchoreo != choreo) {
					Protocol subconv=(Protocol)
							parse(context, Protocol.class, subchoreo);
					
					ret.getNestedProtocols().add(subconv);
					
					context.addProtocol(subconv);
				}
				
				for (int i=0; i < subchoreo.getFinalizers().size(); i++) {
					FinalizerHandler finalizer=subchoreo.getFinalizers().get(i);
					
					ParserRule rule=ParserRuleFactory.getConverter(
							Protocol.class, finalizer);
					
					if (rule != null) {
						
						Protocol subconv=(Protocol)
							rule.parse(context, Protocol.class, finalizer);
					
						ret.getNestedProtocols().add(subconv);
					
						context.addProtocol(subconv);
					}
				}
			}
		}
		
		// Process any sub-choreographies	
		java.util.Iterator<Choreography> citer=
					choreo.getEnclosedChoreographies().iterator();
		
		while (citer.hasNext()) {
			Choreography subchoreo=citer.next();

			Protocol subconv=(Protocol)
					parse(context, Protocol.class, subchoreo);
			
			ret.getNestedProtocols().add(subconv);
			
			context.addProtocol(subconv);
			
			for (int i=0; i < subchoreo.getFinalizers().size(); i++) {
				FinalizerHandler finalizer=subchoreo.getFinalizers().get(i);
				
				ParserRule rule=ParserRuleFactory.getConverter(
						Protocol.class, finalizer);
				
				if (rule != null) {
					
					subconv = (Protocol)
						rule.parse(context, Protocol.class, finalizer);
				
					ret.getNestedProtocols().add(subconv);
				
					context.addProtocol(subconv);
				}
			}
		}
		
		// Check if exception handlers have been defined
		// and/or completion condition
		if (NamesUtil.isSet(choreo.getCompletionCondition()) ||
			(choreo.getExceptionHandler() != null &&
					choreo.getExceptionHandler().getExceptionWorkUnits().size() > 0)) {
			
			Do te=new Do();
			ret.getBlock().getContents().add(te);
			
			// Process all of the activities within the
			// choreography
			convertActivities(context, choreo.getActivities(),
						te.getBlock());
			
			for (int i=0; choreo.getExceptionHandler() != null &&
						i < choreo.getExceptionHandler().
						getExceptionWorkUnits().size(); i++) {
				ExceptionWorkUnit ewu=choreo.getExceptionHandler().
					getExceptionWorkUnits().get(i);
				
				Interrupt interruptPath=new Interrupt();

				/*
				 * TODO: Consider how to deal with catch types
				 *
				if (NamesUtil.isSet(ewu.getExceptionType())) {
					TypeReference ref=new TypeReference();
					ref.setLocalpart(XMLUtils.getLocalname(ewu.getExceptionType()));
					ref.setNamespace(CDLTypeUtil.getNamespace(
							ewu.getExceptionType(), choreo));
					
					catchPath.setType(ref);					
				}
				*/
				
				te.getInterrupts().add(interruptPath);
				
				convertActivities(context, ewu.getActivities(),
								interruptPath.getBlock());
			}
			
			/*
			 * TODO: Deal with completion condition
			 *
			if (NamesUtil.isSet(choreo.getCompletionCondition())) {
				InterruptBlock interrupt=new InterruptBlock();
				
				te.getEscapeBlocks().add(interrupt);
				
				// Set expression from completion condition
				// using xpath
				XPathExpression exp=new XPathExpression();
				
				exp.setQuery(choreo.getCompletionCondition());
				
				interrupt.setExpression(exp);

				// No activities
			}
			*/
		} else {
		
			// Process all of the activities within the
			// choreography
			convertActivities(context, choreo.getActivities(), ret.getBlock());
		}
		
		// Check if top level protocol needs role parameters
		if (choreo.getRoot() == Boolean.TRUE && ret.getParameterDefinitions().size() == 0 &&
				ret.getBlock().size() > 0 && ret.getBlock().get(0) instanceof Introduces) {
			Introduces intros=(Introduces)ret.getBlock().get(0);
			
			ParameterDefinition pd=new ParameterDefinition();
			pd.setName(intros.getIntroducer().getName());
			
			ret.getParameterDefinitions().add(pd);
		}
		
		context.popScope();
		
		return(ret);
	}

	protected static void convertActivities(ParserContext context,
				java.util.List<org.pi4soa.cdl.Activity> acts, Block block) {
		
		java.util.Iterator<org.pi4soa.cdl.Activity> actiter=
						acts.iterator();
		
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
	}
	
}
