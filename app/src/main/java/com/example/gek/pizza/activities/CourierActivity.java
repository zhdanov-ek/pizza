package com.example.gek.pizza.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.LastPosition;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.helpers.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.prefs.Preferences;

import static com.example.gek.pizza.data.Const.REQUEST_CODE_LOCATION;

public class CourierActivity extends FragmentActivity
    implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "COURIER_ACTIVITY";
    private LinearLayout llContainer;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LastPosition mPositionCourier;
    private LatLng mClientLocation;
    private BitmapDescriptor bdPizza;
    private float mZoomMap = Const.ZOOM_MAP_COURIER;
    private SharedPreferences sharedPreferences;
    private CameraUpdate mCameraUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);

        // restore saved zoom of camera
        sharedPreferences = getPreferences(MODE_PRIVATE);
        if (sharedPreferences.contains(Const.SETTINGS_ZOOM_COURIER)){
            mZoomMap = sharedPreferences.getFloat(Const.SETTINGS_ZOOM_COURIER, Const.ZOOM_MAP_COURIER);
        }

        llContainer = (LinearLayout) findViewById(R.id.llContainer);

        // get location courier from DB
        Const.db.child(Const.CHILD_COURIER).addValueEventListener(mPositionCourierListener);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bdPizza = BitmapDescriptorFactory.fromResource(R.drawable.local_pizza_map);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    // Map is ready. Check permissions and connect GoogleApiClient
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        connectToGoogleApiClient();
    }

    // GoogleApiClient is connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationAndMapSettings();
    }


    /**  Set listener for get location of client */
    private void locationAndMapSettings() {
        if (mGoogleApiClient.isConnected()) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    || (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {

                mMap.setMyLocationEnabled(true);
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(Const.LOCATION_INTERVAL_UPDATE * 1000);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                updateUi();
            }
        }else {
            connectToGoogleApiClient();
        }
    }


    /** Receive location of device - refresh the map */
    @Override
    public void onLocationChanged(Location location) {
        mClientLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMap != null){
            updateUi();
        }
    }


    /** Refresh the map */
    private void updateUi(){
        if ((mClientLocation == null) || (mPositionCourier == null)){
            return;
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(mClientLocation)
                .title(Connection.getInstance().getUserName()));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mPositionCourier.getLatitude(), mPositionCourier.getLongitude()))
                .icon(bdPizza)
                .title(getResources().getString(R.string.courier)));

        LatLng latLonCourier = new LatLng(mPositionCourier.getLatitude(), mPositionCourier.getLongitude());


        // set current zoom of camera
        if (mMap.getCameraPosition().zoom > Const.ZOOM_MAP_COURIER){
            mZoomMap = mMap.getCameraPosition().zoom;
        } else {
            if (mZoomMap < mMap.getCameraPosition().zoom){
                mZoomMap = Const.ZOOM_MAP_COURIER;
            }
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(latLonCourier, mZoomMap);
            mMap.moveCamera(mCameraUpdate);
        }

        // If courier out of camera - move camera to courier (courier in centre of screen)
        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        if (! latLngBounds.contains(latLonCourier)) {
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(latLonCourier, mZoomMap);
            mMap.moveCamera(mCameraUpdate);
        }
    }


    /** Listen location of courier from DB */
    private ValueEventListener mPositionCourierListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mPositionCourier = dataSnapshot.getValue(LastPosition.class);
            Log.d(TAG, "onDataChange: get Location " + mPositionCourier.toString());
            if (mMap != null) {
                updateUi();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "onCancelled: " + databaseError.getDetails());
        }
    };


    private void connectToGoogleApiClient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Const.REQUEST_CODE_LOCATION);
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            CourierActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION )) {
                        showSnackToSettingsOpen();
                    }
                }
            }
        }
    }


    // If permission can enable from settings OS show SnackBar
    private void showSnackToSettingsOpen(){
        Snackbar.make(llContainer, R.string.permission_location_not_granded, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.openPermissionSettings(getBaseContext());
                    }
                })
                .show();
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(Const.SETTINGS_ZOOM_COURIER, mZoomMap).apply();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}
