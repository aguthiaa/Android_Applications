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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView settingsProfileImage;
    private EditText status,userName,fullName,country,dob,gender,relationshipStatus;
    private Button updateAccountSettings;

    private Toolbar mToolbar;
    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef,postRef;
    private StorageReference profileImageReference;
    private String currentUserID,downloadImageUrl;

    final static int Gallery_Pick=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        initViews();

        mAuth=FirebaseAuth.getInstance();
        currentUserID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        postRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserID);
        profileImageReference= FirebaseStorage.getInstance().getReference().child("Profile Images");

        mToolbar=(Toolbar) findViewById(R.id.settings_toolbar);
        mDialog=new ProgressDialog(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){


                    if (dataSnapshot.hasChild("profileImage")){

                        String mProfileImage=dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(mProfileImage).placeholder(R.drawable.profile).into(settingsProfileImage);
                    }

                    if (dataSnapshot.hasChild("fullname")){

                        String mName=dataSnapshot.child("fullname").getValue().toString();
                        fullName.setText(mName);

                    }

                    if (dataSnapshot.hasChild("username")){

                        String mUserName=dataSnapshot.child("username").getValue().toString();
                        userName.setText(mUserName);

                    }
                    if (dataSnapshot.hasChild("status")){

                        String mStatus=dataSnapshot.child("status").getValue().toString();
                        status.setText(mStatus);
                    }

                    if (dataSnapshot.hasChild("country")){

                        String mCountry=dataSnapshot.child("country").getValue().toString();
                        country.setText(mCountry);
                    }

                    if (dataSnapshot.hasChild("dob")){

                        String mDate=dataSnapshot.child("dob").getValue().toString();
                        dob.setText(mDate);
                    }

                    if (dataSnapshot.hasChild("gender")){

                        String mGender=dataSnapshot.child("gender").getValue().toString();
                        gender.setText(mGender);
                    }
                    if (dataSnapshot.hasChild("relationship_status")){

                        String mRelationship=dataSnapshot.child("relationship_status").getValue().toString();
                        relationshipStatus.setText(mRelationship);
                    }

                }
                else {

                    //Nothing To do...
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateUpdatedEntries();

            }
        });


        settingsProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);



            }
        });
    }


    //Getting an image from the Gallery


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==Gallery_Pick && resultCode == RESULT_OK && data != null){

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
                settingsProfileImage.setImageURI(resultUri);

                final StorageReference filePath = profileImageReference.child(resultUri.getLastPathSegment() + currentUserID + ".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mDialog.dismiss();
                        String error=e.getMessage();

                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(SettingsActivity.this, "Image Stored successfully to firebase storage", Toast.LENGTH_SHORT).show();

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

                            Toast.makeText(SettingsActivity.this, "Successfully saved to firebase database", Toast.LENGTH_SHORT).show();
                            downloadImageUrl=task.getResult().toString();
                        }

                        else {
                            Toast.makeText(SettingsActivity.this, "An Error occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String error=e.getMessage();

                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

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

    private void validateUpdatedEntries() {

        String userStatus=status.getText().toString().trim();
        String userUsername=userName.getText().toString().trim();
        String userFullname=fullName.getText().toString().trim();
        String userCountry=country.getText().toString().trim();
        String userDateOfBirth=dob.getText().toString().trim();
        String userGender=gender.getText().toString().trim();
        String userRelationshipStatus=relationshipStatus.getText().toString().trim();

        if (!userStatus.isEmpty()){

            if (!userUsername.isEmpty()){
                if (!userFullname.isEmpty()){
                    if (!userCountry.isEmpty()){

                        if (!userGender.isEmpty()){

                            updateAccount(userStatus,userUsername,userFullname,userCountry,userDateOfBirth,userGender,userRelationshipStatus);

                        }
                        else {
                            gender.setError("Required");
                            gender.requestFocus();
                        }

                    }
                    else {

                        country.setError("Required");
                        country.requestFocus();
                    }

                }
                else {

                    fullName.setError("Required");
                    fullName.requestFocus();
                }

            }
            else {

                userName.setError("Required");
                userName.requestFocus();
            }


        }
        else {
            status.setError("Required");
            status.requestFocus();
        }

    }

    private void updateAccount(String userStatus, String userUsername, String userFullname, String userCountry, String userDateOfBirth, String userGender, String userRelationshipStatus) {

        HashMap hashMap=new HashMap();
        hashMap.put("profileImage",downloadImageUrl);
        hashMap.put("username",userUsername);
        hashMap.put("fullname",userFullname);
        hashMap.put("country",userCountry);
        hashMap.put("status",userStatus);
        hashMap.put("gender",userGender);
        hashMap.put("dob",userDateOfBirth);
        hashMap.put("relationship_status",userRelationshipStatus);


        userRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()){


                    Toast.makeText(SettingsActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                }
                else {

                    Toast.makeText(SettingsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error=e.getMessage();

                Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });



    }



    private void sendUserToMainActivity() {

        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




    private void initViews() {

        settingsProfileImage=(CircleImageView) findViewById(R.id.settings_profile_image);
        status=(EditText) findViewById(R.id.settings_status);
        userName=(EditText) findViewById(R.id.settings_username);
        fullName=(EditText) findViewById(R.id.settings_fullname);
        country=(EditText) findViewById(R.id.settings_country);
        dob=(EditText) findViewById(R.id.settings_date_of_birth);
        gender=(EditText) findViewById(R.id.settings_gender);
        relationshipStatus=(EditText) findViewById(R.id.settings_relationship_status);
        updateAccountSettings=(Button) findViewById(R.id.settings_update_button);
    }
}
