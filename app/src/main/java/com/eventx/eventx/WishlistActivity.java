package com.eventx.eventx;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

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
            protected void populateViewHolder(EventViewHolder viewHolder, Event model, int position) {

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
                        Intent singleEventIntent = new Intent(WishlistActivity.this, EventSingleView.class);
                        singleEventIntent.putExtra("event_id", post_key);
                        startActivity(singleEventIntent, ActivityOptions.makeSceneTransitionAnimation(WishlistActivity.this).toBundle());
                    }
                });

                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        mProcessLike = true;


                        mDatabaseLikeCurrentPost = mDatabaseLike.child(post_key);
                        mDatabaseLike.keepSynced(true);
                        mDatabaseLikeCurrentPost.keepSynced(true);
                        if(mAuth.getCurrentUser().getUid()!=null){
                            mLikeUserDb=mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("Like");
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
                                    } else {
                                        mLikeUserDb.child(post_key).setValue(post_key);
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                        mProcessLike = false;
                                        Snackbar.make(v, "Added to your WishList", Snackbar.LENGTH_LONG).show();

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
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        View mView;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;
        ImageButton mLikeBtn;

        public EventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (ImageButton) mView.findViewById(R.id.like_btn);

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
