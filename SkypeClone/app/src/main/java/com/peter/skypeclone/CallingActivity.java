package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
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

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity
{
    private String receiverUserID="",callerUserID="", receiverUserImage = "", receiverUsername = "";
    private String  callerImage ="", callerName ="", checker = "";
    private String callingID ="", ringingID = "";
    private ImageView callerProfileImage, acceptCallBtn, cancelCallBtn;
    private TextView callerUsername;

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        receiverUserID = getIntent().getExtras().get("User ID").toString();

        mediaPlayer = MediaPlayer.create(this, R.raw.ringing);

        initialiazeViews();

        mAuth = FirebaseAuth.getInstance();
        callerUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
               if (dataSnapshot.child(receiverUserID).exists())
               {
                   receiverUserImage = dataSnapshot.child(receiverUserID).child("profileImage").getValue().toString();
                   receiverUsername = dataSnapshot.child(receiverUserID).child("userName").getValue().toString();

                   Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(callerProfileImage);
                   callerUsername.setText(receiverUsername);
               }

               if (dataSnapshot.child(callerUserID).exists())
               {
                   callerImage = dataSnapshot.child(callerUserID).child("profileImage").getValue().toString();
                   callerName = dataSnapshot.child(callerUserID).child("userName").getValue().toString();

               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    private void initialiazeViews()
    {
        callerProfileImage = findViewById(R.id.calling_profile_image);
        acceptCallBtn = findViewById(R.id.calling_call_btn);
        cancelCallBtn = findViewById(R.id.calling_cancel_btn);
        callerUsername = findViewById(R.id.calling_username);

        cancelCallBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mediaPlayer.stop();
                checker = "clicked";

                cancelCall();

            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mediaPlayer.stop();
                final HashMap<String, Object> callPickUpMap = new HashMap<>();
                callPickUpMap.put("picked", "picked");

                usersRef.child(callerUserID).child("Ringing").updateChildren(callPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Intent toVideoCallIntent = new Intent(CallingActivity.this, VideoChatActivity.class);
                                    startActivity(toVideoCallIntent);
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                        String error = e.getMessage();
                        Toast.makeText(CallingActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();


        usersRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing"))
                {
                    HashMap<String, Object> callMap = new HashMap<>();
                    callMap.put("calling", receiverUserID);

                    usersRef.child(callerUserID).child("Calling").updateChildren(callMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        HashMap<String, Object> ringingMap = new HashMap<>();
                                        ringingMap.put("ringing", callerUserID);

                                        usersRef.child(receiverUserID).child("Ringing").updateChildren(ringingMap);
                                        mediaPlayer.start();


                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            String error = e.getMessage();
                            Toast.makeText(CallingActivity.this, error, Toast.LENGTH_LONG).show();

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(callerUserID).hasChild("Ringing") && !dataSnapshot.child(callerUserID).hasChild("Calling"))
                {

                    acceptCallBtn.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child(receiverUserID).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void cancelCall()
    {
        //from senders side.
        usersRef.child(callerUserID).child("Calling").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                {
                    callingID = dataSnapshot.child("calling").getValue().toString();

                    usersRef.child(callingID).child("Ringing").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        usersRef.child(callerUserID).child("Calling").removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    Intent contactsIntent = new Intent(CallingActivity.this, RegisterActivity.class);
                                                    contactsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(contactsIntent);
                                                    finish();
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                String error = e.getMessage();

                                                Toast.makeText(CallingActivity.this, error, Toast.LENGTH_LONG).show();

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

                            Toast.makeText(CallingActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });

                }
                else
                {
                    Intent contactsIntent = new Intent(CallingActivity.this, RegisterActivity.class);
                    contactsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(contactsIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        //from receiver's side

        usersRef.child(callerUserID).child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                        {
                            ringingID = dataSnapshot.child("ringing").getValue().toString();

                            usersRef.child(ringingID).child("Calling").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                usersRef.child(callerUserID).child("Ringing").removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Intent contactsIntent = new Intent(CallingActivity.this, RegisterActivity.class);
                                                                    contactsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(contactsIntent);
                                                                    finish();
                                                                }

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener()
                                                {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {

                                                        String error = e.getMessage();
                                                        Toast.makeText(CallingActivity.this, error, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    String error = e.getMessage();
                                    Toast.makeText(CallingActivity.this, error, Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                        else
                        {
                            Intent contactsIntent = new Intent(CallingActivity.this, RegisterActivity.class);
                            contactsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(contactsIntent);
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }




}
