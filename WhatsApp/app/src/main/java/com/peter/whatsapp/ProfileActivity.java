package com.peter.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private CircleImageView profileImage;
    private TextView username, userStatus;
    private Button sendMessageBtn;

    private DatabaseReference usersRef;
    private String userID, name, status, image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userID = getIntent().getExtras().get("UserID").toString();
        name = getIntent().getExtras().get("name").toString();
        status = getIntent().getExtras().get("status").toString();
        image = getIntent().getExtras().get("image").toString();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

        mToolbar = findViewById(R.id.profile_appbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(name);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.profile_name);
        userStatus = findViewById(R.id.profile_status);
        sendMessageBtn = findViewById(R.id.profile_send_message_btn);


        Picasso.get().load(image).placeholder(R.drawable.profile_image).into(profileImage);
        username.setText(name);
        userStatus.setText(status);
    }
}
