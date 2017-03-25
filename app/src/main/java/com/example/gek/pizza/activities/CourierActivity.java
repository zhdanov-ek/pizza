package com.example.gek.pizza.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.LastPosition;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.REQUEST_CODE_LOCATION;

public class CourierActivity extends FragmentActivity
    implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "COURIER_ACTIVITY";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LastPosition mPositionCourier;
    private LatLng mClientLocation;
    private BitmapDescriptor bdPizza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);

        // get location courier from DB
        Const.db.child(Const.CHILD_COURIER).addValueEventListener(mPositionCourierListener);

//        // get current location of client
//        mClientLocation = getCurrentLocation();*/

        // Устанавливаем колбек для найденного фрагмента, который сработает после загрузки карты
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

    // Инициализировалась карта - подключаем GoogleApiClient
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mGoogleApiClient.connect();
    }

    // Подключился GoogleApiClient
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationAndMapSettings();
    }


    /** Устанавливаем лисенер на изменение положения нашего устройства - получение координат клиента */
    private void locationAndMapSettings() {
        if (mGoogleApiClient.isConnected()) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    || (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {

                mMap.setMyLocationEnabled(true);
               // lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(Const.LOCATION_INTERVAL_UPDATE * 1000);  // проверка положение каждые 10 сек.
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }else {
            mGoogleApiClient.connect();
        }
    }


    /** Получили кооринаты устройства - обновляем карту */
    @Override
    public void onLocationChanged(Location location) {
        mClientLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if ((mMap != null) && (mPositionCourier != null)){
            updateUi();
        }
    }


    /** Перерисовываем всю карту */
    private void updateUi(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(mClientLocation)
                .title(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mPositionCourier.getLatitude(), mPositionCourier.getLongitude()))
                .icon(bdPizza)
                .title("Your pizza"));

        // Формируем границы выводимой карты по всем нашим маркерам, что бы они были видны
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(mClientLocation);
        boundsBuilder.include(new LatLng(mPositionCourier.getLatitude(), mPositionCourier.getLongitude()));
        LatLngBounds bounds = boundsBuilder.build();

        // Позиционируем камеру по указанным границам со смещением от краев экрана относительно крайних маркеров
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Const.OFFSET_FROM_EDGES_OF_THE_MAP);
        mMap.moveCamera(cu);
    }


    /** следим за координатами курьера, которые меняются в БД */
    private ValueEventListener mPositionCourierListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mPositionCourier = dataSnapshot.getValue(LastPosition.class);
            Log.d(TAG, "onDataChange: get Location " + mPositionCourier.toString());
            if ((mMap != null) && (mClientLocation != null)) {
                updateUi();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "onCancelled: " + databaseError.getDetails());
        }
    };


    // Разорвалось соединение GoogleApiClient
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    // При завершении работы с активити разываем соединение GoogleApiClient
    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}
