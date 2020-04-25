package com.peter.skypeclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity
{
    private BottomNavigationView navView;
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;
    private ImageView findPeople;
    private RecyclerView contactsList;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mAuth = FirebaseAuth.getInstance();

        findPeople = findViewById(R.id.find_people);
        contactsList = findViewById(R.id.contacts_recycler_view);
        contactsList.setLayoutManager(new LinearLayoutManager(this));
        contactsList.setHasFixedSize(true);

        navigationItemSelectedListener= new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                handleMenuItemSelectedListener(menuItem);
                return true;
            }
        };

        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);


        findPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(intent);
            }
        });

    }

    private void handleMenuItemSelectedListener(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.navigation_home:
                break;

            case R.id.navigation_Settings:
                Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.navigation_notifications:
                Intent notificationIntent = new Intent(ContactsActivity.this, NotificationsActivity.class);
                startActivity(notificationIntent);
                break;

            case R.id.navigation_logout:
            mAuth.signOut();
                Intent logoutIntent = new Intent(ContactsActivity.this, RegisterActivity.class);
                startActivity(logoutIntent);
                finish();
                break;
        }
    }

}
