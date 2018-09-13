package com.infnet.bikeride.bikeride;

public class RequestListDataModel {

    String packageTypeAndSize;
    String distance;
    String fee;
    String requesterId;

    public RequestListDataModel(String packageTypeAndSize, String distance, String fee,
                                String requesterId) {
        this.packageTypeAndSize = packageTypeAndSize;
        this.distance = distance;
        this.fee = fee;
        this.requesterId = requesterId;
    }

    public String getPackageTypeAndSize() {
        return packageTypeAndSize;
    }

    public void setPackageTypeAndSize(String packageTypeAndSize) {
        this.packageTypeAndSize = packageTypeAndSize;
    }


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    @Override
    public String toString() {
        return "{packageTypeAndSize = " + packageTypeAndSize + ", distance = " + distance + "," +
                " fee = " + fee + ", requesterId = " + requesterId + "}";
    }

}
