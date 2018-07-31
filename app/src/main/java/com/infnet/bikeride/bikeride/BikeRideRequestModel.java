package com.infnet.bikeride.bikeride;

import com.google.firebase.database.IgnoreExtraProperties;

// Read and Write Data on Android (Objects specs)
// https://firebase.google.com/docs/database/android/read-and-write

@IgnoreExtraProperties
public class BikeRideRequestModel {

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

    public String packageType = "";
    public String packageSize = "";

    public String sendersName = "";
    public String pickupAddress = "";
    public double pickupAddressLatitude = 0;
    public double pickupAddressLongitude = 0;

    public String receiversName = "";
    public String deliveryAddress = "";
    public double deliveryddressLatitude = 0;
    public double deliveryAddressLongitude = 0;

    public boolean confirmedPickupByUser = false;
    public boolean confirmedPickupByBiker = false;
    public boolean confirmedDelivery = false;

    public String createTime = "";
    public String updateTime = "";

    public BikeRideRequestModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public BikeRideRequestModel(String userName,
                                String userId,
                                String bikerName,
                                String bikerId,
                                double bikerPositionLatitude,
                                double bikerPositionLongitude,
                                String pickupAddress,
                                double pickupAddressLatitude,
                                double pickupAddressLong,
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
        this.pickupAddress = pickupAddress;
        this.pickupAddressLatitude = pickupAddressLatitude;
        this.pickupAddressLongitude = pickupAddressLong;
        this.deliveryAddress = deliveryAddress;
        this.deliveryddressLatitude = deliveryddressLatitude;
        this.deliveryAddressLongitude = deliveryAddressLong;
        this.createTime = createTime;
    }

    @Override
    public String toString() {
//        return super.toString();
        return "\n\n" +
                "REQUEST OBJECT CONTENT" + "\n" +
                "^^^^^^^^^^^^^^^^^^^^^^" + "\n" +
                "userName                   = " + userName + "\n" +
                "userId                     = " + userId + "\n" +
                "estimatesPickupDistance    = " + estimatesPickupDistance + "\n" +
                "estimatesPickupDuration    = " + estimatesPickupDuration + "\n" +
                "estimatesDeliveryDistance  = " + estimatesDeliveryDistance + "\n" +
                "estimatesDeliveryDuration  = " + estimatesDeliveryDuration + "\n" +
                "bikerName                  = " + bikerName + "\n" +
                "bikerId                    = " + bikerId + "\n" +
                "bikerPositionLatitude      = " + bikerPositionLatitude + "\n" +
                "bikerPositionLongitude     = " + bikerPositionLongitude + "\n" +
                "pickupAddress              = " + pickupAddress + "\n" +
                "pickupAddressLatitude      = " + pickupAddressLatitude + "\n" +
                "pickupAddressLongitude     = " + pickupAddressLongitude + "\n" +
                "deliveryAddress            = " + deliveryAddress + "\n" +
                "deliveryddressLatitude     = " + deliveryddressLatitude + "\n" +
                "deliveryAddressLongitude   = " + deliveryAddressLongitude + "\n" +
                "createTime                 = " + createTime + "\n" +
                "updateTime                 = " + updateTime + "\n\n";
    }
}