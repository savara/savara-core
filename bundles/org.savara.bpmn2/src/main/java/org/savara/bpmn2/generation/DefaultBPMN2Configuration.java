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
 * This class provides the default implementation for the
 * BPMN configuration interface.
 *
 */
public class DefaultBPMN2Configuration implements BPMN2Configuration {

	/**
	 * This is the default constructor.
	 *
	 */
	public DefaultBPMN2Configuration() {
	}
	
	/**
	 * This method returns the UML export format to be used. The list
	 * of valid export formats is available using the
	 * #UMLGenerator.getExportFormats() method.
	 * 
	 * @return The export format, or null if the default is being used.
	 */
	public String getExportFormat() {
		return(m_exportFormat);
	}
	
	/**
	 * This method sets the export format.
	 * 
	 * @param format The export format
	 */
	public void setExportFormat(String format) {
		m_exportFormat = format;
	}
	
	private String m_exportFormat=null;
}
