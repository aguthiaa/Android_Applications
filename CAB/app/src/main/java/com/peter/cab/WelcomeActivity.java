package com.peter.cab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity
{
    private Button driverBtn, customerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initViews();
    }


    private void initViews()
    {
        driverBtn = findViewById(R.id.im_a_driver);
        customerBtn=findViewById(R.id.im_a_customer);

        driverBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                toDriversActivity();
            }
        });

        customerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                toCustomerActivity();
            }
        });
    }

    private void toCustomerActivity()
    {
        Intent intent = new Intent(WelcomeActivity.this, CustomerLoginRegisterActivity.class);
        startActivity(intent);
    }

    private void toDriversActivity()
    {
        Intent intent = new Intent(WelcomeActivity.this, DriverLoginRegisterActivity.class);
        startActivity(intent);
    }

}
