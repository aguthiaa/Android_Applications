package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.ecommerce1.Model.Products;
import com.peter.ecommerce1.ViewHolder.ProductViewHolder;
import com.squareup.picasso.Picasso;

public class SearchProductsActivity extends AppCompatActivity
{
    private EditText productNameInput;
    private Button searchButton;
    private RecyclerView searchRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseReference productsRef;

    private String searchInput;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_products);

        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        searchRecyclerView = (RecyclerView) findViewById(R.id.search_results_recycler_view);
        searchRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(layoutManager);

        productNameInput = (EditText) findViewById(R.id.search_product_name);
        searchButton = (Button) findViewById(R.id.search_product_name_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                searchInput = productNameInput.getText().toString().trim();
                onStart();

            }
        });


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Products> options =
                new FirebaseRecyclerOptions.Builder<Products>().setQuery(productsRef.orderByChild("productName").startAt(searchInput),Products.class).build();

        FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, final int position, @NonNull Products model)
            {
                holder.productNameText.setText(model.getProductName());
                holder.productDescriptionText.setText(model.getProductDescription());
                holder.productPrice.setText("Price = Ksh. "+model.getProductPrice());

                Picasso.get().load(model.getProductImage()).into(holder.productImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String productID = getRef(position).getKey();

                        Intent intent = new Intent(SearchProductsActivity.this,ProductDetailsActivity.class);
                        intent.putExtra("pid",productID);
                        startActivity(intent);

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

        searchRecyclerView.setAdapter(adapter);
        adapter.startListening();


    }
}
