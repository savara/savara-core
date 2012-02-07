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
import org.savara.protocol.model.Fork;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.CustomActivity;

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
	public static java.util.List<String> getLinkNames(org.scribble.protocol.model.Parallel elem) {
		java.util.List<String> ret=new java.util.Vector<String>();
		
		java.util.Set<String> working=new java.util.HashSet<String>();
		
		for (Block path : elem.getPaths()) {
			if (working.size() == 0) {
				path.visit(new ForkJoinLinkVisitor(working));
			} else {
				java.util.Set<String> current=new java.util.HashSet<String>();
				path.visit(new ForkJoinLinkVisitor(current));
				
				java.util.Iterator<String> iter=current.iterator();
				while (iter.hasNext()) {
					String linkName=iter.next();
					
					if (working.contains(linkName)) {
						if (!ret.contains(linkName)) {
							ret.add(linkName);
						}
						working.remove(linkName);
						iter.remove();
					}
				}
				
				// Transfer any unmatched link names to the working set
				working.addAll(current);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This class visits the parallel paths to identify fork/join link names.
	 *
	 */
	protected static class ForkJoinLinkVisitor extends org.scribble.protocol.model.DefaultVisitor {
		
		private java.util.Set<String> _links=null;
		
		public ForkJoinLinkVisitor(java.util.Set<String> links) {
			_links = links;
		}
		
		@Override
		public void accept(CustomActivity elem) {
			if (elem instanceof Fork) {
				_links.add(((Fork)elem).getLabel());
			} else if (elem instanceof Join) {
				_links.addAll(((Join)elem).getLabels());
			}
		}
	}
}
