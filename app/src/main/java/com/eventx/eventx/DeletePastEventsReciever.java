package com.eventx.eventx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class DeletePastEventsReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final DatabaseReference mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("Event");

        mDatabaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot:dataSnapshot.getChildren()){
                    long time = System.currentTimeMillis();  //get time in millis
                    long end = (long) itemSnapshot.child("end_date_time").getValue();
                    //get the end time from firebase database

                    if (end < time){
                        mDatabaseEvents.child(itemSnapshot.getRef().getKey()).removeValue();  //remove the entry
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
