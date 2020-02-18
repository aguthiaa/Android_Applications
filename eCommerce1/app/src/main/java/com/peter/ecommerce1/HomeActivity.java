package com.peter.ecommerce1;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.ecommerce1.Model.Products;
import com.peter.ecommerce1.Prevalent.Prevalent;
import com.peter.ecommerce1.ViewHolder.ProductViewHolder;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference productsRef;
    private RecyclerView productsView;

    RecyclerView.LayoutManager layoutManager;

    private AppBarConfiguration mAppBarConfiguration;

    private String userType ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null)
        {
            userType = getIntent().getExtras().get("Admin").toString();
        }



        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        Paper.init(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                if (!userType.equals("Admin")) {
                    Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
                    startActivity(cartIntent);
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_cart, R.id.nav_orders, R.id.nav_category,
                R.id.nav_settings,R.id.nav_logout, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.user_name);
        CircleImageView userProfileImage = headerView.findViewById(R.id.profile_image);

        if (!userType.equals("Admin"))
        {
            userName.setText(Prevalent.currentOnlineUser.getFullName());
            Picasso.get().load(Prevalent.currentOnlineUser.getProfileImage()).placeholder(R.drawable.profile).into(userProfileImage);
        }

        productsView = (RecyclerView) findViewById(R.id.main_recycler_view);

        productsView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        productsView.setLayoutManager(layoutManager);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (!userType.equals("Admin")) {
                    navMenuHandler(menuItem);

                }
                return false;
            }
        });


    }

    private void navMenuHandler(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.nav_cart:
                Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(cartIntent);
                break;

            case R.id.nav_orders:
                Intent searchIntent = new Intent(HomeActivity.this, SearchProductsActivity.class);
                startActivity(searchIntent);
                break;


            case R.id.nav_category:
                Toast.makeText(this, "Category", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:

                Intent settingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);

                break;

            case R.id.nav_logout:

                Paper.book().destroy();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();


                break;

            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_send:
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    @Override
    protected void onStart() {

        FirebaseRecyclerOptions<Products> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Products>().setQuery(productsRef.orderByChild("productStatus").equalTo("approved"),Products.class).build();

        FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Products, ProductViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull final Products model)
            {
                holder.productNameText.setText(model.getProductName());
                holder.productPrice.setText("Price = Ksh. "+model.getProductPrice());
                holder.productDescriptionText.setText(model.getProductDescription());

                Picasso.get().load(model.getProductImage()).into(holder.productImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (userType.equals("Admin"))
                        {
                            Intent intent = new Intent(HomeActivity.this, AdminManageProductsActivity.class);
                            intent.putExtra("pid",model.getPid());
                            startActivity(intent);
                        }
                        else
                        {
                            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
                            intent.putExtra("pid",model.getPid());
                            startActivity(intent);
                        }
                    }
                });

            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout,parent,false);
                ProductViewHolder holder = new ProductViewHolder(view);
                return holder;
            }

        };

        productsView.setAdapter(adapter);
        adapter.startListening();

        super.onStart();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home, menu);
//        return true;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
