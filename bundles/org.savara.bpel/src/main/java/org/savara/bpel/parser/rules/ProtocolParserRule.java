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

import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.Activity;

/**
 * This interface represents a generation rule from a domain specific component
 * to a Protocol component.
 *
 */
public interface ProtocolParserRule {

	/**
	 * This method determines whether the supplied domain component is supported
	 * by this conversion rule.
	 * 
	 * @param context The conversion context
	 * @param component The domain component
	 * @return Whether the component is supported for this rule
	 */
	public boolean isSupported(Object component);
	
	/**
	 * This method converts a domain specific component to a protocol component and
	 * add it to the supplied activity list.
	 * 
	 * @param context The conversion context
	 * @param component The domain component
	 * @param activities The list of protocol activities
	 */
	public void convert(ConversionContext context, Object component, java.util.List<Activity> activities,
								Journal journal);
	
}
