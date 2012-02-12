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
package org.savara.protocol.model.util;

import java.util.logging.Logger;

import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.Role;

public class RepeatUtil {
	
	private static final Logger LOG=Logger.getLogger(RepeatUtil.class.getName());

	public static Role getDecisionMaker(Repeat repeat) {
		Role ret=null;
		
		Block path=repeat.getBlock();
		
		java.util.List<ModelObject> interactions=
					org.scribble.protocol.util.InteractionUtil.getInitialInteractions(path);
		boolean found=false;
		
		for (ModelObject mobj : interactions) {
			if (mobj instanceof Interaction) {
				Interaction interaction=(Interaction)mobj;
				
				if (ret == null) {
					ret = interaction.getFromRole();
				} else if (!ret.equals(interaction.getFromRole())) {
					LOG.severe("Mismatch between decision making interactions '"+
								ret+"' and '"+interaction.getFromRole()+"'");
				}
				
				found = true;
			}
		}
		
		if (!found) {
			LOG.severe("No interaction found in path: "+path);				
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the localised choice is associated with the
	 * decision maker.
	 * 
	 * @param choice The choice
	 * @return Whether the choice is associated with the decision maker
	 */
	public static boolean isDecisionMaker(Repeat repeat) {
		boolean ret=false;
		
		if (repeat.getRoles().size() > 0 && repeat.getEnclosingProtocol() != null &&
				repeat.getRoles().contains(repeat.getEnclosingProtocol().getLocatedRole())) {
			ret = true;
		}
		
		return(ret);
	}
}
