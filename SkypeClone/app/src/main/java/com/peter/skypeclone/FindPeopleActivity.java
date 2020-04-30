package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.skypeclone.model.FindFriends;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {
    private EditText searchText;
    private RecyclerView searchResulstsList;
    private RelativeLayout cardItemsLayout;
    private String str = "";
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchText = findViewById(R.id.find_friends_search_input);
        searchResulstsList = findViewById(R.id.find_friends_list);

        searchResulstsList.setLayoutManager(new LinearLayoutManager(this));
        searchResulstsList.setHasFixedSize(true);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                String searchTextInput = searchText.getText().toString().trim();

                if (!searchTextInput.isEmpty())
                {
                    str = charSequence.toString();
                    onStart();
                }
                else
                {
                    searchText.setError("Enter a name to begin search");
                    searchText.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView cProfileImage;
        TextView cUsername;
        Button callBtn;
        public FindFriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            cProfileImage = itemView.findViewById(R.id.contacts_design_profile_image);
            cUsername = itemView.findViewById(R.id.contacts_design_username);
            callBtn = itemView.findViewById(R.id.call_btn);

            callBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<FindFriends> options = null;

        if (str.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                    .setQuery(usersRef, FindFriends.class)
                    .build();
        }

        else
        {
            options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                    .setQuery(usersRef.orderByChild("userName")
                            .startAt(str)
                            .endAt(str + "\uf8ff")
                             , FindFriends.class)
                    .build();
        }

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull final FindFriends model)
            {
                holder.cUsername.setText(model.getUserName());
                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.profile_image).into(holder.cProfileImage);

                final String userID = getRef(position).getKey();
               holder.itemView.setOnClickListener(new View.OnClickListener()
               {
                   @Override
                   public void onClick(View view)
                   {
                       Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
                       intent.putExtra("sentUserID", userID);
                       intent.putExtra("sentUserName", model.getUserName());
                       intent.putExtra("sentUserImage", model.getProfileImage());
                       startActivity(intent);
                   }
               });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_design,parent,false);

                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);

                return viewHolder;
            }
        };

        adapter.startListening();
        searchResulstsList.setAdapter(adapter);
    }
}