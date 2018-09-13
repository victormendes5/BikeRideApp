package com.infnet.bikeride.bikeride.dao;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAccess {

                /*---------------------------------------------------------------\
                                               FIELDS
                \---------------------------------------------------------------*/

    //region FIELDS

    private static final String TAG = "FirebaseAccess";

    // ---> Map for active listeners
    HashMap<Integer, Map.Entry<DatabaseReference, ValueEventListener>> mValueListenersHashMap
            = new HashMap<>();

    // ---> Main database reference
    private DatabaseReference mDatabase;

    public FirebaseAccess() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //endregion


                /*---------------------------------------------------------------\
                                            INTERFACES
                \---------------------------------------------------------------*/

    //region INTERFACES

    public interface OnComplete<T> {
        void onSuccess(T data);
        void onFailure(T data);
    }

    public interface OnCompleteVoid {
        void onSuccess();
        void onFailure();
    }

    public interface OnCompleteKey {
        void onSuccess(String key);
        void onFailure();
    }

    public interface Condition<T> {
        boolean ExecuteIf(T data);
    }

    public interface ListenToChanges<T> {
        void onChange(T data);
        void onError(T data);
    }

    //endregion

                /*---------------------------------------------------------------\
                                              METHODS
                \---------------------------------------------------------------*/

    //region METHODS

    public <T> void getAll(
            final Class<T> typeToBeReturnedOnArrayList,
            final OnComplete<ArrayList<T>> onCompleteCallback,
            @NonNull String... path
    ) {

        // ---> Get Firebase reference
        DatabaseReference databaseRef = mDatabase;

        // ---> Setup log path string
        String firebasePath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                databaseRef = databaseRef.child(child);
                firebasePath += child + " > ";
            }
        }

        // ---> Refine path string appearance
        firebasePath = firebasePath.substring(0, firebasePath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "getAll: getting all objects from child - " + firebasePath);

        // ---> Initialize array to be returned
        final ArrayList<T> arrayList = new ArrayList<>();

        // ---> Execute Firebase method for fetching data one time from specified child
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // ---> If snapshot returns null, nothing was found at given path
                if (snapshot == null) {
                    Log.d(TAG, "getAll: no data found at given path.");
                    return;
                }

                // ---> Iterate on path of specified child and add to ArrayList
                for (@NonNull DataSnapshot child : snapshot.getChildren()) {

                    T element;

                    try {
                        element = child.getValue(typeToBeReturnedOnArrayList);

                    } catch (Exception e) {
                        Log.d(TAG, "getAll: an object that can't be converted to " +
                                "the provided type has been detected and ignored.");
                        continue;
                    }

                    Log.d(TAG, "getAll: found object - " + child.toString());

                    arrayList.add(element);
                }

                Log.d(TAG, "getAll: finished retrieving available " +
                        "objects. Found " + arrayList.size() + ".");

                onCompleteCallback.onSuccess(arrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getAll: error accessing Firebase or retrieving data - ",
                        databaseError.toException());
                onCompleteCallback.onFailure(null);
            }
        });
    }

    public <T> void getObjectOrProperty(
            final Class<T> typeToBeReturned,
            final OnComplete<T> onCompleteCallback,
            @NonNull String... path
    ) {

        // ---> Get Firebase reference
        DatabaseReference databaseRef = mDatabase;

        // ---> Setup log path string
        String firebasePath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                databaseRef = databaseRef.child(child);
                firebasePath += child + " > ";
            }
        }

        // ---> Refine path string appearance
        firebasePath = firebasePath.substring(0, firebasePath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "getObjectOrProperty: getting object from path - " + firebasePath);

        // ---> Execute Firebase method for fetching object one time from specified path
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // ---> If snapshot is null, no data was found at given path
                if (snapshot == null) {
                    Log.d(TAG, "getObjectOrProperty: no data found at given path.");
                    onCompleteCallback.onFailure(null);
                }

                // ---> Initialize object to be returned
                T element;

                try {
                    element = snapshot.getValue(typeToBeReturned);

                } catch (Exception e) {
                    Log.d(TAG, "getObjectOrProperty: object found cannot be converted to " +
                            "the specified type.");
                    onCompleteCallback.onFailure(null);
                    return;
                }

                Log.d(TAG, "getObjectOrProperty: found object - " + element.toString());

                onCompleteCallback.onSuccess(element);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getObjectOrProperty: error accessing Firebase or retrieving data - ",
                        databaseError.toException());
                onCompleteCallback.onFailure(null);
            }
        });
    }

    public <T> void addOrUpdate(
            final T dataToUpdate,
            final OnCompleteVoid onCompleteCallback,
            @NonNull String... path
    ) {

        // ---> Get Firebase reference
        DatabaseReference databaseRef = mDatabase;

        // ---> Setup log path string
        String firebasePath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                databaseRef = databaseRef.child(child);
                firebasePath += child + " > ";
            }
        }

        // ---> Refine path string appearance
        firebasePath = firebasePath.substring(0, firebasePath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "addOrUpdate: updating value '" + dataToUpdate.toString() +
                "' on path - " + firebasePath);

        databaseRef
                .setValue(dataToUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "addOrUpdate: add or update SUCCESSFUL!");
                        onCompleteCallback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "addOrUpdate: add or update FAILED!");
                        e.printStackTrace();
                        onCompleteCallback.onFailure();
                    }
                });
    }

    public <T> void addUsingKey(
            final T dataToUpdate,
            final OnCompleteKey onCompleteCallback,
            @NonNull String... path
    ) {

        // ---> Get Firebase reference
        DatabaseReference databaseRef = mDatabase;

        // ---> Setup log path string
        String firebasePath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                databaseRef = databaseRef.child(child);
                firebasePath += child + " > ";
            }
        }

        // ---> Get key for new entry
        final String key = databaseRef.push().getKey();

        // ---> Add key to to save path
        databaseRef = databaseRef.child(key);

        // ---> Add key to display path string
        firebasePath += key;

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "addUsingKey: updating value '" + dataToUpdate.toString() +
                "' on path - " + firebasePath);

        databaseRef
                .setValue(dataToUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "addUsingKey: add or update SUCCESSFUL!");
                        onCompleteCallback.onSuccess(key);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "addUsingKey: add or update FAILED!");
                        e.printStackTrace();
                        onCompleteCallback.onFailure();
                    }
                });
    }

    public void delete (
            final OnCompleteVoid onCompleteCallback,
            @NonNull String... path
    ) {

        // ---> Get Firebase reference
        DatabaseReference databaseRef = mDatabase;

        // ---> Setup log path string
        String firebasePath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                databaseRef = databaseRef.child(child);
                firebasePath += child + " > ";
            }
        }

        // ---> Refine path string appearance
        firebasePath = firebasePath.substring(0, firebasePath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "delete: deleting data on path - " + firebasePath);

        databaseRef
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "delete: SUCCESSFUL!");
                        onCompleteCallback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "delete: FAILED!");
                        e.printStackTrace();
                        onCompleteCallback.onFailure();
                    }
                });
    }

    public <T> void updateMutableDataOnCondition(
            final Class<T> typeOfData,
            final T dataToUpdate,
            final Condition<T> updateCondition,
            final OnComplete<T> onCompleteCallback,
            @NonNull String... path
    ) {

        // ---> Get Firebase reference
        DatabaseReference databaseRef = mDatabase;

        // ---> Setup log path string
        String firebasePath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                databaseRef = databaseRef.child(child);
                firebasePath += child + " > ";
            }
        }

        // ---> Refine path string appearance
        firebasePath = firebasePath.substring(0, firebasePath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "updateMutableDataOnCondition: trying to update value '" +
                dataToUpdate.toString() + "' on path - " + firebasePath);

        databaseRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                T data = mutableData.getValue(typeOfData);

                if (data == null) {
                    return Transaction.abort();
                }

                if (updateCondition.ExecuteIf(data)) {
                    data = dataToUpdate;
                    mutableData.setValue(data);
                    return Transaction.success(mutableData);
                }

                else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {

                T data = dataSnapshot.getValue(typeOfData);

                if (b) {
                    Log.d(TAG, "updateMutableDataOnCondition: onComplete: data successfully " +
                            "updated.");
                    onCompleteCallback.onSuccess(null);
                }
                else {

                    if (databaseError == null && data == null) {
                        Log.d(TAG, "updateMutableDataOnCondition: onComplete: node on " +
                                "specified path doesn't exist.");
                    }

                    else if (databaseError == null && data != null) {
                        Log.d(TAG, "updateMutableDataOnCondition: onComplete: update " +
                                "condition was not met.");
                    }

                    else {
                        Log.d(TAG, "updateMutableDataOnCondition: onComplete: an error has " +
                                "occured - " + databaseError);
                    }

                    onCompleteCallback.onFailure(data);
                }
            }
        });
    }

    public <T> void setListenerToChild (
            final Class<T> typeToBeReturnedOnArrayList,
            final ListenToChanges<ArrayList<T>> onChangesCallback,
            @NonNull String... path
    ) {
        // ---> Get Firebase reference
        DatabaseReference database = mDatabase;

        // ---> Setup log path string
        String fbPath = "";

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                database = database.child(child);
                fbPath += child + " > ";
            }
        }

        // ---> Refine path string appearance and assign to final variable
        final String firebasePath = fbPath.substring(0, fbPath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "setListenerToChild: setting listener on child - " + firebasePath);

        // ---> Initialize array to be returned
        final ArrayList<T> arrayList = new ArrayList<>();

        // ---> Set base reference for listener
        final DatabaseReference baseReference = database;


        // ---> Set listener on base reference
        final ValueEventListener listener = baseReference.addValueEventListener(

            new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    arrayList.clear();

                    // ---> Iterate on path of specified child and add to ArrayList
                    for (@NonNull DataSnapshot child : dataSnapshot.getChildren()) {

                        T element;

                        try {
                            element = child.getValue(typeToBeReturnedOnArrayList);

                        } catch (Exception e) {
                            Log.d(TAG, "setListenerToChild: an object that can't be converted to "
                                    + "the provided type has been detected and ignored.");
                            continue;
                        }

                        Log.d(TAG, "setListenerToChild: found object - " + child.toString());

                        arrayList.add(element);
                    }

                    onChangesCallback.onChange(arrayList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "setListenerToChild: loadPost: onCancelled - error accessing "
                            + "Firebase or retrieving data - ", databaseError.toException());

                    onChangesCallback.onError(null);

                }
            });

        Log.d(TAG, "setListenerToChild: adding listener to ListenersHashMap on index " +
                mValueListenersHashMap.size() + ". DatabaseReference '" + baseReference.toString()
                + "'.");

        mValueListenersHashMap.put(new Integer(mValueListenersHashMap.size()),
                new AbstractMap.SimpleEntry<DatabaseReference, ValueEventListener>(
                        baseReference, listener
                ));

    }

    public <T> void setListenerToObjectOrProperty (
            final Class<T> typeToBeReturned,
            final ListenToChanges<T> onChangesCallback,
            @NonNull String... path
    ) {
        // ---> Setup log path string
        String fbPath = "";

        // ---> Get Firebase reference
        DatabaseReference database = mDatabase;

        // ---> Iterate to point reference to appropriate path and build path string
        if (path.length > 0) {
            for (String child : path) {
                database = database.child(child);
                fbPath += child + " > ";
            }
        }

        // ---> Refine path string appearance and assign to final variable
        final String firebasePath = fbPath.substring(0, fbPath.length() - 3);

        // ---> Inform developer of method start and acquired path
        Log.d(TAG, "setListenerToObjectOrProperty: setting listener on path - " + firebasePath);

        // ---> Set base reference for listener
        final DatabaseReference baseReference = database;

        // ---> Set listener on base reference
        final ValueEventListener listener = baseReference.addValueEventListener(

            new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    T element;

                    try {
                        element = dataSnapshot.getValue(typeToBeReturned);

                    } catch (Exception e) {
                        Log.d(TAG, "setListenerToObjectOrProperty: onDataChange: object or " +
                                "property at path can't be converted to given type.");

                        onChangesCallback.onError(null);
                        return;
                    }

                    if (element == null) {
                        Log.d(TAG, "setListenerToObjectOrProperty: onDataChange: no object " +
                                "or property has been found at provided path.");

                        onChangesCallback.onError(null);
                        return;
                    }

                    Log.d(TAG, "setListenerToObjectOrProperty: found object or property - "
                            + element.toString());

                    onChangesCallback.onChange(element);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "setListenerToObjectOrProperty: loadPost: onCancelled - error " +
                            "accessing Firebase or retrieving data - ", databaseError.toException());

                    onChangesCallback.onError(null);
                }
            });

        Log.d(TAG, "setListenerToChild: adding listener to ListenersHashMap on index " +
                mValueListenersHashMap.size() + ". DatabaseReference '" + baseReference.toString()
                + "'.");

        mValueListenersHashMap.put(new Integer(mValueListenersHashMap.size()),
                new AbstractMap.SimpleEntry<DatabaseReference, ValueEventListener>(
                        baseReference, listener
                ));
    }

    public void removeAllValueEventListeners() {

        if (mValueListenersHashMap.size() == 0) {
            Log.d(TAG, "removeAllValueEventListeners: no active listeners detected.");
            return;
        }

        for (Map.Entry<Integer, Map.Entry<DatabaseReference, ValueEventListener>> entry :
                mValueListenersHashMap.entrySet()) {

            Map.Entry<DatabaseReference, ValueEventListener> listenerData = entry.getValue();

            DatabaseReference databaseReference = listenerData.getKey();
            ValueEventListener valueEventListener = listenerData.getValue();

            databaseReference.removeEventListener(valueEventListener);

            Log.d(TAG, "removeAllValueEventListeners: removing value event listener " +
                    "indexed " + entry.getKey() + " from node '" + databaseReference.toString()
                    + "'.");

        }

        mValueListenersHashMap.clear();
    }

    public void removeLastValueEventListener() {

        if (mValueListenersHashMap.size() == 0) {
            Log.d(TAG, "removeLastValueEventListeners: no active listeners detected.");
            return;
        }

        Integer hashMapLength = new Integer(mValueListenersHashMap.size());

        for (Map.Entry<Integer, Map.Entry<DatabaseReference, ValueEventListener>> entry :
                mValueListenersHashMap.entrySet()) {

            if (entry.getKey() != hashMapLength-1) continue;

            Map.Entry<DatabaseReference, ValueEventListener> listenerData = entry.getValue();

            DatabaseReference databaseReference = listenerData.getKey();
            ValueEventListener valueEventListener = listenerData.getValue();

            databaseReference.removeEventListener(valueEventListener);

            mValueListenersHashMap.remove(hashMapLength-1);

            Log.d(TAG, "removeLastValueEventListeners: removing value event listener " +
                    "indexed " + entry.getKey() + " from node '" + databaseReference.toString()
                    + "'. " + mValueListenersHashMap.size() + " left active.");

            return;
        }
    }

    //endregion

}
