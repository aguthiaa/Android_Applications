package com.peter.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {


    final static int Gallerly_Pick=1;

    private CircleImageView profileImage;
    private EditText userName,fullName,countryName;
    private Button saveUserInfo;


    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;

    private String currentUserID,downloadImageUrl;


    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        mAuth=FirebaseAuth.getInstance();
        currentUserID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        initViews();



        //adding on click listener to the save button


        saveUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                verifyUserInputs();


            }
        });



        //Adding on click listener to profile image

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallerlyIntent=new Intent();
                gallerlyIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallerlyIntent.setType("image/*");
                startActivityForResult(gallerlyIntent,Gallerly_Pick);

            }
        });



        //Getting reference to the database to display the image on the set up activity and the navigation drawer

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("ProfileImage")){

                        String image = Objects.requireNonNull(dataSnapshot.child("ProfileImage").getValue()).toString();

                        //To display the image we need a piccasso library
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
//                        Glide.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileImage);


                    }

                    else {

                        Toast.makeText(SetupActivity.this, "Select a profile image", Toast.LENGTH_SHORT).show();
                    }




                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }



    //Getting an image from the Gallery


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==Gallerly_Pick && resultCode == RESULT_OK && data != null){

            Uri imageUri = data.getData();
           // profileImage.setImageURI(imageUri);

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);


            if (resultCode == RESULT_OK){


                mDialog.setTitle("Updating Profile Image");
                mDialog.setMessage("Please wait...");
                mDialog.show();


                Uri resultUri = result.getUri();
                profileImage.setImageURI(resultUri);

                final StorageReference filePath = userProfileImageRef.child(resultUri.getLastPathSegment() + currentUserID + ".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mDialog.dismiss();
                        String error=e.getMessage();

                        Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(SetupActivity.this, "Image Stored successfully to firebase storage", Toast.LENGTH_SHORT).show();

                    }
                });

                Task<Uri> uriTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful()){

                            throw task.getException();
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()){
                            mDialog.dismiss();

                            Toast.makeText(SetupActivity.this, "Successfully saved to firebase database", Toast.LENGTH_SHORT).show();
                            downloadImageUrl=task.getResult().toString();
                        }

                        else {
                            Toast.makeText(SetupActivity.this, "An Error occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String error=e.getMessage();

                        Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();

                    }
                });

//
//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//
//
//                        if (task.isSuccessful()){
//
//                            mDialog.dismiss();
//
//                            Toast.makeText(SetupActivity.this, "Profile Image Stored to firebase storage successfully", Toast.LENGTH_SHORT).show();
//
//                            //Getting profile image link from firebase storage
//
//                            final String downloadUrl = Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().toString();
//
//
//                            userRef.child("ProfileImage").setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//
//                                            if (task.isSuccessful()){
//
//                                                mDialog.dismiss();
//
//
//                                                Intent selfIntent = new Intent(SetupActivity.this,SetupActivity.class);
//                                                startActivity(selfIntent);
//
//                                                Toast.makeText(SetupActivity.this, "Profile Image stored to firebase database successfully", Toast.LENGTH_SHORT).show();
//
//
//                                            }
//
//                                            else {
//
//                                                mDialog.dismiss();
//
//
//                                                Toast.makeText(SetupActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
//
//                                            }
//
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//
//
//                                    mDialog.dismiss();
//
//
//                                    String error=e.getMessage();
//
//                                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
//
//
//                                }
//                            });
//                        }
//
//                    }
//
//
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//
//                        mDialog.dismiss();
//
//
//                        String error = e.getMessage();
//
//                        Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();
//
//
//                    }
//                });

            }

            else {

                mDialog.dismiss();


                Toast.makeText(this, "Error occurred\nImage can't be Cropped Try Again!", Toast.LENGTH_LONG).show();


            }
        }
    }






    private void verifyUserInputs() {

        mDialog.setTitle("Saving Information");
        mDialog.setMessage("Please wait...");
        mDialog.show();

        String username=userName.getText().toString().trim();
        String fullname=fullName.getText().toString().trim();
        String country=countryName.getText().toString().trim();


        if (!username.isEmpty()){

            if (!fullname.isEmpty()){

                if (!country.isEmpty()){

                    setupProfileInfo(username,fullname,country);


                }

                else {

                    mDialog.dismiss();

                    countryName.setError("Country name is required");
                    countryName.requestFocus();
                }


            }
            else {
                mDialog.dismiss();

                fullName.setError("fullname is required");
                fullName.requestFocus();
            }

        }
        else {
            mDialog.dismiss();
            userName.setError("Username is required");
            userName.requestFocus();
        }

    }





    private void setupProfileInfo(String username, String fullname, String country) {

        HashMap hashMap=new HashMap();
        hashMap.put("profileImage",downloadImageUrl);
        hashMap.put("username",username);
        hashMap.put("fullname",fullname);
        hashMap.put("country",country);
        hashMap.put("status","Hey you'll this SNA-Social Networking App");
        hashMap.put("gender","None");
        hashMap.put("dob","Null");
        hashMap.put("relationship_status","None");

        userRef.updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()){

                    sendUserToMainActivity();


                    mDialog.dismiss();

                    Toast.makeText(SetupActivity.this, "Saved successfully", Toast.LENGTH_LONG).show();
                }
                else {


                    mDialog.dismiss();


                    Toast.makeText(SetupActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mDialog.dismiss();

                String error=e.getMessage();

                Toast.makeText(SetupActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });


    }





    private void sendUserToMainActivity() {

        Intent loginToMainIntent=new Intent(SetupActivity.this,MainActivity.class);

        loginToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginToMainIntent);
        finish();

    }





    private void initViews() {
        profileImage=(CircleImageView) findViewById(R.id.setupPageLogo);
        userName=(EditText) findViewById(R.id.setupUsername);
        fullName=(EditText) findViewById(R.id.setupFullname);
        countryName=(EditText) findViewById(R.id.setupCountry);
        saveUserInfo=(Button) findViewById(R.id.setupButtonSave);


        mDialog=new ProgressDialog(this);
    }
}
