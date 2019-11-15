package com.peter.socialnetworkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;

    private CircleImageView navProfileImage;
    private TextView navProfileUsername;
    private ImageButton addNewPostImgButton;

    private Toolbar mToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,postsRef,likesRef;



    private String currentUserID;
    Boolean likesChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        mAuth=FirebaseAuth.getInstance();
        currentUserID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");


//        FirebaseUser mUser=mAuth.getCurrentUser();
//
//        if (mUser !=null){
//
//            currentUserID=mUser.getUid();




            userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    if (dataSnapshot.exists()){


                        if (dataSnapshot.hasChild("fullname")){


                            String fullname = dataSnapshot.child("fullname").getValue().toString();

                            navProfileUsername.setText(fullname);

                        }

                        if (dataSnapshot.hasChild("profileImage")){

                            String image= dataSnapshot.child("profileImage").getValue().toString();


                            Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);
                        }



                    }

                }



                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });




        initViews();


       postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);





        //Handling action bar toggle

        //adding toolbar to the main activity

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Home");


        actionBarDrawerToggle =new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);

        //adding event listener to drawer layout

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //adding the navigation header to the navigation_view

       View navView=navigationView.inflateHeaderView(R.layout.navigation_header);

       navProfileImage=(CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUsername=(TextView) navView.findViewById(R.id.nav_user_fullname);

        //adding on itemselected listener to the nevigation menu

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                //creating a method to handle all events

                userMenuSelector(menuItem);
                
                
                return false;
            }
        });



        //add new post on click listener


        addNewPostImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                sendUserToPostActivity();


            }
        });


        //Method to display all users posts

        displayAllUsersPost();


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

        userRef.child(currentUserID).child("UserState").updateChildren(currentStateMap);
    }




    private void displayAllUsersPost() {

        Query sortPostsInDescendingOrder = postsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Posts>().setQuery(sortPostsInDescendingOrder,Posts.class).build();

        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(firebaseRecyclerOptions)
                {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

               final String postKey = getRef(position).getKey();

                holder.setFullname(model.getFullname());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileimage(model.getProfileimage());
                holder.setPostImage(model.getPostImage());

                holder.setLikeButtonStatus(postKey);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(MainActivity.this,ClickPostActivity.class);
                        intent.putExtra("PostKey",postKey);
                        startActivity(intent);

                    }
                });


                holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent=new Intent(MainActivity.this, CommentsActivity.class);
                        intent.putExtra("PostKey",postKey);
                        startActivity(intent);

                    }
                });




                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likesChecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (likesChecker.equals(true)){


                                    if (dataSnapshot.child(postKey).hasChild(currentUserID)){

                                        likesRef.child(postKey).child(currentUserID).removeValue();
                                        likesChecker = false;
                                    }

                                    else {

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
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                return new PostsViewHolder(view);
            }
        };


        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");


    }



//Note: Post is the module class
    //and PostsViewHolder is the static class

    public static class PostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        ImageButton likePostButton,commentPostButton;
        TextView numberOfLikes;
        int countLikes;
        String current_user_id;
        DatabaseReference likes_ref;


        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView=itemView;

            likePostButton=(ImageButton) mView.findViewById(R.id.post_dislike);
            commentPostButton=(ImageButton) mView.findViewById(R.id.post_comments);
            numberOfLikes=(TextView) mView.findViewById(R.id.post_number_of_likes);

            likes_ref=FirebaseDatabase.getInstance().getReference().child("Likes");
            current_user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();

        }


        public void setLikeButtonStatus(final String postKey) {

            likes_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(current_user_id)){

                        countLikes =(int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        numberOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }

                    else {

                        countLikes =(int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        numberOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }



        public void setFullname(String fullname){

            TextView username= (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(String profileimage){

            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time){
            TextView postTime= (TextView) mView.findViewById(R.id.post_time);
            postTime.setText(time);
        }

        public void setDate(String date){
            TextView postDate= (TextView) mView.findViewById(R.id.post_date);
            postDate.setText(date);
        }

        public void setDescription(String description){

            TextView postDescription= (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);

        }

        public void setPostImage(String postImage){

            ImageView post_Image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postImage).into(post_Image);
        }

    }





    private void sendUserToPostActivity() {


        Intent addnewPostIntent=new Intent(MainActivity.this,PostActivity.class);
        startActivity(addnewPostIntent);

    }





    //adding event listener to the actionBarDrawerToggle---Button


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.nav_post:

                sendUserToPostActivity();

                break;
                
            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;
                
            case R.id.nav_Home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
                
            case R.id.nav_friends:
                sendUserToFriendsActivity();
                break;
                
            case R.id.nav_find_friends:
                sendUserToFindFriensActivity();
                break;
                
            case R.id.nav_messages:
                sendUserToFriendsActivity();
                break;
                
            case R.id.nav_settings:

                sendUserToSettingsActivity();

                break;
                
            case R.id.nav_logout:

                updateUserStatus("offline");
                mAuth.signOut();

                sendUserToLoginActivity();

                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void initViews() {

        navigationView=(NavigationView) findViewById(R.id.navigation_view);
        drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        postList=(RecyclerView) findViewById(R.id.all_users_post_list);
        mToolBar=(Toolbar) findViewById(R.id.main_page_toolbar);
        addNewPostImgButton=(ImageButton) findViewById(R.id.add_new_post_image_button);



    }

    @Override
    protected void onStart() {

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser == null){

            sendUserToLoginActivity();

        }

        else {

            checkUserExistenceInDatabase();


        }


        super.onStart();
    }




    private void checkUserExistenceInDatabase() {

        final String userId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(userId)){

                    sensUserToSetUpActivity();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    private void sensUserToSetUpActivity() {

        Intent intent=new Intent(MainActivity.this,SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




    private void sendUserToLoginActivity() {


        Intent loginIintent=new Intent(MainActivity.this,LoginActivity.class);
        loginIintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIintent);
        finish();
    }

    private void sendUserToSettingsActivity() {

        Intent intent =new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
    private void sendUserToProfileActivity() {

        Intent intent =new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendUserToFindFriensActivity() {

        Intent intent =new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);
    }


    private void sendUserToFriendsActivity() {

        Intent intent =new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(intent);
    }




}
