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
package org.savara.bpmn2.internal.generation;

/**
 * This class represents a BPMN generation exception.
 *
 */
public class BPMN2GenerationException extends Exception {

	private static final long serialVersionUID = -8580109779260853013L;

	/**
	 * This constructor initializes the UML generation exception
	 * with a message.
	 * 
	 * @param mesg The message
	 */
	public BPMN2GenerationException(String mesg) {
		super(mesg);
	}
	
	/**
	 * This constructor initializes the UML generation exception
	 * with a message and associated exception.
	 * 
	 * @param mesg The message
	 * @param t The associated exception
	 */
	public BPMN2GenerationException(String mesg, Throwable t) {
		super(mesg, t);
	}
}
