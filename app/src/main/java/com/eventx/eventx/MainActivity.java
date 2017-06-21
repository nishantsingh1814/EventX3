package com.eventx.eventx;

import android.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;




import android.os.SystemClock;
import android.provider.CalendarContract;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
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

import at.markushi.ui.CircleButton;
import retrofit2.Call;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityhello";
    private static final int RC_SIGN_IN = 1;
    private Toolbar toolbar;
    private FloatingActionButton mAddFab;


    private ProgressBar mProgressBar;

    private RecyclerView mEventList;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabaseEvents;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfilePhotosStorageReference;
    private DatabaseReference mNotificationRef;

    private DatabaseReference mLikeUserDb;
    DrawerLayout mDrawerLayout;
    private Spinner mCategorySpinner;

    private Spinner mLocationSpinner;

    private String mLocation = "Location";
    private String mCategory = "Category";
    private Query query;
    private DatabaseReference mDatabaseLikeCurrentPost;


    private TextView mNotificationCount;
    private boolean mProcessLike = false;


    private ArrayAdapter<String> locationAdapter;
    private ArrayAdapter<String> categoryAdapter;
    NavigationView mNavigation;

    RecyclerView apiEventList;
    private LinearLayoutManager layoutManager;

    private static int firstVisibleInListview;


    SharedPreferences sp;
    SharedPreferences.Editor edit;


    private int check = 0;

    private SwipeRefreshLayout mRefreshLayout;

    Event temp;
    int callId;
    private AdView mAdView;

    boolean firstOpen;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        sp = getSharedPreferences("EventX", MODE_PRIVATE);
        edit = sp.edit();
        firstOpen = sp.getBoolean("firstOpen", false);
        if (!firstOpen) {
            setChronJob();
            edit.putBoolean("firstOpen", true);
            edit.apply();
        }

        callId = sp.getInt("callId", 1);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();


        mFirebaseStorage = FirebaseStorage.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");




        mDatabaseEvents = mFirebaseDatabase.getReference().child("Event");

        query = mDatabaseEvents.orderByChild("start_date_time");

        mProfilePhotosStorageReference = mFirebaseStorage.getReference().child("profile");

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");

        mDatabaseEvents.keepSynced(true);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mCategorySpinner = (Spinner) findViewById(R.id.post_event_category);
        mLocationSpinner = (Spinner) findViewById(R.id.post_event_state);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        mNavigation = (NavigationView) findViewById(R.id.nav_view);

        ArrayList<String> list = new ArrayList<>();
        list.add("Category");
        list.add("Technology");
        list.add("Workshop");
        list.add("Sports");
        list.add("Art and Culture");
        list.add("Health");
        list.add("Other");

        categoryAdapter = new ArrayAdapter<String>(this, R.layout.layout_drop_title, list);
        categoryAdapter.setDropDownViewResource(R.layout.layout_drop_list);

        mCategorySpinner.setAdapter(categoryAdapter);

        ArrayList<String> stateList = new ArrayList<>();
        stateList.add("Location");
        stateList.add("Andhra Pradesh");
        stateList.add("Arunachal Pradesh");
        stateList.add("Assam");
        stateList.add("Bihar");
        stateList.add("Chhattisgarh");
        stateList.add("Delhi");
        stateList.add("Goa");
        stateList.add("Gujarat");
        stateList.add("Haryana");
        stateList.add("Himachal Pradesh");
        stateList.add("Jammu & Kashmir");
        stateList.add("Jharkhand");
        stateList.add("Karnataka");
        stateList.add("Kerala");
        stateList.add("Madhya Pradesh");
        stateList.add("Maharashtra");
        stateList.add("Manipur");
        stateList.add("Meghalaya");
        stateList.add("Mizoram");
        stateList.add("Nagaland");
        stateList.add("Odisha");
        stateList.add("Punjab");
        stateList.add("Rajasthan");
        stateList.add("Sikkim");
        stateList.add("Tamil Nadu");
        stateList.add("Telangana");
        stateList.add("Tripura");
        stateList.add("Uttarakhand");
        stateList.add("Uttar Pradesh");
        stateList.add("West Bengal");


        mLocationSpinner.getBackground().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        mCategorySpinner.getBackground().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        locationAdapter = new ArrayAdapter<>(this, R.layout.layout_drop_title, stateList);
        locationAdapter.setDropDownViewResource(R.layout.layout_drop_list);

        mLocationSpinner.setAdapter(locationAdapter);


        mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (check++ > 1) {
                    mLocation = mLocationSpinner.getSelectedItem().toString();
                    if (mLocation.equals("Location")) {
                        if (mCategory.equals("Category")) {
                            query = mDatabaseEvents.orderByChild("start_date_time");
                            setUpViews();
                            return;
                        }
                        query = mDatabaseEvents.orderByChild("category").equalTo(mCategory);
                        setUpViews();
                        return;
                    }
                    if (mCategory.equals("Category")) {
                        query = mDatabaseEvents.orderByChild("state").equalTo(mLocation);
                    } else {
                        query = mDatabaseEvents.orderByChild("state_category").equalTo(mLocation + "_" + mCategory);
                    }
                    setUpViews();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (check++ > 1) {
                    mCategory = mCategorySpinner.getSelectedItem().toString();

                    if (mCategory.equals("Category")) {
                        if (mLocation.equals("Location")) {
                            query = mDatabaseEvents.orderByChild("start_date_time");
                            setUpViews();
                            return;
                        }
                        query = mDatabaseEvents.orderByChild("state").equalTo(mLocation);
                        setUpViews();
                        return;
                    }
                    if (mLocation.equals("Location")) {
                        query = mDatabaseEvents.orderByChild("category").equalTo(mCategory);
                    } else {
                        query = mDatabaseEvents.orderByChild("state_category").equalTo(mLocation + "_" + mCategory);
                    }

                    setUpViews();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                if (item.getItemId() == R.id.nav_profile) {

                    Intent userProfile = new Intent(MainActivity.this, UserProfile.class);
                    startActivity(userProfile);
                    overridePendingTransition(R.anim.slide_right, R.anim.no_change);
                }
                if (item.getItemId() == R.id.nav_create_event) {

                    startActivity(new Intent(MainActivity.this, PostEventActivity.class));
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);

                }
                if (item.getItemId() == R.id.nav_share) {

                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");

                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download EventX");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, "Download EventX App " + "\n" + "https://jq49b.app.goo.gl/eNh4");
                    startActivity(Intent.createChooser(sharingIntent, "Choose App"));
                    overridePendingTransition(R.anim.slide_right, R.anim.no_change);


                }
                if (item.getItemId() == R.id.nav_logout) {
                    logout();
                }
                if (item.getItemId() == R.id.nav_wish_list) {

                    startActivity(new Intent(MainActivity.this, WishlistActivity.class));
                    overridePendingTransition(R.anim.slide_right, R.anim.no_change);

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
                if (item.getItemId() == R.id.nav_Rate_Us) {


                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                }
                if (item.getItemId() == R.id.nav_notification) {
                    Intent notificationIntent = new Intent(MainActivity.this, NotificationActiviity.class);
                    startActivity(notificationIntent);
                }
                return true;
            }
        });

        mEventList = (RecyclerView) findViewById(R.id.event_list);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mEventList.setLayoutManager(layoutManager);
        mEventList.setHasFixedSize(true);
