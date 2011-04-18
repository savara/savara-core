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
 * 24 Jul 2008 : Initial version created by gary
 */
package org.savara.bpel.model.change;

import org.savara.protocol.model.change.AbstractModelChangeContext;
import org.savara.protocol.model.change.ModelChangeRule;
import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.ProtocolContext;

/**
 * This class provides a default implementation for the model
 * change context interface.
 */
public class BPELModelChangeContext extends AbstractModelChangeContext {
	
	private static java.util.List<ModelChangeRule> m_rules=new java.util.Vector<ModelChangeRule>();

	static {
		m_rules.add(new ChoiceModelChangeRule());
		m_rules.add(new InteractionModelChangeRule());
		m_rules.add(new ProtocolModelChangeRule());
		m_rules.add(new ProtocolModelModelChangeRule());
		m_rules.add(new RepeatModelChangeRule());
		m_rules.add(new RunModelChangeRule());
	}
	
	/**
	 * This is the constructor for the model change context.
	 * 
	 * @param context The protocol context
	 * @param journal The journal
	 */
	public BPELModelChangeContext(ProtocolContext context, FeedbackHandler journal) {
		super(context, journal);
	}
	
	/**
	 * This method returns a list of model change rules appropriate
	 * for the notation being changed.
	 * 
	 * @return The list of model change rules
	 */
	public java.util.List<ModelChangeRule> getRules() {
		return(m_rules);
	}
}
