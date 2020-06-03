package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private CircleImageView userProfileImage;
    private EditText userUsername, userStatus;
    private Button updateSettingsBtn;
    private static final int GalleryPick = 1;

    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference profileImageRef;
    private String currentUserID;

    private Uri imageUri;
    private String downloadURI;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initializeViews();

        userUsername.setVisibility(View.INVISIBLE);
    }



    private void initializeViews()
    {
        mDialog = new ProgressDialog(this);

        userProfileImage = findViewById(R.id.settings_profile_image);
        userUsername = findViewById(R.id.settings_username);
        userStatus = findViewById(R.id.settings_status);
        updateSettingsBtn = findViewById(R.id.settings_update_btn);


        userProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });

        updateSettingsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyInputFields();

            }
        });


    }

    private void verifyInputFields()
    {
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String username = userUsername.getText().toString().trim();
        String status = userStatus.getText().toString().trim();

            if (!username.isEmpty())
            {
                if (!status.isEmpty())
                {
                    updateUserInfo(username, status);
                }
                else
                {
                    userStatus.setError("Status is required");
                    userStatus.requestFocus();
                    mDialog.dismiss();
                }
            }
            else
            {
                userUsername.setError("Username is required");
                userUsername.requestFocus();
                mDialog.dismiss();
            }

    }

    private void updateUserInfo(String username, String status)
    {

//        final StorageReference filePath = profileImageRef.child(imageUri.getLastPathSegment());
//
//        UploadTask uploadTask = filePath.putFile(imageUri);
//
//        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
//        {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
//            {
//                return null;
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//
//            }
//        }).addOnFailureListener(new OnFailureListener()
//        {
//            @Override
//            public void onFailure(@NonNull Exception e)
//            {
//
//            }
//        })

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("uid", currentUserID);
        userMap.put("name", username);
        userMap.put("status", status);

        rootRef.child("Users").child(currentUserID).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this, "Settings updated successfully", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                }

            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {

                String error = e.getMessage();
                Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();
                userProfileImage.setImageURI(resultUri);

                uploadProfileImage(resultUri);
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

    }

    private void uploadProfileImage(Uri resultUri)
    {
        mDialog.setTitle("Updating profile image");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        final StorageReference filePath = profileImageRef.child(currentUserID +".jpg");
       UploadTask uploadTask = filePath.putFile(resultUri);
       uploadTask.addOnFailureListener(new OnFailureListener()
       {
           @Override
           public void onFailure(@NonNull Exception e) {

               String error = e.getMessage();
               Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
               mDialog.dismiss();
           }
       }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
       {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
           {
               Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully to firebase storage", Toast.LENGTH_SHORT).show();
               mDialog.dismiss();
           }
       });

       Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
       {
           @Override
           public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
           {
               if (!task.isSuccessful())
               {
                   throw task.getException();
               }
               return filePath.getDownloadUrl();
           }
       }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if (task.isSuccessful())
                {
                    downloadURI = task.getResult().toString();

                    rootRef.child("Users").child(currentUserID).child("image").setValue(downloadURI)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(SettingsActivity.this, "profile image saved to firebase database", Toast.LENGTH_SHORT).show();
                                        sendUserToMainActivity();
                                        mDialog.dismiss();
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            String error = e.getMessage();
                            Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                            mDialog.dismiss();

                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image"))
                {
                    String uImage = dataSnapshot.child("image").getValue().toString();
                    String uName = dataSnapshot.child("name").getValue().toString();
                    String uStatus = dataSnapshot.child("status").getValue().toString();

                    userUsername.setText(uName);
                    userStatus.setText(uStatus);
                    Picasso.get().load(uImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                }
                else if ((dataSnapshot.exists()) && dataSnapshot.hasChild("name"))
                {
                    String uName = dataSnapshot.child("name").getValue().toString();
                    String uStatus = dataSnapshot.child("status").getValue().toString();

                    userUsername.setText(uName);
                    userStatus.setText(uStatus);
                }
                else
                {
                    userUsername.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please update your profile information", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
