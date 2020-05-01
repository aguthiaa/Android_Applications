package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    private String receiverUserID = "",currentState = "new", onlineSenderID;
    private DatabaseReference usersRef, friendRequestsRef, contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("sentUserID").toString();

        profileImage = findViewById(R.id.profile_image_view);
        username = findViewById(R.id.profile_username);
        addFriendBtn = findViewById(R.id.profile_add_friend_btn);
        cancelFriendReqsBtn = findViewById(R.id.profile_cancel_request_btn);

        mAuth = FirebaseAuth.getInstance();
        onlineSenderID = mAuth.getCurrentUser().getUid();


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserID);
        friendRequestsRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


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

        manageButtonClickEvents();
        cancelFriendReqsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

            }
        });
    }



    private void manageButtonClickEvents()
    {
        if (onlineSenderID.equals(receiverUserID))
        {
            addFriendBtn.setVisibility(View.GONE);
        }
        else
        {
            addFriendBtn.setVisibility(View.VISIBLE);
            addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if (currentState.equals("new"))
                    {
                        sendFriendRequest();

                    }
                    if (currentState.equals("request_sent"))
                    {
                        cancelFriendRequest();

                    }
                    if (currentState.equals("request_received"))
                    {
                        acceptFriendRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }

                }
            });

        }
    }

    private void acceptFriendRequest()
    {
        contactsRef.child(onlineSenderID).child(receiverUserID).child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(onlineSenderID).child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestsRef.child(onlineSenderID).child(receiverUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    friendRequestsRef.child(receiverUserID).child(onlineSenderID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        currentState = "friends";
                                                                                        Toast.makeText(ProfileActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();
                                                                                        addFriendBtn.setText("Delete Contact");
                                                                                        cancelFriendReqsBtn.setVisibility(View.GONE);
                                                                                    }

                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e)
                                                                        {
                                                                            String error = e.getMessage();

                                                                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                                                        }
                                                                    });
                                                                }

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {
                                                        String error = e.getMessage();

                                                        Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                                    }
                                                });

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {

                                    String error = e.getMessage();

                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {

                String error = e.getMessage();

                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }


    private void cancelFriendRequest()
    {
        friendRequestsRef.child(onlineSenderID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendRequestsRef.child(receiverUserID).child(onlineSenderID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                currentState = "new";
                                                Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                addFriendBtn.setText("Add Friend ");
                                                cancelFriendReqsBtn.setVisibility(View.GONE);
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    String error = e.getMessage();

                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }


    private void sendFriendRequest()
    {

        friendRequestsRef.child(onlineSenderID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendRequestsRef.child(receiverUserID).child(onlineSenderID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                currentState = "request_sent";
                                                Toast.makeText(ProfileActivity.this, "Request Sent.", Toast.LENGTH_SHORT).show();
                                                addFriendBtn.setText("Cancel Friend Request");

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    String error = e.getMessage();

                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

    }
}
