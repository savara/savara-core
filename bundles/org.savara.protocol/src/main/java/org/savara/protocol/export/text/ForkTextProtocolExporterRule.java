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
package org.savara.protocol.export.text;

import org.savara.protocol.model.Fork;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.export.text.TextProtocolExporterRule;
import org.scribble.protocol.model.ModelObject;

public class ForkTextProtocolExporterRule implements TextProtocolExporterRule {

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupported(ModelObject obj) {
		return (obj instanceof Fork);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText(ModelObject obj, Journal journal) {
		String ret=null;
		Fork sync=(Fork)obj;
		
		ret = "sync "+sync.getLabel();
		
		if (sync.getRoles().size() > 0) {
			ret += " at ";
			
			for (int i=0; i < sync.getRoles().size(); i++) {
				if (i > 0) {
					ret += ",";
				}
				ret += sync.getRoles().get(i).getName();
			}
		}
		
		return (ret);
	}

}
