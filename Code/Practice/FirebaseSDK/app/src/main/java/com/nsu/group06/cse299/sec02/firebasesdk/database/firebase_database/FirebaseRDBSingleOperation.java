package com.nsu.group06.cse299.sec02.firebasesdk.database.firebase_database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nsu.group06.cse299.sec02.firebasesdk.DummyModel;
import com.nsu.group06.cse299.sec02.firebasesdk.database.Database;

public class FirebaseRDBSingleOperation<T> extends Database.SingleOperationDatabase<T> {

    private static final String TAG = "FRDBSO-debug";

    // needed for turning dataSnapshots to model class's object
    // courtesy-
    // https://stackoverflow.com/questions/3437897/how-do-i-get-a-class-instance-of-generic-type-t
    private final Class<T> mTypeParameterClass;

    private  FirebaseRDBApiEndPoint mApiEndPoint;

    public FirebaseRDBSingleOperation(Class<T> mTypeParameterClass) {

        super();

        this.mTypeParameterClass = mTypeParameterClass;
    }

    public FirebaseRDBSingleOperation(Class<T> mTypeParameterClass, FirebaseRDBApiEndPoint apiEndPoint,
                                      Database.SingleOperationDatabase.SingleOperationDatabaseCallback singleOperationDatabaseCallback) {

        super(singleOperationDatabaseCallback);

        this.mTypeParameterClass = mTypeParameterClass;
        this.mApiEndPoint = apiEndPoint;
    }


    @Override
    public void create(T data) {

        // generate new data id here
        String id = mApiEndPoint.toApiEndPoint().getRef().push().getKey();

        Log.d(TAG, "create: create new data = "+data.toString()+"-"+id);

        mApiEndPoint.toApiEndPoint().getRef().child(id).setValue(data)
                .addOnFailureListener(e -> {
                    singleOperationDatabaseCallback.onDatabaseOperationFailed("failed to create data = "+data.toString());
                });

    }

    @Override
    public void read() {

        mApiEndPoint.toApiEndPoint().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.exists()){

                    Log.d(TAG, "onDataChange: data doesn't exist");

                    singleOperationDatabaseCallback.onDataRead(null);

                    return;
                }

                /*
                Have to do this because apiEndPoint.toApiEndPoint() returns Query and not Reference
                queries in FirebaseEDB always return data list we have to loop through it's children
                to get the queried data, even if it's only one data
                 */
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    T data = dataSnapshot.getValue(FirebaseRDBSingleOperation.this.mTypeParameterClass);

                    if(data!=null){

                        Log.d(TAG, "onDataChange: "+data.toString());

                        singleOperationDatabaseCallback.onDataRead(data);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                singleOperationDatabaseCallback.onDatabaseOperationFailed("failed to read at = "+mApiEndPoint.getmUrl());
            }
        });
    }

    @Override
    public void update(T data) {

        mApiEndPoint.toApiEndPoint().getRef().setValue(data)
                .addOnFailureListener(e -> {
                    singleOperationDatabaseCallback.onDatabaseOperationFailed("failed to update entry = " + data.toString());
                });
    }

    @Override
    public void delete(T data) {

        mApiEndPoint.toApiEndPoint().getRef().setValue(null)
                .addOnFailureListener(e -> {
                    singleOperationDatabaseCallback.onDatabaseOperationFailed("failed to delete entry = " + data.toString());
                });
    }


    public Class<T> getmTypeParameterClass() {
        return mTypeParameterClass;
    }

    public FirebaseRDBApiEndPoint getmApiEndPoint() {
        return mApiEndPoint;
    }

    public void setmApiEndPoint(FirebaseRDBApiEndPoint mApiEndPoint) {
        this.mApiEndPoint = mApiEndPoint;
    }
}
