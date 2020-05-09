package com.peter.skypeclone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CallingActivity extends AppCompatActivity
{
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        userID = getIntent().getExtras().get("User ID").toString();
    }
}
