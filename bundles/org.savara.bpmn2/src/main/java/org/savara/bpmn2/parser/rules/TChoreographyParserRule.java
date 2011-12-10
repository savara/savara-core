/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpmn2.parser.rules;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TExclusiveGateway;
import org.savara.bpmn2.model.TFlowElement;
import org.savara.bpmn2.model.TFlowNode;
import org.savara.bpmn2.model.TSequenceFlow;
import org.savara.bpmn2.model.TStartEvent;
import org.savara.common.logging.MessageFormatter;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.Protocol;

public class TChoreographyParserRule implements BPMN2ParserRule {

	/**
	 * This method determines whether the rule supports the
	 * supplied BPMN2 model element.
	 * 
	 * @param elem The element
	 * @return Whether the rule parses the supplied element
	 */
	public boolean isSupported(Object elem) {
		return(elem.getClass() == TChoreography.class);
	}

	/**
	 * This method parses the supplied element against the supplied
	 * context.
	 * 
	 * @param context The context
	 * @param elem The element
	 * @param container The container into which converted objects should be placed
	 */
	public void parse(BPMN2ParserContext context, Object elem, Block container) {
		TChoreography choreo=(TChoreography)elem;
		
		// Need to find the 'start event'
		TStartEvent startEvent=null;
		
		for (JAXBElement<? extends TFlowElement> jaxb : choreo.getFlowElement()) {
			if (jaxb.getValue().getClass() == TStartEvent.class) {
				if (startEvent != null) {
					context.getFeedbackHandler().error(MessageFormatter.format(
							java.util.PropertyResourceBundle.getBundle(
									"org.savara.bpmn2.Messages"), "SAVARA-BPMN2-00001"), null);
				} else {
					startEvent = (TStartEvent)jaxb.getValue();
				}
			}
		}

		if (startEvent == null) {
			context.getFeedbackHandler().error(MessageFormatter.format(
					java.util.PropertyResourceBundle.getBundle(
							"org.savara.bpmn2.Messages"), "SAVARA-BPMN2-00002"), null);
		} else {
			processNode(context, startEvent, container);
		}
	}
	
	protected void processNode(BPMN2ParserContext context, TFlowNode elem, Block container) {
		
		BPMN2ParserRule rule=ParserRuleFactory.getParserRule(elem);
		
		if (rule != null) {
			rule.parse(context, elem, container);
		}
		
		// Check outbound connections to see whether sequence or gateway
		if (elem.getOutgoing().size() == 1) {
			
			// Get link
			TSequenceFlow seq=(TSequenceFlow)
					context.getScope().getBPMN2Element(elem.getOutgoing().get(0).getLocalPart());
			
			if (seq != null) {
				Object target=seq.getTargetRef();
				
				if (target instanceof TFlowNode) {
					processNode(context, (TFlowNode)target, container);
				}
			}
		} else if (elem.getOutgoing().size() > 1) {
			
			if (elem instanceof TExclusiveGateway) {
				Choice choice=new Choice();
				
				container.add(choice);
				
				for (QName seqFlowQName : elem.getOutgoing()) {
					TSequenceFlow seq=(TSequenceFlow)
							context.getScope().getBPMN2Element(seqFlowQName.getLocalPart());
					
					Block b=new Block();
					choice.getPaths().add(b);
					
					if (seq.getTargetRef() instanceof TFlowNode) {
						processNode(context, (TFlowNode)seq.getTargetRef(), b);
					}
				}
			}
		}
	}
}
