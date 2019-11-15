package com.peter.socialnetworkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myAllPostsList;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference postsRef,userRef,likesRef;

    Boolean likesChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mToolbar = (Toolbar) findViewById(R.id.my_posts_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        postsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        myAllPostsList=(RecyclerView) findViewById(R.id.my_all_posts_list);
        myAllPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myAllPostsList.setLayoutManager(linearLayoutManager);
        myAllPostsList.setAdapter(new RecyclerView.Adapter() {
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


        displayMyAllPosts();
    }


    private void displayMyAllPosts() {

        Query myPostsQuery = postsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(myPostsQuery,Posts.class).build();
        FirebaseRecyclerAdapter<Posts,MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder holder, final int position, @NonNull Posts model) {

                final String postKey = getRef(position).getKey();

                holder.setProfileimage(model.getProfileimage());
                holder.setFullname(model.getFullname());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setDescription(model.getDescription());
                holder.setPostImage(model.getPostImage());

                holder.setLikeButtonstatus(postKey);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        intent.putExtra("PostKey",postKey);
                        startActivity(intent);
                    }
                });

                holder.commentPostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(MyPostsActivity.this,CommentsActivity.class);
                        intent.putExtra("PostKey",postKey);
                        startActivity(intent);
                    }
                });

                holder.likePostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likesChecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (likesChecker.equals(true))
                                {
                                    if (dataSnapshot.child(postKey).hasChild(currentUserID))
                                    {
                                        likesRef.child(postKey).child(currentUserID).removeValue();
                                        likesChecker = false;
                                    }
                                    else
                                    {
                                        likesRef.child(postKey).child(currentUserID).setValue(true);
                                        likesChecker = true;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });



            }

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                return new MyPostsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        myAllPostsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static  class MyPostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        ImageButton likePostBtn, commentPostBtn;
        TextView numberOfLikes;
        int countLikes;
        String current_user_id;
        DatabaseReference likes_ref;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView=itemView;

            likePostBtn =(ImageButton) mView.findViewById(R.id.post_dislike);
            commentPostBtn =(ImageButton) mView.findViewById(R.id.post_comments);
            numberOfLikes =(TextView) mView.findViewById(R.id.post_number_of_likes);

            current_user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
            likes_ref=FirebaseDatabase.getInstance().getReference().child("Likes");
        }


        public void setLikeButtonstatus(final String postKey) {

            likes_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(current_user_id))
                    {
                        countLikes =(int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostBtn.setImageResource(R.drawable.like);
                        numberOfLikes.setText(countLikes+" Likes");

                    }
                    else
                    {
                        countLikes =(int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostBtn.setImageResource(R.drawable.dislike);
                        numberOfLikes.setText(countLikes+" Likes");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }



        public void setDate(String date)
        {
            TextView mDate=(TextView) mView.findViewById(R.id.post_date);
            mDate.setText(date);

        }
        public void setTime(String time)
        {
            TextView mTime=(TextView) mView.findViewById(R.id.post_time);
            mTime.setText(time);

        }
        public void setDescription(String description)
        {
            TextView mDescription = (TextView) mView.findViewById(R.id.post_description);
            mDescription.setText(description);

        }
        public void setPostImage(String postImage)
        {
            ImageView mPostImage=(ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postImage).into(mPostImage);

        }
        public void setProfileimage(String profileimage)
        {
            CircleImageView mProfileImage =(CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(mProfileImage);

        }
        public void setFullname(String fullname)
        {
            TextView mFullname=(TextView) mView.findViewById(R.id.post_user_name);
            mFullname.setText(fullname);

        }


    }

}
