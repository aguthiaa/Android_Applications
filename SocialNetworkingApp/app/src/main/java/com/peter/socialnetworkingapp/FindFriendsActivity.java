package com.peter.socialnetworkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView searchResultList;
    private ImageButton searchButton;
    private EditText searchInput;

    private Toolbar mToolbar;

    private DatabaseReference allUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mToolbar=(Toolbar) findViewById(R.id.find_friends_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchResultList=(RecyclerView) findViewById(R.id.find_friends_recycler_view);
        searchResultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchResultList.setLayoutManager(linearLayoutManager);

        searchResultList.setAdapter(new RecyclerView.Adapter() {
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

        searchButton=(ImageButton) findViewById(R.id.find_friends_button);
        searchInput=(EditText) findViewById(R.id.search_input_textView);


        allUsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchEntry=searchInput.getText().toString().trim();

//                searchForFriendsAndPeople(searchEntry);

                Toast.makeText(FindFriendsActivity.this, "Searching...", Toast.LENGTH_LONG).show();

                Query searchPeopleAndFriendsQuery=allUsersRef.orderByChild("fullname")
                        .startAt(searchEntry).endAt(searchEntry + "\uf8ff");

                FirebaseRecyclerOptions<FindFriends> firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(searchPeopleAndFriendsQuery,FindFriends.class).build();

                FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(firebaseRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {

                        holder.setProfileImage(model.getProfileImage());
                        holder.setFullname(model.getFullname());
                        holder.setStatus(model.getStatus());

                        holder.mView.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String visit_user_id=getRef(position).getKey();

                                Intent toProfileIntent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                                toProfileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(toProfileIntent);

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                        return new FindFriendsViewHolder(view);

                    }
                };

                firebaseRecyclerAdapter.startListening();
                searchResultList.setAdapter(firebaseRecyclerAdapter);
            }
        });

    }



//    private void searchForFriendsAndPeople(String searchEntry) {
////
////        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();
////
////        Query searchPeopleAndFriendsQuery=allUsersRef.orderByChild("fullname")
////                .startAt(searchEntry).endAt(searchEntry + "\uf8ff");
////
////        FirebaseRecyclerOptions<FindFriends> firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(searchPeopleAndFriendsQuery,FindFriends.class).build();
////
////        FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(firebaseRecyclerOptions) {
////            @Override
////            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {
////
////                holder.setProfileImage(model.getProfileImage());
////                holder.setFullname(model.getFullname());
////                holder.setStatus(model.getStatus());
////
////            }
////
////            @NonNull
////            @Override
////            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////
////                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
////                return new FindFriendsViewHolder(view);
////
////            }
////        };
////
////        firebaseRecyclerAdapter.startListening();
////        searchResultList.setAdapter(firebaseRecyclerAdapter);
//    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setProfileImage(String profileImage){

            CircleImageView mImage=(CircleImageView) mView.findViewById(R.id.search_friends_profile);

            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(mImage);
        }

        public void setFullname(String fullname){

            TextView mFullname=(TextView) mView.findViewById(R.id.search_friend_name);

            mFullname.setText(fullname);

        }

        public void setStatus(String status){

            TextView mStatus=(TextView) mView.findViewById(R.id.search_friend_status);
            mStatus.setText(status);
        }
    }




}
