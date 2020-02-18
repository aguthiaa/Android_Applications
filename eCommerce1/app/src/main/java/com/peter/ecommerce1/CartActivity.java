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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce1.Model.Cart;
import com.peter.ecommerce1.Prevalent.Prevalent;
import com.peter.ecommerce1.ViewHolder.CartViewHolder;

public class CartActivity extends AppCompatActivity
{
    private TextView cartTotalPrice,orderConfirmationMessage;
    private RecyclerView cartItemsView;
    private RecyclerView.LayoutManager layoutManager;
    private Button proceedToNext;

    private int totalPrice = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();

        cartItemsView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        cartItemsView.setLayoutManager(layoutManager);
    }


    private void initViews()
    {
        cartTotalPrice = (TextView) findViewById(R.id.cart_total_amount);
        orderConfirmationMessage = (TextView) findViewById(R.id.cart_order_message);
        cartItemsView = (RecyclerView) findViewById(R.id.cart_list_items_recycler_view);
        proceedToNext = (Button) findViewById(R.id.cart_proceed_to_check_out);

        proceedToNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price",String.valueOf(totalPrice));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

        checkOrderState();

        final DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartRef.child("User View")
                        .child(Prevalent.currentOnlineUser.getPhoneNumber())
                        .child("Products"),Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter =
                new FirebaseRecyclerAdapter<Cart, CartViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model)
            {

                holder.productNameText.setText(model.getProductName());
                holder.productQuantityText.setText("Quantity: "+model.getQuantity());
                holder.productPriceText.setText("Price: "+model.getProductPrice());

                int oneProductTotalPrice =(Integer.valueOf(model.getProductPrice()) * Integer.valueOf(model.getQuantity()));

                totalPrice = totalPrice + oneProductTotalPrice;

                cartTotalPrice.setText("Total Price =" + "Ksh. "+totalPrice);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Edit",
                                        "Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);

                        builder.setTitle("Cart Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid",model.getPid());
                                    startActivity(intent);
                                }

                                if (i == 1)
                                {
                                    cartRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getPhoneNumber())
                                            .child("Products")
                                            .child(model.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {

                                                    if (task.isSuccessful())
                                                    {
                                                        cartRef.child("Admin View")
                                                                .child(Prevalent.currentOnlineUser.getPhoneNumber())
                                                                .child("Products")
                                                                .child(model.getPid())
                                                                .removeValue()
                                                                .addOnFailureListener(new OnFailureListener()
                                                                {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e)
                                                                    {
                                                                        String error = e.getMessage();
                                                                        Toast.makeText(CartActivity.this, error, Toast.LENGTH_LONG).show();

                                                                    }
                                                                }).addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(CartActivity.this, "Product Removed Successfully", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(CartActivity.this, CartActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }

                                                            }
                                                        });
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            String error = e.getMessage();

                                            Toast.makeText(CartActivity.this, error, Toast.LENGTH_LONG).show();

                                        }
                                    });
                                }

                            }
                        });

                        builder.show();
                    }
                });
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

        cartItemsView.setAdapter(adapter);
        adapter.startListening();


    }
    private void checkOrderState()
    {
        DatabaseReference ordersRef;

        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhoneNumber());

        ordersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String shippingState = dataSnapshot.child("state").getValue().toString();
                    String userName = dataSnapshot.child("fullName").getValue().toString();

                    if (shippingState.equals("shipped"))
                    {
                        cartTotalPrice.setText("Hello, "+userName+" You order has been shipped successfully");
                        cartItemsView.setVisibility(View.GONE);
                        proceedToNext.setVisibility(View.GONE);
                        orderConfirmationMessage.setVisibility(View.VISIBLE);
                        orderConfirmationMessage.setText("Thank You For Shopping With Us,\nYour Order has been shipped successfully");

                        Toast.makeText(CartActivity.this, "You will Continue shopping as soon as you receive your first order", Toast.LENGTH_LONG).show();
                    }
                    else if (shippingState.equals("not shipped"))
                    {
                        cartTotalPrice.setText("Hello, "+userName+" Your Order is being confirmed");
                        cartItemsView.setVisibility(View.GONE);
                        proceedToNext.setVisibility(View.GONE);
                        orderConfirmationMessage.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
