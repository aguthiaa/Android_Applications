 package com.peter.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

 public class MainActivity extends AppCompatActivity
{
    private Toolbar mainToolBar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessAdapter myTabsAccessAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser onlineUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainToolBar = findViewById(R.id.main_page_app_bar_layout);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("WhatsApp");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessAdapter = new TabsAccessAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessAdapter);



        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);


        mAuth = FirebaseAuth.getInstance();
        onlineUser = mAuth.getCurrentUser();


    }


    @Override
    protected void onStart()
    {
        super.onStart();

        if (onlineUser == null)
        {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }
}
