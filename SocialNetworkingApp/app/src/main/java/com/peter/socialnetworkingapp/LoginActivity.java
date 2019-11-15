package com.peter.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private EditText mEmail,mPassword;
    private TextView registerText,forgotPassword;
    private ImageView google_signin_button;


    private ProgressDialog mDialog;


    private FirebaseAuth mAuth;

    private final static int RC_SIGN_IN=1;
    private GoogleApiClient mGoogleSignInClient;

    private Boolean emailAddressChecker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




        initViews();


        //adding on click listener to the login button


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyLoginInputs();


            }
        });

        //Handling registerText textView event

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toRegister=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(toRegister);

            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toResetPassword=new Intent(LoginActivity.this,ResetPasswordActivity.class);
                startActivity(toResetPassword);
            }
        });



        //Google sign-in integration

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                Toast.makeText(LoginActivity.this, "Connection failed!", Toast.LENGTH_SHORT).show();

            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        google_signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 signIn();
            }
        });

    }


//Google Sign -in method

    private void signIn() {
        mDialog.setMessage("Please wait...");
        mDialog.show();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

                String error=e.getMessage();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();

            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();

                            sendUserToLoginActivity();
                        }

                        // ...
                    }
                });
    }




    private void verifyLoginInputs() {


        mDialog.setTitle("Login");
        mDialog.setMessage("Please wait...");
        mDialog.show();



        String email=mEmail.getText().toString().trim();
        String password=mPassword.getText().toString().trim();


        if (!email.isEmpty()){

            if (!password.isEmpty()){

                allowLogin(email,password);
            }

            else{

                mDialog.dismiss();
                mPassword.setError("Password is required");
                mPassword.requestFocus();
            }

        }
        else {

            mDialog.dismiss();
            mEmail.setError("Email is required");
            mEmail.requestFocus();
        }

    }




    private void allowLogin(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {



                if (task.isSuccessful()){

                    mDialog.dismiss();

                   verifyUserEmailAddress();

                }

                else {

                    mDialog.dismiss();

                    Toast.makeText(LoginActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mDialog.dismiss();

                String error=e.getMessage();

                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();


            }
        });

    }

    private  void verifyUserEmailAddress(){

        FirebaseUser user=mAuth.getCurrentUser();
        emailAddressChecker=user.isEmailVerified();

        if (emailAddressChecker)
        {

            sendUserToMainActivity();
        }
        else
        {
            Toast.makeText(this, "Please Verify Your Account", Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }




    private void sendUserToMainActivity() {

        Intent loginToMainIntent=new Intent(LoginActivity.this,MainActivity.class);

        loginToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginToMainIntent);
        finish();


    }

    private void sendUserToLoginActivity() {

        Intent loginToMainIntent=new Intent(LoginActivity.this,LoginActivity.class);

        loginToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginToMainIntent);
        finish();


    }


    @Override
    protected void onStart() {

        FirebaseUser currentUser=mAuth.getCurrentUser();


        if (currentUser != null){

            sendUserToMainActivity();
        }
        super.onStart();
    }

    private void initViews() {
        loginBtn=(Button) findViewById(R.id.loginButton);
        mEmail=(EditText) findViewById(R.id.loginEmail);
        mPassword=(EditText) findViewById(R.id.loginPassword);
        registerText=(TextView) findViewById(R.id.registerTextView);
        forgotPassword=(TextView) findViewById(R.id.forgot_password_text);
        google_signin_button=(ImageView) findViewById(R.id.googleImageView);


        mAuth=FirebaseAuth.getInstance();
        mDialog=new ProgressDialog(this);
    }
}
