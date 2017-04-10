package com.eventx.eventx;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static android.R.attr.data;
import static android.graphics.Color.BLACK;


public class PostEventActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST = 1;
    private static final int MAX_LENGTH = 30;

    private ImageButton mSelectPostImage;
    private EditText mPostEventName;
    private EditText mPostEventVenue;
    private Spinner mPostEventCategory;
    private EditText mPostEventDescription;
    private Button mPostEventStartDateBtn;
    private Button mPostEventStartTimeBtn;
    private Button mPostEventEndDateBtn;
    private Button mPostEventEndTimeBtn;
    private Spinner mPostEventState;

    private Button mPostEventBtn;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener startDate;
    private DatePickerDialog.OnDateSetListener endDate;
    private TimePickerDialog.OnTimeSetListener startTime;
    private TimePickerDialog.OnTimeSetListener endTime;

    private ProgressDialog mProgress;


    private Uri eventImageUri = null;

    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_event);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Event");
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());


        mSelectPostImage = (ImageButton) findViewById(R.id.post_event_image);
        mPostEventName = (EditText) findViewById(R.id.post_event_name);
        mPostEventVenue = (EditText) findViewById(R.id.post_event_venue);
        mPostEventCategory = (Spinner) findViewById(R.id.post_event_category);
        mPostEventDescription = (EditText) findViewById(R.id.post_event_description);
        mPostEventStartDateBtn = (Button) findViewById(R.id.post_event_start_date_btn);
        mPostEventStartTimeBtn = (Button) findViewById(R.id.post_event_start_time_btn);
        mPostEventEndDateBtn = (Button) findViewById(R.id.post_event_end_date_btn);
        mPostEventEndTimeBtn = (Button) findViewById(R.id.post_event_end_time_btn);
        mPostEventState=(Spinner)findViewById(R.id.post_event_state);
        mPostEventBtn = (Button) findViewById(R.id.post_event_btn);
        mProgress = new ProgressDialog(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mPostEventStartDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PostEventActivity.this, startDate, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mPostEventStartTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(PostEventActivity.this, startTime, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), false).show();
            }
        });
        mPostEventEndDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PostEventActivity.this, endDate, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mPostEventEndTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(PostEventActivity.this, endTime, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false).show();
            }
        });

        startDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, monthOfYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                mPostEventStartDateBtn.setText(sdf.format(startCalendar.getTime()));
                mPostEventStartDateBtn.setTextColor(BLACK);
            }
        };
        startTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startCalendar.set(Calendar.MINUTE, minute);

                String timeSet = "";
                if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    timeSet = "PM";
                } else if (hourOfDay == 0) {
                    hourOfDay += 12;
                    timeSet = "AM";
                } else if (hourOfDay == 12)
                    timeSet = "PM";
                else
                    timeSet = "AM";

                String hour = "";
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else {
                    hour = "" + hourOfDay;
                }
                String minutes = "";
                if (minute < 10)
                    minutes = "0" + minute;
                else {
                    minutes = "" + minute;
                }
                mPostEventStartTimeBtn.setTextColor(BLACK);
                mPostEventStartTimeBtn.setText(hour + ":" + minutes + " " + timeSet);
            }
        };
        endDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, monthOfYear);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                mPostEventEndDateBtn.setTextColor(BLACK);
                mPostEventEndDateBtn.setText(sdf.format(endCalendar.getTime()));
            }
        };
        endTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endCalendar.set(Calendar.MINUTE, minute);

                String timeSet = "";
                if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    timeSet = "PM";
                } else if (hourOfDay == 0) {
                    hourOfDay += 12;
                    timeSet = "AM";
                } else if (hourOfDay == 12)
                    timeSet = "PM";
                else
                    timeSet = "AM";

                String hour = "";
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else {
                    hour = "" + hourOfDay;
                }
                String minutes = "";
                if (minute < 10)
                    minutes = "0" + minute;
                else {
                    minutes = "" + minute;
                }

                mPostEventEndTimeBtn.setTextColor(BLACK);
                mPostEventEndTimeBtn.setText(hour + ":" + minutes + " " + timeSet);
            }
        };

        mPostEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });


        mSelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("Posting your Event ");

        final String mEventName = mPostEventName.getText().toString().trim();
        final String mEventVenue = mPostEventVenue.getText().toString().trim();
        final String mEventCategory = mPostEventCategory.getSelectedItem().toString();
        final String mEventDescription = mPostEventDescription.getText().toString().trim();
        final String mEventState=mPostEventState.getSelectedItem().toString();
        String mEventStartDate = mPostEventStartDateBtn.getText().toString();
        String mEventStartTime = mPostEventStartTimeBtn.getText().toString();
        String mEventEndDate = mPostEventEndDateBtn.getText().toString();
        String mEventEndTime = mPostEventEndTimeBtn.getText().toString();
        final long epochStart = startCalendar.getTimeInMillis();
        final long epochEnd = endCalendar.getTimeInMillis();

        if (!TextUtils.isEmpty(mEventName) && !TextUtils.isEmpty(mEventVenue) && !(mEventDescription.length() < 25) && !mEventStartDate.equals("Event Start Date") && !mEventStartTime.equals("Event Start Time") && !mEventEndDate.equals("Event End Date") && !mEventEndTime.equals("Event End Time") && !(epochEnd < epochStart)&&!(epochEnd<Calendar.getInstance().getTimeInMillis()) && eventImageUri != null) {
            mProgress.show();
            StorageReference filePath = mStorage.child("Event_images").child(randomString());
            filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    DatabaseReference newPost = mDatabase.push();
                    newPost.child("name").setValue(mEventName);
                    newPost.child("venue").setValue(mEventVenue);
                    newPost.child("category").setValue(mEventCategory);
                    newPost.child("description").setValue(mEventDescription);
                    newPost.child("start_date_time").setValue(epochStart);
                    newPost.child("end_date_time").setValue((epochEnd));
                    newPost.child("image").setValue(downloadUrl.toString());
                    newPost.child("uid").setValue(mCurrentUser.getUid());
                    newPost.child("state").setValue(mEventState);
                    newPost.child("state_category").setValue(mEventState+"_"+mEventCategory);


                    mProgress.dismiss();
                    Intent mainIntent = new Intent(PostEventActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgress.dismiss();
                    Toast.makeText(PostEventActivity.this, "Error while posting", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (TextUtils.isEmpty(mEventName)) {
                Toast.makeText(PostEventActivity.this, "Enter Event Name", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(mEventVenue)) {
                Toast.makeText(PostEventActivity.this, "Enter Event Venue", Toast.LENGTH_SHORT).show();
            } else if (mEventDescription.length() < 25) {
                Toast.makeText(PostEventActivity.this, "Event Description should be of at least 25 letters", Toast.LENGTH_SHORT).show();
            } else if (mEventStartDate.equals("Event Start Date")) {
                Toast.makeText(PostEventActivity.this, "Enter event Start Date", Toast.LENGTH_SHORT).show();
            } else if (mEventStartTime.equals("Event Start Time")) {
                Toast.makeText(PostEventActivity.this, "Enter event Start Time", Toast.LENGTH_SHORT).show();
            } else if (mEventEndDate.equals("Event End Date")) {
                Toast.makeText(PostEventActivity.this, "Enter event End Date", Toast.LENGTH_SHORT).show();
            } else if (mEventEndTime.equals("Event End Time")) {
                Toast.makeText(PostEventActivity.this, "Enter event End Time", Toast.LENGTH_SHORT).show();
            } else if (epochEnd < epochStart) {
                Toast.makeText(PostEventActivity.this, "Event end date & time should be after start date & time", Toast.LENGTH_SHORT).show();
            } else if (eventImageUri == null) {
                Toast.makeText(PostEventActivity.this, "Select Event Image", Toast.LENGTH_SHORT).show();
            }else if((epochEnd<Calendar.getInstance().getTimeInMillis())){
                Toast.makeText(PostEventActivity.this, "Please select future time", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            eventImageUri = data.getData();

            CropImage.activity(eventImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(2, 1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                mSelectPostImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
