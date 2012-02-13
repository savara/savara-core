/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 * Copyright 2012 Red Hat, Inc.
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
 * 13 Dec 2008 : Initial version created by gary
 * Feb 2012 : Update based on scribble v2
 */
package org.savara.protocol.model.stateless;

import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.*;

/**
 * This class provides a tool for transforming a stateful
 * local definition into a stateless definition. 
 */
public interface StatelessTransformer {

	/**
	 * This method transforms the supplied definition.
	 * 
	 * @param stateful The stateful protocol model
	 * @param messageBased Whether the transformation should be
	 * 			message based, instead of RPC based
	 * @param journal The journal
	 * @return The stateless protocol model
	 */
	public ProtocolModel transform(ProtocolModel stateful, boolean messageBased,
							Journal journal);
	
}
