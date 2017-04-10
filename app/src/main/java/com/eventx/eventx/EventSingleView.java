package com.eventx.eventx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventSingleView extends AppCompatActivity {

    private ImageView eventImage;
    private TextView eventName;
    private TextView eventDate;
    private TextView eventVenue;
    private TextView eventCategory;
    private TextView eventDescription;

    private Button mapView;
    private Toolbar toolbar;



    private DatabaseReference mDatabaseEvents;
    private FirebaseAuth mAuth;
    private String mPostKey;

    private Button mRemoveBtn;

    private String event_state;
    private String event_venue;
    private String event_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_single_view);

        toolbar=(Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseEvents= FirebaseDatabase.getInstance().getReference().child("Event");

        mAuth=FirebaseAuth.getInstance();
        mPostKey=getIntent().getExtras().getString("event_id");
        eventName=(TextView)findViewById(R.id.view_event_name);
        eventDate=(TextView)findViewById(R.id.view_event_date);
        eventVenue=(TextView)findViewById(R.id.view_event_venue);
        eventCategory=(TextView)findViewById(R.id.view_event_category);
        eventDescription=(TextView)findViewById(R.id.view_description);
        eventImage=(ImageView)findViewById(R.id.view_image);
        mRemoveBtn=(Button)findViewById(R.id.remove_btn);
        mapView=(Button)findViewById(R.id.map);
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressString = event_venue+" "+event_state;

                Uri location=Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",addressString).build();
                Intent intent = new Intent(Intent.ACTION_VIEW);

                intent.setData(location);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        });





        mDatabaseEvents.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String event_name=(String)dataSnapshot.child("name").getValue();
                setTitle("Event: " +event_name);

               event_state=(String)dataSnapshot.child("state").getValue();
                event_venue=(String)dataSnapshot.child("venue").getValue();
                String event_description=(String)dataSnapshot.child("description").getValue();
                event_image=(String)dataSnapshot.child("image").getValue();
                long event_start_date=(long)dataSnapshot.child("start_date_time").getValue();
                long event_end_date=(long)dataSnapshot.child("end_date_time").getValue();
                String event_category=(String)dataSnapshot.child("category").getValue();

                String post_uid=(String)dataSnapshot.child("uid").getValue();

                Date startDate = new Date(event_start_date);
                SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy");
                String startdateString = formatter.format(startDate);
                formatter = new SimpleDateFormat("hh:mm:a");
                String starttimeString = formatter.format(startDate);

                Date endDate = new Date(event_end_date);
                SimpleDateFormat endFormatter = new SimpleDateFormat("E, dd MMM yyyy");
                String enddateString = endFormatter.format(endDate);
                endFormatter = new SimpleDateFormat("hh:mm:a");
                String endtimeString = endFormatter.format(startDate);

                eventName.setText(event_name);
                eventDescription.setText(event_description);
                eventVenue.setText(event_venue+","+event_state);
                eventCategory.setText(event_category);
                eventDate.setText(startdateString + " at " + starttimeString + "-\n\n" + enddateString + " at " + endtimeString);
                Picasso.with(EventSingleView.this).load(event_image).into(eventImage);

                if(mAuth.getCurrentUser().getUid().equals(post_uid)){
                    mRemoveBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent(EventSingleView.this,ImageAcitivity.class);
                imageIntent.putExtra("image",event_image);
                startActivity(imageIntent);
            }
        });

        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(EventSingleView.this);
                dialog.setMessage("Are you sure?");
                dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseEvents.child(mPostKey).removeValue();
                        Intent mainIntent=new Intent(EventSingleView.this,MainActivity.class);

                        startActivity(mainIntent);
                        finish();
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
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
