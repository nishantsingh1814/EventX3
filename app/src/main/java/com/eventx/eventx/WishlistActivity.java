package com.eventx.eventx;

import android.app.ActivityOptions;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import at.markushi.ui.CircleButton;

public class WishlistActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseUserLike;

    private DatabaseReference mDatabaseEvents;

    private RecyclerView likeEvents;
    private FirebaseAuth mAuth;

    private Toolbar toolbar;
    private DatabaseReference mLikeUserDb;


    private DatabaseReference mDatabaseLikeCurrentPost;


    private boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike;
    LinearLayoutManager layoutManager;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        sp=getSharedPreferences("EventX",MODE_PRIVATE);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your Wishlist");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
        mDatabaseUser=mDatabase.child("Users");
        mDatabaseUserLike=mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("Like");
        mDatabaseEvents=mDatabase.child("Event");



        likeEvents=(RecyclerView)findViewById(R.id.like_events);
        layoutManager = new LinearLayoutManager(this);
        likeEvents.setLayoutManager(layoutManager);




    }

    @Override
    protected void onStart() {
        super.onStart();

        setUpViews();
    }
    private void setUpViews() {

        FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter = new FirebaseIndexRecyclerAdapter<Event, EventViewHolder>(
                Event.class, R.layout.event_row, EventViewHolder.class, mDatabaseUserLike,mDatabaseEvents
        ) {

            @Override
            protected void populateViewHolder(EventViewHolder viewHolder,final Event model, int position) {

                final String post_key = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setLocation(model.getVenue());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setStartDateTime(model.getStart_date_time());
                viewHolder.setLikeBtn(post_key);
                viewHolder.setInterested(post_key);
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleEventIntent = new Intent(WishlistActivity.this, EventView.class);
                        singleEventIntent.putExtra("event_id", post_key);
                        startActivity(singleEventIntent);
                        overridePendingTransition(R.anim.slide_right,R.anim.no_change);

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
                        if(mAuth.getCurrentUser()!=null){
                            mLikeUserDb=mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("Like");
                        }
                        mDatabaseLikeCurrentPost.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mLikeUserDb.child(post_key).removeValue();
                                        mProcessLike = false;

                                        Snackbar.make(v, "Removed from your WishList", Snackbar.LENGTH_LONG).show();
                                        if (ActivityCompat.checkSelfPermission(WishlistActivity.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
                                        if(sp.getLong(model.getName(), -1)==-1){
                                            mProcessLike = false;
                                            return;
                                        }
                                        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,sp.getLong(model.getName(),-1));
                                        int rows = getContentResolver().delete(deleteUri, null, null);
                                        mProcessLike = false;
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
        likeEvents.setAdapter(firebaseRecyclerAdapter);

        mDatabaseUserLike.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){
                    TextView noEvents=(TextView)findViewById(R.id.no_item);
                    noEvents.setVisibility(View.VISIBLE);
                }else{
                    TextView noEvents=(TextView)findViewById(R.id.no_item);
                    noEvents.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        View mView;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;
        CircleButton mLikeBtn;
        CircleButton mShareBtn;

        public EventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (CircleButton) mView.findViewById(R.id.like_btn);
            mShareBtn = (CircleButton) mView.findViewById(R.id.share_btn);

            mAuth = FirebaseAuth.getInstance();

        }



        public void setName(String name) {
            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like").child(post_key);
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (mAuth.getCurrentUser().getUid() != null) {
                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            mLikeBtn.setColor(Color.WHITE);
                            mLikeBtn.setImageResource(R.drawable.thumb);
                        } else {
                            mLikeBtn.setColor(Color.parseColor("#F1643B"));
                            mLikeBtn.setImageResource(R.drawable.ic_thumbs_up_hand_symbol);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

        public void setInterested(String post_key){
            final TextView interested=(TextView)mView.findViewById(R.id.interested_people);

            FirebaseDatabase.getInstance().getReference().child("Like").child(post_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long count =dataSnapshot.getChildrenCount();
                    if(count==0){
                        interested.setText( "Nobody interested");
                    }
                    else if(count==1){
                        interested.setText(count +" person interested");
                    }
                    else {
                        interested.setText(count + " people interested");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

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

        public void setImage(final Context context, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_change,R.anim.slide_down);
    }
}
