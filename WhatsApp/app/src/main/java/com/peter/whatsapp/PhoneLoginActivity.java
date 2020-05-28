package com.peter.whatsapp;

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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private EditText phoneInput, phoneVerificationInput;
    private Button sendVerificationCodeBtn, verifyBtn;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        phoneInput = findViewById(R.id.phone_number_input);
        phoneVerificationInput = findViewById(R.id.phone_verification_input);
        sendVerificationCodeBtn = findViewById(R.id.send_verification_btn);
        verifyBtn = findViewById(R.id.verification_btn);

        mDialog = new ProgressDialog(this);





        sendVerificationCodeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyIfPhoneNumberExist();

            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mDialog.setMessage("Please wait...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);
                phoneInput.setVisibility(View.INVISIBLE);

                String verificationCode = phoneVerificationInput.getText().toString().trim();

                if (!verificationCode.isEmpty())
                {
                   PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, verificationCode);
                   singInUserWithPhoneCredential(phoneAuthCredential);
                   mDialog.dismiss();
                }
                else
                {
                   phoneVerificationInput.setError("Enter the Verification code sent ");
                   phoneVerificationInput.requestFocus();
                   mDialog.dismiss();
                }

            }
        });



        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                singInUserWithPhoneCredential(phoneAuthCredential);
                mDialog.dismiss();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                Toast.makeText(PhoneLoginActivity.this, "Wrong phone number input format,  use this format:  +2547XX XXX XXX", Toast.LENGTH_LONG).show();

                sendVerificationCodeBtn.setVisibility(View.VISIBLE);
                phoneInput.setVisibility(View.VISIBLE);

                phoneVerificationInput.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                mDialog.dismiss();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                verificationID = s;
                resendingToken = forceResendingToken;

                Toast.makeText(PhoneLoginActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();

                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);
                phoneInput.setVisibility(View.INVISIBLE);

                phoneVerificationInput.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
                mDialog.dismiss();
            }
        };



    }

    private void singInUserWithPhoneCredential(PhoneAuthCredential phoneAuthCredential)
    {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(PhoneLoginActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();

                            Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                            FirebaseUser user = task.getResult().getUser();
                            mDialog.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(PhoneLoginActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();

            }
        });

    }

    private void verifyIfPhoneNumberExist()
    {
        mDialog.setTitle("Phone Verification");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String phoneNumber = phoneInput.getText().toString().trim();

        if (!phoneNumber.isEmpty())
        {
            sendPhoneVerificationCode(phoneNumber);

        }
        else
        {
            mDialog.dismiss();
            phoneInput.setError("Phone number is required!");
            phoneInput.requestFocus();
        }
    }



    private void sendPhoneVerificationCode(String phoneNumber)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                PhoneLoginActivity.this,
                mCallbacks
        );
    }


}
