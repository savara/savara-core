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

/**
 * This class represents a factory for converter rules.
 */
public class ParserRuleFactory {

	/**
	 * This method converts the supplied CDL type into a
	 * Scribble model object.
	 * 
	 * @param scribbleType The Scribble target type
	 * @param cdlType The CDL type to be converted
	 * @return The converter rule, or null if not found
	 */
	public static ParserRule getConverter(Class<?> scribbleType,
						org.pi4soa.cdl.CDLType cdlType) {
		ParserRule ret=null;
		
		for (int i=0; ret == null && i < m_rules.length; i++) {
			if (m_rules[i].isSupported(scribbleType, cdlType)) {
				ret = m_rules[i];
			}
		}
		
		return(ret);
	}
	
	private static ParserRule[] m_rules={
		new ProtocolModelParserRule(),
		new ProtocolParserRule(),
		new ParallelParserRule(),
		new ChoiceParserRule(),
		new ConditionalParserRule(),
		new WhenParserRule(),
		new WhileParserRule(),
		new InteractionParserRule(),
		new FinalizeParserRule(),
		new FinalizerHandlerParserRule(),
		new AssignParserRule(),
		new SequenceParserRule(),
		new PerformParserRule()
	};
}
