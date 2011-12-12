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
package org.savara.protocol.projection;

import org.savara.protocol.model.Sync;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.projection.impl.ProjectorRule;
import org.scribble.protocol.projection.impl.ProtocolProjectorContext;

/**
 * This class provides the Unordered implementation of the
 * projector rule.
 */
public class SyncProjectorRule implements ProjectorRule {

    /**
     * This method determines whether the projection rule is
     * appropriate for the supplied model object.
     * 
     * @param obj The model object to be projected
     * @return Whether the rule is relevant for the
     *                 model object
     */
    public boolean isSupported(ModelObject obj) {
        return (obj.getClass() == Sync.class);
    }
    
    /**
     * This method projects the supplied model object based on the
     * specified role.
     * 
     * @param context The context
     * @param model The model object
     * @param role The role
     * @param l The model listener
     * @return The projected model object
     */
    public Object project(ProtocolProjectorContext context, ModelObject model,
                    Role role, Journal l) {
    	Sync ret=null;
    	Sync source=(Sync)model;

        if (source.getRoles().contains(role)) {
        	ret = new Sync();

        	ret.derivedFrom(source);
            
        	ret.getRoles().add(new Role(role));
        	
        	ret.setLabel(source.getLabel());
        }
        
        return (ret);
    }
}
