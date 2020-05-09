package com.peter.skypeclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.skypeclone.model.FindFriends;
import com.squareup.picasso.Picasso;

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

    private String currentUser;
    private DatabaseReference contactsRef, usersRef;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

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

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView contactImage;
        TextView contactName;
        Button videoCallBtn;

        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            contactImage = itemView.findViewById(R.id.contacts_design_profile_image);
            contactName = itemView.findViewById(R.id.contacts_design_username);
            videoCallBtn = itemView.findViewById(R.id.call_btn);

        }
    }

    @Override
    protected void onStart()
    {
        validateUser();

        super.onStart();

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(contactsRef.child(currentUser), FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriends, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull FindFriends model)
                    {
                        final String userID = getRef(position).getKey();

                        DatabaseReference contactTypeRef = getRef(position).child("Contact").getRef();

                        contactTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("Saved"))
                                    {
                                        usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {

                                                String image = dataSnapshot.child("profileImage").getValue().toString();
                                                String name = dataSnapshot.child("userName").getValue().toString();

                                                Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.contactImage);
                                                holder.contactName.setText(name);

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });

                        holder.videoCallBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent callIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                                callIntent.putExtra("User ID",userID);
                                startActivity(callIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_design, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);

                        return viewHolder;
                    }
                };
        adapter.startListening();
        contactsList.setAdapter(adapter);
    }



    private void validateUser()
    {
        usersRef.child(currentUser).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists())
                {
                    Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingsIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
