package com.infnet.bikeride.bikeride.services;

import android.util.Log;

public abstract class CurrentUserData {

    private static final String TAG = "CurrentUserData";

    private static String id = "";
    private static String firstName = "";
    private static String lastName = "";
    private static String email = "";
    private static String phoneNumber = "";
    private static Boolean isBiker = false;
    private static String urlPhoto = "";


    public static String getId() {
        return id;
    }

    public static void setId(String id) {

        CurrentUserData.id = id;
    }

    public static String getFirstName() { return firstName; }

    public static void setFirstName(String firstName) {

        if (firstName.equals(null) || firstName.equals("")) {
            ifNullorEmptyMessage("First Name");
        }

        else {

            Log.d(TAG, "setFirstName: setting first name to - " + firstName);
            CurrentUserData.firstName = firstName;
        }
    }

    public static String getLastName() {
        return lastName;
    }

    public static void setLastName(String lastName) {

        if (lastName.equals(null) || lastName.equals("")) {
            ifNullorEmptyMessage("Last Name");
        }

        else {

            Log.d(TAG, "setLastName: setting last name to - " + lastName);
            CurrentUserData.lastName = lastName;
        }
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {

        if (email.equals(null) || email.equals("")) {
            ifNullorEmptyMessage("Email");
        }

        else {

            Log.d(TAG, "setLastName: setting email to - " + email);
            CurrentUserData.email = email;
        }
    }

    public static String getPhoneNumber() {
        return phoneNumber;
    }

    public static void setPhoneNumber(String phoneNumber) {

        if (phoneNumber.equals(null) || phoneNumber.equals("")) {
            ifNullorEmptyMessage("Phone Number");
        }

        else {

            Log.d(TAG, "setLastName: setting phone number to - " + phoneNumber);
            CurrentUserData.phoneNumber = phoneNumber;
        }
    }

    public static Boolean getIsBiker() {
        return isBiker;
    }

    public static void setIsBiker(Boolean isBiker) {

        if (isBiker) {
            Log.d(TAG, "setLastName: user IS biker.");
        }

        else {

            Log.d(TAG, "setLastName: user IS NOT biker.");
        }

        CurrentUserData.isBiker = isBiker;
    }

    public static String getUrlPhoto() {
        return urlPhoto;
    }

    public static void setUrlPhoto(String urlPhoto) {

        if (urlPhoto.equals(null) || urlPhoto.equals("")) {
            ifNullorEmptyMessage("URL Photo");
        }

        else {

            Log.d(TAG, "setLastName: setting URL photo to - " + urlPhoto);
            CurrentUserData.urlPhoto = urlPhoto;
        }
    }

    private static void ifNullorEmptyMessage (String prop) {

        Log.d(TAG, "ifNullorEmptyMessage: property " + prop + " is unavailable.");
    }
}
