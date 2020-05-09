package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.skypeclone.model.FindFriends;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity
{
    private RecyclerView notificationsList;
    private DatabaseReference friendRequestRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String onlineUser, requestUserID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsList = findViewById(R.id.notifications_recycler_view);
        notificationsList.setLayoutManager(new LinearLayoutManager(this));
        notificationsList.setHasFixedSize(true );

        mAuth  = FirebaseAuth.getInstance();
        onlineUser = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        friendRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView friendProfileImage;
        TextView friendProfileName;
        Button acceptRequest, cancelRequest;
        RelativeLayout cardItemsLayout;

        public NotificationsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            friendProfileImage = itemView.findViewById(R.id.friends_profile_image);
            friendProfileName = itemView.findViewById(R.id.friends_username);
            acceptRequest = itemView.findViewById(R.id.accept_friend_request_btn);
            cancelRequest = itemView.findViewById(R.id.cancel_friend_request_btn);
            cardItemsLayout = itemView.findViewById(R.id.card_items_layout);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions <FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(friendRequestRef.child(onlineUser), FindFriends.class)
                .build();

        FirebaseRecyclerAdapter<FindFriends, NotificationsViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriends, NotificationsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int position, @NonNull FindFriends model)
                    {
                        holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                acceptFriendRequest();

                            }
                        });

                        holder.cancelRequest.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                cancelFriendRequest();

                            }
                        });



                        requestUserID = getRef(position).getKey();
                        DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();

                        requestTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        holder.cardItemsLayout.setVisibility(View.VISIBLE);

                                        usersRef.child(requestUserID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if (dataSnapshot.exists())
                                                {
                                                    String image = dataSnapshot.child("profileImage").getValue().toString();

                                                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.friendProfileImage);

                                                    String name = dataSnapshot.child("userName").getValue().toString();

                                                    holder.friendProfileName.setText(name);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });

                                    }
                                    else
                                    {
                                        holder.cardItemsLayout.setVisibility(View.GONE);
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });






                    }

                    @NonNull
                    @Override
                    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_design, parent, false);

                        NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);

                        return viewHolder;
                    }
                };
        adapter.startListening();
        notificationsList.setAdapter(adapter);
    }



    private void cancelFriendRequest()
    {
        friendRequestRef.child(onlineUser).child(requestUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendRequestRef.child(requestUserID).child(onlineUser).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(NotificationsActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {

                                    String error = e.getMessage();
                                    Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {

                String error = e.getMessage();
                Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

    }



    private void acceptFriendRequest()
    {
        contactsRef.child(onlineUser).child(requestUserID).child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(requestUserID).child(onlineUser).child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.child(onlineUser).child(requestUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(requestUserID).child(onlineUser).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        Toast.makeText(NotificationsActivity.this, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
                                                                                    }

                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e)
                                                                        {

                                                                            String error = e.getMessage();
                                                                            Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
                                                                        }
                                                                    });
                                                                }

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {

                                                        String error = e.getMessage();
                                                        Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {

                                    String error = e.getMessage();
                                    Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error = e.getMessage();
                Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
