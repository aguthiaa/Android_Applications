package com.peter.ecommerce1;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.peter.ecommerce1.Prevalent.Prevalent;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

    private CircleImageView userProfileImage;
    private EditText mPhoneNumber, mFullName,mAddress;
    private TextView changeProfile, closeSettings, updateSettings;
    private Button setSecurityQbtn;

    private Uri imageUri;
    private String profileUrl = "";
    private StorageTask uploadTask;
    private StorageReference profilePicturesRef;
    private String checker="";
    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();

        profilePicturesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }

    private void initViews()
    {
        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        mPhoneNumber = (EditText) findViewById(R.id.settings_phone_number);
        mFullName = (EditText) findViewById(R.id.settings_full_name);
        mAddress = (EditText) findViewById(R.id.settings_address);
        changeProfile = (TextView) findViewById(R.id.settings_profile_text);
        closeSettings = (TextView) findViewById(R.id.settings_close);
        updateSettings = (TextView) findViewById(R.id.settings_update);
        setSecurityQbtn = (Button) findViewById(R.id.settings_set_security_question);

        setSecurityQbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","settings");
                startActivity(intent);
            }
        });


        userInfoDisplay(userProfileImage,mPhoneNumber,mFullName,mAddress);

        closeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        updateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checker.equals("clicked"))
                {
                    userInfoSaved();
                }
                else
                {
                    updateOnlyUserInfo();
                }
            }
        });

        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker = "clicked";

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUri = result.getUri();

            userProfileImage.setImageURI(imageUri);
        }

        else
        {
            Toast.makeText(this, "Error, Try Again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    private void updateOnlyUserInfo()
    {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String, Object> userMap = new HashMap<>();

        userMap.put("fullName",mFullName.getText().toString().trim());
        userMap.put("phoneOrder",mPhoneNumber.getText().toString().trim());
        userMap.put("email",mAddress.getText().toString().trim());


        usersRef.child(Prevalent.currentOnlineUser.getPhoneNumber()).updateChildren(userMap);

        Toast.makeText(SettingsActivity.this, "Profile information updated successfully", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void userInfoSaved()
    {
        if (!(mPhoneNumber.getText().toString().trim()).isEmpty())
        {
            if (!(mFullName.getText().toString().trim()).isEmpty())
            {
                if (!(mAddress.getText().toString().trim()).isEmpty())
                {
                    if (checker.equals("clicked"))
                    {
                        uploadProfileImage();
                    }

                }
                else
                {
                    mAddress.setError("Address is required");
                    mAddress.requestFocus();
                }
            }
            else
            {
                mFullName.setError("Full Name is required");
                mFullName.requestFocus();
            }
        }
        else
        {
            mPhoneNumber.setError("Phone Number is required");
            mPhoneNumber.requestFocus();
        }
    }

    private void uploadProfileImage()
    {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        if (imageUri != null)
        {
            final StorageReference filePath = profilePicturesRef.child(Prevalent.currentOnlineUser.getPhoneNumber() +".jpg");

            uploadTask = filePath.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {

                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    mDialog.dismiss();
                    String error = e.getMessage();
                    Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful())
                    {


                       Uri downloadProfileUrl = task.getResult();
                       profileUrl = downloadProfileUrl.toString();

                       DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String, Object> userMap = new HashMap<>();

                        userMap.put("fullName",mFullName.getText().toString().trim());
                        userMap.put("phoneOrder",mPhoneNumber.getText().toString().trim());
                        userMap.put("email",mAddress.getText().toString().trim());
                        userMap.put("profileImage",profileUrl);

                        usersRef.child(Prevalent.currentOnlineUser.getPhoneNumber()).updateChildren(userMap);

                        mDialog.dismiss();

                        Toast.makeText(SettingsActivity.this, "Profile information updated successfully", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(intent);

                    }

                    else
                    {
                        mDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        else
        {
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoDisplay(final CircleImageView userProfileImage, final EditText mPhoneNumber, final EditText mFullName, final EditText mAddress)
    {

        DatabaseReference userRef;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhoneNumber());


        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("profileImage").exists())
                    {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        String name = dataSnapshot.child("fullName").getValue().toString();
                        String number = dataSnapshot.child("phoneOrder").getValue().toString();
                        String address = dataSnapshot.child("email").getValue().toString();


                        Picasso.get().load(image).into(userProfileImage);
                        mPhoneNumber.setText(number);
                        mFullName.setText(name);
                        mAddress.setText(address);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
