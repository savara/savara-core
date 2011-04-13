//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-833 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.12 at 11:32:56 PM BST 
//


package org.savara.activity.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InteractionActivity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InteractionActivity">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.savara.org/activity}Activity">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://www.savara.org/activity}MessageParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="destinationType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="destinationAddress" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="replyToAddress" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="operationName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="faultName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="exchangeType" type="{http://www.savara.org/activity}ExchangeType" default="undefined" />
 *       &lt;attribute name="outbound" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InteractionActivity", propOrder = {
    "parameter"
})
public class InteractionActivity
    extends Activity
{

    protected List<MessageParameter> parameter;
    @XmlAttribute
    protected String destinationType;
    @XmlAttribute
    protected String destinationAddress;
    @XmlAttribute
    protected String replyToAddress;
    @XmlAttribute
    protected String operationName;
    @XmlAttribute
    protected String faultName;
    @XmlAttribute
    protected ExchangeType exchangeType;
    @XmlAttribute
    protected Boolean outbound;

    /**
     * Gets the value of the parameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageParameter }
     * 
     * 
     */
    public List<MessageParameter> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<MessageParameter>();
        }
        return this.parameter;
    }

    /**
     * Gets the value of the destinationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationType() {
        return destinationType;
    }

    /**
     * Sets the value of the destinationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationType(String value) {
        this.destinationType = value;
    }

    /**
     * Gets the value of the destinationAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * Sets the value of the destinationAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationAddress(String value) {
        this.destinationAddress = value;
    }

    /**
     * Gets the value of the replyToAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyToAddress() {
        return replyToAddress;
    }

    /**
     * Sets the value of the replyToAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyToAddress(String value) {
        this.replyToAddress = value;
    }

    /**
     * Gets the value of the operationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Sets the value of the operationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperationName(String value) {
        this.operationName = value;
    }

    /**
     * Gets the value of the faultName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaultName() {
        return faultName;
    }

    /**
     * Sets the value of the faultName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaultName(String value) {
        this.faultName = value;
    }

    /**
     * Gets the value of the exchangeType property.
     * 
     * @return
     *     possible object is
     *     {@link ExchangeType }
     *     
     */
    public ExchangeType getExchangeType() {
        if (exchangeType == null) {
            return ExchangeType.UNDEFINED;
        } else {
            return exchangeType;
        }
    }

    /**
     * Sets the value of the exchangeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExchangeType }
     *     
     */
    public void setExchangeType(ExchangeType value) {
        this.exchangeType = value;
    }

    /**
     * Gets the value of the outbound property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutbound() {
        if (outbound == null) {
            return true;
        } else {
            return outbound;
        }
    }

    /**
     * Sets the value of the outbound property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutbound(Boolean value) {
        this.outbound = value;
    }

}
