package com.eventx.eventx.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eventx.eventx.Comment;
import com.eventx.eventx.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Nishant on 4/28/2017.
 */

public class EventCommentsFragment extends Fragment {

    private String mPostKey;

    private RecyclerView mCommentList;
    private Button mAddComment;
    private EditText mCommentBox;

    private boolean commentsOpened;

    private TextView mNoComments;
    private Query query;
    private LinearLayoutManager layoutManager;

    private DatabaseReference mCommentReference;

    private AdView mAdView;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseNotification;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseEvents;

    private String postUser;
    private String eventName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.event_comment_frag, container, false);
//        mAdView = (AdView) v.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mPostKey = bundle.getString("post_key");
        }
        mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("Event");
        mDatabaseEvents.keepSynced(true);
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notification");
        mDatabaseNotification.keepSynced(true);
        mCommentReference = FirebaseDatabase.getInstance().getReference().child("Comment");
        mCommentReference.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mDatabaseEvents.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postUser = (String) dataSnapshot.child("uid").getValue();
                eventName = (String) dataSnapshot.child("name").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference mCommentEventRef = mCommentReference.child(mPostKey);
        query = mCommentEventRef.orderByChild("time");
        mAuth = FirebaseAuth.getInstance();

        mCommentBox = (EditText) v.findViewById(R.id.comment_box);
        mCommentList = (RecyclerView) v.findViewById(R.id.comment_list);

        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mCommentList.setLayoutManager(layoutManager);

        mNoComments = (TextView) v.findViewById(R.id.no_comments);

        mAddComment = (Button) v.findViewById(R.id.add_comment);


        mCommentList.setNestedScrollingEnabled(false);
        mAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = mCommentBox.getText().toString().trim();
                if (!TextUtils.isEmpty(comment)) {
                    mNoComments.setVisibility(View.GONE);


                    if (!mAuth.getCurrentUser().getUid().equals(postUser)) {
                        DatabaseReference notificationUser = mDatabaseNotification.child(postUser).child(mAuth.getCurrentUser().getUid());
                        notificationUser.child("e_id").setValue(mPostKey);
                        notificationUser.child("e_name").setValue(eventName);
                        notificationUser.child("u_name").setValue(mAuth.getCurrentUser().getDisplayName());
                    }


                    final DatabaseReference newCommentPost = mCommentReference.child(mPostKey).push();
                    newCommentPost.child("user_id").setValue(mAuth.getCurrentUser().getUid());
                    newCommentPost.child("comment").setValue(comment);
                    newCommentPost.child("time").setValue(System.currentTimeMillis());
                    newCommentPost.child("username").setValue(mAuth.getCurrentUser().getDisplayName());
                    if (mAuth.getCurrentUser().getPhotoUrl() != null) {
                        newCommentPost.child("profile_pic").setValue(mAuth.getCurrentUser().getPhotoUrl().toString());
                    } else {

                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("profile_image").getValue() == null) {
                                } else {
                                    newCommentPost.child("profile_pic").setValue((String) dataSnapshot.child("profile_image").getValue());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    mCommentBox.setText("");
                } else {
                    Snackbar.make(v, "Comment cannot be empty", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        setUpComments();
        return v;
    }

    private void setUpComments() {


        FirebaseRecyclerAdapter<Comment, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(Comment.class, R.layout.comment_item, CommentViewHolder.class, query) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {

                final String comment_key = getRef(position).getKey();
                if (mAuth.getCurrentUser().getUid().equals(model.getUser_id())) {
                    viewHolder.mDeleteBtn.setVisibility(View.VISIBLE);
                    viewHolder.mEditBtn.setVisibility(View.VISIBLE);
                }
                viewHolder.mEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        View dialogVIew = LayoutInflater.from(getContext()).inflate(R.layout.edit_comment_dialog, null);
                        dialog.setView(dialogVIew);
                        dialog.setTitle("Edit Comment");
                        final EditText editText = (EditText) dialogVIew.findViewById(R.id.edit_comment_ed);

                        mCommentReference.child(mPostKey).child(comment_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("comment").getValue()!= null)
                                    editText.setText(dataSnapshot.child("comment").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (editText.getText().toString().length() == 0) {
                                    Snackbar.make(v, "Comment Can't be empty", Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                mCommentReference.child(mPostKey).child(comment_key).child("comment").setValue(editText.getText().toString().trim());
                            }
                        });
                        dialog.show();


                    }
                });
                viewHolder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle("Delete Comment");
                        dialog.setMessage("Are you sure?");
                        dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mDatabaseNotification != null && mDatabaseNotification.child(postUser) != null && mDatabaseNotification.child(postUser).child(mAuth.getCurrentUser().getUid()) != null) {
                                    mDatabaseNotification.child(postUser).child(mAuth.getCurrentUser().getUid()).removeValue();
                                }
                                mCommentReference.child(mPostKey).child(comment_key).removeValue();
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
                viewHolder.setComment(model.getComment());
                viewHolder.setCommentTime(model.getTime());
                viewHolder.setProfileName(model.getUsername());
                viewHolder.setProfilePic(model.getProfile_pic(), getContext());
            }
        };

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    mNoComments.setVisibility(View.VISIBLE);
                } else {
                    mNoComments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mCommentList.setVisibility(View.VISIBLE);
        mCommentList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;
        Button mEditBtn;
        Button mDeleteBtn;
        FirebaseAuth mAuth;


        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mEditBtn = (Button) itemView.findViewById(R.id.edit_comment);
            mDeleteBtn = (Button) itemView.findViewById(R.id.delete_comment);
            mAuth = FirebaseAuth.getInstance();
        }

        public void setProfilePic(String profilePicUrl, Context context) {
            CircleImageView profilePic;
            profilePic = (CircleImageView) mView.findViewById(R.id.profile_image);
            Picasso.with(context).load(profilePicUrl).into(profilePic);
        }

        public void setComment(String commentString) {
            TextView comment;

            comment = (TextView) mView.findViewById(R.id.comment_tv);
            comment.setText(commentString);
        }

        public void setProfileName(String profileNameString) {
            TextView profileName;

            profileName = (TextView) mView.findViewById(R.id.profile_name);
            profileName.setText(profileNameString);
        }

        public void setCommentTime(long commentTimeLong) {
            TextView commentTime;
            Date startDate = new Date(commentTimeLong);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
            String startdateString = formatter.format(startDate);
            formatter = new SimpleDateFormat("hh:mm:a");
            String starttimeString = formatter.format(startDate);


            commentTime = (TextView) mView.findViewById(R.id.comment_time);

            commentTime.setText(starttimeString + "," + startdateString);
        }
    }
}
