
package org.jboss.examples.creditagency;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.examples.creditagency package. 
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

    private final static QName _CreditCheckRequest_QNAME = new QName("http://www.jboss.org/examples/creditAgency", "CreditCheckRequest");
    private final static QName _CreditRating_QNAME = new QName("http://www.jboss.org/examples/creditAgency", "CreditRating");
    private final static QName _CustomerUnknown_QNAME = new QName("http://www.jboss.org/examples/creditAgency", "CustomerUnknown");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.examples.creditagency
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreditCheckType }
     * 
     */
    public CreditCheckType createCreditCheckType() {
        return new CreditCheckType();
    }

    /**
     * Create an instance of {@link CreditRatingType }
     * 
     */
    public CreditRatingType createCreditRatingType() {
        return new CreditRatingType();
    }

    /**
     * Create an instance of {@link CustomerUnknownType }
     * 
     */
    public CustomerUnknownType createCustomerUnknownType() {
        return new CustomerUnknownType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreditCheckType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/creditAgency", name = "CreditCheckRequest")
    public JAXBElement<CreditCheckType> createCreditCheckRequest(CreditCheckType value) {
        return new JAXBElement<CreditCheckType>(_CreditCheckRequest_QNAME, CreditCheckType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreditRatingType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/creditAgency", name = "CreditRating")
    public JAXBElement<CreditRatingType> createCreditRating(CreditRatingType value) {
        return new JAXBElement<CreditRatingType>(_CreditRating_QNAME, CreditRatingType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CustomerUnknownType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/creditAgency", name = "CustomerUnknown")
    public JAXBElement<CustomerUnknownType> createCustomerUnknown(CustomerUnknownType value) {
        return new JAXBElement<CustomerUnknownType>(_CustomerUnknown_QNAME, CustomerUnknownType.class, null, value);
    }

}
