package com.peter.socialnetworkingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private CircleImageView person_profile;
    private TextView person_fullname,person_username,person_status,person_country,person_dob,person_gender,person_relationship_satus;
    private Button send_friend_request,decline_friend_request;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef,friendRequestRef,friendsRef;
    private String senderUserID,receiverUserID,CURRENT_STATE,saveCurrentDate,saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        receiverUserID=getIntent().getExtras().get("visit_user_id").toString();

        mToolbar=(Toolbar) findViewById(R.id.person_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Person Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        senderUserID=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef=FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");

        initViews();

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileImage")){

                        String image=dataSnapshot.child("profileImage").getValue().toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(person_profile);
                    }

                    if (dataSnapshot.hasChild("fullname")){

                        String mFullname=dataSnapshot.child("fullname").getValue().toString();
                        person_fullname.setText(mFullname);
                    }
                    if (dataSnapshot.hasChild("username")){

                        String mUsername=dataSnapshot.child("username").getValue().toString();
                        person_username.setText("@"+mUsername);
                    }
                    if (dataSnapshot.hasChild("status")){

                        String mStatus=dataSnapshot.child("status").getValue().toString();
                        person_status.setText(mStatus);
                    }
                    if (dataSnapshot.hasChild("country")){

                        String mCountry=dataSnapshot.child("country").getValue().toString();
                        person_country.setText("Country: "+mCountry);
                    }
                    if (dataSnapshot.hasChild("dob")){

                        String mDOB=dataSnapshot.child("dob").getValue().toString();
                        person_dob.setText("Date of birth: "+mDOB);
                    }
                    if (dataSnapshot.hasChild("gender")){

                        String mGender=dataSnapshot.child("gender").getValue().toString();
                        person_gender.setText("Gender: "+mGender);
                    }
                    if (dataSnapshot.hasChild("relationship_status")){

                        String mRelationship=dataSnapshot.child("relationship_status").getValue().toString();
                        person_relationship_satus.setText("Relationship: "+mRelationship);
                    }
                }
                
                maintainSendAndReceiveFriendRequestButtonText();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        decline_friend_request.setVisibility(View.INVISIBLE);
        decline_friend_request.setEnabled(false);

        if (!senderUserID.equals(receiverUserID)){


            send_friend_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    send_friend_request.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){

                        sendFriendRequest();

                    }

                    if (CURRENT_STATE.equals("request_sent")){

                        cancelFriendRequest();

                    }

                    if (CURRENT_STATE.equals("request_received")){

                        acceptFriendRequest();
                    }

                    if (CURRENT_STATE.equals("friends")){

                        unFriendAnExistingFriend();
                    }

                }
            });


        }
        else {

            decline_friend_request.setVisibility(View.INVISIBLE);
            send_friend_request.setVisibility(View.INVISIBLE);
        }
    }



    private void unFriendAnExistingFriend() {

        friendsRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                send_friend_request.setEnabled(true);

                                                CURRENT_STATE="not_friends";

                                                send_friend_request.setText("Send Friend Request");

                                                decline_friend_request.setVisibility(View.INVISIBLE);
                                                decline_friend_request.setEnabled(false);

                                            }
                                        }
                                    });
                        }

                    }
                });
    }





    private void acceptFriendRequest() {

        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yy");
        saveCurrentDate=currentDate.format(calForDate.getTime());

//        Calendar calForTime=Calendar.getInstance();
//        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
//        saveCurrentTime=currentTime.format(calForTime.getTime());
        friendsRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if (task.isSuccessful()){


                            friendsRef.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        friendRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            friendRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()){

                                                                                send_friend_request.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                send_friend_request.setText("UnFriend This Person");

                                                                                decline_friend_request.setVisibility(View.INVISIBLE);
                                                                                decline_friend_request.setEnabled(false);
                                                                            }
                                                                            else {

                                                                                Toast.makeText(PersonProfileActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                                                            }


                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                    String error = e.getMessage();

                                                                    Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                                                }
                                                            });
                                                        }
                                                        else {

                                                            Toast.makeText(PersonProfileActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                String error=e.getMessage();

                                                Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                            }
                                        });
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    String error=e.getMessage();
                                    Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                }
                            });

                        }
                        else {


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error=e.getMessage();
                Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });


    }




    private void cancelFriendRequest() {

        friendRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                send_friend_request.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                send_friend_request.setText("Send Friend Request");

                                                decline_friend_request.setVisibility(View.INVISIBLE);
                                                decline_friend_request.setEnabled(false);
                                            }
                                            else {

                                                Toast.makeText(PersonProfileActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    String error = e.getMessage();

                                    Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                        else {

                            Toast.makeText(PersonProfileActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error=e.getMessage();

                Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }




    private void maintainSendAndReceiveFriendRequestButtonText() {

        friendRequestRef.child(senderUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserID)){

                            String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){

                                CURRENT_STATE = "request_sent";
                                send_friend_request.setText("Cancel Friend Request");

                                decline_friend_request.setVisibility(View.INVISIBLE);
                                decline_friend_request.setEnabled(false);
                            }

                            else if (request_type.equals("received")){


                                CURRENT_STATE ="request_received";
                                send_friend_request.setText("Accept Friend Request");

                                decline_friend_request.setVisibility(View.VISIBLE);
                                decline_friend_request.setEnabled(true);

                                decline_friend_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        cancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else {


                            friendsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                    if (dataSnapshot.hasChild(receiverUserID)){

                                        CURRENT_STATE ="friends";
                                        send_friend_request.setText("UnFriend This Person");

                                        decline_friend_request.setVisibility(View.INVISIBLE);
                                        decline_friend_request.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }




    private void sendFriendRequest() {

        friendRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    friendRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        send_friend_request.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        send_friend_request.setText("Cancel Friend Request");

                                        decline_friend_request.setVisibility(View.INVISIBLE);
                                        decline_friend_request.setEnabled(false);
                                    }
                                    else {

                                        Toast.makeText(PersonProfileActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String error = e.getMessage();

                            Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

                        }
                    });
                }
                else {

                    Toast.makeText(PersonProfileActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error=e.getMessage();

                Toast.makeText(PersonProfileActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }




    private void initViews() {

        person_profile=(CircleImageView) findViewById(R.id.person_profile_image);
        person_fullname=(TextView) findViewById(R.id.person_profile_name);
        person_username=(TextView) findViewById(R.id.person_profile_user_name);
        person_status=(TextView) findViewById(R.id.person_profile_status);
        person_country=(TextView) findViewById(R.id.person_profile_country);
        person_dob=(TextView) findViewById(R.id.person_profile_date_of_birth);
        person_gender=(TextView) findViewById(R.id.person_profile_gender);
        person_relationship_satus=(TextView) findViewById(R.id.person_profile_relationship_status);
        send_friend_request=(Button) findViewById(R.id.person_profile_send_friend_request);
        decline_friend_request=(Button) findViewById(R.id.person_profile_decline_friend_request);

        CURRENT_STATE = "not_friends";
    }


}
