package com.infnet.bikeride.bikeride.activityrequestbiker;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.infnet.bikeride.bikeride.RequestListDataModel;
import com.infnet.bikeride.bikeride.activityrequestbiker.adapters.RequestsListAdapter;
import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.AvailableBikerModel;
import com.infnet.bikeride.bikeride.models.RequestModel;
import com.infnet.bikeride.bikeride.services.BRLocations;
import com.infnet.bikeride.bikeride.services.GoogleMapsAPI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class RequestBikerManager extends FirebaseAccess {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    private static final String TAG = "RequestBikerManager";

    //region CONSTANTS


    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private AppCompatActivity mReferredActivity;
    private RequestModel mRequest = new RequestModel();

    private RequestsListAdapter mRequestListAdapter;
    ArrayList<RequestListDataModel> mRequestList = new ArrayList<>();

    private BRLocations mLocation;

    // ---> Google APIs
    private GoogleMapsAPI mGoogleMaps;

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
        void onSuccess(RequestModel request);
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

        mGoogleMaps = new GoogleMapsAPI(mReferredActivity, Constants.ViewId.MAP, new GoogleMapsAPI.Maps() {

                @Override
                public void OnMapReady() {

                    mGoogleMaps.centerMapOnDeviceLocation();
                }
        });
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

        Log.d(TAG, "broadcastBikerLocation: updating biker location on map ...");

        mGoogleMaps.centerMapOnDeviceLocation();

        addOrUpdate(bikerData,

            new OnCompleteVoid() {

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

            }, Constants.ChildName.AVAILABLE_BIKERS, getUid());

    }

    //endregion


               /*---------------------------------------------------------------\
                                    MONITOR AVAILABLE REQUESTS
               \---------------------------------------------------------------*/

    //region MONITOR AVAILABLE REQUESTS METHODS

    public void monitorAvailableRequests(final MonitorRequests callbacks) {

        Log.d(TAG, "monitorAvailableRequests: starting to monitor requests ...");

        setListenerToChild(RequestModel.class,

            new ListenToChanges<ArrayList<RequestModel>>() {

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

            }, Constants.ChildName.REQUESTS);
    }

    //endregion


               /*---------------------------------------------------------------\
                         CONNECT DATA TO REQUESTS LIST AND MANAGE SELECTION
               \---------------------------------------------------------------*/

    //region CONNECT DATA TO REQUESTS LIST AND GET SELECTION METHODS

    public void connectDataToRequestsListAndManageSelection(int viewId,
                                                            final OnRequestSelected callbacks) {

        ListView listView = mReferredActivity.findViewById(Constants.ViewId.REQUESTS_LIST);

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
        updateMutableDataOnCondition(String.class, getUid(),

            // --> Only sign if no biker signed it before.
            new Condition<String>() {
                @Override
                public boolean ExecuteIf(String data) {

                    if (data.equals("")) return true;
                    return false;
                }
            },

            new OnComplete<String>() {

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
            Constants.ChildName.REQUESTS, requesterId, "bikerId");
    }

    private void awaitRequestDeletionOnRequestsNode(final String requesterId,
                                                    final OnRequestSelected callbacks) {

        mIsRequestDeletedOnRequestsNode = false;

        deletionOfRequestOnRequestsNodeTimeout(callbacks);

        setListenerToObjectOrProperty(RequestModel.class,

            // ---> Awaiting request deletion on requests node
            new ListenToChanges<RequestModel>() {
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

                    removeLastValueEventListener();

                    // ---> Deletion on request node has been detected.
                    // ---> Await creation of request on deliveries node.
                    awaitCreationOfRequestOnDeliveriesNode(requesterId,
                            callbacks);
                }

            }, Constants.ChildName.REQUESTS, requesterId);

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

                    removeLastValueEventListener();

                    // update biker data on available bikers node ONCE

                }
            }, Constants.Timeouts.REQUEST_LONG);
    }

    private void awaitCreationOfRequestOnDeliveriesNode (final String requesterId,
                                                         final OnRequestSelected callbacks) {

        mIsRequestCreatedOnDeliveriesNode = false;

        creationOfRequestOnDeliveriesNodeTimeout(callbacks);

        setListenerToObjectOrProperty (RequestModel.class,

            new ListenToChanges<RequestModel>() {

                @Override
                public void onChange(RequestModel data) {

                    if (data == null) return;

                    if (data.bikerId.equals(getUid())) {

                        mIsRequestCreatedOnDeliveriesNode = true;

                        Log.d(TAG, "connectDataToRequestsListAndManageSelection: Second " +
                                "step of transfer confirmed (request has been copied to " +
                                "deliveries node).");

                        removeLastValueEventListener();

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

            }, Constants.ChildName.DELIVERIES, requesterId);
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

                    removeLastValueEventListener();

                    // update biker data on available bikers node ONCE

                }
            }, Constants.Timeouts.REQUEST_LONG);
    }

    private void fulfillBikerDataOnRequest(final String requesterId,
                                           RequestModel data,
                                           final OnRequestSelected callbacks) {

        data.bikerName = getBikerName();
        data.bikerPositionLatitude = mLocation.getLatitude();
        data.bikerPositionLongitude = mLocation.getLongitude();

        final RequestModel request = data;

        Log.d(TAG, "connectDataToRequestsListAndManageSelection: attempting to fulfill " +
                "this bikers data on confirmed request ...");

        addOrUpdate(data,

            new OnCompleteVoid() {

                @Override
                public void onSuccess() {

                    Log.d(TAG, "connectDataToRequestsListAndManageSelection: successfully " +
                            "updated this biker's name and coordinates on accepted request " +
                            "object on deliveries node.");

                    mIsRequestSelected = true;
                    deleteThisBikersObjectFromAvailableBikersNode(request, callbacks);
                }

                @Override
                public void onFailure() {

                    Log.d(TAG, "connectDataToRequestsListAndManageSelection: FAILURE! Could " +
                            "not update this biker's data on confirmed request ...");

                }
            }, Constants.ChildName.DELIVERIES, requesterId);
    }

    private void deleteThisBikersObjectFromAvailableBikersNode(
            final RequestModel request,
            final OnRequestSelected callbacks) {

        Log.d(TAG, "connectDataToRequestsListAndManageSelection: attempting to remove " +
                "available biker object from available bikers node ...");

        delete(new OnCompleteVoid() {

            @Override
            public void onSuccess() {

                Log.d(TAG, "connectDataToRequestsListAndManageSelection: successfully " +
                        "removed available biker object from available bikers node.");

                Log.d(TAG, "connectDataToRequestsListAndManageSelection: request handshake " +
                        "successfully completed. Redirecting to delivery tracking " +
                        "mReferredActivity ...");

                callbacks.onSuccess(request);
            }

            @Override
            public void onFailure() {

                Log.d(TAG, "connectDataToRequestsListAndManageSelection: FAILURE! Could " +
                        "not remove available biker object from available bikers node.");

            }
        }, Constants.ChildName.AVAILABLE_BIKERS, getUid());

    }

    public void deleteThisBikersObjectFromAvailableBikersNode () {

        Log.d(TAG, "deleteThisBikersObjectFromAvailableBikersNode: deleting this biker's " +
                "object from available bikers node ...");

        delete(new OnCompleteVoid() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        }, Constants.ChildName.AVAILABLE_BIKERS, getUid());
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
        return Constants.MockedIds.Biker;
    }

    private String getBikerName() {
        return "ThisBikersName";
    }

    public void removeLocationListener() {

        Log.d(TAG, "removeLocationListener: removing location listener ...");

        mLocation.removeLocationListener();
    }

    public boolean noRequestsAvailable () {

        if (mRequestList.size() == 0) return true;

        return false;
    }

    public void verifyPermissionsRequestResult (int requestCode, int[] grantResults) {

        mGoogleMaps.verifyPermissionRequestResult(requestCode, grantResults);
    }

    //endregion
}
