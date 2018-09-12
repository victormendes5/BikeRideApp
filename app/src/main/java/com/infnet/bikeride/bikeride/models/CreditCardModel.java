package com.infnet.bikeride.bikeride.models;

import java.io.Serializable;

public class CreditCardModel implements Serializable {

    private String userId;
    private String name;
    private String numberCard;
    private String expiration;
    private String cvc;

    public CreditCardModel(){

    }

    public CreditCardModel(String name, String numberCard, String expiration, String cvc){
        this.name = name;
        this.numberCard = numberCard;
        this.expiration = expiration;
        this.cvc = cvc;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumberCard() {
        return numberCard;
    }

    public void setNumberCard(String numberCard) {
        this.numberCard = numberCard;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

}
