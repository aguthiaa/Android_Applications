package com.peter.cab;

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

public class DriverLoginRegisterActivity extends AppCompatActivity
{
    private TextView titleText, dontHaveAnAccount,haveAnAccount;
    private EditText dEmail,dPassword;
    private Button loginDriver, registerDriver;
    private ProgressDialog mDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference driversRef;

    private String onlineDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        initViews();

        mAuth = FirebaseAuth.getInstance();

    }

    private void initViews()
    {
        titleText = findViewById(R.id.driver_login_text);
        dontHaveAnAccount = findViewById(R.id.driver_dont_have_an_account_link);
        haveAnAccount = findViewById(R.id.driver_already_have_an_account_link);
        dEmail = findViewById(R.id.driver_login_email);
        dPassword = findViewById(R.id.driver_login_password);
        loginDriver = findViewById(R.id.driver_login_btn);
        registerDriver = findViewById(R.id.driver_register_btn);

        mDialog = new ProgressDialog(this);

        registerDriver.setVisibility(View.INVISIBLE);
        haveAnAccount.setVisibility(View.INVISIBLE);

        dontHaveAnAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                titleText.setText("Driver Register");
                dontHaveAnAccount.setVisibility(View.GONE);
                loginDriver.setVisibility(View.INVISIBLE);
                registerDriver.setVisibility(View.VISIBLE);
                haveAnAccount.setVisibility(View.VISIBLE);

            }
        });

        haveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                titleText.setText("Driver Login");
                dontHaveAnAccount.setVisibility(View.VISIBLE);
                loginDriver.setVisibility(View.VISIBLE);
                registerDriver.setVisibility(View.INVISIBLE);
                haveAnAccount.setVisibility(View.INVISIBLE);

            }
        });

        registerDriver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyUserInputs();

            }
        });

        loginDriver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyLoginInputs();

            }
        });
    }

    private void verifyLoginInputs()
    {

        mDialog.setTitle("Login.");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = dEmail.getText().toString().trim();
        String password = dPassword.getText().toString().trim();

        if (!email.isEmpty())
        {

            if (!password.isEmpty())
            {
                loginDriver(email, password);
            }
            else
            {
                dPassword.setError("Password is required");
                dPassword.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            dEmail.setError("Email is required");
            dEmail.requestFocus();
            mDialog.dismiss();
        }


    }

    private void loginDriver(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                       if (task.isSuccessful())
                       {
                           Toast.makeText(DriverLoginRegisterActivity.this, "Driver Login successful", Toast.LENGTH_LONG).show();

                           Intent intent = new Intent(DriverLoginRegisterActivity.this, DriversMapActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);
                           finish();

                           mDialog.dismiss();
                       }
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(DriverLoginRegisterActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }


    private void verifyUserInputs()
    {
        mDialog.setTitle("Register.");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = dEmail.getText().toString().trim();
        String password = dPassword.getText().toString().trim();

        if (!email.isEmpty())
        {

            if (!password.isEmpty())
            {
                registerNewDriver(email, password);
            }
            else
            {
                dPassword.setError("Password is required");
                dPassword.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            dEmail.setError("Email is required");
            dEmail.requestFocus();
            mDialog.dismiss();
        }

    }



    private void registerNewDriver(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    onlineDriver = mAuth.getCurrentUser().getUid();
                    driversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriver);

                    driversRef.setValue(true);
                    Toast.makeText(DriverLoginRegisterActivity.this, "New Driver Registered successfully", Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(DriverLoginRegisterActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }
}
