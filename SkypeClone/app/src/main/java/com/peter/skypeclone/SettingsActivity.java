package com.peter.skypeclone;

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

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity
{
    private ProgressDialog mDialog;

    private ImageView profileImage;
    private EditText name, userStatus;
    private Button saveBtn;
    private Uri imageUri;
    private StorageReference profilePictureRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    String currentOnlineUser, downloadURL;

    private static int GallerlyPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        currentOnlineUser = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentOnlineUser);
        profilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }


    private void initializeViews()
    {
        profileImage = findViewById(R.id.settings_profile_image);
        name = findViewById(R.id.settings_username);
        userStatus = findViewById(R.id.settings_status);
        saveBtn = findViewById(R.id.settings_save_btn);

        mDialog = new ProgressDialog(this);

        profileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent gallerlyIntent = new Intent();
                gallerlyIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallerlyIntent.setType("image/*");

                startActivityForResult(gallerlyIntent, GallerlyPick);

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyInputFields();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GallerlyPick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();

            profileImage.setImageURI(imageUri);
        }
    }

    private void verifyInputFields()
    {
        final String userName = name.getText().toString().trim();
        final String status = userStatus.getText().toString().trim();

        if (imageUri != null)
        {

            if (!userName.isEmpty())
            {
                if (!status.isEmpty())
                {
                    saveUserInformation(userName, status);
                }
                else
                {
                    userStatus.setError("Say Something!");
                    userStatus.requestFocus();
                }
            }
            else
            {
                name.setError("Username is required!");
                name.requestFocus();
            }
        }
        else
        {
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("profileImage"))
                    {
                        updateOnlyUsernameAndStatus(userName, status);
                    }
                    else
                    {
                        Toast.makeText(SettingsActivity.this, "Select a profile image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }

    private void updateOnlyUsernameAndStatus(String userName, String status)
    {
        HashMap<String, Object> usersMap = new HashMap<>();
        usersMap.put("uID", currentOnlineUser);
        usersMap.put("userName", userName);
        usersMap.put("status", status);

        usersRef.updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this, "Profile Information Updated successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }

    private void saveUserInformation(final String userName, final String status)
    {
        final StorageReference filePath = profilePictureRef.child(currentOnlineUser);

        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task)
                    throws Exception
            {
                if (!task.isSuccessful())
                {
                    throw task.getException();
                }
                downloadURL = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if (task.isSuccessful())
                {
                    downloadURL = task.getResult().toString();
                    HashMap<String, Object> usersMap = new HashMap<>();
                    usersMap.put("uID", currentOnlineUser);
                    usersMap.put("profileImage", downloadURL);
                    usersMap.put("userName", userName);
                    usersMap.put("status", status);

                    usersRef.updateChildren(usersMap).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            String error = e.getMessage();

                            Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                                startActivity(intent);
                            }

                        }
                    });


                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "An Error occurred!", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });

    }
}
