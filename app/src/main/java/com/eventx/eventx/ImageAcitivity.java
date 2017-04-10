package com.eventx.eventx;

import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageAcitivity extends AppCompatActivity {

    ImageView imageView;
    private ScaleGestureDetector SGD;
    private Matrix matrix;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_acitivity);

        String eventImg=getIntent().getStringExtra("image");
        imageView=(ImageView)findViewById(R.id.image);
        Picasso.with(this).load(eventImg).into(imageView);
        PhotoViewAttacher photoViewAttacher=new PhotoViewAttacher(imageView);
        photoViewAttacher.update();

    }


}
