package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FindPeopleActivity extends AppCompatActivity {
    private EditText searchInput;
    private RecyclerView searchResulstsList;
    private RelativeLayout cardItemsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);


        searchInput = findViewById(R.id.find_friends_search_input);
        searchResulstsList = findViewById(R.id.find_friends_list);

        searchResulstsList.setLayoutManager(new LinearLayoutManager(this));
        searchResulstsList.setHasFixedSize(true);
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
        }
    }

}