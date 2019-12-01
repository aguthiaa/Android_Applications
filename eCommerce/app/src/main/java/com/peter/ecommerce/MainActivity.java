package com.peter.ecommerce;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button joinNowButton, loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

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
