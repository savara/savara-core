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
package org.savara.bpmn2.generation;

/**
 * This interface represents the configuration information used
 * by the BPMN projection to determine what model components and
 * features should be included.
 *
 */
public interface BPMN2Configuration {

	/**
	 * This method returns the UML export format to be used. The list
	 * of valid export formats is available using the
	 * #UMLGenerator.getExportFormats() method.
	 * 
	 * @return The export format, or null if the default is being used.
	 */
	public String getExportFormat();
	
}
