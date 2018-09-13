package com.infnet.bikeride.bikeride.services;

import android.util.Log;

import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.AvailableBikerModel;
import com.infnet.bikeride.bikeride.models.RequestModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public abstract class Mockers extends FirebaseAccess {

    private static final String TAG = "Mockers";

    private static FirebaseAccess mFirebase = new FirebaseAccess();

    public static void mockBikerData () {

        Log.d(TAG, "mockBikerData: mocking biker data ...");

        ArrayList<AvailableBikerModel> array = new ArrayList<>();

        array.add(new AvailableBikerModel(
                "Biker Ipanema",
                "bikeripanemabikeripanemabikeripanema",
                -22.983546,
                -43.197581,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new AvailableBikerModel(
                "Biker Copacabana",
                "bikercopacabanabikercopacabanabikercopacabana",
                -22.973599,
                -43.189674,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new AvailableBikerModel(
                "Biker Flamengo",
                "bikerflamengobikerflamengobikerflamengo",
                -22.932376,
                -43.177529,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new AvailableBikerModel(
                "Biker Catete",
                "bikercatetebikercatetebikercatete",
                -22.925806,
                -43.176970,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new AvailableBikerModel(
                "Biker Centro",
                "bikercentrobikercentrobikercentro",
                -22.909491,
                -43.183332,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        for (AvailableBikerModel biker : array) {
            mFirebase. addOrUpdate(biker,
                    new FirebaseAccess.OnCompleteVoid() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {

                        }
                    }, Constants.ChildName.AVAILABLE_BIKERS, biker.bikerId);
        }
    }

    public static void mockRequestData () {

        Log.d(TAG, "mockRequestData: mocking request data ...");

        ArrayList<RequestModel> array = new ArrayList<>();

        array.add(new RequestModel(
                "Request Barra",
                "requestBarrarequestBarrarequestBarra",
                "",
                "",
                0d,
                0d,
                "37.2 km",
                "2 hours 5 mins",
                "2.5 km",
                "9 mins",
                "R$40,95",
                "mail",
                "Small",
                "Sender name 1",
                "Av das Américas, 3900 - Barra da Tijuca",
                -22.999705,
                -43.351809,
                "Receiver name 1",
                "Av. Ayrton Senna, 3000 - Barra da Tijuca",
                -22.983517,
                -43.365343,
                getCurrentISODateTime()
        )) ;

        array.add(new RequestModel(
                "Request Centro",
                "requestCentrorequestCentrorequestCentro",
                "",
                "",
                0d,
                0d,
                "4.7 km",
                "19 mins",
                "1.7 km",
                "6 mins",
                "R$7,25",
                "box",
                "Large",
                "Sender name 2",
                "Av. Rio Branco, 88 - Centro",
                -22.902803,
                -43.178441,
                "Receiver name 2",
                "Rua Frei Caneca, 57 - Centro",
                -22.909069,
                -43.189191,
                getCurrentISODateTime()
        ));

        array.add(new RequestModel(
                "Request Tijuca",
                "requestTijucarequestTijucarequestTijuca",
                "",
                "",
                0d,
                0d,
                "8.0 km",
                "31 mins",
                "4.9 km",
                "18 mins",
                "R$15,35",
                "unusual",
                "Medium",
                "Sender name 3",
                "Rua Conde de Bonfim, 460 - Tijuca",
                -22.926135,
                -43.235256,
                "Receiver name 3",
                "Rua Canavieiras, 700 - Grajau",
                -22.920759,
                -43.267421,
                getCurrentISODateTime()
        ));

        array.add(new RequestModel(
                "Request Copacabana",
                "requestCopacabanarequestCopacabanarequestCopacabana",
                "",
                "",
                0d,
                0d,
                "7.8 km",
                "34 mins",
                "8.8 km",
                "33 mins",
                "R$15,35",
                "mail",
                "Medium",
                "Sender name 4",
                "Rua Barata Ribeiro, 111 - Copacabana",
                -22.963776,
                -43.178535,
                "Receiver name 4",
                "Rua Jardim Botânico, 1003 - Jardim Botânico",
                -22.971757,
                -43.223972,
                getCurrentISODateTime()
        ));

        for (RequestModel request : array) {
            mFirebase.addOrUpdate(request,
                    new FirebaseAccess.OnCompleteVoid() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {

                        }
                    }, Constants.ChildName.REQUESTS, request.userId);
        }
    }

    private static String getCurrentISODateTime () {
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName());

        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }


}
