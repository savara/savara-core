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

import org.savara.protocol.model.Join;
import org.savara.protocol.model.Sync;
import org.scribble.protocol.model.CustomActivity;
import org.scribble.protocol.model.DefaultVisitor;
import org.scribble.protocol.model.Parallel;
import org.scribble.protocol.model.Protocol;

/**
 * This class provides utility functions related to the join/fork constructs.
 * 
 * NOTE: This class is in experimental status. Once join/fork concept has become
 * stable it will be included in the scribble model.
 *
 */
public class ForkJoinUtil {

	/**
	 * This method returns the unique link names associated with the parallel
	 * construct.
	 * 
	 * @param parallel The parallel construct
	 * @return The list of unique link names
	 */
	public static java.util.List<String> getLinkNames(final Parallel parallel) {
		final java.util.List<String> ret=new java.util.Vector<String>();
		
		parallel.visit(new DefaultVisitor() {
			
			@Override
			public void end(Parallel par) {
				if (par != parallel) {
					// Exclude link names just associated with contained
					// parallel construct
					java.util.List<String> links=getLinkNames(par);
					
					ret.removeAll(links);
				}
			}
			
			public void accept(CustomActivity act) {
				if (act instanceof Sync) {
					String linkName = ((Sync)act).getLabel();
					if (!ret.contains(linkName)) {
						ret.add(linkName);
					}
				} else if (act instanceof Join) {
					for (String linkName : ((Join)act).getLabels()) {
						if (!ret.contains(linkName)) {
							ret.add(linkName);
						}
					}
				}
			}
		});
		
		// Exclude link names that are used outside the scope of the
		// supplied parallel
		Protocol protocol=parallel.getEnclosingProtocol();
		
		if (protocol != null) {
			protocol.visit(new DefaultVisitor() {
				
				@Override
				public boolean start(Parallel par) {
					// Visit contained paths if not the same parallel
					// construct that was supplied
					return(par != parallel);
				}
				
				@Override
				public void accept(CustomActivity act) {
					if (act instanceof Sync) {
						String linkName = ((Sync)act).getLabel();
						ret.remove(linkName);
					} else if (act instanceof Join) {
						for (String linkName : ((Join)act).getLabels()) {
							ret.remove(linkName);
						}
					}
				}
			});
		}
		
		return (ret);
	}
}
