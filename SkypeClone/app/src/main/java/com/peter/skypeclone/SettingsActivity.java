package com.peter.skypeclone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity
{
    private ProgressDialog mDialog;

    private ImageView userImage;
    private EditText name, userStatus;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
    }


    private void initializeViews()
    {
        userImage = findViewById(R.id.settings_profile_image);
        name = findViewById(R.id.settings_username);
        userStatus = findViewById(R.id.settings_status);
        saveBtn = findViewById(R.id.settings_save_btn);

        mDialog = new ProgressDialog(this);

        userImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent gallerlyIntent = new Intent();

                gallerlyIntent.setType("images/jpg");

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyInputFields();

            }
        });
    }


    private void verifyInputFields()
    {
    }
}
