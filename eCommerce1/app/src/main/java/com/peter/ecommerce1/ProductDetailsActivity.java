package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce1.Model.Products;
import com.peter.ecommerce1.Prevalent.Prevalent;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity
{

    private ImageView productImage;
    private TextView productName, productDescription, productPrice;
    private Button addToCart;
    private ElegantNumberButton  productQuantity;

    private String productID = "", state ="Normal";
    private String saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initViews();

        productID = getIntent().getStringExtra("pid");

        getProductDetails(productID);
    }




    private void getProductDetails(String productID)
    {

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        productsRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    Products products = dataSnapshot.getValue(Products.class);

                    productName.setText(products.getProductName());
                    productDescription.setText(products.getProductDescription());
                    productPrice.setText(products.getProductPrice());

                    Picasso.get().load(products.getProductImage()).into(productImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkOrderState();
    }

    private void initViews()
    {

        productImage = (ImageView) findViewById(R.id.product_image_details);
        productName = (TextView) findViewById(R.id.product_name_details);
        productDescription = (TextView) findViewById(R.id.product_description_details);
        productPrice = (TextView) findViewById(R.id.product_price_details);
        addToCart = (Button) findViewById(R.id.product_add_to_cart_btn);
        productQuantity = (ElegantNumberButton) findViewById(R.id.product_number_btn);

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (state.equals("order placed") || state.equals("order shipped"))
                {
                    Toast.makeText(ProductDetailsActivity.this, "Wait for you order to be shipped to continue shopping", Toast.LENGTH_LONG).show();
                }
                else
                {
                    addingToCartList();
                }
            }
        });
    }


    private void addingToCartList()
    {
        Calendar calendar = Calendar.getInstance();


        SimpleDateFormat currentDate = new SimpleDateFormat("MMM-dd-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        final HashMap<String,Object> cartMap = new HashMap<>();
        cartMap.put("pid",productID);
        cartMap.put("date",saveCurrentDate);
        cartMap.put("time", saveCurrentTime);
        cartMap.put("productName",productName.getText().toString());
        cartMap.put("quantity",productQuantity.getNumber());
        cartMap.put("productPrice",productPrice.getText().toString());
        cartMap.put("discount","");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhoneNumber()).child("Products").child(productID)
                .updateChildren(cartMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                     if (task.isSuccessful())
                     {

                         cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhoneNumber()).child("Products").child(productID)
                                 .updateChildren(cartMap)
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task)
                                     {

                                         if (task.isSuccessful())
                                         {
                                             Toast.makeText(ProductDetailsActivity.this, "Added to Cart", Toast.LENGTH_LONG).show();

                                             Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
                                             startActivity(intent);
                                         }
                                         else
                                         {
                                             Toast.makeText(ProductDetailsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {

                                 String error = e.getMessage();

                                 Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                             }
                         });
                     }
                     else
                     {
                         Toast.makeText(ProductDetailsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                     }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error = e.getMessage();

                Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
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

                    if (shippingState.equals("shipped"))
                    {
                        state = "order shipped";

                    }
                    else if (shippingState.equals("not shipped"))
                    {

                        state = "order placed";
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
