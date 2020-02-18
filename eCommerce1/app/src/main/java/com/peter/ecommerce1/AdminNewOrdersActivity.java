package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce1.Model.Orders;

import java.util.HashMap;

public class AdminNewOrdersActivity extends AppCompatActivity
{
    private RecyclerView ordersRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);

        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        ordersRecyclerView = (RecyclerView) findViewById(R.id.new_orders_recycler_view);

        layoutManager = new LinearLayoutManager(this);
        ordersRecyclerView.setLayoutManager(layoutManager);
        ordersRecyclerView.setHasFixedSize(true);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Orders> options = new FirebaseRecyclerOptions.Builder<Orders>()
                .setQuery(ordersRef, Orders.class)
                .build();

        FirebaseRecyclerAdapter<Orders, OrdersViewHolder> adapter = new FirebaseRecyclerAdapter<Orders, OrdersViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull OrdersViewHolder holder, final int position, @NonNull final Orders model)
            {
                holder.orderName.setText("Name: "+model.getFullName());
                holder.orderPhone.setText("Phone Number "+model.getPhoneNumber());
                holder.totalPrice.setText("Total Price Ksh. "+model.getTotalAmount());
                holder.orderAddress.setText("Shipping Address "+model.getAddress()+", "+model.getCity());
                holder.orderDate.setText("Order Date and Time "+model.getDate()+" "+model.getTime());

                holder.showOrderProducts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String UID = getRef(position).getKey();

                        Intent intent = new Intent(AdminNewOrdersActivity.this, AdminUserOrderProductsActivity.class);
                        intent.putExtra("uid",UID);
                        startActivity(intent);
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Yes",
                                        "No"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(AdminNewOrdersActivity.this);
                        builder.setTitle("Have You Delivered This Order Products?");
                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {

                                if (i == 0)
                                {
                                    String uID = getRef(position).getKey();

                                    removeOrder(uID);
                                }
                                else
                                {
                                    finish();
                                }

                            }
                        });
                        builder.show();


                    }
                });

            }

            @NonNull
            @Override
            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_layout,parent,false);

                OrdersViewHolder holder = new OrdersViewHolder(view);

                return holder;
            }
        };

        ordersRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    private void removeOrder(String uID)
    {
        ordersRef.child(uID).removeValue();
    }


    public static class OrdersViewHolder extends RecyclerView.ViewHolder
    {
        public TextView orderName, orderPhone, totalPrice, orderAddress, orderDate;
        public Button showOrderProducts;

        public OrdersViewHolder(@NonNull View itemView)
        {
            super(itemView);

            orderName = itemView.findViewById(R.id.order_product_name);
            orderPhone = itemView.findViewById(R.id.order_phone_number);
            totalPrice = itemView.findViewById(R.id.order_total_price);
            orderAddress = itemView.findViewById(R.id.order_address_city);
            orderDate = itemView.findViewById(R.id.order_date_time);
            showOrderProducts = itemView.findViewById(R.id.show_order_products);

        }
    }


}
