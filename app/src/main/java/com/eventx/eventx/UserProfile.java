package com.eventx.eventx;

import android.*;
import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.Query;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import at.markushi.ui.CircleButton;

import static android.R.id.edit;


public class UserProfile extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private ImageView mProfilePhoto;
    private TextView mUserName;
    private TextView mUserEvents;

    private RecyclerView mUserEventList;

    private DatabaseReference mDatabaseEvent;
    private DatabaseReference mDatabaseCurrentUser;
    private StorageReference mStorage;
    private Uri mProfileImageUri = null;
    private Uri resultUri = null;
    private UploadTask uploadTask;
    private ProgressDialog mProgress;
    FirebaseUser user;

    Event temp;
    private Toolbar toolbar;
    private DatabaseReference mDatabaseLikeCurrentPost;


    private boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth mAuth;
    private Query mQuery;
    private DatabaseReference mLikeUserDb;
    private DatabaseReference mDatabaseUser;

    SharedPreferences sp;
    SharedPreferences.Editor edit;


    int callId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        sp = getSharedPreferences("EventX", MODE_PRIVATE);
        edit = sp.edit();
        callId = sp.getInt("callId", 1);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Your Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseEvent = FirebaseDatabase.getInstance().getReference().child("Event");


        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Event");
        mQuery = mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);



        mStorage = FirebaseStorage.getInstance().getReference().child("profile");

        mUserEventList = (RecyclerView) findViewById(R.id.user_Events);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mUserEventList.setLayoutManager(layoutManager);
        mUserEventList.setNestedScrollingEnabled(false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mProgress = new ProgressDialog(this);

        mUserEvents = (TextView) findViewById(R.id.your_events);
        mProfilePhoto = (ImageButton) findViewById(R.id.profile_photo_btn);
        mUserName = (TextView) findViewById(R.id.user_name);

        mDatabaseUser.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(user.getProviders().toString().equals("[phone]")){
                    mUserName.setText(dataSnapshot.child("username").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(!user.getProviders().toString().equals("[phone]")) {
            mUserName.setText(user.getDisplayName());
        }
        Log.i("helpo",user.getProviders().toString());
        if (user.getPhotoUrl() != null) {


            if(user.getProviders().toString().equals("[google.com]")){
                Picasso.with(UserProfile.this).load(user.getPhotoUrl().toString().replace("/s96-c/","/s300-c/")).into(mProfilePhoto);
            }
            else{
                String facebookUserId = "";
                for(UserInfo profile : user.getProviderData()) {
                    if(profile.getProviderId().equals("facebook.com")) {
                        facebookUserId = profile.getUid();
                    }
                }

                String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                Picasso.with(UserProfile.this).load(photoUrl).into(mProfilePhoto);

            }

            mProfilePhoto.setEnabled(false);
        } else {
            mDatabaseUser.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("profile_image").getValue() == null) {
                    } else {
                        Picasso.with(UserProfile.this).load((String) dataSnapshot.child("profile_image").getValue()).into(mProfilePhoto);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        mProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(UserProfile.this);
                dialog.setMessage("Change profile pic?");
                dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, GALLERY_REQUEST);
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
        setUpViews();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void setUpViews() {
        FirebaseRecyclerAdapter<Event, UserEventViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, UserEventViewHolder>(
                Event.class, R.layout.event_row, UserEventViewHolder.class, mQuery
        ) {
            @Override
            protected void populateViewHolder(UserEventViewHolder viewHolder, final Event model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setLocation(model.getVenue());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setStartDateTime(model.getStart_date_time());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);
                viewHolder.setInterested(post_key);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleEventIntent = new Intent(UserProfile.this, EventView.class);
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
                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        mProcessLike = true;

                        mDatabaseLike =FirebaseDatabase.getInstance().getReference().child("Like");
                        mDatabaseLikeCurrentPost=mDatabaseLike.child(post_key);
                        mDatabaseLike.keepSynced(true);
                        mDatabaseLikeCurrentPost.keepSynced(true);
                        mLikeUserDb=mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("Like");

                        mDatabaseLikeCurrentPost.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                        mLikeUserDb.child(post_key).removeValue();
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        Snackbar.make(v, "Removed from your WishList", Snackbar.LENGTH_LONG).show();
                                        mProcessLike = false;
                                        if (ActivityCompat.checkSelfPermission(UserProfile.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
                                            return;
                                        }
                                        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, sp.getLong(model.getName(), -1));
                                        int rows = getContentResolver().delete(deleteUri, null, null);
                                    } else {
                                        mLikeUserDb.child(post_key).setValue(post_key);
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                        mProcessLike = false;
                                        Snackbar.make(v, "Added to your WishList", Snackbar.LENGTH_LONG).show();
                                        if (ActivityCompat.checkSelfPermission(UserProfile.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                            String[] permissions = {android.Manifest.permission.WRITE_CALENDAR};
                                            ActivityCompat.requestPermissions(UserProfile.this, permissions, 11);
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
        mUserEventList.setAdapter(firebaseRecyclerAdapter);

        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){
                    TextView noEvents=(TextView)findViewById(R.id.no_item);
                    TextView yourEventsTv=(TextView)findViewById(R.id.your_events);
                    yourEventsTv.setVisibility(View.GONE);
                    noEvents.setVisibility(View.VISIBLE);
                }else{
                    TextView noEvents=(TextView)findViewById(R.id.no_item);
                    noEvents.setVisibility(View.GONE);
                    TextView yourEventsTv=(TextView)findViewById(R.id.your_events);
                    yourEventsTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static class UserEventViewHolder extends RecyclerView.ViewHolder {
        View mView;
        CircleButton mLikeButton;
        CircleButton mShareBtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public UserEventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mShareBtn = (CircleButton) mView.findViewById(R.id.share_btn);
            mLikeButton = (CircleButton) mView.findViewById(R.id.like_btn);
            mAuth = FirebaseAuth.getInstance();
        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like").child(post_key);
            mDatabaseLike.keepSynced(true);
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                        mLikeButton.setColor(Color.WHITE);
                        mLikeButton.setImageResource(R.drawable.thumb);
                    } else {
                        mLikeButton.setColor(Color.parseColor("#F1643B"));
                        mLikeButton.setImageResource(R.drawable.ic_thumbs_up_hand_symbol);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mProfileImageUri = data.getData();

            CropImage.activity(mProfileImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                mProgress.setMessage("uploading...");
                mProgress.show();
                mProfilePhoto.setImageURI(resultUri);

                StorageReference filePath = mStorage.child(resultUri.getLastPathSegment());
                uploadTask = filePath.putFile(resultUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                        DatabaseReference current_user_db = mDatabaseUser.child(user.getUid());
                        current_user_db.child("profile_image").setValue(downloadUrl);
                        mProgress.dismiss();

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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

                    edit.putLong(temp.getName(),eventID);
                    edit.commit();
                }

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
