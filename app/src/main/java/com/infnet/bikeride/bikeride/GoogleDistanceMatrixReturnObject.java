package com.infnet.bikeride.bikeride;


import com.google.firebase.database.IgnoreExtraProperties;

// Read and Write Data on Android (Objects specs)
// https://firebase.google.com/docs/database/android/read-and-write

public class GoogleDistanceMatrixReturnObject {

    public String[] destination_addresses;
    public String[] origin_addresses;
    public String status;

    public class GDMelement {

    }

    public String bikerName = "";
    public String bikerId = "";
    public Double bikerPositionLatitude = 0d;
    public Double bikerPositionLongitude = 0d;
    public String createTime = "";
    public String lastUpdatedOn = "";

    public GoogleDistanceMatrixReturnObject() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public GoogleDistanceMatrixReturnObject(String bikerName,
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
}
