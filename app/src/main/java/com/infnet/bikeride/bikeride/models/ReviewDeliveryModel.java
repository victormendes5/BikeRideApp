package com.infnet.bikeride.bikeride.models;

public class ReviewDeliveryModel {

    private float nota = 0.00f;
    private String comment = "";

    public float getNota() {
        return nota;
    }

    public void setNota(float nota) {
        this.nota = nota;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
