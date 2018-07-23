package com.infnet.bikeride.bikeride;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigurationFirebase {


    private static DatabaseReference referenceFirebase;
    private static FirebaseAuth autentication;

    public static DatabaseReference getFirebase (){

        if (referenceFirebase == null){
            referenceFirebase = FirebaseDatabase.getInstance().getReference();
        }

        return referenceFirebase;
    }


    public static FirebaseAuth getFirebaseAuth(){

        if (autentication == null){
            autentication = FirebaseAuth.getInstance();
        }

        return autentication;
    }

}
