package com.peter.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rey.material.widget.SnackBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.transform.Result;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private ImageView newproductImage;
    private EditText inputProductName, inputProductDescription, inputProductPrice;
    private Button addNewProduct;

    private String categoryName, saveCurrentDate, saveCurrentTime, productRandomKey, downloadImageUrl;
    private String productName, productDescription, productPrice;
    private Uri imageUri;

    private StorageReference productImageRef;
    private DatabaseReference productsRef;

    private ProgressDialog mDialog;

    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        initViews();
        categoryName=getIntent().getExtras().get("category").toString();
        //Toast.makeText(this, categoryName, Toast.LENGTH_LONG).show();

        productImageRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        newproductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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



    private void validateProductData() {

        mDialog.setTitle("Add New Product");
        mDialog.setMessage("Please wait");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        productName = inputProductName.getText().toString().trim();
        productDescription = inputProductDescription.getText().toString().trim();
        productPrice = inputProductPrice.getText().toString().trim();

        if (imageUri != null)
        {

            if (!productName.isEmpty())
            {

                if (!productDescription.isEmpty())
                {

                    if (!productPrice.isEmpty())
                    {

                        storeProductInformation();
                    }
                    else
                        {
                            mDialog.dismiss();
                        inputProductPrice.setError("Product Price is required");
                        inputProductPrice.requestFocus();
                    }
                }
                else
                    {
                        mDialog.dismiss();
                    inputProductDescription.setError("Product Description is required");
                    inputProductDescription.requestFocus();
                }
            }
            else
                {
                    mDialog.dismiss();
                inputProductName.setError("Product Name is required");
                inputProductName.requestFocus();
            }
        }
        else
            {
                mDialog.dismiss();
                Toast.makeText(this, "Product Image is required", Toast.LENGTH_LONG).show();
        }
    }



    private void storeProductInformation() {

//        Calendar calendar = Calendar.getInstance();
//
//        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
//        saveCurrentDate = currentDate.format(calendar.getTime());
//
//        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
//        saveCurrentTime = currentTime.format(calendar.getTime());
//
//        productRandomKey = saveCurrentDate + saveCurrentTime;
//
//        final StorageReference filePath = productImageRef.child(imageUri.getLastPathSegment() + productRandomKey +".jpg");
//
//        final UploadTask uploadTask = filePath.putFile(imageUri);
//
//        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Toast.makeText(AdminAddNewProductActivity.this, "Product Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
//
//
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                mDialog.dismiss();
//                String error = e.getMessage();
//                Toast.makeText(AdminAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
//            }
//        });
//
//
//        final Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//
//                if (!task.isSuccessful())
//                {
//                    throw task.getException();
//                }
//
//                downloadImageUrl = filePath.getDownloadUrl().toString();
////                downloadImageUrl = task.getResult().toString();
//                return filePath.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//
//                if (task.isSuccessful())
//                {
//                    mDialog.dismiss();
//                    downloadImageUrl = task.getResult().toString();
//                    Toast.makeText(AdminAddNewProductActivity.this, "Product Image Url Created successfully", Toast.LENGTH_SHORT).show();
//                    saveProductInfoToDatabase();
//                }
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                mDialog.dismiss();
//                String error = e.getMessage();
//                Toast.makeText(AdminAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
//            }
//        });


        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = productImageRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");
        UploadTask uploadTask = filePath.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                String error = e.getMessage();

                Toast.makeText(AdminAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                mDialog.dismiss();
                Toast.makeText(AdminAddNewProductActivity.this, "Product successfully uploaded to firebase storage", Toast.LENGTH_SHORT).show();
            }
        });

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {

                    throw task.getException();
                }

                downloadImageUrl = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {

                    mDialog.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this, "Product image successfully saved to firebase database", Toast.LENGTH_SHORT).show();
                    downloadImageUrl = task.getResult().toString();
                    saveProductInfoToDatabase();
                } else {
                    mDialog.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this, "An Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                String error = e.getMessage();

                Toast.makeText(AdminAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

    }



    private void saveProductInfoToDatabase() {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pid",productRandomKey);
        hashMap.put("date",saveCurrentDate);
        hashMap.put("time",saveCurrentTime);
        hashMap.put("productImage",downloadImageUrl);
        hashMap.put("productName", productName);
        hashMap.put("productDescription", productDescription);
        hashMap.put("productPrice", productPrice);
        hashMap.put("category", categoryName);

        productsRef.child(productRandomKey).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            mDialog.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "New Product successfully added to Firebase Database", Toast.LENGTH_SHORT).show();

                            sendUserToAdminCategoryActivity();
                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mDialog.dismiss();
                String error = e.getMessage();
                Toast.makeText(AdminAddNewProductActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });
    }


    private void openGallery() {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            newproductImage.setImageURI(imageUri);
        }
    }



    private void initViews() {

        newproductImage = (ImageView) findViewById(R.id.select_product_image);
        inputProductName = (EditText) findViewById(R.id.product_name);
        inputProductDescription = (EditText) findViewById(R.id.product_description);
        inputProductPrice = (EditText) findViewById(R.id.product_price);
        addNewProduct = (Button) findViewById(R.id.add_new_product);

        mDialog = new ProgressDialog(this);
    }

    private void sendUserToAdminCategoryActivity() {

        Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
        startActivity(intent);
    }
}
