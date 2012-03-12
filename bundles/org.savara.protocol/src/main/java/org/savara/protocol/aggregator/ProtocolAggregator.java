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
package org.savara.protocol.aggregator;

import org.savara.common.logging.FeedbackHandler;
import org.scribble.protocol.model.ProtocolModel;

/**
 * This interface implements the protocol aggregation mechanism,
 * responsible for deriving a global protocol model from a set
 * of local protocol models.
 *
 */
public interface ProtocolAggregator {

	/**
	 * This method aggregates a set of protocol local models into a
	 * single global model.
	 * 
	 * @param locals The localmodels
	 * @param handler The handler
	 * @return The global model
	 */
	public ProtocolModel aggregateGlobalModel(java.util.Collection<ProtocolModel> locals,
					FeedbackHandler handler);

	/**
	 * This method aggregates a set of protocol local models, representing
	 * particular paths through a process, into a single local model.
	 * 
	 * @param protocolName The name of the aggregated local model
	 * @param locals The localmodels
	 * @param handler The handler
	 * @return The aggregated local model
	 */
	public ProtocolModel aggregateLocalModel(String protocolName,
				java.util.Collection<ProtocolModel> locals, FeedbackHandler handler);

}
