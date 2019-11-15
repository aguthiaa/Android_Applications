package com.peter.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private ImageButton selectImagePost;
    private EditText postDescription;
    private Button updatePost;

    private Toolbar mToolbar;
    private ProgressDialog mDialog;


    private static final int  GAllery_Pick=1;
    private Uri imageUri;

    private StorageReference postImageReference;

    private String description, saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl,current_user_ID;

    private DatabaseReference userRef, postRef;
    private FirebaseAuth mAuth;

    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mDialog=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        current_user_ID=mAuth.getCurrentUser().getUid();

        postImageReference= FirebaseStorage.getInstance().getReference();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        postRef= FirebaseDatabase.getInstance().getReference().child("Posts");



        selectImagePost=(ImageButton) findViewById(R.id.postImageView);
        postDescription=(EditText) findViewById(R.id.postImageCaption);
        updatePost=(Button) findViewById(R.id.updatePostButton);


        mToolbar=(Toolbar) findViewById(R.id.add_new_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update post");




        selectImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                openGallery();

            }
        });



        updatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validatePost();

            }
        });



    }

    private void validatePost() {

        mDialog.setMessage("Please wait...");
        mDialog.show();

        description =postDescription.getText().toString().trim();

        if (imageUri == null){
            mDialog.dismiss();
            Toast.makeText(this, "Please select an image post...", Toast.LENGTH_LONG).show();
        }

        else if (description.isEmpty()){
            mDialog.dismiss();
            postDescription.setError("Say something...");
            postDescription.requestFocus();

        }

        else {

            storeImageToFirebaserStorage();
        }
    }



    private void storeImageToFirebaserStorage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = postImageReference.child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
        UploadTask uploadTask = filePath.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                String error = e.getMessage();

                Toast.makeText(PostActivity.this, error, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                mDialog.dismiss();
                Toast.makeText(PostActivity.this, "Post successfully uploaded to firebase storage", Toast.LENGTH_SHORT).show();
            }
        });

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {

                    throw task.getException();
                }

                downloadUrl = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {

                    mDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Post image successfully saved to firebase database", Toast.LENGTH_SHORT).show();
                    downloadUrl = task.getResult().toString();
                    savingPostInformationToDatabase();
                } else {
                    mDialog.dismiss();
                    Toast.makeText(PostActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                String error = e.getMessage();

                Toast.makeText(PostActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
//        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//
//                if (task.isSuccessful()){
//
//                    mDialog.dismiss();
//
//                    downloadUrl = Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().toString();
//                    Toast.makeText(PostActivity.this, "Image uploaded successfully to firebase storage...", Toast.LENGTH_LONG).show();
//                    savingPostInformationToDatabase();
//
//
//                }
//                else {
//                    mDialog.dismiss();
//                    Toast.makeText(PostActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                mDialog.dismiss();
//                String error =e.getMessage();
//
//                Toast.makeText(PostActivity.this, error, Toast.LENGTH_LONG).show();
//
//            }
//        });

    }



    private void savingPostInformationToDatabase() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()){

                    countPosts=dataSnapshot.getChildrenCount();
                }

                else {

                    countPosts = 0;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        userRef.child(current_user_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String userFullname=dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage=dataSnapshot.child("profileImage").getValue().toString();


                    HashMap postsMap=new HashMap();

                    postsMap.put("uid", current_user_ID);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", description);
                    postsMap.put("postImage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullname);
                    postsMap.put("counter", countPosts);


                    postRef.child(current_user_ID + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()){
                                mDialog.dismiss();

                                Toast.makeText(PostActivity.this, "Post successfully saved to firebase Database", Toast.LENGTH_LONG).show();

                                sendUserToMainActivity();
                            }
                            else {

                                mDialog.dismiss();
                                Toast.makeText(PostActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mDialog.dismiss();
                            String error = e.getMessage();

                            Toast.makeText(PostActivity.this, error, Toast.LENGTH_LONG).show();

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }





    private void openGallery() {

        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GAllery_Pick);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==GAllery_Pick && resultCode ==RESULT_OK && data!=null){

            imageUri = data.getData();
            selectImagePost.setImageURI(imageUri);


        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id =item.getItemId();

        if (id == android.R.id.home){


            sendUserToMainActivity();

        }

        return super.onOptionsItemSelected(item);
    }




    private void sendUserToMainActivity() {


        Intent mainIntent= new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }



}
