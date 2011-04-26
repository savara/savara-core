/*
 * Copyright 2005-6 Pi4 Technologies Ltd
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
 * 30 Jan 2007 : Initial version created by gary
 */
package org.savara.bpmn2.internal.generation.process;

import org.savara.bpmn2.model.BPMNDiagram;
import org.savara.bpmn2.model.BPMNPlane;
import org.savara.bpmn2.model.TDefinitions;



public class BPMN2NotationFactory {

	private TDefinitions m_definitions=null;
	private BPMNPlane m_plane=null;
	
	public BPMN2NotationFactory(TDefinitions defns) {
		m_definitions = defns;
	}
	
	public Object createDiagram(BPMN2ModelFactory factory,
			Object diagramModel, int x, int y, int width, int height) {
		BPMNDiagram ret=new BPMNDiagram();
		
		m_definitions.getBPMNDiagram().add(ret);
		
		m_plane = new BPMNPlane();
		ret.setBPMNPlane(m_plane);
		
		// TODO: Need to set reference to the Collaboration
		
		ret.setBPMNPlane(m_plane);
		
		return(ret);
	}
	
	public Object createPool(BPMN2ModelFactory factory,
				Object poolModel, Object diagramNotation,
				int x, int y, int width, int height) {
		
	}
	
	public Object createTask(BPMN2ModelFactory factory,
			Object taskModel, Object parentNotation,
					int x, int y, int width, int height);
	
	public Object createJunction(BPMN2ModelFactory factory,
			Object junctionModel, Object parentNotation,
					int x, int y, int width, int height);
		
	public Object createMessageLink(BPMN2ModelFactory factory,
			Object linkModel, Object diagramNotation);
	
}
