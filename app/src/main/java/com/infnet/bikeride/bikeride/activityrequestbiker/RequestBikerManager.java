package com.infnet.bikeride.bikeride.activityrequestbiker;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.RequestListDataModel;
import com.infnet.bikeride.bikeride.activityrequestbiker.adapters.RequestsListAdapter;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.AvailableBikerModel;
import com.infnet.bikeride.bikeride.models.RequestModel;
import com.infnet.bikeride.bikeride.services.BRLocations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class RequestBikerManager {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "RequestBikerManager";
    private static final String REQUESTS_CHILD = "Requests";
    private static final String DELIVERIES_CHILD = "Deliveries";
    private static final String AVAILABLE_BIKERS_CHILD = "AvailableBikers";
    private static final int REQUESTS_LIST_VIEW_ID = R.id.newRequestsList;

    private static final int REQUEST_TIMEOUT = 20000;

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private AppCompatActivity mReferredActivity;
    private RequestModel mRequest = new RequestModel();
    private FirebaseAccess mFirebase = new FirebaseAccess();

    private RequestsListAdapter mRequestListAdapter;
    ArrayList<RequestListDataModel> mRequestList = new ArrayList<>();

    private BRLocations mLocation;

    private String mThisSessionsCreationTime = getCurrentISODateTime();

    private boolean mIsRequestDeletedOnRequestsNode = false;
    private boolean mIsRequestCreatedOnDeliveriesNode = false;
    private boolean mIsRequestSelected = false;

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
        void onSuccess(String requesterId);
        void onProcedureTimeout();
    }

    //endregion


    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public RequestBikerManager(Context context) {
        mReferredActivity = (AppCompatActivity) context;

        mLocation = new BRLocations(mReferredActivity, new BRLocations.OnLocationChanged() {
            @Override
            public void OnChange() {
                broadcastBikerLocation();
            }
        });

        // mockBikerData();
        // mockRequestData();
    }

    //endregion


      /*====================================================================================\
     /                                                                                       \
    (                                    BIKER REQUEST LOGIC                                  )
     \                                                                                       /
      \====================================================================================*/

               /*---------------------------------------------------------------\
                                     BROADCAST BIKER LOCATION
               \---------------------------------------------------------------*/

    //region BROADCAST BIKER LOCATION

    private void broadcastBikerLocation () {

        if (mIsRequestSelected) {

            Log.d(TAG, "broadcastBikerLocation: this biker has selected a request. " +
                    "Ignoring this data update on available biker node.");

            return;
        }

        final AvailableBikerModel bikerData = new AvailableBikerModel(
                getBikerName(),
                getUid(),
                mLocation.getLatitude(),
                mLocation.getLongitude(),
                mThisSessionsCreationTime,
                getCurrentISODateTime()
        );

        mFirebase.addOrUpdate(bikerData,

            new FirebaseAccess.OnCompleteVoid() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "broadcastBikerLocation: successfully transmitted updated " +
                            "biker data to available bikers node (Name: " + bikerData.bikerName +
                            " / ID: " +  bikerData.bikerId +  " / Latitude: " +
                            bikerData.bikerPositionLatitude +  " / Longitude: " +
                            bikerData.bikerPositionLongitude + ")");
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "broadcastBikerLocation: could not update biker data on " +
                            "available bikers node. Some error occurred while trying to contact " +
                            "Firebase.");
                }

            }, AVAILABLE_BIKERS_CHILD, getUid());

    }

    //endregion


               /*---------------------------------------------------------------\
                                    MONITOR AVAILABLE REQUESTS
               \---------------------------------------------------------------*/

    //region MONITOR AVAILABLE REQUESTS METHODS

    public void monitorAvailableRequests(final MonitorRequests callbacks) {

        Log.d(TAG, "monitorAvailableRequests: starting to monitor requests ...");

        mFirebase.setListenerToChild(RequestModel.class,

            new FirebaseAccess.ListenToChanges<ArrayList<RequestModel>>() {

                @Override
                public void onChange(ArrayList<RequestModel> data) {

                    mRequestList.clear();

                    if (data.size() == 0) {

                        Log.d(TAG, "monitorAvailableRequests: data successfully retrieved " +
                                "from requests node but no requests are available at this moment.");
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

                        RequestModel model = data.get(i);

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
                public void onError(ArrayList<RequestModel> data) {

                }

                @Override
                public boolean removeListenerCondition(DataSnapshot data) {

                    return false;
                }
            }, REQUESTS_CHILD);
    }

    //endregion


               /*---------------------------------------------------------------\
                         CONNECT DATA TO REQUESTS LIST AND MANAGE SELECTION
               \---------------------------------------------------------------*/

    //region CONNECT DATA TO REQUESTS LIST AND GET SELECTION METHODS

    public void connectDataToRequestsListAndManageSelection(int viewId,
                                                            final OnRequestSelected callbacks) {

        ListView listView = mReferredActivity.findViewById(REQUESTS_LIST_VIEW_ID);

        mRequestListAdapter = new RequestsListAdapter(mReferredActivity, mRequestList,

                new RequestsListAdapter.OnItemSelected() {

                    @Override
                    public void onRequestChosen (final String requesterId) {

                        callbacks.onSelected();
                        attemptToSignRequest(requesterId, callbacks);
                    }
                });

        listView.setAdapter(mRequestListAdapter);
    }

    private void attemptToSignRequest (final String requesterId,
                                       final OnRequestSelected callbacks) {

        Log.d(TAG, "connectDataToRequestsListAndManageSelection: this biker has accepted a " +
                "request from user with ID: " + requesterId + ".");

        Log.d(TAG, "connectDataToRequestsListAndManageSelection: Attempting to sign " +
                "request with this biker's ID (" + getUid() + ") ...");

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

                    Log.d(TAG, "connectDataToRequestsListAndManageSelection: this biker " +
                            "successfully signed the request. Awaiting transfer to deliveries " +
                            "node (2 step confirmation).");

                    awaitRequestDeletionOnRequestsNode(requesterId, callbacks);
                }

                // ---> Biker attempted, but could not sign the request.
                @Override
                public void onFailure(String data) {

                    if (data == null) {
                        Log.d(TAG, "connectDataToRequestsListAndManageSelection: could not " +
                                "find request. Either it's been canceled or another biker " +
                                "has signed it before.");

                        callbacks.onUnavailable();
                    }

                    else if (!data.equals("") && !data.equals(getUid())) {
                        Log.d(TAG, "connectDataToRequestsListAndManageSelection: could not " +
                                "sign request. Another biker has signed it before.");

                        callbacks.onUnavailable();
                    }
                }
            },
            REQUESTS_CHILD, requesterId, "bikerId");
    }

    private void awaitRequestDeletionOnRequestsNode(final String requesterId,
                                                    final OnRequestSelected callbacks) {

        mIsRequestDeletedOnRequestsNode = false;

        deletionOfRequestOnRequestsNodeTimeout(callbacks);

        mFirebase.setListenerToObjectOrProperty(RequestModel.class,

            // ---> Awaiting request deletion on requests node
            new FirebaseAccess.ListenToChanges<RequestModel>() {
                @Override
                public void onChange(RequestModel data) {

                }

                // ---> Request deletion is returned on this error callback,
                // ---> because logic relies on data needs being null.
                @Override
                public void onError(RequestModel data) {

                    if (data != null) return;

                    mIsRequestDeletedOnRequestsNode = true;

                    Log.d(TAG, "connectDataToRequestsListAndManageSelection: First step of " +
                            "transfer confirmed (request has been deleted from requests node).");

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

    private void deletionOfRequestOnRequestsNodeTimeout (final OnRequestSelected callbacks) {

        new android.os.Handler().postDelayed(

            new Runnable() {

                public void run() {

                    if (mIsRequestDeletedOnRequestsNode) return;

                    Log.d(TAG,
                            "connectDataToRequestsListAndManageSelection: deletion of " +
                                    "request object on requests node timed out before being " +
                                    "detected.");

                    mIsRequestSelected = false;

                    callbacks.onProcedureTimeout();

                    mFirebase.removeLastValueEventListener();

                    // update biker data on available bikers node ONCE

                }
            }, REQUEST_TIMEOUT);
    }

    private void awaitCreationOfRequestOnDeliveriesNode (final String requesterId,
                                                         final OnRequestSelected callbacks) {

        mIsRequestCreatedOnDeliveriesNode = false;

        creationOfRequestOnDeliveriesNodeTimeout(callbacks);

        mFirebase.setListenerToObjectOrProperty (RequestModel.class,

            new FirebaseAccess.ListenToChanges<RequestModel>() {

                @Override
                public void onChange(RequestModel data) {

                    if (data == null) return;

                    if (data.bikerId.equals(getUid())) {

                        mIsRequestCreatedOnDeliveriesNode = true;

                        Log.d(TAG, "connectDataToRequestsListAndManageSelection: Second " +
                                "step of transfer confirmed (request has been copied to " +
                                "deliveries node).");

                        mFirebase.removeLastValueEventListener();

                        Log.d(TAG, "connectDataToRequestsListAndManageSelection: Full " +
                                "transference of request to deliveries node confirmed.");

                        fulfillBikerDataOnRequest(requesterId, data, callbacks);
                    }

                    else {
                        callbacks.onUnavailable();
                    }
                }

                @Override
                public void onError(RequestModel data) {

                }

                @Override
                public boolean removeListenerCondition (DataSnapshot data) {

                    return false;
                }
            }, DELIVERIES_CHILD, requesterId);
    }

    private void creationOfRequestOnDeliveriesNodeTimeout (final OnRequestSelected callbacks) {

        new android.os.Handler().postDelayed(

            new Runnable() {

                public void run() {

                    if (mIsRequestCreatedOnDeliveriesNode) return;

                    Log.d(TAG,
                            "connectDataToRequestsListAndManageSelection: creation of " +
                                    "request object on deliveries node timed out before being " +
                                    "detected.");

                    mIsRequestSelected = false;

                    callbacks.onProcedureTimeout();

                    mFirebase.removeLastValueEventListener();

                    // update biker data on available bikers node ONCE

                }
            }, REQUEST_TIMEOUT);
    }

    private void fulfillBikerDataOnRequest(final String requesterId,
                                           RequestModel data,
                                           final OnRequestSelected callbacks) {

        data.bikerName = getBikerName();
        data.bikerPositionLatitude = mLocation.getLatitude();
        data.bikerPositionLongitude = mLocation.getLongitude();

        Log.d(TAG, "connectDataToRequestsListAndManageSelection: attempting to fulfill " +
                "this bikers data on confirmed request ...");

        mFirebase.addOrUpdate(data,

            new FirebaseAccess.OnCompleteVoid() {

                @Override
                public void onSuccess() {

                    Log.d(TAG, "connectDataToRequestsListAndManageSelection: successfully " +
                            "updated this biker's name and coordinates on accepted request " +
                            "object on deliveries node.");

                    mIsRequestSelected = true;
                    deleteThisBikersObjectFromAvailableBikersNode(requesterId, callbacks);
                }

                @Override
                public void onFailure() {

                    Log.d(TAG, "connectDataToRequestsListAndManageSelection: FAILURE! Could " +
                            "not update this biker's data on confirmed request ...");

                }
            }, DELIVERIES_CHILD, requesterId);
    }

    private void deleteThisBikersObjectFromAvailableBikersNode(
            final String requesterId,
            final OnRequestSelected callbacks) {

        Log.d(TAG, "connectDataToRequestsListAndManageSelection: attempting to remove " +
                "available biker object from available bikers node ...");

        mFirebase.delete(new FirebaseAccess.OnCompleteVoid() {

            @Override
            public void onSuccess() {

                Log.d(TAG, "connectDataToRequestsListAndManageSelection: successfully " +
                        "removed available biker object from available bikers node.");

                Log.d(TAG, "connectDataToRequestsListAndManageSelection: request handshake " +
                        "successfully completed. Redirecting to delivery tracking " +
                        "mReferredActivity ...");

                callbacks.onSuccess(requesterId);
            }

            @Override
            public void onFailure() {

                Log.d(TAG, "connectDataToRequestsListAndManageSelection: FAILURE! Could " +
                        "not remove available biker object from available bikers node.");

            }
        }, AVAILABLE_BIKERS_CHILD, getUid());

    }

    public void deleteThisBikersObjectFromAvailableBikersNode () {

        Log.d(TAG, "deleteThisBikersObjectFromAvailableBikersNode: deleting this biker's " +
                "object from available bikers node ...");

        mFirebase.delete(new FirebaseAccess.OnCompleteVoid() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        }, AVAILABLE_BIKERS_CHILD, getUid());
    }

    //endregion


    /*=======================================================================================
                                              OTHER
     =======================================================================================*/

    //region OTHER METHODS

    private ArrayList<RequestModel> sortByShortestTotalDistance (
            ArrayList<RequestModel> array ) {

        Collections.sort(array,

            new Comparator<RequestModel>() {

                @Override
                public int compare(RequestModel requestModel,
                                   RequestModel t1) {

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
        return "ThisBikersId";
    }

    private String getBikerName() {
        return "ThisBikersName";
    }

    public void removeAllListeners() {

        Log.d(TAG, "removeAllListeners: removing all Firebase listeners ...");

        mFirebase.removeAllValueEventListeners();
    }

    public void removeLastListener() {

        Log.d(TAG, "removeAllListeners: last Firebase listener ...");


        mFirebase.removeLastValueEventListener();
    }

    public void removeLocationListener() {

        Log.d(TAG, "removeLocationListener: removing location listener ...");

        mLocation.removeLocationListener();
    }

    public boolean noRequestsAvailable () {

        if (mRequestList.size() == 0) {
            return true;
        }

        return false;
    }

    //endregion


    /*=======================================================================================
                                              MOCKERS
     =======================================================================================*/

    //region DATA MOCKERS

    public void mockBikerData () {

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
                    }, REQUESTS_CHILD, request.userId);
        }
    }

    //endregion
}
