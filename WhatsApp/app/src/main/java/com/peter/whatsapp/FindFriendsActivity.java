package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.whatsapp.model.Friends;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView friendsList;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        mToolbar = findViewById(R.id.find_friends_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


        friendsList = findViewById(R.id.find_friends_recycler_list);
        friendsList.setLayoutManager(new LinearLayoutManager(this));
        friendsList.setHasFixedSize(true);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        private CircleImageView userProfileImage;
        private TextView userName, userStatus;
        private ImageView onlineStatus;

        public FindFriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_username);
            userStatus = itemView.findViewById(R.id.user_status);
            onlineStatus = itemView.findViewById(R.id.user_online_status);
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>().setQuery(usersRef, Friends.class).build();
        FirebaseRecyclerAdapter<Friends, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friends, FindFriendsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull final Friends model)
                    {
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());


                        final String userID = getRef(position).getKey();
                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("UserID", userID);
                                profileIntent.putExtra("name", model.getName());
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        FindFriendsViewHolder holder = new FindFriendsViewHolder(view);
                        return holder;
                    }
                };

        adapter.startListening();
        friendsList.setAdapter(adapter);


    }
}
