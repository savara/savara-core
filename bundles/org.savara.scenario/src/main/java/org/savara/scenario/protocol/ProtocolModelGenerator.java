/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.scenario.protocol;

import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.model.ProtocolModel;

/**
 * This interface represents a protocol model generator.
 *
 */
public interface ProtocolModelGenerator {

	/**
	 * This method generates a set of local protocol models from a supplied
	 * scenario. A protocol model will be provided for each role involved
	 * in the scenario.
	 * 
	 * @param scenario The scenario
	 * @param handler The feedback handler
	 * @return The set of local protocol models
	 */
	public java.util.Set<ProtocolModel> generate(org.savara.scenario.model.Scenario scenario,
								FeedbackHandler handler);
	
}
