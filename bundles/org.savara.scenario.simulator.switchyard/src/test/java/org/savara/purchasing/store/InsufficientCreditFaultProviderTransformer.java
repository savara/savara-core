/*
 * Generated by Savara.
 */
package org.savara.purchasing.store;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.switchyard.common.xml.QNameUtil;
import org.switchyard.transform.Transformer;

public class InsufficientCreditFaultProviderTransformer extends org.switchyard.transform.BaseTransformer<org.savara.purchasing.store.InsufficientCreditFault,String>
				implements Transformer<org.savara.purchasing.store.InsufficientCreditFault,String> {
    
	private static final Logger LOG=Logger.getLogger(InsufficientCreditFaultProviderTransformer.class.getName());
	
	public QName getTo() {
		return (QName.valueOf("{http://www.jboss.org/examples/store}BuyFailed"));
	}
	
	public String transform(org.savara.purchasing.store.InsufficientCreditFault type) {
		Marshaller marshaller;
		JAXBContext _jaxbContext;

		try {
			_jaxbContext = JAXBContext.newInstance("org.jboss.examples.store");
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Failed to create JAXBContext for '" + getFrom() + "'.", e);
			return (null);
		}

		try {
			marshaller = _jaxbContext.createMarshaller();
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Failed to create Marshaller for type '" + getFrom() + "'.", e);
			return (null);
		}

		try {
			StringWriter resultWriter = new StringWriter();
			Object javaObject = type.getFaultInfo();
			JAXBElement jaxbElement = new JAXBElement(getTo(), QNameUtil.toJavaMessageType(getFrom()), javaObject);

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(jaxbElement, resultWriter);

			return (resultWriter.toString());
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Failed to unmarshall for type '" + getFrom() + "'.", e);
			return (null);
		}
	}
}
