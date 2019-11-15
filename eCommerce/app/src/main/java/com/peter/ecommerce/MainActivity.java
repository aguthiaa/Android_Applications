package com.peter.ecommerce;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button joinNowButton, loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
    }



    private void initializeViews() {

        joinNowButton = (Button) findViewById(R.id.welcome_page_register_button);
        loginButton= (Button) findViewById(R.id.welcome_page_login_button);
    }


}
