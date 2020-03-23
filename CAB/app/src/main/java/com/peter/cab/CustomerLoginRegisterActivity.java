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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CustomerLoginRegisterActivity extends AppCompatActivity
{
    private TextView textTitle, dontHaveAnAccount, alreadyHaveAnAccount;
    private EditText cEmail, cPassword;
    private Button cLoginBtn, cRegisterBtn;

    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference customersRef;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        initViews();

        mAuth = FirebaseAuth.getInstance();

    }

    private void initViews()
    {
        textTitle = findViewById(R.id.customer_login_text);
        dontHaveAnAccount = findViewById(R.id.customer_dont_have_an_account_link);
        alreadyHaveAnAccount = findViewById(R.id.customer_already_have_an_account_link);
        cEmail = findViewById(R.id.customer_login_email);
        cPassword = findViewById(R.id.customer_login_password);
        cLoginBtn = findViewById(R.id.customer_login_btn);
        cRegisterBtn = findViewById(R.id.customer_register_btn);

        mDialog = new ProgressDialog(this);

        cRegisterBtn.setVisibility(View.INVISIBLE);
        alreadyHaveAnAccount.setVisibility(View.INVISIBLE);

        dontHaveAnAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                textTitle.setText("Customer Register");
                cLoginBtn.setVisibility(View.INVISIBLE);
                dontHaveAnAccount.setVisibility(View.INVISIBLE);
                cRegisterBtn.setVisibility(View.VISIBLE);
                alreadyHaveAnAccount.setVisibility(View.VISIBLE);

            }
        });

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                textTitle.setText("Customer Login");
                cLoginBtn.setVisibility(View.VISIBLE);
                dontHaveAnAccount.setVisibility(View.VISIBLE);
                alreadyHaveAnAccount.setVisibility(View.INVISIBLE);
                cRegisterBtn.setVisibility(View.INVISIBLE);

            }
        });

        cRegisterBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyRegisterInputs();

            }
        });

        cLoginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyCustomerLoginInputs();

            }
        });
    }


    private void verifyCustomerLoginInputs()
    {

        mDialog.setTitle("Login.");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = cEmail.getText().toString().trim();
        String password = cPassword.getText().toString().trim();

        if (!email.isEmpty())
        {

            if (!password.isEmpty())
            {
                LoginCustomer(email, password);
            }
            else
            {
                cPassword.setError("Password is required");
                cPassword.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            cEmail.setError("Email is required");
            cEmail.requestFocus();
            mDialog.dismiss();
        }

    }

    private void LoginCustomer(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                            startActivity(intent);
                            mDialog.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(CustomerLoginRegisterActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }


    private void verifyRegisterInputs()
    {
        mDialog.setTitle("Register.");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = cEmail.getText().toString().trim();
        String password = cPassword.getText().toString().trim();

        if (!email.isEmpty())
        {

            if (!password.isEmpty())
            {
                registerNewCustomer(email, password);
            }
            else
            {
                cPassword.setError("Password is required");
                cPassword.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            cEmail.setError("Email is required");
            cEmail.requestFocus();
            mDialog.dismiss();
        }
    }



    private void registerNewCustomer(final String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            userID = mAuth.getCurrentUser().getUid();
                            customersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

                            customersRef.setValue(true);
                            Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Registered Successfully", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(CustomerLoginRegisterActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }

    }
