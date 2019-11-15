package com.peter.socialnetworkingapp;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentsList;
    private EditText commentsInput;
    private ImageButton postComment;
    private String Post_Key;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        Post_Key=getIntent().getExtras().get("PostKey").toString();


        initViews();

        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);
        commentsList.setAdapter(new RecyclerView.Adapter() {
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

        mAuth=FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");


        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
//                usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        if (dataSnapshot.exists()){
//
//                            if (dataSnapshot.hasChild("usernanme")){
//
//                                String username=dataSnapshot.child("username").getValue().toString();
//
//                                validateComment(username);
//
//                                commentsInput.setText("");
//                            }
//                            else {
//                                //To do...
//                            }
//
//
//                        }
//                        else {
//
//                            //To do...
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

                validateComment();
            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(postsRef,Comments.class).build();
        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setUsername(model.getUsername());

            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent,false);

                return new CommentsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        commentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public CommentsViewHolder(@NonNull View itemView) {


            super(itemView);

            mView=itemView;
        }



        public void setComment(String comment){

            TextView mComment=(TextView) mView.findViewById(R.id.comment_text);
            mComment.setText(comment);

        }

        public void setDate(String date){

            TextView mDate=(TextView) mView.findViewById(R.id.comment_date);
            mDate.setText(date);


        }
        public void setTime(String time){

            TextView mTime=(TextView) mView.findViewById(R.id.comment_time);
            mTime.setText(time);


        }

        public void setUsername(String username){

            TextView mUsername=(TextView) mView.findViewById(R.id.comment_user_name);
            mUsername.setText("@"+username);
        }
    }

    private void validateComment() {

        final String comment= commentsInput.getText().toString().trim();



        if (!comment.isEmpty()){

            usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.hasChild("username")){

                            String username=dataSnapshot.child("username").getValue().toString();

                            Calendar calForDate= Calendar.getInstance();
                            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
                            final String saveCurrentDate=currentDate.format(calForDate.getTime());

                            Calendar calForTime=Calendar.getInstance();
                            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
                            final String saveCurrentTime=currentTime.format(calForTime.getTime());

                            final String randomCommentKey=currentUserID + saveCurrentDate + saveCurrentTime;

                            HashMap commentsMap=new HashMap();
                            commentsMap.put("uid", currentUserID);
                            commentsMap.put("comment", comment);
                            commentsMap.put("date", saveCurrentDate);
                            commentsMap.put("time", saveCurrentTime);
                            commentsMap.put("username", username);


                            postsRef.child(randomCommentKey).updateChildren(commentsMap)
                                    .addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(CommentsActivity.this, "Comment added Successfully", Toast.LENGTH_SHORT).show();


                                            }

                                            else {

                                                Toast.makeText(CommentsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    String error=e.getMessage();

                                    Toast.makeText(CommentsActivity.this, error, Toast.LENGTH_LONG).show();

                                }
                            });
                            commentsInput.setText("");
                        }


                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else {

            commentsInput.setError("Say something...");
            commentsInput.requestFocus();
        }
    }


    private void initViews() {

        commentsList= (RecyclerView) findViewById(R.id.all_post_comments_view);
        commentsInput=(EditText) findViewById(R.id.post_comment_input);
        postComment=(ImageButton) findViewById(R.id.post_comment_button);

    }


}
