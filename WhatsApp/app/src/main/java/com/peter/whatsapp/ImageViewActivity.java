package com.peter.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity
{
    private ImageView imageViewer;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imageURL = getIntent().getExtras().get("ImageURL").toString();


        imageViewer = findViewById(R.id.image_viewer);

        Picasso.get().load(imageURL).into(imageViewer);

    }
}
