package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText, verificationCodeText;
    private Button continueTonNextBtn;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();


        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        ccp = findViewById(R.id.country_code_picker);

        phoneText = findViewById(R.id.phoneText);
        verificationCodeText = findViewById(R.id.codeText);
        continueTonNextBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);

        mDialog = new ProgressDialog(this);

        ccp.registerCarrierNumberEditText(phoneText);

        continueTonNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (continueTonNextBtn.getText().equals("Submit") || checker.equals("Code sent"))
                {
                    String codeSent = verificationCodeText.getText().toString().trim();

                    if (!codeSent.isEmpty())
                    {
                        mDialog.setMessage("Please wait...");
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, codeSent);
                        signInWithPhoneAuthCredential(credential);
                    }
                    else
                    {
                        mDialog.dismiss();
                        verificationCodeText.setError("Verification code required");
                        verificationCodeText.requestFocus();
                    }

                }
                else
                    {
                    phoneNumber = ccp.getFullNumberWithPlus();

                    if (!phoneNumber.equals("")) {
                        mDialog.setTitle("Phone Verification");
                        mDialog.setMessage("Please wait...");
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,
                                60,
                                TimeUnit.SECONDS,
                                RegisterActivity.this,
                                mCallbacks);

                    } else {
                        phoneText.setError("Phone Number is required");
                        phoneText.requestFocus();
                    }
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                mDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();

                relativeLayout.setVisibility(View.VISIBLE);
                continueTonNextBtn.setText("Continue");

                verificationCodeText.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                verificationID = s;
                resendingToken = forceResendingToken;
                relativeLayout.setVisibility(View.GONE);
                checker = "Code sent";
                continueTonNextBtn.setText("Submit");
                verificationCodeText.setVisibility(View.VISIBLE);
                mDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Verification code sent.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            mDialog.dismiss();

                            Toast.makeText(RegisterActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();

                            sendUserToMainActtivity();

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        }
                        else
                            {
                                String error = task.getException().toString();

                                Toast.makeText(RegisterActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void sendUserToMainActtivity()
    {
        Intent intent = new Intent(RegisterActivity.this, ContactsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null)
        {
            sendUserToMainActtivity();
        }
    }
}