//        apiEventList = (RecyclerView) findViewById(R.id.api_event_list);
//
//
//        apiEventList.setLayoutManager(new LinearLayoutManager(this));
//        apiEventList.setHasFixedSize(true);
//        apiAdapter=new EventsAdapter(mEventModel,this);
//
//        apiEventList.setAdapter(apiAdapter);
//
//        apiEventList.setNestedScrollingEnabled(false);
//        mEventList.setNestedScrollingEnabled(false);

        mEventList.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0)
                    mAddFab.hide();
                else if (dy < 0)
                    mAddFab.show();
            }
        });


        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);


        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddFab = (FloatingActionButton) findViewById(R.id.add_event_fab);
        mAddFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostEventActivity.class));
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);

            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.GreenTheme)
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
                                    ))
                                    .build(), RC_SIGN_IN);
                }
            }
        };

        mRefreshLayout.setColorSchemeColors(Color.parseColor("#00ADB5"));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpViews();
                mRefreshLayout.setRefreshing(false);
            }
        });

        setNotificaionCount();
        setUpViews();
        if(mAuth.getCurrentUser()!=null) {
            FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
        }
    }

    private void setNotificaionCount() {
        if(mAuth.getCurrentUser()!=null) {

            mNotificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");
            if(mNotificationRef!=null&&mNotificationRef.child(mAuth.getCurrentUser().getUid())!=null) {

                mNotificationRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            mNotificationCount = (TextView) findViewById(R.id.notification_count);

                            mNotificationCount.setVisibility(View.VISIBLE);
                            mNotificationCount.setText("You Have "+dataSnapshot.getChildrenCount()+" new Notifications");
                        }else{
                            mNotificationCount = (TextView) findViewById(R.id.notification_count);

                            mNotificationCount.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

    }

    public void setChronJob() {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, DeletePastEventsReciever.class);
        PendingIntent operation = PendingIntent.getBroadcast(MainActivity.this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3 * 60 * 1000, 24 * 60 * 60 * 1000, operation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search for Events..");


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                searchIntent.putExtra("searchString", queryText);
                startActivity(searchIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                        Intent singleEventIntent = new Intent(MainActivity.this, EventView.class);
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
                            startActivity(Intent.createChooser(sharingIntent, "Select App"));
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

                                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

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

                                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                            String[] permissions = {Manifest.permission.WRITE_CALENDAR};
                                            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);

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
        mEventList.setAdapter(firebaseRecyclerAdapter);


//        ApiInterface apiInterface= ApiClient.getApiInterface();
//        Call<Result> resultCall=apiInterface.getDelhiEvents();
//        resultCall.enqueue(new retrofit2.Callback<Result>() {
//            @Override
//            public void onResponse(Call<Result> call, Response<Result> response) {
//                if(response.isSuccessful()){
//                    mEventModel.addAll(response.body().getEvents());
//
//                    apiAdapter.notifyDataSetChanged();
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Result> call, Throwable t) {
//
//            }
//        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    TextView noEvents = (TextView) findViewById(R.id.no_item);
                    noEvents.setVisibility(View.VISIBLE);
                } else {
                    TextView noEvents = (TextView) findViewById(R.id.no_item);
                    noEvents.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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


        mNavigation.getMenu().findItem(R.id.nav_contact).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_create_event).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_feedback).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_logout).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_profile).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_Rate_Us).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_wish_list).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_share).setChecked(false);
        mNavigation.getMenu().findItem(R.id.nav_notification).setChecked(false);


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

        CircleButton mLikeBtn;
        CircleButton mShareBtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public EventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (CircleButton) mView.findViewById(R.id.like_btn);
            mShareBtn = (CircleButton) mView.findViewById(R.id.share_btn);
            mAuth = FirebaseAuth.getInstance();

        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like").child(post_key);
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (mAuth.getCurrentUser() != null) {
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


    private void logout() {

        AuthUI.getInstance().signOut(this);


    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ContentResolver cr = getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DTSTART, temp.getStart_date_time());
                    values.put(CalendarContract.Events.DTEND, temp.getEnd_date_time());
                    values.put(CalendarContract.Events.TITLE, temp.getName());
                    values.put(CalendarContract.Events.DESCRIPTION, temp.getDescription());
                    values.put(CalendarContract.Events.CALENDAR_ID, callId++);
                    edit.putInt("callId", callId);

                    values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    edit.putLong(temp.getName(), eventID);
                    edit.commit();
                }
        }
    }


}
