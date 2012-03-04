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

import org.savara.bpmn2.model.TChoreographyTask;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Interaction;

/**
 * This class represents the BPMN2 activity node for a Choreography Task.
 * 
 */
public class ChoreographyTask extends SimpleActivity {
	
	private Interaction _interaction=null;

	/**
	 * This constructor initializes the send state.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public ChoreographyTask(Interaction act,
			BPMNActivity parent, org.savara.bpmn2.internal.generation.BPMN2ModelFactory model,
			org.savara.bpmn2.internal.generation.BPMN2NotationFactory notation) {
		super(act, parent, model, notation);
		
		_interaction = act;
	}
	
	protected Object createNode(Activity act) {
		return(getModelFactory().createChoreographyTask(getContainer(), act));
	}
	
	public void draw(Object parent) {
		getNotationFactory().createChoreographyTask(getModelFactory(),
				getNode(), parent, getX(), getY(), getWidth(), getHeight());
	}

	public int getHeight() {
		return(100);
	}
	
	/**
	 * This method returns the interaction.
	 * 
	 * @return The interaction
	 */
	public Interaction getInteraction() {
		return(_interaction);
	}
	
	/**
	 * This method returns the choreography task.
	 * 
	 * @return The choreography task
	 */
	public TChoreographyTask getChoreographyTask() {
		return((TChoreographyTask)getNode());
	}
}
