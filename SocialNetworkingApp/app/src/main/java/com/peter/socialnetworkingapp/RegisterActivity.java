package com.peter.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword,registerConfirmPassword;
    private Button register;

    FirebaseAuth mAuth;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDialog=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        initViews();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyInputs();

            }
        });
    }




    private void verifyInputs() {

        mDialog.setTitle("Register");
        mDialog.setMessage("Please wait...");
        mDialog.show();

        String email=registerEmail.getText().toString().trim();
        String password=registerPassword.getText().toString().trim();
        String confirm_password=registerConfirmPassword.getText().toString().trim();


        if (!email.isEmpty()){

            if (!password.isEmpty()){

                if (!confirm_password.isEmpty()){

                    if (password.equals(confirm_password)){

                        createNewUser(email,password);

                    }
                    else {

                        mDialog.dismiss();


                        Toast.makeText(this, "Password MisMatch!", Toast.LENGTH_LONG).show();
                    }

                }
                else {


                    mDialog.dismiss();

                    registerConfirmPassword.setError("Password is required");
                    registerConfirmPassword.requestFocus();
                }

            }
            else {


                mDialog.dismiss();

                registerPassword.setError("Password is required");
                registerPassword.requestFocus();
            }

        }
        else {

            mDialog.dismiss();

            registerEmail.setError("Email is required");
            registerEmail.requestFocus();
        }
    }





    private void createNewUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    sendEmailVerificationMessage();

                    mDialog.dismiss();

                }
                else {

                    mDialog.dismiss();

                    Toast.makeText(RegisterActivity.this, "An Error occurred", Toast.LENGTH_LONG).show();

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


    @Override
    protected void onStart() {

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser != null){

            sendUserTomainActivity();
        }

        super.onStart();
    }

    private void sendEmailVerificationMessage()
    {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {

                                Toast.makeText(RegisterActivity.this, "Registered successfully,verify your account", Toast.LENGTH_LONG).show();
                                sendUserToLoginActivity();
                                mAuth.signOut();
                            }
                            else
                            {
                                Toast.makeText(RegisterActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    String error =e.getMessage();

                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                    mAuth.signOut();

                }
            });
        }
    }



    private void sendUserTomainActivity() {

        Intent loginToMainIntent=new Intent(RegisterActivity.this,MainActivity.class);

        loginToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginToMainIntent);
        finish();


    }
    private void sendUserToLoginActivity() {

        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();


    }




    private void initViews() {

        registerEmail=(EditText) findViewById(R.id.registerEmail);
        registerPassword=(EditText) findViewById(R.id.registerPassword);
        registerConfirmPassword=(EditText) findViewById(R.id.confirmPassword);
        register=(Button) findViewById(R.id.createAccountButton);

    }
}
