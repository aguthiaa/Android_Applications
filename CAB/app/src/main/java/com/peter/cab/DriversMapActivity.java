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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;

    private FirebaseAuth mAuth;
    private FirebaseUser currentOnlineUser;
    private Boolean driverLogoutStatus = false;
    private DatabaseReference driversAvailabilityRef, driversWorkingRef, assignedCustomerRef, assignedCustomerPickupRef;

    private String driverID, customerID="";

    private Button driverSettings, driverLogout;

    Marker driverMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);

        mAuth = FirebaseAuth.getInstance();
        currentOnlineUser = mAuth.getCurrentUser();
        driversAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driversWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        driverID = mAuth.getCurrentUser().getUid();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        driverSettings = findViewById(R.id.driver_settings_btn);
        driverLogout = findViewById(R.id.driver_logout_btn);

        driverLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                driverLogoutStatus = true;

                mAuth.signOut();
                deauthenticateDriverAvailability();

                logoutDriver();

            }
        });

        getAssignedCustomerRequest();
    }



    private void getAssignedCustomerRequest()
    {
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("onlineCustomerID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    customerID = dataSnapshot.getValue().toString();

                    getAssignedCustomerPickupLocation();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void getAssignedCustomerPickupLocation()
    {
        assignedCustomerPickupRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests").child(customerID).child("l");
        assignedCustomerPickupRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();

                    double locationLat = 0;
                    double locationLng = 0;

                    if (customerLocationMap.get(0) != null)
                    {
                        locationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }

                    if (customerLocationMap.get(1) != null)
                    {
                        locationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Pickup Location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

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
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

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
       if (getApplicationContext() != null)
       {
           lastLocation = location;

           LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
           mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
           mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


           GeoFire geoFire = new GeoFire(driversAvailabilityRef);
           GeoFire geoFire1 = new GeoFire(driversWorkingRef);

           switch (customerID)
           {
               case "":
                   geoFire1.removeLocation(driverID, new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   geoFire.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener()
                   {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   break;

                   default:
                       geoFire.removeLocation(driverID, new GeoFire.CompletionListener() {
                           @Override
                           public void onComplete(String key, DatabaseError error) {

                           }
                       });
                       geoFire1.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                           @Override
                           public void onComplete(String key, DatabaseError error)
                           {

                           }
                       });
                       break;
           }
       }

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

    @Override
    protected void onStop() {
        super.onStop();

        if (!driverLogoutStatus)
        {
            deauthenticateDriverAvailability();
        }


    }

    private void deauthenticateDriverAvailability()
    {

            GeoFire geoFire = new GeoFire(driversAvailabilityRef);

            geoFire.removeLocation(driverID, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error)
                {

                }
            });
    }


    private void logoutDriver()
    {
        Intent intent = new Intent(DriversMapActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
