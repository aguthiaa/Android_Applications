package com.peter.cab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;

    private LatLng latLng;
    private int radius =1;

    private FirebaseAuth mAuth;
    private DatabaseReference customersRef, driversRef, driversLocationRef, driversWorkingRef;
    private String userID;
    private Boolean customerLogoutStatus = false;
    private Boolean driverFound = false;
    private String driverFoundID;

    private Button logoutBtn, settingsBtn, callDriverBtn;

    Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        customersRef = FirebaseDatabase.getInstance().getReference().child("Customers Available");
        driversRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driversWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");



        logoutBtn = findViewById(R.id.customer_logout_btn);
        settingsBtn = findViewById(R.id.customer_settings_btn);
        callDriverBtn = findViewById(R.id.customer_call_to_a_driver);


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                customerLogoutStatus = true;

                mAuth.signOut();
                deauthenticateCustomer();

                customerLogoutIntent();

            }
        });

        callDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                GeoFire geoFire = new GeoFire(customersRef);
                geoFire.setLocation(userID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error)
                    {

                    }
                });

                latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title("My Pickup Location"));

                callDriverBtn.setText("Getting Nearest Drivers...");

                getNearestDrivers();

            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       buildGoogleApiClient();


        mMap.setMyLocationEnabled(true);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
       locationRequest = new LocationRequest();
       locationRequest.setInterval(1000);
       locationRequest.setFastestInterval(1000);
       locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

       LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastLocation = location;

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    private void getNearestDrivers()
    {
        GeoFire geoFire = new GeoFire(driversRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),radius);

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if (!driverFound)
                {
                    driverFound = true;
                    driverFoundID = key;

                    driversLocationRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("onlineCustomerID", userID);
                    driversLocationRef.updateChildren(driverMap);

                    gettingDriverLocation();

                    callDriverBtn.setText("Looking for Driver Location...");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if (!driverFound)
                {
                    radius = radius + 1;
                    getNearestDrivers();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void gettingDriverLocation()
    {
        driversWorkingRef.child(driverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    callDriverBtn.setText("Driver Found.");


                    if (driverLocationMap.get(0) != null)
                    {
                        locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                    }
                    if (driverLocationMap.get(1) != null)
                    {
                        locationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    if (driverMarker != null)
                    {
                        driverMarker.remove();
                    }


                    Location location1 = new Location("");
                    location1.setLatitude(latLng.latitude);
                    location1.setLongitude(latLng.longitude);


                    Location location2 = new Location("");
                    location2.setLatitude(driverLatLng.latitude);
                    location2.setLongitude(driverLatLng.longitude);

                    float distance = location1.distanceTo(location2);

                    callDriverBtn.setText("Driver Found: "+distance+" Away.");


                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver Location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (!customerLogoutStatus)
        {
            deauthenticateCustomer();
        }
    }


    private void deauthenticateCustomer()
    {
        GeoFire geoFire = new GeoFire(customersRef);

        geoFire.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    private void customerLogoutIntent()
    {
        Intent intent = new Intent(CustomersMapActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
