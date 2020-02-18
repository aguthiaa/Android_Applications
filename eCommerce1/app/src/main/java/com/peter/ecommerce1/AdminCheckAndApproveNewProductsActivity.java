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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.ecommerce1.Model.Products;
import com.peter.ecommerce1.ViewHolder.ProductViewHolder;
import com.squareup.picasso.Picasso;

public class AdminCheckAndApproveNewProductsActivity extends AppCompatActivity
{
    private RecyclerView newProductsRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_check_and_approve_new_products);

        newProductsRecycler = (RecyclerView) findViewById(R.id.new_products_recycler_view);
        newProductsRecycler.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        newProductsRecycler.setLayoutManager(layoutManager);

        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Products> options =
                new FirebaseRecyclerOptions.Builder<Products>()
                        .setQuery(productsRef.orderByChild("productStatus").equalTo("not approved"),Products.class)
                .build();

        FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter =
                new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder holder, final int position, @NonNull Products model)
                    {
                        holder.productNameText.setText(model.getProductName());
                        holder.productPrice.setText(model.getProductPrice());
                        holder.productDescriptionText.setText(model.getProductDescription());

                        Picasso.get().load(model.getProductImage()).into(holder.productImageView);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                final String productID = getRef(position).getKey();

                                CharSequence options[] = new CharSequence[]
                                        {
                                          "Yes",
                                          "No"
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminCheckAndApproveNewProductsActivity.this);
                                builder.setTitle("Are You Sure You Want To Approve This Products?");
                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if (i == 0)
                                        {
                                        changeProductState(productID);
                                        }
                                        else if (i == 1)
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
                    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout,parent,false);

                        ProductViewHolder holder = new ProductViewHolder(view);
                        return holder;
                    }
                };
        newProductsRecycler.setAdapter(adapter);
        adapter.startListening();
    }


    private void changeProductState(String productID)
    {
        productsRef.child(productID).child("productStatus").setValue("approved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(AdminCheckAndApproveNewProductsActivity.this, "Product Approved Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminCheckAndApproveNewProductsActivity.this,AdminCheckAndApproveNewProductsActivity.class);
                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(AdminCheckAndApproveNewProductsActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }
}
