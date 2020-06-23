package com.peter.whatsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView chatMessagesList;
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private String receiverUserID, receiverUserName, receiverImage = "";

    private CircleImageView userProfileImage;
    private TextView rUsername, rLastSeen;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverUserID = getIntent().getExtras().get("UserID").toString();
        receiverUserName = getIntent().getExtras().get("UserName").toString();
        receiverImage = getIntent().getExtras().get("ProfileImage").toString();

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

        chatMessagesList = findViewById(R.id.chat_message_list);
        chatMessagesList.setLayoutManager(new LinearLayoutManager(this));
        chatMessagesList.setHasFixedSize(true);



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

    }


}
