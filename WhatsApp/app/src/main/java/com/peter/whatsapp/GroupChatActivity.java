package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private TextView groupMessageView;
    private EditText userMessage;
    private ImageButton sendMessage;
    private ScrollView mScrollView;
    private String groupName, currentUserID, currentUserName;
    private String saveCurrentDate, saveCurrentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = getIntent().getExtras().get("groupName").toString();

        mToolbar = findViewById(R.id.group_chat_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(groupName);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        initializeViews();

        getUserInformation();
    }



    private void initializeViews()
    {
        groupMessageView = findViewById(R.id.group_chat_text);
        userMessage = findViewById(R.id.group_chat_message);
        sendMessage = findViewById(R.id.group_chat_send_message);
        mScrollView = findViewById(R.id.group_chat_scroll_view);

        sendMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyMessageInput();

                userMessage.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }



    private void verifyMessageInput()
    {
        String message = userMessage.getText().toString().trim();
        String messageKey = groupNameRef.push().getKey();

        if (!message.isEmpty())
        {
            sendNewMessage(message, messageKey);
        }
        else
        {
            userMessage.setError("Messages is required");
            userMessage.requestFocus();
        }
    }



    private void sendNewMessage(String message, String messageKey)
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());


        HashMap<String, Object> groupMessageKey = new HashMap<>();
        groupNameRef.updateChildren(groupMessageKey);

        groupMessageKeyRef = groupNameRef.child(messageKey);

        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currentUserName);
        messageInfoMap.put("message", message);
        messageInfoMap.put("date", saveCurrentDate);
        messageInfoMap.put("time", saveCurrentTime);

        groupMessageKeyRef.updateChildren(messageInfoMap);



    }


    private void getUserInformation()
    {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if (dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if (dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
                }
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

    private void displayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage =(String) ((DataSnapshot) iterator.next()).getValue();
            String senderName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            groupMessageView.append(senderName+":\n"+chatMessage+"\n"+chatDate+"     "+chatTime+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
