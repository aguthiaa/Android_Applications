package com.peter.whatsapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.whatsapp.model.Messages;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private String messageSenderID;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView recieverProfileImage;
        TextView recieverMessage, senderMessage;
        ImageView senderImageView, receiverImageView;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            recieverProfileImage = itemView.findViewById(R.id.receiver_profile_image);
            recieverMessage = itemView.findViewById(R.id.receiver_text_message);
            senderMessage = itemView.findViewById(R.id.sender_message_text);
            senderImageView = itemView.findViewById(R.id.message_sender_image_view);
            receiverImageView = itemView.findViewById(R.id.message_receiver_image_view);
        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);
        MessageViewHolder viewHolder = new MessageViewHolder(view);

        mAuth = FirebaseAuth.getInstance();

        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {

        messageSenderID = mAuth.getCurrentUser().getUid();

        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
//        String messageText = messages.getMessage();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverUserProfileImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverUserProfileImage).placeholder(R.drawable.profile_image).into(holder.recieverProfileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        holder.recieverMessage.setVisibility(View.GONE);
        holder.recieverProfileImage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.senderImageView.setVisibility(View.GONE);
        holder.receiverImageView.setVisibility(View.GONE);



        if (fromMessageType.equals("text"))
        {

            if (fromUserID.equals(messageSenderID))
            {
                holder.senderMessage.setVisibility(View.VISIBLE);

                holder.senderMessage.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessage.setText(messages.getMessage()+"\n\n"+messages.getTime()+"-"+messages.getDate());
            }

            else
            {

                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverMessage.setVisibility(View.VISIBLE);

                holder.recieverMessage.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.recieverMessage.setText(messages.getMessage()+"\n\n"+messages.getTime()+" "+messages.getDate());
            }
        }

        else if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderID))
            {
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.fail).into(holder.senderImageView);
            }

            else
            {
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.fail).into(holder.receiverImageView);
            }
        }

        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx"))
        {
            if (fromUserID.equals(messageSenderID))
            {
                holder.senderImageView.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-a7a3b.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=6f47cc2d-4a63-418a-b1a6-958cd56f74a1")
                        .into(holder.senderImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });

            }
            else
            {
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverImageView.setVisibility(View.VISIBLE);

               Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-a7a3b.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=6f47cc2d-4a63-418a-b1a6-958cd56f74a1")
                       .into(holder.receiverImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });
            }
        }



    }



    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }






}
