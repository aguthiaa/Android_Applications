package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity
{
    private EditText registerEmail, registerPassword, registerRePassword;
    private Button registerBtn;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference();

        initializeViews();
    }



    private void initializeViews()
    {
        mDialog = new ProgressDialog(this);

        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerRePassword = findViewById(R.id.register_confirm_password);
        registerBtn = findViewById(R.id.register_btn);
        loginLink = findViewById(R.id.register_login_link);

        registerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyInputs();

            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendUserToLoginActivity();

            }
        });


    }


    private void verifyInputs()
    {
        mDialog.setTitle("Register");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String rePassword = registerRePassword.getText().toString().trim();

        if (!email.isEmpty())
        {
            if (!password.isEmpty())
            {
                if (!rePassword.isEmpty())
                {
                    if (password.equals(rePassword))
                    {
                        createNewUser(email, password);
                    }
                    else
                    {
                        mDialog.dismiss();
                        registerRePassword.setError("Password mismatch");
                        registerRePassword.requestFocus();
                    }

                }
                else
                {
                    mDialog.dismiss();
                    registerRePassword.setError("This field is required");
                    registerRePassword.requestFocus();
                }
            }
            else
            {
                mDialog.dismiss();
                registerPassword.setError("Password is required");
                registerPassword.requestFocus();
            }
        }
        else
        {
            mDialog.dismiss();
            registerEmail.setError("Email is Required");
            registerEmail.requestFocus();
        }
    }

    private void createNewUser(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            String currentOnlineUser = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();


                            usersRef.child("Users").child(currentOnlineUser).setValue("");

                            usersRef.child("Users").child(currentOnlineUser).child("device_token")
                                    .setValue(deviceToken);
                            mDialog.dismiss();
                            sendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();

            }
        });

    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
