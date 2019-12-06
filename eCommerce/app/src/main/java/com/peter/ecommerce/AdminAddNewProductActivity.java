package com.peter.ecommerce;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private ImageView newproductImage;
    private EditText inputProductName, inputProductDescription, inputProductPrice;
    private Button addNewProduct;

    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        initViews();
        categoryName=getIntent().getExtras().get("category").toString();
        //Toast.makeText(this, categoryName, Toast.LENGTH_LONG).show();
    }

    private void initViews() {

        newproductImage = (ImageView) findViewById(R.id.select_product_image);
        inputProductName = (EditText) findViewById(R.id.product_name);
        inputProductDescription = (EditText) findViewById(R.id.product_description);
        inputProductPrice = (EditText) findViewById(R.id.product_price);
        addNewProduct = (Button) findViewById(R.id.add_new_product);
    }
}
