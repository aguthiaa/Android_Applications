package com.peter.cab;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DriverLoginRegisterActivity extends AppCompatActivity
{
    private TextView titleText, dontHaveAnAccount,haveAnAccount;
    private EditText dEmail,dPassword;
    private Button loginDriver, registerDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        initViews();
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
    }
}
