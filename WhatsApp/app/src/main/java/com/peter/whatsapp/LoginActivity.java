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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private EditText loginEmail, loginPassword;
    private TextView forgotPassword, registerHere;
    private Button loginBtn, phoneLoginBtn;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeViews();
    }



    private void initializeViews()
    {
        mDialog = new ProgressDialog(this);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        forgotPassword = findViewById(R.id.login_forgot_password_link);
        registerHere = findViewById(R.id.login_register_text);
        loginBtn = findViewById(R.id.login_btn);
        phoneLoginBtn = findViewById(R.id.login_phone_option_btn);

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                verifyUserInputs();

            }
        });

        registerHere.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);

            }
        });

        phoneLoginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent phoneIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneIntent);

            }
        });


    }



    private void verifyUserInputs()
    {
        mDialog.setTitle("Login");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (!email.isEmpty())
        {
            if (!password.isEmpty())
            {
                allowUserLogin(email, password);

            }
            else
            {
                mDialog.dismiss();
                loginPassword.setError("Password is required");
                loginPassword.requestFocus();
            }

        }
        else
        {
            mDialog.dismiss();
            loginEmail.setError("Email is required");
            loginEmail.requestFocus();
        }
    }


    private void allowUserLogin(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {

                            String currentUserID = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            usersRef.child(currentUserID).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
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
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();

            }
        });
    }



    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();

    }
}
