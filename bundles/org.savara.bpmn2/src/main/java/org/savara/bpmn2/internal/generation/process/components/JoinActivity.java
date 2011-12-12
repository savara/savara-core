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

import org.savara.protocol.model.Join;
import org.scribble.protocol.model.Activity;

/**
 * This class represents the BPMN2 activity node for a Join activity.
 * 
 */
public class JoinActivity extends SimpleActivity {
	
	private Join _join=null;

	/**
	 * This constructor initializes the sync.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public JoinActivity(Join act,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.process.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.process.BPMN2NotationFactory notation) {
		super(act, parent, model, notation);
		
		_join = act;
	}
	
	protected Object createNode(Activity act) {
		return(getModelFactory().createJoinTask(getContainer(), act));
	}
	
	/**
	 * This method returns the behavioral join activity.
	 * 
	 * @return The join activity
	 */
	public Join getJoin() {
		return(_join);
	}
	
	public int getWidth() {
		return(30);
	}
	
	public int getHeight() {
		return(30);
	}
	
}
