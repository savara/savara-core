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
package org.savara.protocol.export.monitor;

import org.savara.protocol.model.Join;
import org.savara.protocol.model.Sync;
import org.savara.protocol.model.util.ForkJoinUtil;
import org.scribble.protocol.model.CustomActivity;
import org.scribble.protocol.monitor.model.Fork;
import org.scribble.protocol.monitor.model.LinkDeclaration;
import org.scribble.protocol.monitor.model.Node;

/**
 * This class implements a derived export monitor visitor to deal with the
 * Savara specific fork/join constructs.
 * 
 * Experimental, as these constructs will eventually move into Scribble.
 *
 */
public class ForkJoinMonitorExportVisitor extends org.scribble.protocol.export.monitor.MonitorExportVisitor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(CustomActivity elem) {

		startActivity(elem);
		
		Node node=null;
		
		if (elem instanceof Sync) {
			node = new Fork();
			((Fork)node).setLinkName(((Sync) elem).getLabel());
			
		} else if (elem instanceof Join) {
			node = new org.scribble.protocol.monitor.model.Join();
			
			String expr=null;
			
			for (String label : ((Join)elem).getLabels()) {
				if (expr == null) {
					expr = label;
				} else {
					if (((Join)elem).getXOR()) {
						expr += " or ";
					} else {
						expr += " and ";
					}
					expr += label;
				}
			}
			
			((org.scribble.protocol.monitor.model.Join)node).setExpression(expr);
		}
		
		getNodes().add(node);
		
		getPendingNextIndex().add(node);
		
		endActivity(elem);
	}

	@Override
    public boolean start(org.scribble.protocol.model.Parallel elem) {
		
		java.util.List<String> linkNames=ForkJoinUtil.getLinkNames(elem);
		
		for (String linkName : linkNames) {
			// Need to see effects doing startActivity twice
			startActivity(elem);
			
			// Check if this parallel has any link declarations
			LinkDeclaration node=new LinkDeclaration();
			node.setName(linkName);
			
			getNodes().add(node);
			
			getPendingNextIndex().add(node);
			
			endActivity(elem);
		}

		return (super.start(elem));
    }
}