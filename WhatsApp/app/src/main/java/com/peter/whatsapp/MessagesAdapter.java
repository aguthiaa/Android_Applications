package com.peter.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
            }
            else
            {
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverImageView.setVisibility(View.VISIBLE);

               Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-a7a3b.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=6f47cc2d-4a63-418a-b1a6-958cd56f74a1")
                       .into(holder.receiverImageView);

            }
        }


        if (fromUserID.equals(messageSenderID))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String messageType = userMessagesList.get(position).getType();

                    if (messageType.equals("pdf") || messageType.equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and view this document",
                                        "Delete for me",
                                        "Delete for everyone",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Download or Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if (i == 1)
                                {
                                    deleteSentMessages(position,holder);
                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 2)
                                {
                                    deleteMessageForEveryone(position, holder);

                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);

                                }
                                if (i == 3)
                                {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                        builder.show();
                    }


                    else if (messageType.equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Delete for everyone",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Download or Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessages(position,holder);

                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);

                                }
                                if (i == 1)
                                {
                                    deleteMessageForEveryone(position, holder);

                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 2)
                                {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (messageType.equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View this image",
                                        "Delete for me",
                                        "Delete for everyone",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Download or Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    imageViewIntent.putExtra("ImageURL", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 1)
                                {
                                    deleteSentMessages(position,holder);
                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 2)
                                {
                                    deleteMessageForEveryone(position, holder);

                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 3)
                                {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String messageType = userMessagesList.get(position).getType();

                    if (messageType.equals("pdf") || messageType.equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and view this document",
                                        "Delete for me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Download or Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if (i == 1)
                                {
                                    deleteReceivedMessages(position, holder);
                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 2)
                                {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                        builder.show();
                    }


                    else if (messageType.equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Download or Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteReceivedMessages(position, holder);

                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 1)
                                {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (messageType.equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View this image",
                                        "Delete for me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Download or Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    imageViewIntent.putExtra("ImageURL", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 1)
                                {
                                    deleteReceivedMessages(position, holder);
                                    Intent imageViewIntent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(imageViewIntent);
                                }
                                if (i == 2)
                                {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }



    }

    private void deleteSentMessages(int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {

                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void deleteReceivedMessages(int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getMessageID())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(holder.itemView.getContext(), "Message Delete successfully", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }


    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }






}
