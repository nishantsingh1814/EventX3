package com.eventx.eventx.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.eventx.eventx.MainActivity;
import com.eventx.eventx.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.twitter.sdk.android.core.TwitterCore.TAG;

/**
 * Created by Nishant on 4/28/2017.
 */

public class EventContactFragment extends Fragment {

    TextView mPhoneTv;
    TextView mEmailTv;
    ImageButton mCallBtn;
    ImageButton mEmailBtn;
    private AdView mAdView;


    TextView mHostName;
    DatabaseReference mDatabaseEvents;
    private String mPostKey;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.event_contact_frag,container,false);
        mAdView = (AdView) v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mPostKey = bundle.getString("post_key");
        }
        mHostName=(TextView)v.findViewById(R.id.host_name);
        mPhoneTv=(TextView)v.findViewById(R.id.event_phone);
        mEmailBtn=(ImageButton)v.findViewById(R.id.email_button);
        mEmailTv=(TextView)v.findViewById(R.id.event_email);
        mCallBtn=(ImageButton)v.findViewById(R.id.call_button);

        mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("Event");
        mDatabaseEvents.keepSynced(true);

        mDatabaseEvents.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String phone=(String) dataSnapshot.child("phone").getValue();
                String host=(String)dataSnapshot.child("h_name").getValue();
                String email=(String) dataSnapshot.child("email").getValue();
                if(host==null){
                    mHostName.setText("");
                }else{
                    mHostName.setText(host);
                }
                if(email==null){
                    mEmailTv.setText("");
                }else {
                    mEmailTv.setText(email);
                }
                if(phone==null||phone.equals("")){
                    mPhoneTv.setText("-");
                }

                else{
                    mPhoneTv.setText(phone);
                }
                mCallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mPhoneTv.getText().equals("-")){
                            Snackbar.make(v,"No Phone Number Available",Snackbar.LENGTH_SHORT).show();
                        }else{
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                String[] permissions = {Manifest.permission.CALL_PHONE};
                                requestPermissions(permissions,1);

                                return;
                            }
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_CALL);
                            Uri uri = Uri.parse("tel:"+mPhoneTv.getText());
                            intent.setData(uri);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(getContext(),"No suitable Apps Found",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                mEmailBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SENDTO);

                        Uri uri = Uri.parse("mailto:"+mEmailTv.getText());
                        intent.setData(uri);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(getContext(),"No suitable Apps Found",Toast.LENGTH_SHORT);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    Uri uri = Uri.parse("tel:"+mPhoneTv.getText());
                    intent.setData(uri);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getContext(),"No suitable Apps Found",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.i("asdf", "onRecquestPermissionsResult: "+"j");
                    return;
                }
        }
    }
}
