package org.savara.activity.store.rdbms.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author: Jeff Yu
 * @date: 05/09/11
 */
@Entity
@DiscriminatorValue("INTERACTION")
public class InteractionActivityEntity extends  ActivityEntity{

     @Column(name="DESTINATION_TYPE")
     private String destinationType;

     @Column(name="DESTINATION_ADDRESS")
     private String destinationAddress;

     @Column(name="REPLY_TO_ADDRESS")
     private String replyToAddress;

     @Column(name="OPERATION_NAME")
     private String operationName;

     @Column(name="FAULT_NAME")
     private String faultName;

     @Column(name="OUTBOUND")
     private Boolean outbound;

     @Column(name="EXCHANGE_TYPE")
     private String exchangeType;

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getReplyToAddress() {
        return replyToAddress;
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getFaultName() {
        return faultName;
    }

    public void setFaultName(String faultName) {
        this.faultName = faultName;
    }

    public Boolean getOutbound() {
        return outbound;
    }

    public void setOutbound(Boolean outbound) {
        this.outbound = outbound;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }
}
