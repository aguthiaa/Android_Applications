package com.peter.ecommerce1;

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

public class RegisterActivity extends AppCompatActivity {

    private EditText name, phone, password, confirm_password;
    private Button createAccount;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
    }

    private void initViews()
    {
        mDialog = new ProgressDialog(this);

        name = (EditText) findViewById(R.id.register_full_name);
        phone = (EditText) findViewById(R.id.register_phone_number);
        password = (EditText) findViewById(R.id.register_password);
        confirm_password = (EditText) findViewById(R.id.register_confirm_password);
        createAccount = (Button) findViewById(R.id.register_account_btn);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyInputs();
            }
        });
    }



    private void verifyInputs()
    {
        mDialog.setTitle("Register");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String fullName = name.getText().toString().trim();
        String phoneNumber  = phone.getText().toString().trim();
        String passcode = password.getText().toString().trim();
        String rePasscode = confirm_password.getText().toString().trim();

        if (!fullName.isEmpty())
        {

            if (!phoneNumber.isEmpty())
            {

                if (!passcode.isEmpty())
                {

                    if (!rePasscode.isEmpty())
                    {

                        if (passcode.equals(rePasscode))
                        {
                            validatePhoneNumber(fullName,phoneNumber,passcode);
                        }
                        else
                        {
                            Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }
                    else
                    {
                        confirm_password.setError("Confirm Password");
                        confirm_password.requestFocus();
                        mDialog.dismiss();
                    }
                }
                else
                {
                    password.setError("Password is required");
                    password.requestFocus();
                    mDialog.dismiss();
                }
            }
            else
            {
                phone.setError("Phone Number is required");
                phone.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            name.setError("Name is required");
            name.requestFocus();
            mDialog.dismiss();
        }
    }


    private void validatePhoneNumber(final String fullName, final String phoneNumber, final String passcode)
    {

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!(dataSnapshot.child("Users").child(phoneNumber).exists()))
                {
                    HashMap<String, Object> userDataMap = new HashMap<>();

                    userDataMap.put("fullName",fullName);
                    userDataMap.put("phoneNumber",phoneNumber);
                    userDataMap.put("password",passcode);

                    rootRef.child("Users").child(phoneNumber).updateChildren(userDataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                                        mDialog.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String error = e.getMessage();
                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                            mDialog.dismiss();

                        }
                    });
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "This Phone Number: "+phoneNumber+" Already Exists,"+"\n"+"Try Using a different Number!", Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                    Intent intent = new Intent(RegisterActivity.this,RegisterActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
