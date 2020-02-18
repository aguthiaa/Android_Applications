package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.ecommerce1.Model.Cart;
import com.peter.ecommerce1.Prevalent.Prevalent;
import com.peter.ecommerce1.ViewHolder.CartViewHolder;

public class AdminUserOrderProductsActivity extends AppCompatActivity
{
    private RecyclerView orderDetailsRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseReference cartRef;

    private String orderID ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_order_products);

        orderID = getIntent().getStringExtra("uid");

        orderDetailsRecyclerView = (RecyclerView) findViewById(R.id.order_details_recycler_view);

        orderDetailsRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        orderDetailsRecyclerView.setLayoutManager(layoutManager);


        cartRef = FirebaseDatabase.getInstance().getReference().child("Cart List")
                .child("Admin View")
                .child(orderID)
        .child("Products");
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>().setQuery(cartRef, Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull Cart model)
            {
                holder.productNameText.setText("Product Name: "+model.getProductName());
                holder.productQuantityText.setText("Quantity: "+model.getQuantity());
                holder.productPriceText.setText("Price: "+model.getProductPrice());

            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout,parent,false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        orderDetailsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}
