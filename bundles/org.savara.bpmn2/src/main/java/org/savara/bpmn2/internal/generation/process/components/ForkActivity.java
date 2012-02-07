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
package org.savara.bpmn2.internal.generation.process.components;

import org.savara.protocol.model.Fork;
import org.scribble.protocol.model.Activity;

/**
 * This class represents the BPMN2 activity node for a Fork activity.
 * 
 */
public class ForkActivity extends SimpleActivity {
	
	private Fork _sync=null;

	/**
	 * This constructor initializes the sync.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ForkActivity(Fork act,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
		super(act, parent, model, notation);
		
		_sync = act;
	}
	
	protected Object createNode(Activity act) {
		return(getModelFactory().createForkTask(getContainer(), act));
	}
	
	/**
	 * This method returns the behavioral sync activity.
	 * 
	 * @return The sync activity
	 */
	public Fork getFork() {
		return(_sync);
	}
	
	public int getWidth() {
		return(30);
	}
	
	public int getHeight() {
		return(30);
	}
	
}
