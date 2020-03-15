package com.peter.cab;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CustomerLoginRegisterActivity extends AppCompatActivity
{
    private TextView textTitle, dontHaveAnAccount, alreadyHaveAnAccount;
    private EditText cEmail, cPassword;
    private Button cLoginBtn, cRegisterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        initViews();
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
    }
}
