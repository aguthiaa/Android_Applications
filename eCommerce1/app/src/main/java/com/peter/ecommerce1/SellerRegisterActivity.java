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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SellerRegisterActivity extends AppCompatActivity
{
    private EditText sellerName, sellerPhone, sellerEmail, sellerPassword, sellerAddress;
    private Button registerBtn, toLoginBtn;

    private DatabaseReference sellersRef;
    private FirebaseAuth mAuth;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_register);

        initViews();

        mAuth = FirebaseAuth.getInstance();
        sellersRef = FirebaseDatabase.getInstance().getReference();
    }


    private void initViews()
    {
        sellerName = (EditText) findViewById(R.id.seller_full_name);
        sellerPhone = (EditText) findViewById(R.id.seller_phone_number);
        sellerEmail = (EditText) findViewById(R.id.seller_email);
        sellerPassword = (EditText) findViewById(R.id.seller_password);
        sellerAddress = (EditText) findViewById(R.id.seller_address);
        registerBtn = (Button) findViewById(R.id.seller_register_btn);
        toLoginBtn = (Button) findViewById(R.id.seller_already_have_an_account_btn);
        mDialog = new ProgressDialog(this);

        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(SellerRegisterActivity.this, SellerLoginActivity.class);
                startActivity(intent);

            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                verifySellerInputs();

            }
        });
    }


    private void verifySellerInputs()
    {
        mDialog.setTitle("Seller Registration");
        mDialog.setMessage("Please wait...");
        mDialog.show();

        String name = sellerName.getText().toString().trim();
        String phone = sellerPhone.getText().toString().trim();
        String email = sellerEmail.getText().toString().trim();
        String password = sellerPassword.getText().toString().trim();
        String address = sellerAddress.getText().toString().trim();

        if (!name.isEmpty())
        {
            if (!phone.isEmpty())
            {
                if (!email.isEmpty())
                {
                    if (!password.isEmpty())
                    {
                        if (!address.isEmpty())
                        {
                            registerSeller(name,phone,email,password,address);
                        }
                        else
                        {
                            sellerAddress.setError("Address is required");
                            sellerAddress.requestFocus();
                            mDialog.dismiss();
                        }
                    }
                    else
                    {
                        sellerPassword.setError("Password is required");
                        sellerPassword.requestFocus();
                        mDialog.dismiss();
                    }

                }
                else
                {
                    sellerEmail.setError("Email is required");
                    sellerEmail.requestFocus();
                    mDialog.dismiss();
                }
            }
            else
            {
                sellerPhone.setError("Phone Number is required");
                sellerPhone.requestFocus();
                mDialog.dismiss();
            }

        }
        else
        {
            sellerName.setError("Name is required");
            sellerName.requestFocus();
            mDialog.dismiss();
        }
    }



    private void registerSeller(final String name, final String phone, final String email, String password, final String address)
    {

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SellerRegisterActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();

                            String sid = mAuth.getCurrentUser().getUid();

                            HashMap<String, Object> sellersMap = new HashMap<>();
                            sellersMap.put("sid", sid);
                            sellersMap.put("name", name);
                            sellersMap.put("phone", phone);
                            sellersMap.put("email", email);
                            sellersMap.put("address", address);

                            sellersRef.child("Sellers").child(sid).updateChildren(sellersMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(SellerRegisterActivity.this, "Sellers Info saved to firebase database", Toast.LENGTH_SHORT).show();
                                                Intent intent  = new Intent(SellerRegisterActivity.this, SellerLoginActivity.class);
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
                                    Toast.makeText(SellerRegisterActivity.this, error, Toast.LENGTH_LONG).show();
                                    mDialog.dismiss();

                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(SellerRegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                mDialog.dismiss();

            }
        });

    }

}
