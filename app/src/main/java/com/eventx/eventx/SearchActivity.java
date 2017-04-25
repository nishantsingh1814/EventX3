package com.eventx.eventx;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.id.edit;

public class SearchActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseEvents;
    private Query query;
    private RecyclerView mEventSearchList;

    private String queryText;
    private LinearLayoutManager layoutManager;
    private Event temp;
    private boolean mProcessLike;

    private DatabaseReference mDatabaseLikeCurrentPost;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mLikeUserDb;

    private FirebaseAuth mAuth;


    SharedPreferences sp;
    SharedPreferences.Editor edit;


    int callId;

    Toolbar toolbar;
    TextView mSearchTv;
    String originalQueryText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchTv = (TextView) findViewById(R.id.search_tv);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Search Results");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sp = getSharedPreferences("EventX", MODE_PRIVATE);
        edit = sp.edit();
        callId = sp.getInt("callId", 1);

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");


        queryText = getIntent().getStringExtra("searchString");
        originalQueryText=queryText;
        mSearchTv.setText("Search Results for " + "\"" + originalQueryText+ "\"");
        queryText = queryText.toLowerCase();

        mEventSearchList = (RecyclerView) findViewById(R.id.event_search_list);
        mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("Event");
        query = mDatabaseEvents.orderByChild("s_name").startAt(queryText).endAt(queryText + "\uf8ff");

        layoutManager = new LinearLayoutManager(SearchActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
        mEventSearchList.setLayoutManager(layoutManager);
        mEventSearchList.setHasFixedSize(true);
        mEventSearchList.setNestedScrollingEnabled(false);

        setUpViews();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_change,R.anim.slide_down);
    }

    private void setUpViews() {

        final FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(
                Event.class, R.layout.event_row, EventViewHolder.class, query
        ) {


            @Override
            protected void populateViewHolder(EventViewHolder viewHolder, final Event model, final int position) {

                temp = model;
                final String post_key = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setLocation(model.getVenue() + "," + model.getState());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setInterested(post_key);
                viewHolder.setStartDateTime(model.getStart_date_time());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleEventIntent = new Intent(SearchActivity.this, EventSingleView.class);
                        singleEventIntent.putExtra("event_id", post_key);
                        startActivity(singleEventIntent);
                        overridePendingTransition(R.anim.slide_right, R.anim.no_change);
                    }
                });

                viewHolder.mShareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");

                            Uri data = Uri.parse("https://jq49b.app.goo.gl/eNh4");
                            Uri.Builder link = new Uri.Builder();
                            link.scheme("https").authority("eventx-77033.firebaseapp.com").appendPath("Event.html").appendQueryParameter("eventid", post_key);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "EventX");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hey i just found an event here" + "\n\n" + link.build().toString() + "\n\nDownload our android app here \n" + data);
                            startActivity(Intent.createChooser(sharingIntent,"Select App"));
                            overridePendingTransition(R.anim.slide_right, R.anim.no_change);

                        } catch (Exception e) {
                            //e.toString();
                        }
                    }
                });
                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        mProcessLike = true;


                        mDatabaseLikeCurrentPost = mDatabaseLike.child(post_key);
                        mDatabaseLike.keepSynced(true);
                        mDatabaseLikeCurrentPost.keepSynced(true);
                        if (mAuth.getCurrentUser() != null) {
                            mLikeUserDb = mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("Like");
                        }
                        mDatabaseLikeCurrentPost.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mLikeUserDb.child(post_key).removeValue();
                                        Snackbar.make(v, "Removed from your WishList", Snackbar.LENGTH_LONG).show();
                                        mProcessLike = false;

                                        if (ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling

                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        Uri deleteUri = null;
                                        if (sp.getLong(model.getName(), -1) == -1) {
                                            return;
                                        }
                                        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, sp.getLong(model.getName(), -1));
                                        int rows = getContentResolver().delete(deleteUri, null, null);

                                    } else {
                                        mLikeUserDb.child(post_key).setValue(post_key);
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());


                                        mProcessLike = false;

                                        Snackbar.make(v, "Added to your WishList", Snackbar.LENGTH_LONG).show();

                                        if (ActivityCompat.checkSelfPermission(SearchActivity.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                            String[] permissions = {android.Manifest.permission.WRITE_CALENDAR};
                                            ActivityCompat.requestPermissions(SearchActivity.this, permissions, 1);
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        ContentResolver cr = getContentResolver();
                                        ContentValues values = new ContentValues();
                                        values.put(CalendarContract.Events.DTSTART, model.getStart_date_time());
                                        values.put(CalendarContract.Events.DTEND, model.getEnd_date_time());
                                        values.put(CalendarContract.Events.TITLE, model.getName());
                                        values.put(CalendarContract.Events.DESCRIPTION, model.getDescription());
                                        values.put(CalendarContract.Events.CALENDAR_ID, callId++);
                                        edit.putInt("callId", callId);

                                        values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
                                        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
// get the event ID that is the last element in the Uri
                                        long eventID = Long.parseLong(uri.getLastPathSegment());

                                        edit.putLong(model.getName(), eventID);
                                        edit.commit();


                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });
            }
        };

        mEventSearchList.setAdapter(firebaseRecyclerAdapter);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(!dataSnapshot.hasChildren()){
                   mSearchTv.setText("No Events Found for "+"\""+originalQueryText+"\"");
               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton mLikeBtn;
        ImageButton mShareBtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public EventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (ImageButton) mView.findViewById(R.id.like_btn);
            mShareBtn = (ImageButton) mView.findViewById(R.id.share_btn);
            mAuth = FirebaseAuth.getInstance();

        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like").child(post_key);
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (mAuth.getCurrentUser() != null) {
                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                            mLikeBtn.setColorFilter(Color.parseColor("#607D8B"));
                        } else {
                            mLikeBtn.setColorFilter(Color.parseColor("#e6e6e6"));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setName(String name) {
            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setStartDateTime(long startDateTime) {
            Date originalDate = new Date(startDateTime);
            SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy");
            String dateString = formatter.format(originalDate);
            formatter = new SimpleDateFormat("hh:mm a");
            String time = formatter.format(originalDate);

            TextView post_start_date_time = (TextView) mView.findViewById(R.id.post_start_date_time);
            post_start_date_time.setText(dateString + " at " + time);
        }

        public void setLocation(String location) {
            TextView post_location = (TextView) mView.findViewById(R.id.post_location);
            post_location.setText(location);
        }

        public void setCategory(String category) {
            TextView post_category = (TextView) mView.findViewById(R.id.post_category);
            post_category.setText(category);
        }

        public void setInterested(String post_key) {
            final TextView interested = (TextView) mView.findViewById(R.id.interested_people);

            FirebaseDatabase.getInstance().getReference().child("Like").child(post_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();
                    if (count == 0) {
                        interested.setText("Nobody interested");
                    } else if (count == 1) {
                        interested.setText(count + " person interested");
                    } else {
                        interested.setText(count + " people interested");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setImage(final Context context, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            //Picasso.with(context).load(image).into(post_image);
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image).into(post_image);
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("poiuy", "onRequestPermissionsResult: ");

                    ContentResolver cr = getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DTSTART, temp.getStart_date_time());
                    values.put(CalendarContract.Events.DTEND, temp.getEnd_date_time());
                    values.put(CalendarContract.Events.TITLE, temp.getName());
                    values.put(CalendarContract.Events.DESCRIPTION, temp.getDescription());
                    values.put(CalendarContract.Events.CALENDAR_ID, callId++);
                    edit.putInt("callId", callId);

                    values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
// get the event ID that is the last element in the Uri
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    edit.putLong(temp.getName(), eventID);
                    edit.commit();
                }

        }
    }
}
