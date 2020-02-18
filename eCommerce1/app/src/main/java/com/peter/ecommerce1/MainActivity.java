package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce1.Model.Users;
import com.peter.ecommerce1.Prevalent.Prevalent;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private Button joinNow,login;
    private TextView becomeAsellerLink;

//    private String parentDbName ="Users";
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        initViews();

        String userPhoneKey = Paper.book().read(Prevalent.userPhoneKey);
        String userPasswordKey =Paper.book().read(Prevalent.userPasswordKey);

        if (userPhoneKey != null && userPasswordKey !=null)
        {
            if (!userPhoneKey.isEmpty() && !userPasswordKey.isEmpty())
            {
                loginUser(userPhoneKey,userPasswordKey);

                mDialog.setTitle("Login");
                mDialog.setMessage("Please wait...");
                mDialog.show();
            }
        }
    }



    private void loginUser(final String userPhoneKey, final String userPasswordKey)
    {

        DatabaseReference rootRef;
        rootRef= FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.child("Users").child(userPhoneKey).exists())
                {
                    Users usersData = dataSnapshot.child("Users").child(userPhoneKey).getValue(Users.class);

                    if (usersData.getPhoneNumber().equals(userPhoneKey))
                    {

                        if (usersData.getPassword().equals(userPasswordKey))
                        {
                            Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(MainActivity.this,HomeActivity.class);
                            startActivity(intent);

                            Prevalent.currentOnlineUser = usersData;
                            mDialog.dismiss();


                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_LONG).show();
                            mDialog.show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "User with this Phoe Number: "+userPhoneKey+" does not exist\n"+

                            "Try creating a new account", Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    private void initViews() {

        joinNow = (Button) findViewById(R.id.welcome_register_button);
        login = (Button) findViewById(R.id.welcome_login_button);
        becomeAsellerLink = (TextView) findViewById(R.id.become_a_seller_link);

        mDialog=new ProgressDialog(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        
        joinNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        becomeAsellerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, SellerRegisterActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            Intent intent = new Intent(MainActivity.this, SellersHomeActivity.class);
            startActivity(intent);
            finish();
        }


    }
}
