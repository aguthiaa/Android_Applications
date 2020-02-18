package com.peter.ecommerce1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class  SellerProductsCategoryActivity extends AppCompatActivity
{
    private ImageView tShirt,sportShirt,femaleDress,sweather,glasses,wallet,hat,shoes,headPhones,laptop,watch,mobilePhone;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        initViews();

    }



    private void initViews()
    {

        tShirt = (ImageView) findViewById(R.id.category_tShirts);
        sportShirt = (ImageView) findViewById(R.id.category_sports);
        femaleDress = (ImageView) findViewById(R.id.category_felame_dresses);
        sweather = (ImageView) findViewById(R.id.category_sweathers);
        glasses = (ImageView) findViewById(R.id.category_glasses);
        wallet = (ImageView) findViewById(R.id.category_purses_bags_wallets);
        hat = (ImageView) findViewById(R.id.category_hats);
        shoes = (ImageView) findViewById(R.id.category_shoes);
        headPhones = (ImageView) findViewById(R.id.category_headphones);
        laptop = (ImageView) findViewById(R.id.category_laptops);
        watch = (ImageView) findViewById(R.id.category_watches);
        mobilePhone = (ImageView) findViewById(R.id.category_mobile_phone);


        tShirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","TShirt");
                startActivity(intent);
            }
        });

        sportShirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Sport Shirt");
                startActivity(intent);
            }
        });


        femaleDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Female Dress");
                startActivity(intent);
            }
        });

        sweather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Sweather");
                startActivity(intent);
            }
        });


        glasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Glasses");
                startActivity(intent);
            }
        });

        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Wallet");
                startActivity(intent);
            }
        });

        hat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Hats");
                startActivity(intent);
            }
        });

        shoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Shoes");
                startActivity(intent);
            }
        });

        headPhones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Head Phones");
                startActivity(intent);
            }
        });

        laptop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Laptops");
                startActivity(intent);
            }
        });

        watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Watches");
                startActivity(intent);
            }
        });

        mobilePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SellerProductsCategoryActivity.this, SellerAddNewProductActivity.class);
                intent.putExtra("category","Mobile Phone");
                startActivity(intent);
            }
        });

    }


}
