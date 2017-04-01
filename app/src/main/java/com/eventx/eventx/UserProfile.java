package com.eventx.eventx;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import retrofit2.http.Query;

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

    private Toolbar toolbar;
    private DatabaseReference mDatabaseLikeCurrentPost;


    private boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth mAuth;
    private com.google.firebase.database.Query mQuery;
    private DatabaseReference mLikeUserDb;
    private DatabaseReference mDatabaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Your Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseEvent = FirebaseDatabase.getInstance().getReference().child("Event");


        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Event");
        Log.i("userhello", "onCreate: "+currentUserId);
        mQuery = mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);

        mStorage = FirebaseStorage.getInstance().getReference().child("profile");

        mUserEventList = (RecyclerView) findViewById(R.id.user_Events);
        mUserEventList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mUserEventList.setLayoutManager(layoutManager);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mProgress = new ProgressDialog(this);

        mUserEvents = (TextView) findViewById(R.id.your_events);
        mProfilePhoto = (ImageButton) findViewById(R.id.profile_photo_btn);
        mUserName = (TextView) findViewById(R.id.user_name);


        mUserName.setText(user.getDisplayName());


        if (user.getPhotoUrl() != null) {
            Picasso.with(UserProfile.this).load(user.getPhotoUrl()).into(mProfilePhoto);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpViews();

    }

    private void setUpViews() {
        FirebaseRecyclerAdapter<Event, UserEventViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, UserEventViewHolder>(
                Event.class, R.layout.event_row, UserEventViewHolder.class, mQuery
        ) {
            @Override
            protected void populateViewHolder(UserEventViewHolder viewHolder, Event model, int position) {
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
                        Intent singleEventIntent = new Intent(UserProfile.this, EventSingleView.class);
                        singleEventIntent.putExtra("event_id", post_key);
                        startActivity(singleEventIntent, ActivityOptions.makeSceneTransitionAnimation(UserProfile.this).toBundle());
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
        mUserEventList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserEventViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton mLikeButton;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public UserEventViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeButton = (ImageButton) mView.findViewById(R.id.like_btn);
            mAuth = FirebaseAuth.getInstance();
        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like").child(post_key);
            mDatabaseLike.keepSynced(true);
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                        mLikeButton.setColorFilter(Color.parseColor("#607D8B"));
                    } else {
                        mLikeButton.setColorFilter(Color.parseColor("#e6e6e6"));
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
}
