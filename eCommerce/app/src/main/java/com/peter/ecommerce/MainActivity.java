package com.peter.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce.Model.Users;
import com.peter.ecommerce.Prevalent.Prevalent;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private Button joinNowButton, loginButton;
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        mDialog = new ProgressDialog(this);

        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToLoginActivity();

            }
        });

        joinNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToRegisterActivity();

            }
        });

        String userPhoneKey = Paper.book().read(Prevalent.userPhoneKey);
        String userPasswordKey = Paper.book().read(Prevalent.userPasswordKey);

        if (userPhoneKey != "" && userPasswordKey != "")
        {

            if (!TextUtils.isEmpty(userPhoneKey) && !TextUtils.isEmpty(userPasswordKey)) {
                allowAccess(userPhoneKey, userPasswordKey);
                mDialog.setTitle("Login");
                mDialog.setMessage("Please wait...");
                mDialog.show();
            }
        }
    }

    private void allowAccess(final String number, final String passcode) {

        final DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();

        //Checking whether the user exists or not

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Users").child(number).exists())
                {

                    Users usersData=dataSnapshot.child("Users").child(number).getValue(Users.class);

                    if (usersData.getPhoneNumber().equals(number))
                    {
                        if (usersData.getPassword().equals(passcode))
                        {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            sendUserToHomeActivity();
                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Password is Incorrect", Toast.LENGTH_LONG).show();
                        }
                    }

                }
                else
                {
                    mDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Account with this phone number: "+number+" does not exist", Toast.LENGTH_LONG).show();
                    Toast.makeText(MainActivity.this, "You need to create a new account", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToHomeActivity() {
        Intent intent = new Intent(MainActivity.this,HomeActivity.class);
        startActivity(intent);
    }


    private void sendUserToRegisterActivity() {

        Intent intent=new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }



    private void sendUserToLoginActivity() {

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }




    private void initializeViews() {

        joinNowButton = (Button) findViewById(R.id.welcome_page_register_button);
        loginButton= (Button) findViewById(R.id.welcome_page_login_button);
    }


}
