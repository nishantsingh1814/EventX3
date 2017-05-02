package com.eventx.eventx;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.eventx.eventx.Fragments.EventAboutFragment;
import com.eventx.eventx.Fragments.EventCommentsFragment;
import com.eventx.eventx.Fragments.EventContactFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.fragment;
import static com.eventx.eventx.R.array.event_category;

public class EventView extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mTablayout;

    private ImageView mEventImage;

    private String event_image;

    private String mPostKey;
    private DatabaseReference mDatabaseEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        if (getIntent().getExtras().getString("event_id") != null) {
            mPostKey = getIntent().getExtras().getString("event_id");
        } else {
            Uri data = getIntent().getData();
            if (data.getQueryParameter("eventid") != null) {
                mPostKey = data.getQueryParameter("eventid");
            } else {
                startActivity(new Intent(EventView.this, MainActivity.class));
            }
        }
        mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("Event");
        mDatabaseEvents.keepSynced(true);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(
                R.id.collapse);
        collapsingToolbar.setTitleEnabled(false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEventImage = (ImageView) findViewById(R.id.event_image);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTablayout = (TabLayout) findViewById(R.id.tab_layout);

        mViewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));

        mTablayout.setupWithViewPager(mViewPager);
        mDatabaseEvents.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("start_date_time").getValue() != null) {
                    String event_name = (String) dataSnapshot.child("name").getValue();
                    setTitle("Event: " + event_name);

                    event_image = (String) dataSnapshot.child("image").getValue();

                    Picasso.with(EventView.this).load(event_image).into(mEventImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(EventView.this, ImageAcitivity.class);
                imageIntent.putExtra("image", event_image);
                if (Build.VERSION.SDK_INT >= 21) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(EventView.this, v, "trans");
                    startActivity(imageIntent, options.toBundle());
                } else {
                    startActivity(imageIntent);
                }
            }
        });
        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

            }
        });

    }

    private class PageAdapter extends FragmentPagerAdapter {

        private String[] fragments = {"About", "Contact", "Discussion"};

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    EventAboutFragment fragment = new EventAboutFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("post_key", mPostKey);
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    EventContactFragment fragment2 = new EventContactFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("post_key", mPostKey);
                    fragment2.setArguments(bundle2);
                    return fragment2;
                case 2:
                    EventCommentsFragment fragment3 = new EventCommentsFragment();
                    Bundle bundle3 = new Bundle();
                    bundle3.putString("post_key", mPostKey);
                    fragment3.setArguments(bundle3);
                    return fragment3;
            }
            EventAboutFragment fragment = new EventAboutFragment();
            Bundle bundle = new Bundle();
            bundle.putString("post_key", mPostKey);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }
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
        if (getIntent().getExtras().getString("event_id") == null) {
            startActivity(new Intent(EventView.this, MainActivity.class));
        }
        overridePendingTransition(R.anim.no_change, R.anim.slide_left);
    }
}
