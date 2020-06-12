package com.peter.whatsapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.peter.whatsapp.model.Friends;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment
{
    private View requestsView;
    private RecyclerView chatRequestsList;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference chatRequestsRef, usersRef, contactsRef;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsView = inflater.inflate(R.layout.fragment_requests, container, false);

        chatRequestsList = requestsView.findViewById(R.id.chat_requests_list);
        chatRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRequestsList.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        return requestsView;
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView username, userStatus;
        Button acceptRequest, cancelRequest;

        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.accept_profile_image);
            username = itemView.findViewById(R.id.accept_username);
            userStatus = itemView.findViewById(R.id.accept_user_status);
            acceptRequest = itemView.findViewById(R.id.accept_chat_request);
            cancelRequest = itemView.findViewById(R.id.cancel_chat_request);
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>().setQuery(chatRequestsRef.child(currentUserID), Friends.class).build();
        FirebaseRecyclerAdapter<Friends, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friends, RequestsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Friends model)
                    {
                        final String userRequestIDs = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        usersRef.child(userRequestIDs).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if (dataSnapshot.hasChild("image"))
                                                {
                                                    String uImage = dataSnapshot.child("image").getValue().toString();
                                                    String uName = dataSnapshot.child("name").getValue().toString();
                                                    String uStatus = dataSnapshot.child("status").getValue().toString();


                                                    Picasso.get().load(uImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                    holder.username.setText(uName);
                                                    holder.userStatus.setText(uStatus);



                                                }
                                                else
                                                {
                                                    String uName = dataSnapshot.child("name").getValue().toString();
                                                    String uStatus = dataSnapshot.child("status").getValue().toString();

                                                    holder.username.setText(uName);
                                                    holder.userStatus.setText(uStatus);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });

                        holder.acceptRequest.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                acceptChatRequest(userRequestIDs);

                            }
                        });

                        holder.cancelRequest.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                cancelChatRequest(userRequestIDs);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.accept_chat_request_layout, parent, false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;
                    }
                };
        adapter.startListening();
        chatRequestsList.setAdapter(adapter);

    }

    private void cancelChatRequest(final String userRequestIDs)
    {
        chatRequestsRef.child(currentUserID).child(userRequestIDs).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestsRef.child(userRequestIDs).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {

                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(), "Chat Request Rejected successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptChatRequest(final String userRequestIDs)
    {
        contactsRef.child(currentUserID).child(userRequestIDs).child("Contact").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(userRequestIDs).child(currentUserID).child("Contact").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                chatRequestsRef.child(currentUserID).child(userRequestIDs).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    chatRequestsRef.child(userRequestIDs).child(currentUserID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        Toast.makeText(getContext(), "Contact added successfully", Toast.LENGTH_SHORT).show();
                                                                                    }

                                                                                }
                                                                            });
                                                                }

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
                                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
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
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();

            }
        });
    }
}
