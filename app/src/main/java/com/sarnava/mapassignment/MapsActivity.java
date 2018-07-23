package com.sarnava.mapassignment;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    Double latti,longi;
    int count=0;
    private GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    String[] Permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    FloatingActionButton fab,fab_lo;
    NameViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_lo = (FloatingActionButton) findViewById(R.id.lo);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mModel = ViewModelProviders.of(this).get(NameViewModel.class);

        // Create the observer which updates the UI.
        final Observer<List<String>> nameObserver = new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> list) {
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);

                String la = list.get(0);
                String lat[] = la.split(",");
                String lo = list.get(1);
                String lng[] = lo.split(",");

                for(int i =0; i< lat.length ;i++){
                    LatLng ll = new LatLng(Double.parseDouble(lat[i]), Double.parseDouble(lng[i]));
                    options.add(ll);
                }
                mMap.clear();
                mMap.addPolyline(options);

            }
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mModel.getCurrentName().observe(this, nameObserver);


        //toggle tracker on/off
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("Tracker", Context.MODE_PRIVATE);
                boolean b= sp.getBoolean("istrackerOn", true);
                SharedPreferences.Editor editor= sp.edit();

                SharedPreferences loc = getSharedPreferences("Location", Context.MODE_PRIVATE);
                SharedPreferences.Editor e = loc.edit();
                String latitude = loc.getString("lat","");
                String longitude = loc.getString("lng","");
                String lat[] = latitude.split(",");
                String lng[] = longitude.split(",");

                String status;

                if(b){
                    editor.putBoolean("istrackerOn",false);
                    status="off";

                    try {
                        LatLng ll1 = new LatLng(Double.parseDouble(lat[0]), Double.parseDouble(lng[0]));
                        MarkerOptions marker1 = new MarkerOptions().position(ll1)
                                .title("Start Point")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                        mMap.addMarker(marker1);

                        LatLng ll2 = new LatLng(Double.parseDouble(lat[lat.length - 1]), Double.parseDouble(lng[lat.length - 1]));
                        MarkerOptions marker2 = new MarkerOptions().position(ll2)
                                .title("End Point")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        mMap.addMarker(marker2);
                    }catch (Exception ex){
                        Toast.makeText(getApplicationContext(),"In same place",Toast.LENGTH_SHORT).show();
                    }

                    fab.setImageResource(R.mipmap.ic_track_off);
                }else {
                    e.putString("lat", "");
                    e.putString("lng", "");
                    e.apply();

                    editor.putBoolean("istrackerOn",true);
                    status="on";
                    mMap.clear();

                    fab.setImageResource(R.mipmap.ic_track_on);
                }
                editor.apply();
                Toast.makeText(getApplicationContext(),"Tracker is "+ status,Toast.LENGTH_SHORT).show();
            }
        });

        //log out
        fab_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("Info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor= sp.edit();
                editor.putBoolean("isloggedin",false);
                editor.apply();
                startActivity(new Intent(getApplicationContext(),RegActivity.class));
                finish();
            }
        });


        if (!hasPermissions(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, 1);
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        SharedPreferences sp = getSharedPreferences("Tracker", Context.MODE_PRIVATE);
        boolean b= sp.getBoolean("istrackerOn", true);

        //if tracker if off, we are displaying the already tracked path stored in sp
        if(!b) {
            fab.setImageResource(R.mipmap.ic_track_off);

            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            SharedPreferences loc = getSharedPreferences("Location", Context.MODE_PRIVATE);
            String latitude = loc.getString("lat","");
            String longitude = loc.getString("lng","");
            String lat[] = latitude.split(",");
            String lng[] = longitude.split(",");

            try{
                for(int i=0; i< lat.length ;i++){
                    LatLng point = new LatLng( Double.parseDouble(lat[i]), Double.parseDouble(lng[i]) );
                    options.add(point);
                }
                mMap.addPolyline(options);
            }catch (Exception e){}

            if(lat.length > 2){
                //marking the starting and ending point
                LatLng ll1 = new LatLng( Double.parseDouble(lat[0]), Double.parseDouble(lng[0]) );
                MarkerOptions marker1 = new MarkerOptions().position(ll1)
                        .title("Start Point")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                mMap.addMarker(marker1);

                LatLng ll2 = new LatLng( Double.parseDouble(lat[lat.length-1]), Double.parseDouble(lng[lat.length-1]) );
                MarkerOptions marker2 = new MarkerOptions().position(ll2)
                        .title("End Point")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mMap.addMarker(marker2);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }


    public static boolean hasPermissions(Context context, String... permissions){

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && context!=null && permissions!=null){
            for(String permission: permissions){
                if(ActivityCompat.checkSelfPermission(context, permission)!= PackageManager.PERMISSION_GRANTED){
                    return  false;
                }
            }
        }
        return true;
    }


    @Override
    public void onLocationChanged(Location location) {
        SharedPreferences loc = getSharedPreferences("Location", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = loc.edit();
        String latitude = loc.getString("lat","");
        String longitude = loc.getString("lng","");

        latti=location.getLatitude();
        longi=location.getLongitude();

        if(count==0) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latti, longi))
                    .zoom(19)
                    .bearing(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            ++count;
        }

        SharedPreferences sp = getSharedPreferences("Tracker", Context.MODE_PRIVATE);
        boolean b= sp.getBoolean("istrackerOn", true);
        if(b) {

            latitude += latti+",";
            longitude += longi+",";

            e.putString("lat", latitude);
            e.putString("lng", longitude);
            e.apply();

            //storing the latitude and longitude in a list
            List<String> list = new ArrayList<>();
            list.add(0,latitude);
            list.add(1,longitude);
            mModel.getCurrentName().setValue(list);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);  // 1 sec
        locationRequest.setSmallestDisplacement(2.5F);  // 2.5 meters
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Permissions, 1);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}






















