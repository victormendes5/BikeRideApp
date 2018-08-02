package com.infnet.bikeride.bikeride;

import com.google.firebase.auth.FirebaseAuth;

public class ConfigurationFirebase {


    private static FirebaseAuth autentication;

//    public static DatabaseReference getFirebase (){
//
//        if (referenceFirebase == null){
//            referenceFirebase = FirebaseDatabase.getInstance().getReference();
//        }
//
//        return referenceFirebase;
//    }


    public static FirebaseAuth getFirebaseAuth(){

        if (autentication == null){
            autentication = FirebaseAuth.getInstance();
        }

        return autentication;
    }

}
