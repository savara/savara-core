
package org.jboss.examples.logistics;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.examples.logistics package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DeliveryConfirmed_QNAME = new QName("http://www.jboss.org/examples/logistics", "DeliveryConfirmed");
    private final static QName _DeliveryRequest_QNAME = new QName("http://www.jboss.org/examples/logistics", "DeliveryRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.examples.logistics
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeliveryRequestType }
     * 
     */
    public DeliveryRequestType createDeliveryRequestType() {
        return new DeliveryRequestType();
    }

    /**
     * Create an instance of {@link DeliveryConfirmedType }
     * 
     */
    public DeliveryConfirmedType createDeliveryConfirmedType() {
        return new DeliveryConfirmedType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/logistics", name = "DeliveryConfirmed")
    public JAXBElement<DeliveryConfirmedType> createDeliveryConfirmed(DeliveryConfirmedType value) {
        return new JAXBElement<DeliveryConfirmedType>(_DeliveryConfirmed_QNAME, DeliveryConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/logistics", name = "DeliveryRequest")
    public JAXBElement<DeliveryRequestType> createDeliveryRequest(DeliveryRequestType value) {
        return new JAXBElement<DeliveryRequestType>(_DeliveryRequest_QNAME, DeliveryRequestType.class, null, value);
    }

}
