package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce1.Model.Users;
import com.peter.ecommerce1.Prevalent.Prevalent;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private EditText number,password;
    private Button loginBtn;
    private CheckBox rememberMe;
    private TextView imAnAdminLink, imNotAnAdminLink, forgotPasswordLink;

    private String parentDbName = "Users";

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Paper.init(this);

        initViews();
    }



    private void initViews()
    {

        mDialog = new ProgressDialog(this);

        number = (EditText) findViewById(R.id.login_phone_number);
        password = (EditText) findViewById(R.id.login_password);
        loginBtn = (Button) findViewById(R.id.login_page_btn);
        rememberMe = (CheckBox) findViewById(R.id.remember_me_checkbox);
        imAnAdminLink = (TextView) findViewById(R.id.admin_link);
        imNotAnAdminLink = (TextView) findViewById(R.id.not_admin_link);
        forgotPasswordLink = (TextView) findViewById(R.id.forgot_password_link);

        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","login");
                startActivity(intent);
            }
        });



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyInputs();
            }
        });

        imAnAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginBtn.setText("Login Admin");
                imAnAdminLink.setVisibility(View.INVISIBLE);
                imNotAnAdminLink.setVisibility(View.VISIBLE);
                rememberMe.setVisibility(View.INVISIBLE);
                forgotPasswordLink.setVisibility(View.INVISIBLE);
                parentDbName ="Admins";

            }
        });

        imNotAnAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginBtn.setText("Login");
                imAnAdminLink.setVisibility(View.VISIBLE);
                imNotAnAdminLink.setVisibility(View.INVISIBLE);
                rememberMe.setVisibility(View.VISIBLE);
                forgotPasswordLink.setVisibility(View.VISIBLE);

                parentDbName ="Users";
            }
        });

    }

    private void verifyInputs()
    {

        mDialog.setTitle("Login");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String phoneNumber = number.getText().toString().trim();
        String mPassword = password.getText().toString().trim();

        if (!phoneNumber.isEmpty())
        {
            if (!mPassword.isEmpty())
            {
                loginUser(phoneNumber,mPassword);

            }
            else
            {
                password.setError("Password is required");
                password.requestFocus();
                mDialog.dismiss();
            }

        }
        else
        {
            number.setError("Phone Number is required");
            number.requestFocus();
            mDialog.dismiss();
        }
    }



    private void loginUser(final String phoneNumber, final String mPassword) {

        if (rememberMe.isChecked())
        {

            Paper.book().write(Prevalent.userPhoneKey,phoneNumber);
            Paper.book().write(Prevalent.userPasswordKey,mPassword);
        }

        DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.child(parentDbName).child(phoneNumber).exists())
                {

                    Users usersData = dataSnapshot.child(parentDbName).child(phoneNumber).getValue(Users.class);

                    if (usersData.getPhoneNumber().equals(phoneNumber))
                    {
                        if (usersData.getPassword().equals(mPassword))
                        {

                            if (parentDbName.equals("Admins"))
                            {
                                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                mDialog.dismiss();
                            }

                            else if (parentDbName.equals("Users"))
                            {
                                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                Prevalent.currentOnlineUser = usersData;
                                mDialog.dismiss();
                            }

                            else
                            {
                                Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            }
                        }

                        else
                        {
                            Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }


                    }
                    else
                    {

                        Toast.makeText(LoginActivity.this, "User with phone number: "+phoneNumber+"" +
                                " does not exist\n"+"Try creating a new account", Toast.LENGTH_LONG).show();
                        mDialog.dismiss();
                    }

                }
                else
                {
                    //create new account
                    Toast.makeText(LoginActivity.this, "User with phone number: "+phoneNumber+"" +
                            " does not exist\n"+"Try creating a new account", Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }




}
