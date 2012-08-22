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
package org.savara.scenario.simulator.switchyard;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.jboss.examples.creditagency.CustomerUnknownType;
import org.savara.examples.creditagency.CustomerUnknownFault;

public class CustomUnknownTransformer extends org.switchyard.transform.BaseTransformer<DOMSource,org.savara.examples.creditagency.CustomerUnknownFault> {

	public QName getFrom() {
		return (QName.valueOf("{http://www.jboss.org/examples/creditAgency}CustomerUnknown"));
	}
	//org.jboss.examples.creditagency.CustomerUnknownType
	
	@Override
	public CustomerUnknownFault transform(DOMSource type) {
		return new CustomerUnknownFault("Customer Unknown", new CustomerUnknownType());
	}
}
