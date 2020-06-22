package com.peter.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView chatMessagesList;
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private String receiverUserID, receiverUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverUserID = getIntent().getExtras().get("UserID").toString();
        receiverUserName = getIntent().getExtras().get("UserName").toString();

        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(receiverUserName);

        chatMessagesList = findViewById(R.id.chat_message_list);
        chatMessagesList.setLayoutManager(new LinearLayoutManager(this));
        chatMessagesList.setHasFixedSize(true);

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.chat_send_message_btn);

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
