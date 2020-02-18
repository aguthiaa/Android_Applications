package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AdminManageProductsActivity extends AppCompatActivity
{
    private ImageView changeImage;
    private EditText pName, pPrice,pDescription;
    private Button applyChanges,deleteProduct;

    private DatabaseReference productsRef;

    private String productID = "";

    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_products);

        mDialog = new ProgressDialog(this);
        productID= getIntent().getStringExtra("pid");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productID);

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.exists())
                {
                    String name = dataSnapshot.child("productName").getValue().toString();
                    pName.setText(name);

                    String price = dataSnapshot.child("productPrice").getValue().toString();
                    pPrice.setText(price);

                    String description = dataSnapshot.child("productDescription").getValue().toString();
                    pDescription.setText(description);

                    String image = dataSnapshot.child("productImage").getValue().toString();
                    Picasso.get().load(image).into(changeImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        initViews();
    }

    private void initViews()
    {
        changeImage = (ImageView) findViewById(R.id.manage_product_image);
        pName = (EditText) findViewById(R.id.manage_product_name);
        pPrice = (EditText) findViewById(R.id.manage_product_price);
        pDescription = (EditText) findViewById(R.id.manage_product_description);
        applyChanges = (Button) findViewById(R.id.manage_changes_btn);
        deleteProduct = (Button) findViewById(R.id.manage_delete_btn);


        deleteProduct.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deeleteProduct();
            }
        });



        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mDialog.setTitle("Update Product Details");
                mDialog.setMessage("Please wait...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                String name = pName.getText().toString();
                String price = pPrice.getText().toString();
                String description = pDescription.getText().toString();

                if (!name.isEmpty())
                {
                    if (!price.isEmpty())
                    {
                        if (!description.isEmpty())
                        {
                            updateData(name,price,description);
                        }
                        else
                        {
                            pDescription.setError("Product Description is required");
                            pDescription.requestFocus();
                            mDialog.dismiss();
                        }
                    }
                    else
                    {
                        pPrice.setError("Product Price ios required");
                        pPrice.requestFocus();
                        mDialog.dismiss();
                    }
                }
                else
                {
                    pName.setError("Product Name is required");
                    pName.requestFocus();
                    mDialog.dismiss();
                }
            }
        });

    }


    private void deeleteProduct()
    {
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        productsRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(AdminManageProductsActivity.this, "Product removed successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminManageProductsActivity.this, SellerProductsCategoryActivity.class);
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
             String error = e.getMessage();
                Toast.makeText(AdminManageProductsActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }



    private void updateData(String name, String price, String description)
    {
        HashMap<String,Object> productsMap = new HashMap<>();
        productsMap.put("pid",productID);
        productsMap.put("productName",name);
        productsMap.put("productPrice",price);
        productsMap.put("productDescription",description);

        productsRef.updateChildren(productsMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String error = e.getMessage();

                        Toast.makeText(AdminManageProductsActivity.this, error, Toast.LENGTH_LONG).show();
                        mDialog.dismiss();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(AdminManageProductsActivity.this, "Product Details updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminManageProductsActivity.this, SellerProductsCategoryActivity.class);
                    startActivity(intent);
                    finish();
                    mDialog.dismiss();
                }

            }
        });
    }


}
