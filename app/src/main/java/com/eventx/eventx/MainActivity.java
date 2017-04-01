package com.eventx.eventx;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.R.attr.data;
import static android.R.attr.start;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;
    private Toolbar toolbar;
    private ActionBarDrawerToggle mToggle;
    private FloatingActionButton mAddFab;


    private ProgressBar mProgressBar;

    private RecyclerView mEventList;


    private NavigationView mNavigation;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabaseEvents;
    private ChildEventListener mChildEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfilePhotosStorageReference;

    private DatabaseReference mLikeUserDb;


    private ArrayAdapter<String> spinnerCategory;
    private DrawerLayout mDrawerLayout;
    private Query query;
    private DatabaseReference mDatabaseLikeCurrentPost;

    private boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();



        mFirebaseStorage = FirebaseStorage.getInstance();
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseEvents = mFirebaseDatabase.getReference().child("Event");
        query = mDatabaseEvents;
        mProfilePhotosStorageReference = mFirebaseStorage.getReference().child("profile");

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");

        mDatabaseEvents.keepSynced(true);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigation = (NavigationView) findViewById(R.id.nav_bar);

        ArrayList<String> list = new ArrayList<>();
        list.add("All Events");
        list.add("Technology");
        list.add("Workshop");
        list.add("Sports");
        list.add("Art and Culture");
        list.add("Gaming");
        list.add("Health");

        spinnerCategory = new ArrayAdapter<>(this, R.layout.layout_drop_title, list);
        spinnerCategory.setDropDownViewResource(R.layout.layout_drop_list);

        Spinner mNavigationSpinner = new Spinner(getSupportActionBar().getThemedContext());
        mNavigationSpinner.setAdapter(spinnerCategory);
        mNavigationSpinner.setGravity(8388613);
        toolbar.addView(mNavigationSpinner);
////////////////////////////////////////////////////////////////////////////////
        mNavigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    query = mDatabaseEvents.orderByChild("category").equalTo("Technology");
                    setUpViews();
                } else if (position == 2) {
                    query = mDatabaseEvents.orderByChild("category").equalTo("Workshop");
                    setUpViews();
                } else if (position == 3) {
                    query = mDatabaseEvents.orderByChild("category").equalTo("Sports");
                    setUpViews();
                } else if (position == 4) {
                    query = mDatabaseEvents.orderByChild("category").equalTo("Art and Culture");
                    setUpViews();
                } else if (position == 5) {
                    query = mDatabaseEvents.orderByChild("category").equalTo("Gaming");
                    setUpViews();
                } else if (position == 6) {
                    query = mDatabaseEvents.orderByChild("category").equalTo("Health");
                    setUpViews();
                } else {
                    query = mDatabaseEvents;
                    setUpViews();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
////////////////////////////////////////////////////////////////////////////////
        mNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_profile) {
                    Intent userProfile = new Intent(MainActivity.this, UserProfile.class);
                    startActivity(userProfile);
                }
                if (item.getItemId() == R.id.nav_create_event) {
                    startActivity(new Intent(MainActivity.this, PostEventActivity.class));
                }
                if (item.getItemId() == R.id.nav_logout) {
                    logout();
                }
                if (item.getItemId() == R.id.nav_wish_list) {
                    startActivity(new Intent(MainActivity.this,WishlistActivity.class));
                }

                if (item.getItemId() == R.id.nav_contact) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SENDTO);

                    Uri uri = Uri.parse("mailto:eventx.team2017@gmail.com");
                    intent.setData(uri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "No suitable Apps Found", Toast.LENGTH_SHORT);
                    }
                }
                if (item.getItemId() == R.id.nav_feedback) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SENDTO);

                    Uri uri = Uri.parse("mailto:eventx.team2017@gmail.com");
                    intent.setData(uri);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "No suitable Apps Found", Toast.LENGTH_SHORT);
                    }
                }
                return false;
            }
        });

        mEventList = (RecyclerView) findViewById(R.id.event_list);
        mEventList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mEventList.setLayoutManager(layoutManager);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);


        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddFab = (FloatingActionButton) findViewById(R.id.add_event_fab);
        mAddFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostEventActivity.class));
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.GreenTheme)
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
                                    ))
                                    .build(), RC_SIGN_IN);
                }
            }
        };

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setUpViews();
    }


    private void setUpViews() {

        FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(
                Event.class, R.layout.event_row, EventViewHolder.class, query
        ) {


            @Override
            protected void populateViewHolder(EventViewHolder viewHolder, Event model, int position) {

                final String post_key = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setLocation(model.getVenue());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setInterested(post_key);
                viewHolder.setStartDateTime(model.getStart_date_time());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleEventIntent = new Intent(MainActivity.this, EventSingleView.class);
                        singleEventIntent.putExtra("event_id", post_key);
                        startActivity(singleEventIntent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
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
                            mLikeUserDb=mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("Like");
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
        mEventList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                if (!response.getProviderType().equals("google.com") && !response.getProviderType().equals("facebook.com")) {
                    startActivity(new Intent(this, SetProfilePic.class));
                    finish();
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "cancelled", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    /*AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setMessage("Check your internet connection");
                    dialog.setTitle("No Internet Detected");
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    dialog.show();*/
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Unknown Error", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }


    public static class EventViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton mLikeBtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public EventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (ImageButton) mView.findViewById(R.id.like_btn);

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

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }

    private void logout() {

        AuthUI.getInstance().signOut(this);


    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
