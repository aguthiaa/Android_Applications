package com.peter.socialnetworkingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {



    private RecyclerView messagesList;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;


    private ImageButton selectImage, sendMessage;
    private EditText messageInput;

    private Toolbar mToolbar;

    private String messageReceiverID, messageReceiverName,messageSenderID,saveCurrentDate,saveCurrentTime;


    private TextView receiverUserName,userLastSeen;
    private CircleImageView receiverProfileImage;


    private FirebaseAuth mAuth;
    private DatabaseReference rootRef , usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("userName").toString();

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        rootRef= FirebaseDatabase.getInstance().getReference();

        initViews();

        displayReceiverInfo();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendingMessage();

            }
        });

        fetchMessages();
    }



    private void fetchMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        if (dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messageList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }




    private void sendingMessage() {

        updateUserStatus("online");

        String messageText= messageInput.getText().toString().trim();

        if (!messageText.isEmpty())
        {

            String message_sender_ref="Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref="Messages/" + messageReceiverID + "/" +messageSenderID;

            DatabaseReference user_message_key=rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime=currentTime.format(calForTime.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref +"/"+ message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref +"/"+ message_push_id,messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {

                        messageInput.setText("");
                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    }

                    else {

                        messageInput.setText("");
                        Toast.makeText(ChatActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    }



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    String error = e.getMessage();

                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();

                }
            });

        }
        else
        {
            messageInput.setError("Please type a message first...");
            messageInput.requestFocus();
        }
    }


    public  void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calFordate=Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calFordate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime =currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        usersRef.child(messageSenderID).child("UserState").updateChildren(currentStateMap);
    }


    private void displayReceiverInfo() {
        receiverUserName.setText(messageReceiverName);


        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("profileImage")){

                        final String messageReceiverImage= dataSnapshot.child("profileImage").getValue().toString();

                        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                    }
                    if (dataSnapshot.hasChild("UserState"))
                    {
                        final String type=dataSnapshot.child("UserState").child("type").getValue().toString();
                        final String lastSeenDate=dataSnapshot.child("UserState").child("date").getValue().toString();
                        final String lastSeenTime=dataSnapshot.child("UserState").child("time").getValue().toString();

                        if (type.equals("online"))
                        {

                            userLastSeen.setText("online");
                        }

                        else
                        {
                            userLastSeen.setText("last seen "+lastSeenTime+"  "+lastSeenDate);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void initViews() {

        mToolbar=(Toolbar) findViewById(R.id.chats_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.chats_custom_bar,null);
        actionBar.setCustomView(actionBarView);

        messagesAdapter = new MessagesAdapter(messageList);

        messagesList =(RecyclerView) findViewById(R.id.chats_view);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayoutManager);
        messagesList.setAdapter(messagesAdapter);


        selectImage=(ImageButton) findViewById(R.id.chats_select_image);
        sendMessage=(ImageButton) findViewById(R.id.chats_send_message);
        messageInput=(EditText) findViewById(R.id.chats_message_input);

        userLastSeen=(TextView)  findViewById(R.id.custom_profile_last_seen);
        receiverUserName=(TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage=(CircleImageView) findViewById(R.id.custom_profile_image);
    }


}
