package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.whatsapp.model.Messages;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView chatMessagesList;
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private String receiverUserID, receiverUserName, receiverImage = "";
    private String messageSenderID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private CircleImageView userProfileImage;
    private TextView rUsername, rLastSeen;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverUserID = getIntent().getExtras().get("UserID").toString();
        receiverUserName = getIntent().getExtras().get("UserName").toString();
        receiverImage = getIntent().getExtras().get("ProfileImage").toString();

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();


        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_app_bar, null);
        actionBar.setCustomView(actionBarView);

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.chat_send_message_btn);
        userProfileImage = findViewById(R.id.chat_receiver_profile_image);
        rUsername = findViewById(R.id.chat_receiver_username);
        rLastSeen = findViewById(R.id.chat_receiver_last_seen);

        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(userProfileImage);
        rUsername.setText(receiverUserName);
        rLastSeen.setText("Last Seen: Date, Time.");


        messagesAdapter = new MessagesAdapter(messageList);

        chatMessagesList = findViewById(R.id.chat_message_list);
        chatMessagesList.setLayoutManager(new LinearLayoutManager(this));
        chatMessagesList.setHasFixedSize(true);
        chatMessagesList.setAdapter(messagesAdapter);



//       messagesAdapter = new MessagesAdapter(messageList);





        sendMessageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyMessageInput();

            }
        });

    }

    private void verifyMessageInput()
    {
        String message = messageInput.getText().toString().trim();

        if (!message.isEmpty())
        {
            sendMessageNow(message);

        }
        else
        {
            messageInput.setError("Type message first");
            messageInput.requestFocus();
        }
    }

    private void sendMessageNow(String message)
    {
        String messageSenderRef = "Messages/" + messageSenderID + "/" + receiverUserID;
        String messagereceiverRef = "Messages/" + receiverUserID + "/" +messageSenderID;

        DatabaseReference userMessageKey = rootRef.child("Messages").child(messageSenderID).child(receiverUserID).push();
        String messagePushID = userMessageKey.getKey();

        Map messageMap = new HashMap();
        messageMap.put("message", message);
        messageMap.put("type", "text");
        messageMap.put("from", messageSenderID);

        Map messageBodyDetailsMap = new HashMap();
        messageBodyDetailsMap.put(messageSenderRef+"/"+messagePushID, messageMap);
        messageBodyDetailsMap.put(messagereceiverRef + "/" + messagePushID, messageMap);


        rootRef.updateChildren(messageBodyDetailsMap)
                .addOnCompleteListener(new OnCompleteListener()
                {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, "Messages sent successfully", Toast.LENGTH_SHORT).show();
                        }

                        messageInput.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });


    }


    @Override
    protected void onStart()
    {
        super.onStart();

        rootRef.child("Messages").child(messageSenderID).child(receiverUserID)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messageList.add(messages);

                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }
}
