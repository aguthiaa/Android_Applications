package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.ecommerce1.Model.Products;
import com.peter.ecommerce1.ViewHolder.ProductViewHolder;
import com.peter.ecommerce1.ViewHolder.SellerProductViewHolder;
import com.squareup.picasso.Picasso;

public class SellersHomeActivity extends AppCompatActivity
{
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    private RecyclerView unApprovedProducts;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseReference productsRef;
    private FirebaseAuth mAuth;

    private String currentOnlineUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers_home);

        toolbar = (Toolbar) findViewById(R.id.seller_home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        unApprovedProducts = (RecyclerView) findViewById(R.id.seller_un_approved_products);
        unApprovedProducts.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        currentOnlineUser = mAuth.getCurrentUser().getUid();
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");


        layoutManager = new LinearLayoutManager(SellersHomeActivity.this);
        unApprovedProducts.setLayoutManager(layoutManager);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.seller_bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {

                switch (menuItem.getItemId())
                {
                    case R.id.navigation_home2:

                        toolbar.setTitle("Your Products");
                        return true;



                    case R.id.navigation_dashboard:
                        toolbar.setTitle("Your Products");
                        Intent addIntent = new Intent(SellersHomeActivity.this, SellerProductsCategoryActivity.class);
                        startActivity(addIntent);
                        return true;



                    case R.id.navigation_notifications:
                        toolbar.setTitle("Logout");
                        final FirebaseAuth mAuth;
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        Intent logoutIntent = new Intent(SellersHomeActivity.this, MainActivity.class);
                        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(logoutIntent);
                        finish();

                        return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Products> options =
                new FirebaseRecyclerOptions.Builder<Products>()
                        .setQuery(productsRef.orderByChild("sID").equalTo(currentOnlineUser),Products.class)
                        .build();

        FirebaseRecyclerAdapter<Products, SellerProductViewHolder> adapter =
                new FirebaseRecyclerAdapter<Products, SellerProductViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull SellerProductViewHolder holder, int position, @NonNull Products model)
                    {
                        holder.sName.setText(model.getProductName());
                        holder.sPrice.setText("Price Ksh. "+model.getProductPrice());
                        holder.sDescription.setText(model.getProductDescription());
                        holder.sStatus.setText("Product Status: "+model.getProductStatus());

                        Picasso.get().load(model.getProductImage()).into(holder.sImage);

                        final String productID = getRef(position).getKey();

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                               CharSequence options[] = new CharSequence[]
                                       {

                                               "Yes",
                                               "No"
                                       };

                                AlertDialog.Builder builder = new AlertDialog.Builder(SellersHomeActivity.this);
                                builder.setTitle("Do You Want To Delete this Product?");
                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if (i == 0)
                                        {
                                            removeProduct(productID);
                                        }
                                        else
                                        {

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public SellerProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_items_layout,parent,false);
                        SellerProductViewHolder holder = new SellerProductViewHolder(view);
                        return holder;
                    }
                };
        unApprovedProducts.setAdapter(adapter);
        adapter.startListening();
    }



    private void removeProduct(String productID)
    {
        productsRef.child(productID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(SellersHomeActivity.this, "Product Deleted successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String  error = e.getMessage();
                Toast.makeText(SellersHomeActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }

}
