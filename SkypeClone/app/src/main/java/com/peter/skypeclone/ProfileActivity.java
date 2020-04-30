package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
{
    private ImageView profileImage;
    private Button addFriendBtn, cancelFriendReqsBtn;
    private TextView username;
    private String receiveUserID = "";
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiveUserID = getIntent().getExtras().get("sentUserID").toString();

        profileImage = findViewById(R.id.profile_image_view);
        username = findViewById(R.id.profile_username);
        addFriendBtn = findViewById(R.id.profile_add_friend_btn);
        cancelFriendReqsBtn = findViewById(R.id.profile_cancel_request_btn);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiveUserID);


        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                     String uName = dataSnapshot.child("userName").getValue().toString();
                     username.setText(uName);
                     String profilePic = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(profilePic).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
