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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SellerLoginActivity extends AppCompatActivity
{
    private EditText sLoginEmail, sLoginPassword;
    private Button loginBtn;

    private FirebaseAuth mAuth;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_login);

        initViews();

        mAuth = FirebaseAuth.getInstance();
    }



    private void initViews()
    {
        sLoginEmail = findViewById(R.id.seller_login_email);
        sLoginPassword = findViewById(R.id.seller_login_password);
        loginBtn = findViewById(R.id.seller_login_btn);

        mDialog = new ProgressDialog(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                verifySellerInputs();

            }
        });
    }



    private void verifySellerInputs()
    {
        mDialog.setTitle("Login");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String email = sLoginEmail.getText().toString().trim();
        String password = sLoginPassword.getText().toString().trim();

        if (!email.isEmpty())
        {
            if (!password.isEmpty())
            {
                loginUser(email,password);
            }
            else
            {
                sLoginPassword.setError("Password is required");
                sLoginPassword.requestFocus();
                mDialog.dismiss();
            }

        }
        else
        {
            sLoginEmail.setError("Email is required");
            sLoginEmail.requestFocus();
            mDialog.dismiss();
        }
    }


    private void loginUser(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SellerLoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SellerLoginActivity.this, SellersHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(SellerLoginActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();

            }
        });

    }


}
