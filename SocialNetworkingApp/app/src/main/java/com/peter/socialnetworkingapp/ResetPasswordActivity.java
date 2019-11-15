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
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText resetEmailInput;
    private Button resetPassword;

    private Toolbar mToolbar;
    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mToolbar=(Toolbar) findViewById(R.id.reset_password_toolbar);
        mDialog=new ProgressDialog(this);

        initViews();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth=FirebaseAuth.getInstance();



        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateEmailInput();

            }
        });
    }



    private void validateEmailInput() {

        mDialog.setMessage("Please wait...");
        mDialog.show();

        String mEmail=resetEmailInput.getText().toString().trim();

        if (!mEmail.isEmpty()){

            sendPasswordResetEmail(mEmail);
        }
        else {
            mDialog.dismiss();
            resetEmailInput.setError("Email is required");
            resetEmailInput.requestFocus();
        }
    }

    private void sendPasswordResetEmail(String mEmail) {

        mAuth.sendPasswordResetEmail(mEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    mDialog.dismiss();
                    Toast.makeText(ResetPasswordActivity.this, "Reset Password link has been sent to your email account", Toast.LENGTH_LONG).show();

                    Intent intent=new Intent(ResetPasswordActivity.this,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                else {
                    mDialog.dismiss();

                    Toast.makeText(ResetPasswordActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                String error = e.getMessage();
                Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }




    private void initViews() {

        resetEmailInput=(EditText) findViewById(R.id.reset_password_email_input);
        resetPassword=(Button) findViewById(R.id.reset_password_button);
    }
}
