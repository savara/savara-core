/*
 * Copyright 2005-7 Pi4 Technologies Ltd
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
 * Jan 25, 2007 : Initial version created by gary
 */
package org.savara.bpmn2.internal.generation.process.components;

import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Interaction;

/**
 * This class represents the BPMN2 activity node for a Send activity.
 * 
 */
public class SendActivity extends SimpleActivity {
	
	private Interaction m_send=null;

	/**
	 * This constructor initializes the send state.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public SendActivity(Interaction act,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
		super(act, parent, model, notation);
		
		m_send = act;
	}
	
	protected Object createNode(Activity act) {
		return(getModelFactory().createSendTask(getContainer(), act));
	}
	
	/**
	 * This method returns the behavioral send activity.
	 * 
	 * @return The send activity
	 */
	public Interaction getSend() {
		return(m_send);
	}
	
}
