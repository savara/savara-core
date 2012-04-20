/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpmn2.internal.generation.components;

import org.savara.bpmn2.model.TServiceTask;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Interaction;

/**
 * This class represents the BPMN2 activity node for a Service Task activity.
 * 
 */
public class ServiceTaskActivity extends SimpleActivity {
	
	private Interaction _request=null;

	/**
	 * This constructor initializes the send state.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ServiceTaskActivity(Interaction act,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation) {
		super(act, parent, model, notation);
		
		_request = act;
	}
	
	protected Object createNode(Activity act) {
		return(getModelFactory().createServiceTask(getContainer(), act));
	}
	
	/**
	 * This method returns the request.
	 * 
	 * @return The request
	 */
	public Interaction getRequest() {
		return(_request);
	}
	
	/**
	 * This method returns the service task.
	 * 
	 * @return The service task
	 */
	public TServiceTask getServiceTask() {
		return((TServiceTask)getNode());
	}
}
