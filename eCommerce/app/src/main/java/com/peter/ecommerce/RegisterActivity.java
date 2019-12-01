package com.peter.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{
    private EditText fullName, phoneNumber, password, confirmPassword;
    private Button registerBtn;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDialog = new ProgressDialog(this);

        initializeViews();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                verifyInputs();
            }
        });
    }


    private void verifyInputs()
    {

        mDialog.setTitle("Create Account");
        mDialog.setMessage("Please wait");
        mDialog.show();

        String mFullName=fullName.getText().toString().trim();
        String mPhoneNumber=phoneNumber.getText().toString().trim();
        String mPassword=password.getText().toString().trim();
        String mConfirmPassword=confirmPassword.getText().toString().trim();

        if (!mFullName.isEmpty())
        {

            if (!mPhoneNumber.isEmpty())
            {
                if (!mPassword.isEmpty())
                {
                    if (!mConfirmPassword.isEmpty())
                    {

                        if (mPassword.equals(mConfirmPassword))
                        {
                            validatePhoneNumberToRegister(mFullName,mPhoneNumber,mPassword);
                        }
                        else
                        {
                            Toast.makeText(this, "Error Password Mismatch", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();
                        }
                    }
                    else
                    {
                        mDialog.dismiss();
                        confirmPassword.setError("Required");
                        confirmPassword.requestFocus();
                    }
                }
                else
                {
                    mDialog.dismiss();
                    password.setError("Password is Required");
                    password.requestFocus();
                }

            }
            else
            {
                mDialog.dismiss();
                phoneNumber.setError("Phone Number is required");
                phoneNumber.requestFocus();
            }
        }
        else
        {
            mDialog.dismiss();
            fullName.setError("Name is Required");
            fullName.requestFocus();
        }
    }


    private void validatePhoneNumberToRegister(final String mFullName, final String mPhoneNumber, final String mPassword) {
        final DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!(dataSnapshot.child("Users").child(mPhoneNumber).exists()))
                {

                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("phoneNumber",mPhoneNumber);
                    hashMap.put("fullName",mFullName);
                    hashMap.put("password",mPassword);

                    rootRef.child("Users").child(mPhoneNumber).updateChildren(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful())
                                    {
                                        mDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                        sendUserToLoginActivity();

                                    }
                                    else
                                    {
                                        mDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    mDialog.dismiss();
                                    String error = e.getMessage();
                                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else
                {
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,  mPhoneNumber+" Number is already in use by another account", Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Try using a different Phone Number", Toast.LENGTH_LONG).show();

                    sendUserToMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity() {

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
    }


    private void initializeViews()
    {

        fullName=(EditText) findViewById(R.id.register_full_name_input);
        phoneNumber=(EditText) findViewById(R.id.register_phone_number_input);
        password =(EditText) findViewById(R.id.register_password_input);
        confirmPassword=(EditText) findViewById(R.id.register_confirm__password_input);
        registerBtn=(Button) findViewById(R.id.register_page_button);
    }


}
