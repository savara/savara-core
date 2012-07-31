/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.parser.rules;

import java.util.List;

import org.savara.bpel.model.TActivity;
import org.savara.bpel.model.TElseif;
import org.savara.bpel.model.TIf;
import org.savara.bpel.util.ActivityUtil;
import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.model.*;

/**
 * This class represents an 'if' grouping construct.
 *  
 */
public class IfParserRule implements ProtocolParserRule {

	public boolean isSupported(Object component) {
		return(component instanceof TIf);
	}
		
	public void parse(ParserContext context, Object component, List<Activity> activities,
								FeedbackHandler handler) {
		TIf bpelElem=(TIf)component;
		
		//getSource().setComponentURI(getURI());
		
		org.scribble.protocol.model.Choice elem=
					new org.scribble.protocol.model.Choice();
		elem.setRole(new Role(context.getRole()));
		
		Block cb=new Block();
		
		// TODO: Convert the conditional expression
		
		TActivity act=ActivityUtil.getActivity(bpelElem);
		
		if (act != null) {
			context.parse(act, cb.getContents(), handler);
		}
		
		elem.getPaths().add(cb);
		
		// Convert 'else if' paths
		for (int i=0; i < bpelElem.getElseif().size(); i++) {
			TElseif elseIfElem=bpelElem.getElseif().get(i);
			
			cb = new Block();
			
			context.parse(elseIfElem, cb.getContents(), handler);
			
			elem.getPaths().add(cb);
		}
		
		// Convert 'else' path
		if (bpelElem.getElse() != null) {
			cb = new Block();
			
			context.parse(bpelElem.getElse(), cb.getContents(), handler);
			
			elem.getPaths().add(cb);
		}
		
		activities.add(elem);
	}
}
