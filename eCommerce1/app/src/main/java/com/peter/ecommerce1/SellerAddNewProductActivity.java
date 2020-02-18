package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SellerAddNewProductActivity extends AppCompatActivity
{

    private String categoryName,pName,pDescription,pPrice,saveCurrentDate,saveCurrentTime,productRandomKey,downloadImageUrl;
    private static final int GalleryPick = 1;
    private Uri imageUri;
    private ProgressDialog mDialog;

    private Button addNewProduct;
    private ImageView productImage;
    private EditText productName,productDescription,productPrice;

    private StorageReference productRef;
    private DatabaseReference productsInfoRef, sellersRef;
    private FirebaseAuth mAuth;

    private String currentOnlineUser, sName,sPhone, sEmail, sAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();

        productRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        mAuth = FirebaseAuth.getInstance();
        currentOnlineUser = mAuth.getCurrentUser().getUid();
        productsInfoRef = FirebaseDatabase.getInstance().getReference().child("Products");
        sellersRef=FirebaseDatabase.getInstance().getReference().child("Sellers").child(currentOnlineUser);

        initViews();


        sellersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.exists())
                {
                    sName = dataSnapshot.child("name").getValue().toString();
                    sPhone = dataSnapshot.child("phone").getValue().toString();
                    sEmail = dataSnapshot.child("email").getValue().toString();
                    sAddress = dataSnapshot.child("address").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }



    private void initViews()
    {
        addNewProduct = (Button) findViewById(R.id.add_new_product);
        productImage = (ImageView) findViewById(R.id.add_product_image);
        productName = (EditText) findViewById(R.id.add_product_name);
        productDescription = (EditText) findViewById(R.id.add_product_description);
        productPrice = (EditText) findViewById(R.id.add_product_price);

        mDialog = new ProgressDialog(this);

        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                openGallery();
            }
        });

        addNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateProductData();
            }
        });

    }



    private void openGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data !=null)
        {
            imageUri = data.getData();

            productImage.setImageURI(imageUri);
        }
    }

    private void validateProductData()
    {
        mDialog.setTitle("Add New Product");
        mDialog.setMessage("Please wait...");
        mDialog.show();
        pName = productName.getText().toString().trim();
        pDescription = productDescription.getText().toString().trim();
        pPrice = productPrice.getText().toString().trim();


        if (imageUri != null)
        {

            if (!pName.isEmpty())
            {
                if (!pDescription.isEmpty())
                {
                    if (!pPrice.isEmpty())
                    {

                        storeProductInformation();

                    }
                    else
                    {
                        productPrice.setError("Product Price is required");
                        productPrice.requestFocus();
                        mDialog.dismiss();
                    }
                }
                else
                {
                    productDescription.setError("Product Description is required");
                    productDescription.requestFocus();
                    mDialog.dismiss();
                }

            }
            else
            {
                productName.setError("Product Name is required");
                productName.requestFocus();
                mDialog.dismiss();
            }

        }
        else
        {
            Toast.makeText(this, "Add a Product Image", Toast.LENGTH_LONG).show();
            mDialog.dismiss();
        }
    }


    private void storeProductInformation()
    {
       Calendar calForDate = Calendar.getInstance();
       SimpleDateFormat currentDate = new SimpleDateFormat("MMM-dd-yyyy");
       saveCurrentDate  =currentDate.format(calForDate.getTime());

       Calendar calForTime = Calendar.getInstance();
       SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
       saveCurrentTime = currentTime.format(calForTime.getTime());

       productRandomKey = saveCurrentDate + saveCurrentTime;

       final StorageReference filePath = productRef.child(imageUri.getLastPathSegment() + productRandomKey +".jpg");

        UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error = e.getMessage();

                Toast.makeText(SellerAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(SellerAddNewProductActivity.this, "Image Uploaded Successfully to Firebase Storage", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                if (!task.isSuccessful())
                {
                    throw task.getException();
                }

                downloadImageUrl = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful())
                {
                    Toast.makeText(SellerAddNewProductActivity.this, "Product Image uploaded successfully to firebase database", Toast.LENGTH_SHORT).show();
                    downloadImageUrl = task.getResult().toString();
                    mDialog.dismiss();
                    
                    saveProductInformationToDatabase();
                }
                else
                {
                    Toast.makeText(SellerAddNewProductActivity.this, "An Error occured", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error =e.getMessage();
                mDialog.dismiss();

                Toast.makeText(SellerAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void saveProductInformationToDatabase() {

        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("category",categoryName);
        productMap.put("productImage",downloadImageUrl);
        productMap.put("productName",pName);
        productMap.put("productDescription",pDescription);
        productMap.put("sellerName",sName);
        productMap.put("sellerEmail",sEmail);
        productMap.put("sellerAddress",sAddress);
        productMap.put("sID",currentOnlineUser);
        productMap.put("sellerPhone",sPhone);
        productMap.put("productPrice",pPrice);
        productMap.put("productStatus","not approved");

        productsInfoRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(SellerAddNewProductActivity.this, SellerProductsCategoryActivity.class);
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                            Toast.makeText(SellerAddNewProductActivity.this, "Product Information Successfully saved in Firebase Database", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(SellerAddNewProductActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error = e.getMessage();

                Toast.makeText(SellerAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }

}
