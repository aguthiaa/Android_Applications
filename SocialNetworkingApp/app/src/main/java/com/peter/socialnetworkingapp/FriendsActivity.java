package com.peter.socialnetworkingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView friendsList;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference friendsRef,usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);

        friendsList =(RecyclerView) findViewById(R.id.all_friends_list);

        friendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendsList.setLayoutManager(linearLayoutManager);
        friendsList.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });

        displayAllFriends();
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

        usersRef.child(currentUserID).child("UserState").updateChildren(currentStateMap);
    }



    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
    }


    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }



    private void displayAllFriends() {

        FirebaseRecyclerOptions<Friends> firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(friendsRef,Friends.class).build();
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model) {

                holder.setDate(model.getDate());

                final String usersIDs=getRef(position).getKey();

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            final String userName=dataSnapshot.child("fullname").getValue().toString();
                            final String profile_Image=dataSnapshot.child("profileImage").getValue().toString();
                            final String type;

                            if (dataSnapshot.hasChild("UserState"))
                            {

                                type=dataSnapshot.child("UserState").child("type").getValue().toString();

                                if (type.equals("online"))
                                {

                                    holder.userOnlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    holder.userOnlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            holder.setFullname(userName);
                            holder.setProfileImage(profile_Image);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence options []=new CharSequence[]
                                            {userName+"'s Profile","Send Message"};

                                    AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int position) {

                                            if (position == 0){

                                                Intent intent=new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                intent.putExtra("visit_user_id",usersIDs);
                                                startActivity(intent);

                                            }

                                            if (position == 1){

                                                Intent intent=new Intent(FriendsActivity.this,ChatActivity.class);
                                                intent.putExtra("visit_user_id",usersIDs);
                                                intent.putExtra("userName",userName);
                                                startActivity(intent);

                                            }

                                        }
                                    });
                                    builder.show();
                                }
                            });

                        }
                        else {

                            //To do...
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);


                return new FriendsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        friendsList.setAdapter(firebaseRecyclerAdapter);
    }




    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        ImageView userOnlineStatusView;

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView=itemView;

            userOnlineStatusView =(ImageView) mView.findViewById(R.id.all_user_online_icon);
        }


        public void setProfileImage(String profileImage){

            CircleImageView mImage=(CircleImageView) mView.findViewById(R.id.search_friends_profile);

            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(mImage);
        }

        public void setFullname(String fullname){

            TextView mFullname=(TextView) mView.findViewById(R.id.search_friend_name);

            mFullname.setText(fullname);

        }

        public void setDate(String date){

            TextView mDate=(TextView) mView.findViewById(R.id.search_friend_status);
            mDate.setText("Friends Since "+date);
        }
    }
}
