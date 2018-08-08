package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class BRRequestManagerBiker {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "BikerRequestManager";
    private static final String REQUESTS_CHILD = "Requests";
    private static final String DELIVERIES_CHILD = "Deliveries";
    private static final String AVAILABLE_BIKERS_CHILD = "AvailableBikers";

    private static final int REQUEST_TIMEOUT = 10000;

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private AppCompatActivity mReferredActivity;
    private BRRequestModel mRequest = new BRRequestModel();
    private FirebaseAccess mFirebase = new FirebaseAccess();

    private RequestsListAdapter mRequestListAdapter;
    ArrayList<RequestListDataModel> mRequestList = new ArrayList<>();

    //endregion


    /*=======================================================================================
                                            INTERFACES
     =======================================================================================*/

    //region INTERFACES

    public interface MonitorRequests {
        void onRequestsUpdate();
        void onNoRequestsAtThisMoment();
        void onError();
    }

    public interface OnRequestSelected {
        void onSelected();
        void onUnavailable();
        void onSuccess();
    }

    //endregion


    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public BRRequestManagerBiker(Context context) {
        mReferredActivity = (AppCompatActivity) context;
        // mockBikerData();
        mockRequestData();
    }

    //endregion


      /*====================================================================================\
     /                                                                                       \
    (                                    BIKER REQUEST LOGIC                                  )
     \                                                                                       /
      \====================================================================================*/


               /*---------------------------------------------------------------\
                                    MONITOR AVAILABLE REQUESTS
               \---------------------------------------------------------------*/

    //region MONITOR AVAILABLE REQUESTS METHODS

    public void monitorAvailableRequests(final MonitorRequests callbacks) {

        Log.d(TAG, "monitorAvailableRequests: starting to monitor requests ...");

        mFirebase.setListenerToChild(BRRequestModel.class,

            new FirebaseAccess.ListenToChanges<ArrayList<BRRequestModel>>() {

                @Override
                public void onChange(ArrayList<BRRequestModel> data) {

                    mRequestList.clear();

                    if (data.size() == 0) {

                        Log.d(TAG, "monitorAvailableRequests: data successfully retrieved " +
                                "from requests node but no request is available at this moment.");
                        callbacks.onNoRequestsAtThisMoment();
                        return;
                    }

                    Log.d(TAG, "monitorAvailableRequests: data successfully retrieved from " +
                            "requests node. Found " + data.size() + " requests.");

                    Log.d(TAG, "monitorAvailableRequests: sorting data by shortest " +
                            "distances ...");

                    data = sortByShortestTotalDistance(data);

                    Log.d(TAG, "monitorAvailableRequests: preparing up to three closest " +
                            "requests to be displayed ...");

                    for (int i = 0; i<data.size(); i++) {

                        if (i>2) continue;

                        BRRequestModel model = data.get(i);

                        if (!model.bikerId.equals("")) return;

                        String typeAndSize = model.packageSize + " sized " +
                                model.packageType;

                        double pickupDistance =
                                Double.valueOf(model.estimatesPickupDistance
                                        .replace("km", "").trim());

                        double deliveryDistance =
                                Double.valueOf(model.estimatesDeliveryDistance
                                        .replace("km", "").trim());

                        double distanceDbl = pickupDistance + deliveryDistance;

                        String distance = distanceDbl + " km";

                        RequestListDataModel dataModel = new RequestListDataModel(
                                typeAndSize,
                                distance,
                                model.estimatesFee,
                                model.userId
                        );

                        Log.d(TAG, "monitorAvailableRequests: extracted RequestListDataModel"
                                + " - " + dataModel.toString());

                        mRequestList.add(dataModel);
                    }

                    mRequestListAdapter.notifyDataSetChanged();

                    if (mRequestList.size() == 0) {

                        Log.d(TAG, "monitorAvailableRequests: all available requests are " +
                                "currently signed by other bikers.");

                        callbacks.onNoRequestsAtThisMoment();
                        return;
                    }

                    callbacks.onRequestsUpdate();
                }

                @Override
                public void onError(ArrayList<BRRequestModel> data) {

                }

                @Override
                public boolean removeListenerCondition(DataSnapshot data) {

                    return false;
                }
            }, REQUESTS_CHILD);
    }

    //endregion


               /*---------------------------------------------------------------\
                          CONNECT DATA TO REQUESTS LIST AND GET SELECTION
               \---------------------------------------------------------------*/

    //region CONNECT DATA TO REQUESTS LIST AND GET SELECTION METHODS

    public void connectDataToRequestsListAndGetSelection(int viewId,
                                                         final OnRequestSelected callbacks) {

        ListView listView = mReferredActivity.findViewById(R.id.newRequestsList);

        mRequestListAdapter = new RequestsListAdapter(mReferredActivity, mRequestList,

            new RequestsListAdapter.OnItemSelected() {
                @Override
                public void onRequestChosen (final String requesterId) {

                    attemptToSignRequest(requesterId, callbacks);
                }
            });

        listView.setAdapter(mRequestListAdapter);
    }


    private void attemptToSignRequest (final String requesterId,
                                                  final OnRequestSelected callbacks) {

        Log.d(TAG, "signRequestAndMonitorAcceptance: this biker has accepted a request " +
                "from user with ID: " + requesterId + ".");

        Log.d(TAG, "signRequestAndMonitorAcceptance: Attempting to sign request " +
                "with this biker's ID (" + getUid() + ") ...");

        // ---> Attempt to sign request
        mFirebase.updateMutableDataOnCondition(String.class, getUid(),

            // --> Only sign if no biker signed it before.
            new FirebaseAccess.Condition<String>() {
                @Override
                public boolean ExecuteIf(String data) {

                    if (data.equals("")) return true;
                    return false;
                }
            },

            new FirebaseAccess.OnComplete<String>() {

                // ---> Biker successfully signed the request
                @Override
                public void onSuccess(String data) {

                    Log.d(TAG, "signRequestAndMonitorAcceptance: this biker successfully " +
                            "signed the request. Awaiting transfer to deliveries node " +
                            "(2 step confirmation).");

                    awaitRequestDeletionOnRequestsNode(requesterId, callbacks);
                }

                // ---> Biker attempted, but could not sign the request.
                @Override
                public void onFailure(String data) {

                    if (data == null) {
                        Log.d(TAG, "signRequestAndMonitorAcceptance: could not find " +
                                "request. Either it's been canceled or another biker " +
                                "has signed it before.");
                    }

                    else if (!data.equals(getUid())) {
                        Log.d(TAG, "signRequestAndMonitorAcceptance: could not sign " +
                                "request. Another biker has signed it before.");
                    }
                }
            },
            REQUESTS_CHILD, requesterId, "bikerId");
    }

    private void awaitRequestDeletionOnRequestsNode(final String requesterId,
                                                    final OnRequestSelected callbacks) {

        mFirebase.setListenerToObjectOrProperty(BRRequestModel.class,

            // ---> Awaiting request deletion on requests node
            new FirebaseAccess.ListenToChanges<BRRequestModel>() {
                @Override
                public void onChange(BRRequestModel data) {

                }

                // ---> Request deletion is returned on this error callback,
                // ---> because logic relies on data needs being null.
                @Override
                public void onError(BRRequestModel data) {

                    if (data != null) return;

                    Log.d(TAG, "signRequestAndMonitorAcceptance: First step of transfer " +
                            "confirmed (request has been deleted from requests node).");

                    mFirebase.removeLastValueEventListener();

                    // ---> Deletion on request node has been detected.
                    // ---> Await creation of request on deliveries node.
                    awaitCreationOfRequestOnDeliveriesNode(requesterId,
                            callbacks);
                }

                @Override
                public boolean removeListenerCondition(DataSnapshot data) {



                    return false;
                }
            }, REQUESTS_CHILD, requesterId);

    }

    private void awaitCreationOfRequestOnDeliveriesNode (final String requesterId,
                                                         final OnRequestSelected callbacks) {

        mFirebase.setListenerToObjectOrProperty (BRRequestModel.class,

            new FirebaseAccess.ListenToChanges<BRRequestModel>() {

                @Override
                public void onChange(BRRequestModel data) {

                    if (data == null) return;

                    if (data.bikerId.equals(getUid())) {

                        Log.d(TAG, "signRequestAndMonitorAcceptance: Second step of " +
                                "transfer confirmed (request has been copied to deliveries node).");

                        Log.d(TAG, "signRequestAndMonitorAcceptance: Full transference of " +
                                "request to deliveries node confirmed. Navigating to tracking " +
                                "activity ...");

                        // navigate to delivery tracking activity
                    }
                }

                @Override
                public void onError(BRRequestModel data) {

                }

                @Override
                public boolean removeListenerCondition (DataSnapshot data) {

                    return false;
                }
            }, DELIVERIES_CHILD, requesterId);
    }

    //endregion


    /*=======================================================================================
                                              OTHER
     =======================================================================================*/

    //region OTHER METHODS

    private ArrayList<BRRequestModel> sortByShortestTotalDistance (
            ArrayList<BRRequestModel> array ) {

        Collections.sort(array, new Comparator<BRRequestModel>() {
            @Override
            public int compare(BRRequestModel requestModel,
                               BRRequestModel t1) {

                double pickupDistance =
                        Double.valueOf(requestModel.estimatesPickupDistance
                                .replace("km", "").trim());

                double deliveryDistance =
                        Double.valueOf(requestModel.estimatesDeliveryDistance
                                .replace("km", "").trim());

                double totalDistanceFirst = pickupDistance + deliveryDistance;

                pickupDistance =
                        Double.valueOf(t1.estimatesPickupDistance
                                .replace("km", "").trim());

                deliveryDistance =
                        Double.valueOf(t1.estimatesDeliveryDistance
                                .replace("km", "").trim());

                double totalDistanceSecond = pickupDistance + deliveryDistance;


                if (totalDistanceFirst >
                        totalDistanceSecond) {
                    return 1;
                }

                else if (totalDistanceFirst ==
                        totalDistanceSecond) {
                    return 0;
                }

                else {
                    return -1;
                }
            }
        });

        return array;
    }

    private String getCurrentISODateTime () {
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName());

        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    private String getUid() {
        return "EvenAnotherUserId";
    }

    public void removeAllListeners() {
        mFirebase.removeAllValueEventListeners();
    }

    public void removeLastListener() {
        mFirebase.removeLastValueEventListener();
    }

    //endregion


    /*=======================================================================================
                                              MOCKERS
     =======================================================================================*/

    //region DATA MOCKERS

    public void mockBikerData () {

        ArrayList<BRAvailableBikerModel> array = new ArrayList<>();

        array.add(new BRAvailableBikerModel(
                "Biker Ipanema",
                "bikeripanemabikeripanemabikeripanema",
                -22.983546,
                -43.197581,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BRAvailableBikerModel(
                "Biker Copacabana",
                "bikercopacabanabikercopacabanabikercopacabana",
                -22.973599,
                -43.189674,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BRAvailableBikerModel(
                "Biker Flamengo",
                "bikerflamengobikerflamengobikerflamengo",
                -22.932376,
                -43.177529,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BRAvailableBikerModel(
                "Biker Catete",
                "bikercatetebikercatetebikercatete",
                -22.925806,
                -43.176970,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BRAvailableBikerModel(
                "Biker Centro",
                "bikercentrobikercentrobikercentro",
                -22.909491,
                -43.183332,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        for (BRAvailableBikerModel biker : array) {
            mFirebase.addOrUpdate(biker,
                    new FirebaseAccess.OnCompleteVoid() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {

                        }
                    }, AVAILABLE_BIKERS_CHILD, biker.bikerId);
        }
    }

    public void mockRequestData () {

        ArrayList<BRRequestModel> array = new ArrayList<>();

        array.add(new BRRequestModel(
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

        array.add(new BRRequestModel(
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

        array.add(new BRRequestModel(
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

        array.add(new BRRequestModel(
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

        for (BRRequestModel request : array) {
            mFirebase.addOrUpdate(request,
                    new FirebaseAccess.OnCompleteVoid() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {

                        }
                    }, REQUESTS_CHILD, request.userId);
        }
    }

    //endregion
}
