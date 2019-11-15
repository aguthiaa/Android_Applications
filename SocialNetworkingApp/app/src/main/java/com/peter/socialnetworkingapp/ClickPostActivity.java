package com.peter.socialnetworkingapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView post_image;
    private TextView post_description;
    private Button edit_post, delete_post;
    private String postKey, currentUserID, databaseUserID, mDescription, mPostImage;

    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_click_post);

        initViews();

        delete_post.setVisibility(View.INVISIBLE);
        edit_post.setVisibility(View.INVISIBLE);



        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        postKey=getIntent().getExtras().get("PostKey").toString();

        clickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);


        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    databaseUserID=dataSnapshot.child("uid").getValue().toString();

                    if (dataSnapshot.hasChild("description")){

                        mDescription=dataSnapshot.child("description").getValue().toString();
                        post_description.setText(mDescription);
                    }

                    if (dataSnapshot.hasChild("postImage")){

                        mPostImage=dataSnapshot.child("postImage").getValue().toString();
                        Picasso.get().load(mPostImage).into(post_image);
                    }

                    if (currentUserID.equals(databaseUserID)){

                        delete_post.setVisibility(View.VISIBLE);
                        edit_post.setVisibility(View.VISIBLE);
                    }

                    else {
                        //Data does not exist
                    }

                    edit_post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            editCurrentPost(mDescription);

                        }
                    });

                }
                else {
                    //Data still does not exist
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        delete_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Deleting User's post
                clickPostRef.removeValue();
                Toast.makeText(ClickPostActivity.this, "Post Deleted successfully", Toast.LENGTH_LONG).show();

                sendUserToMainActivity();

            }
        });


    }



    private void editCurrentPost(String mDescription) {

        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post.");
        final EditText inputField=new EditText(ClickPostActivity.this);
        inputField.setText(mDescription);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post description updated successfully", Toast.LENGTH_LONG).show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

            }
        });

        Dialog dialog=builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
        dialog.show();
    }





    private void sendUserToMainActivity() {

        Intent intent=new Intent(ClickPostActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




    private void initViews() {

        post_image=(ImageView) findViewById(R.id.edit_post_image);
        post_description=(TextView) findViewById(R.id.edit_post_description);
        edit_post=(Button) findViewById(R.id.edit_post_button);
        delete_post=(Button) findViewById(R.id.delete_post_button);
    }
}
