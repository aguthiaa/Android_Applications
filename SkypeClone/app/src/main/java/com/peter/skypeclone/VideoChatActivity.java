package com.peter.skypeclone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class VideoChatActivity extends AppCompatActivity
{
    private FrameLayout subscriberContainer, publisherContainer;
    private ImageView cancelVideoChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        initializeView();
    }



    private void initializeView()
    {
        subscriberContainer = findViewById(R.id.subscriber_container);
        publisherContainer = findViewById(R.id.publisher_container);
        cancelVideoChatBtn = findViewById(R.id.cancel_video_chat_btn);
    }
}
