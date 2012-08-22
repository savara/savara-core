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

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.savara.examples.store.AccountNotFoundFault;
import org.switchyard.common.xml.QNameUtil;
import org.switchyard.exception.SwitchYardException;
import org.switchyard.transform.Transformer;

public class AccountNotFoundTransformer extends org.switchyard.transform.BaseTransformer<AccountNotFoundFault,String>
				implements Transformer<AccountNotFoundFault,String> {
    
	public QName getTo() {
		return (QName.valueOf("{http://www.jboss.org/examples/store}AccountNotFound"));
	}
	
	@Override
	public String transform(AccountNotFoundFault type) {
        Marshaller marshaller;
        JAXBContext _jaxbContext;

        try {
            _jaxbContext = JAXBContext.newInstance("org.jboss.examples.store");
        } catch (JAXBException e) {
            throw new SwitchYardException("Failed to create JAXBContext for '" + getFrom() + "'.", e);
        }

        try {
            marshaller = _jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            throw new SwitchYardException("Failed to create Marshaller for type '" + getFrom() + "'.", e);
        }

        try {
            StringWriter resultWriter = new StringWriter();
            Object javaObject = type.getFaultInfo();
            JAXBElement jaxbElement = new JAXBElement(getTo(), QNameUtil.toJavaMessageType(getFrom()), javaObject);

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(jaxbElement, resultWriter);

            return (resultWriter.toString());
        } catch (JAXBException e) {
            throw new SwitchYardException("Failed to unmarshall for type '" + getFrom() + "'.", e);
        }

	}
}
