package com.eventx.eventx;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetProfilePic extends AppCompatActivity {


    private static final int GALLERY_REQUEST = 1;
    private ImageButton mProfileImageBtn;
    private TextView mAddImage;
    private TextView mSkipBtn;
    private Uri mProfileImageUri = null;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;

    private UploadTask uploadTask;

    private ProgressDialog mProgress;
    private Uri resultUri;
    private String user_id;

    private EditText mUserNameEt;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_pic);

        sp=getSharedPreferences("EventX",MODE_PRIVATE);
        editor=sp.edit();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("profile");
        mUserNameEt=(EditText)findViewById(R.id.user_name_et);
        mProgress = new ProgressDialog(this);

        mProfileImageBtn = (ImageButton) findViewById(R.id.sign_up_profile_pic);
        mSkipBtn = (Button) findViewById(R.id.skip_profile_pic);
        mAddImage = (TextView) findViewById(R.id.tv_add_image);
        user_id = mAuth.getCurrentUser().getUid();


        mDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    startActivity(new Intent(SetProfilePic.this,MainActivity.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(mAuth.getCurrentUser().getProviders().toString().equals("[phone]")){
            mUserNameEt.setVisibility(View.VISIBLE);
        }



        mProfileImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setMessage("Uploading profile pic");

                if (resultUri != null) {
                    mProgress.show();
                    if(mAuth.getCurrentUser().getProviders().toString().equals("[phone]")){
                        String userName=mUserNameEt.getText().toString();
                        if(!TextUtils.isEmpty(userName)) {
                            DatabaseReference current_user_db = mDatabase.child(user_id);
                            current_user_db.child("username").setValue(userName);
                        }else{
                            Snackbar.make(v,"Enter your name",Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    StorageReference filePath = mStorage.child(resultUri.getLastPathSegment());
                    uploadTask = filePath.putFile(resultUri);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                            DatabaseReference current_user_db = mDatabase.child(user_id);
                            current_user_db.child("profile_image").setValue(downloadUrl);
                            mProgress.dismiss();
                            Intent mainIntent = new Intent(SetProfilePic.this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                } else {
                    if(mAuth.getCurrentUser().getProviders().toString().equals("[phone]")){
                        String userName=mUserNameEt.getText().toString();
                        if(!TextUtils.isEmpty(userName)) {
                            DatabaseReference current_user_db = mDatabase.child(user_id);
                            current_user_db.child("username").setValue(userName);
                        }else{
                            Snackbar.make(v,"Enter your name",Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Intent mainIntent = new Intent(SetProfilePic.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mProfileImageUri = data.getData();

            CropImage.activity(mProfileImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this);
            mSkipBtn.setText("Done");
            mAddImage.setVisibility(View.INVISIBLE);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                mProfileImageBtn.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}

