package com.eventx.eventx.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eventx.eventx.MainActivity;
import com.eventx.eventx.R;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nishant on 4/28/2017.
 */

public class EventAboutFragment extends Fragment {

    private String mPostKey;

    private TextView eventName;
    private TextView eventDate;
    private TextView eventVenue;
    private TextView eventCategory;
    private TextView eventDescription;

    private Button mapView;

    private DatabaseReference mDatabaseEvents;
    private FirebaseAuth mAuth;


    private Button mRemoveBtn;

    private String event_state;
    private String event_venue;

    private AdView mAdView;

    private DatabaseReference mDatabaseUsers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.event_about_fragment, container,false);

        mAdView = (AdView) v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mPostKey = bundle.getString("post_key");
        }
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("Event");
        mDatabaseEvents.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        eventName = (TextView) v.findViewById(R.id.view_event_name);
        eventDate = (TextView) v.findViewById(R.id.view_event_date);
        eventVenue = (TextView) v.findViewById(R.id.view_event_venue);
        eventCategory = (TextView) v.findViewById(R.id.view_event_category);
        eventDescription = (TextView) v.findViewById(R.id.view_description);
        mRemoveBtn = (Button) v.findViewById(R.id.remove_btn);
        mapView = (Button) v.findViewById(R.id.map);
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressString = event_venue + " " + event_state;

                Uri location = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", addressString).build();
                Intent intent = new Intent(Intent.ACTION_VIEW);

                intent.setData(location);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "You don't any maps app", Toast.LENGTH_LONG).show();
                }


            }
        });

        mDatabaseEvents.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("start_date_time").getValue() != null) {
                    String event_name = (String) dataSnapshot.child("name").getValue();

                    event_state = (String) dataSnapshot.child("state").getValue();
                    event_venue = (String) dataSnapshot.child("venue").getValue();
                    String event_description = (String) dataSnapshot.child("description").getValue();

                    long event_start_date = (long) dataSnapshot.child("start_date_time").getValue();
                    long event_end_date = (long) dataSnapshot.child("end_date_time").getValue();

                    String event_category = (String) dataSnapshot.child("category").getValue();

                    String post_uid = (String) dataSnapshot.child("uid").getValue();

                    Date startDate = new Date(event_start_date);
                    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy");
                    String startdateString = formatter.format(startDate);
                    formatter = new SimpleDateFormat("hh:mm:a");
                    String starttimeString = formatter.format(startDate);

                    Date endDate = new Date(event_end_date);
                    SimpleDateFormat endFormatter = new SimpleDateFormat("E, dd MMM yyyy");
                    String enddateString = endFormatter.format(endDate);
                    endFormatter = new SimpleDateFormat("hh:mm a");
                    String endtimeString = endFormatter.format(endDate);

                    eventName.setText(event_name);
                    eventDescription.setText(event_description);
                    eventVenue.setText(event_venue + "," + event_state);
                    eventCategory.setText(event_category);
                    eventDate.setText(startdateString + " at " + starttimeString + "-\n\n" + enddateString + " at " + endtimeString);


                    if (mAuth.getCurrentUser().getUid().equals(post_uid)) {
                        mRemoveBtn.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setMessage("Are you sure?");
                dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseEvents.child(mPostKey).removeValue();
                        Intent mainIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(mainIntent);
                        getActivity().finish();
                    }
                });
                dialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                dialog.show();
            }
        });
        return v;
    }


}
