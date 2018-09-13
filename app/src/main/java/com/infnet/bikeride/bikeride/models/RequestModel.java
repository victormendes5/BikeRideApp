package com.infnet.bikeride.bikeride.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

// Read and Write Data on Android (Objects specs)
// https://firebase.google.com/docs/database/android/read-and-write

@IgnoreExtraProperties
public class RequestModel implements Serializable {

    public String userName = "";
    public String userId = "";

    public String bikerName = "";
    public String bikerId = "";
    public double bikerPositionLatitude = 0;
    public double bikerPositionLongitude = 0;

    public String estimatesPickupDistance = "";
    public String estimatesPickupDuration = "";
    public String estimatesDeliveryDistance = "";
    public String estimatesDeliveryDuration = "";
    public String estimatesFee = "";

    public String packageType = "";
    public String packageSize = "";

    public String sendersName = "";
    public String pickupAddress = "";
    public double pickupAddressLatitude = 0;
    public double pickupAddressLongitude = 0;

    public String receiversName = "";
    public String deliveryAddress = "";
    public double deliveryAddressLatitude = 0;
    public double deliveryAddressLongitude = 0;

    public boolean confirmedPickupByUser = false;
    public boolean confirmedPickupByBiker = false;
    public boolean confirmedDeliveryByBiker = false;
    public boolean confirmedDeliveryByUser = false;

    public String createTime = "";
    public String updateTime = "";

    public RequestModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public RequestModel(String userName,
                        String userId,
                        String bikerName,
                        String bikerId,
                        double bikerPositionLatitude,
                        double bikerPositionLongitude,
                        String estimatesPickupDistance,
                        String estimatesPickupDuration,
                        String estimatesDeliveryDistance,
                        String estimatesDeliveryDuration,
                        String estimatesFee,
                        String packageType,
                        String packageSize,
                        String sendersName,
                        String pickupAddress,
                        double pickupAddressLatitude,
                        double pickupAddressLong,
                        String receiversName,
                        String deliveryAddress,
                        double deliveryddressLatitude,
                        double deliveryAddressLong,
                        String createTime) {
        this.userName = userName;
        this.userId = userId;
        this.bikerName = bikerName;
        this.bikerId = bikerId;
        this.bikerPositionLatitude = bikerPositionLatitude;
        this.bikerPositionLongitude = bikerPositionLongitude;
        this.estimatesPickupDistance = estimatesPickupDistance;
        this.estimatesPickupDuration = estimatesPickupDuration;
        this.estimatesDeliveryDistance = estimatesDeliveryDistance;
        this.estimatesDeliveryDuration = estimatesDeliveryDuration;
        this.estimatesFee = estimatesFee;
        this.packageType = packageType;
        this.packageSize = packageSize;
        this.sendersName = sendersName;
        this.pickupAddress = pickupAddress;
        this.pickupAddressLatitude = pickupAddressLatitude;
        this.pickupAddressLongitude = pickupAddressLong;
        this.receiversName = receiversName;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAddressLatitude = deliveryddressLatitude;
        this.deliveryAddressLongitude = deliveryAddressLong;
        this.createTime = createTime;
    }

    public String getPickupAddressShrt (int charSize) {

        String address = pickupAddress;

        if (address.length() >= charSize+1) {
            address = address.substring(0, charSize) + " ...";
        }

        return  address;
    }

    public String getDeliveryAddressShrt (int charSize) {

        String address = deliveryAddress;

        if (address.length() >= charSize+1) {
            address = address.substring(0, charSize) + " ...";
        }

        return  address;
    }

    @Override
    public String toString() {

        return "\n\n" +
                "REQUEST OBJECT CONTENT" + "\n" +
                "^^^^^^^^^^^^^^^^^^^^^^" + "\n" +
                "userName                   = " + userName + "\n" +
                "userId                     = " + userId + "\n" +
                "estimatesPickupDistance    = " + estimatesPickupDistance + "\n" +
                "estimatesPickupDuration    = " + estimatesPickupDuration + "\n" +
                "estimatesDeliveryDistance  = " + estimatesDeliveryDistance + "\n" +
                "estimatesDeliveryDuration  = " + estimatesDeliveryDuration + "\n" +
                "estimatesFee               = " + estimatesFee + "\n" +
                "bikerName                  = " + bikerName + "\n" +
                "bikerId                    = " + bikerId + "\n" +
                "bikerPositionLatitude      = " + bikerPositionLatitude + "\n" +
                "bikerPositionLongitude     = " + bikerPositionLongitude + "\n" +
                "pickupAddress              = " + pickupAddress + "\n" +
                "pickupAddressLatitude      = " + pickupAddressLatitude + "\n" +
                "pickupAddressLongitude     = " + pickupAddressLongitude + "\n" +
                "deliveryAddress            = " + deliveryAddress + "\n" +
                "deliveryAddressLatitude    = " + deliveryAddressLatitude + "\n" +
                "deliveryAddressLongitude   = " + deliveryAddressLongitude + "\n" +
                "confirmedPickupByUser      = " + confirmedPickupByUser + "\n" +
                "confirmedPickupByBiker     = " + confirmedPickupByBiker + "\n" +
                "confirmedDeliveryByBiker   = " + confirmedDeliveryByBiker + "\n" +
                "confirmedDeliveryByUser    = " + confirmedDeliveryByUser + "\n" +
                "createTime                 = " + createTime + "\n" +
                "updateTime                 = " + updateTime + "\n\n";
    }
}