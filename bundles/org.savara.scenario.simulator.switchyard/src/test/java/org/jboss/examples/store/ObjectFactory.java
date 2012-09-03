
package org.jboss.examples.store;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.examples.store package. 
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

    private final static QName _BuyConfirmed_QNAME = new QName("http://www.jboss.org/examples/store", "BuyConfirmed");
    private final static QName _BuyRequest_QNAME = new QName("http://www.jboss.org/examples/store", "BuyRequest");
    private final static QName _AccountNotFound_QNAME = new QName("http://www.jboss.org/examples/store", "AccountNotFound");
    private final static QName _BuyFailed_QNAME = new QName("http://www.jboss.org/examples/store", "BuyFailed");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.examples.store
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BuyConfirmedType }
     * 
     */
    public BuyConfirmedType createBuyConfirmedType() {
        return new BuyConfirmedType();
    }

    /**
     * Create an instance of {@link BuyFailedType }
     * 
     */
    public BuyFailedType createBuyFailedType() {
        return new BuyFailedType();
    }

    /**
     * Create an instance of {@link BuyRequestType }
     * 
     */
    public BuyRequestType createBuyRequestType() {
        return new BuyRequestType();
    }

    /**
     * Create an instance of {@link AccountNotFoundType }
     * 
     */
    public AccountNotFoundType createAccountNotFoundType() {
        return new AccountNotFoundType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/store", name = "BuyConfirmed")
    public JAXBElement<BuyConfirmedType> createBuyConfirmed(BuyConfirmedType value) {
        return new JAXBElement<BuyConfirmedType>(_BuyConfirmed_QNAME, BuyConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/store", name = "BuyRequest")
    public JAXBElement<BuyRequestType> createBuyRequest(BuyRequestType value) {
        return new JAXBElement<BuyRequestType>(_BuyRequest_QNAME, BuyRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccountNotFoundType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/store", name = "AccountNotFound")
    public JAXBElement<AccountNotFoundType> createAccountNotFound(AccountNotFoundType value) {
        return new JAXBElement<AccountNotFoundType>(_AccountNotFound_QNAME, AccountNotFoundType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jboss.org/examples/store", name = "BuyFailed")
    public JAXBElement<BuyFailedType> createBuyFailed(BuyFailedType value) {
        return new JAXBElement<BuyFailedType>(_BuyFailed_QNAME, BuyFailedType.class, null, value);
    }

}
