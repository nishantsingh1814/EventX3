package com.eventx.eventx;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class NotificationActiviity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView notificationList;
    DatabaseReference mNotificationRef;

    Query query;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_activiity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");
        mNotificationRef = FirebaseDatabase.getInstance().getReference().child("Notification").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notificationList = (RecyclerView) findViewById(R.id.notifications);
        query = mNotificationRef;

        layoutManager = new LinearLayoutManager(NotificationActiviity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notificationList.setLayoutManager(layoutManager);

        setUpNotifications();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpNotifications() {

        FirebaseRecyclerAdapter<Notification, NotificationViewHolder> adapter = new FirebaseRecyclerAdapter<Notification, NotificationViewHolder>(Notification.class, R.layout.notification_item, NotificationViewHolder.class, query) {
            @Override
            protected void populateViewHolder(NotificationViewHolder viewHolder, final Notification model, int position) {

                final String commmenterID=getRef(position).getKey();
                viewHolder.setNotificationText(model.getU_name() + " commented on your " + model.getE_name() + " Event");
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNotificationRef.child(commmenterID).removeValue();
                        Intent singleEventIntent = new Intent(NotificationActiviity.this, EventView.class);
                        singleEventIntent.putExtra("event_id", model.getE_id());
                        startActivity(singleEventIntent);
                        overridePendingTransition(R.anim.slide_right, R.anim.no_change);
                    }
                });
            }
        };

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
        notificationList.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpNotifications();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNotificationText(String notificationText) {
            TextView notification = (TextView) mView.findViewById(R.id.notification_item);
            notification.setText(notificationText);
        }
    }
}
