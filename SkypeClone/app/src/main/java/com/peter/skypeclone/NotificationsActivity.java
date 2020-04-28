package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationsActivity extends AppCompatActivity
{
    private RecyclerView notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsList = findViewById(R.id.notifications_recycler_view);
        notificationsList.setLayoutManager(new LinearLayoutManager(this));
        notificationsList.setHasFixedSize(true );
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView friendProfileImage;
        TextView friendProfileName;
        Button acceptRequest, cancelRequest;
        RelativeLayout cardItemsLayout;

        public NotificationsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            friendProfileImage = itemView.findViewById(R.id.friends_profile_image);
            friendProfileName = itemView.findViewById(R.id.friends_username);
            acceptRequest = itemView.findViewById(R.id.accept_friend_request_btn);
            cancelRequest = itemView.findViewById(R.id.cancel_friend_request_btn);
            cardItemsLayout = itemView.findViewById(R.id.card_items_layout);
        }
    }
}
