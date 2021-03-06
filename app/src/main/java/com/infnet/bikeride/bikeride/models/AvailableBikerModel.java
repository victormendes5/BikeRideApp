package com.infnet.bikeride.bikeride.models;

import com.google.firebase.database.IgnoreExtraProperties;

// Read and Write Data on Android (Objects specs)
// https://firebase.google.com/docs/database/android/read-and-write

@IgnoreExtraProperties
public class AvailableBikerModel {

    public String bikerName = "";
    public String bikerId = "";
    public Double bikerPositionLatitude = 0d;
    public Double bikerPositionLongitude = 0d;
    public String createTime = "";
    public String lastUpdatedOn = "";

    public AvailableBikerModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public AvailableBikerModel(String bikerName,
                               String bikerId,
                               Double bikerPositionLatitude,
                               Double bikerPositionLongitude,
                               String createTime,
                               String lastUpdatedOn) {

        this.bikerName = bikerName;
        this.bikerId = bikerId;
        this.bikerPositionLatitude = bikerPositionLatitude;
        this.bikerPositionLongitude = bikerPositionLongitude;
        this.createTime = createTime;
        this.lastUpdatedOn = lastUpdatedOn;
    }

    @Override
    public String toString() {
        return "\n\n" +
                "BIKER AVAILABLE OBJECT CONTENT" + "\n" +
                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^" + "\n" +
                "bikerName                 = " + bikerName + "\n" +
                "bikerId                   = " + bikerId + "\n" +
                "bikerPositionLatitude     = " + bikerPositionLatitude + "\n" +
                "bikerPositionLongitude    = " + bikerPositionLongitude + "\n" +
                "createTime                = " + createTime + "\n" +
                "lastUpdatedOn             = " + lastUpdatedOn + "\n\n";
    }
}