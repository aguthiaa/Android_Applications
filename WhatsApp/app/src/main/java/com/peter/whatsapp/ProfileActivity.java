package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private CircleImageView profileImage;
    private TextView username, userStatus;
    private Button sendMessageBtn, cancelMessageReqBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, chatRequestRef, contactsRef;
    private String senderUserID, receiverUserID, name, currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("UserID").toString();
        name = getIntent().getExtras().get("name").toString();

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mToolbar = findViewById(R.id.profile_appbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(name);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.profile_name);
        userStatus = findViewById(R.id.profile_status);
        sendMessageBtn = findViewById(R.id.profile_send_message_btn);
        cancelMessageReqBtn = findViewById(R.id.profile_cancel_message_btn);




        currentState = "new";

        usersRef.child(receiverUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                 if (dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String uName = dataSnapshot.child("name").getValue().toString();
                    String uStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(profileImage);
                    username.setText(uName);
                    userStatus.setText(uStatus);

                    manageChatRequest();
                }

                else
                {
                    String uName = dataSnapshot.child("name").getValue().toString();
                    String uStatus = dataSnapshot.child("status").getValue().toString();

                    username.setText(uName);
                    userStatus.setText(uStatus);

                    manageChatRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void manageChatRequest()
    {

        if (!senderUserID.equals(receiverUserID))
        {
            sendMessageBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    sendMessageBtn.setEnabled(false);

                    if (currentState.equals("new"))
                    {
                        sendChatRequest();
                    }

                    if (currentState.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        acceptChatRequest();
                    }

                    if (currentState.equals("friends"))
                    {
                        removeContact();
                    }
                }
            });
        }
        else
        {
            sendMessageBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void removeContact()
    {
        contactsRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                currentState = "new";
                                                sendMessageBtn.setEnabled(true);
                                                sendMessageBtn.setText("Send Message");

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


    private void sendChatRequest()
    {
        chatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                 sendMessageBtn.setEnabled(true);

                                                 currentState = "request_sent";
                                                 sendMessageBtn.setText("Cancel Chat Request");
                                                 sendMessageBtn.setEnabled(true);
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
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }


    private void acceptChatRequest()
    {
       contactsRef.child(senderUserID).child(receiverUserID).child("Contact").setValue("saved")
       .addOnCompleteListener(new OnCompleteListener<Void>()
       {
           @Override
           public void onComplete(@NonNull Task<Void> task)
           {
               if (task.isSuccessful())
               {
                   contactsRef.child(receiverUserID).child(senderUserID).child("Contact").setValue("saved")
                           .addOnCompleteListener(new OnCompleteListener<Void>()
                           {
                               @Override
                               public void onComplete(@NonNull Task<Void> task)
                               {
                                   if (task.isSuccessful())
                                   {
                                       chatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task)
                                                   {
                                                       if (task.isSuccessful())
                                                       {
                                                           chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {

                                                                           if (task.isSuccessful())
                                                                           {
                                                                               sendMessageBtn.setEnabled(true);
                                                                               currentState = "friends";
                                                                               sendMessageBtn.setText("Remove This Contact");

                                                                               cancelMessageReqBtn.setVisibility(View.INVISIBLE);
                                                                           }
                                                                       }
                                                                   });
                                                       }

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

    @Override
    protected void onStart()
    {
        super.onStart();

        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receiverUserID))
                {
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (requestType.equals("sent"))
                    {
                        currentState = "request_sent";
                        sendMessageBtn.setText("Cancel Chat Request");
                        sendMessageBtn.setEnabled(true);
                    }
                    else if (requestType.equals("received"))
                    {
                        currentState = "request_received";
                        sendMessageBtn.setText("Accept Chat Request");
                        cancelMessageReqBtn.setVisibility(View.VISIBLE);


                        cancelMessageReqBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                cancelChatRequest();
                                currentState = "new";
                                cancelMessageReqBtn.setVisibility(View.INVISIBLE);

                            }
                        });
                    }
                }
                else
                {
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(receiverUserID))
                            {
                                currentState = "friends";
                                sendMessageBtn.setText("Remove This Contact");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

//        chatRequestRef.child(receiverUserID).addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                if (dataSnapshot.hasChild(senderUserID))
//                {
//                    String requestType = dataSnapshot.child(senderUserID).child("request_type").getValue().toString();
//
//                    if (requestType.equals("received"))
//                    {
//                        currentState = "request_received";
//                        sendMessageBtn.setText("Accept Chat Request");
//                        cancelMessageReqBtn.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });

    }

    private void cancelChatRequest()
    {
        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageBtn.setText("Send Message Request");
                                                currentState = "new";
                                                sendMessageBtn.setEnabled(true);
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
