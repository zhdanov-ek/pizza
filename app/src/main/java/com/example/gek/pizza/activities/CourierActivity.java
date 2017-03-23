package com.example.gek.pizza.activities;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.LastPosition;
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

public class CourierActivity extends FragmentActivity
    implements OnMapReadyCallback{

    public static final String TAG = "COURIER_ACTIVITY";
    private GoogleMap mMap;
    private LastPosition mPositionCourier;
    private LatLng mClientLocation;
    private BitmapDescriptor bdPizza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);

        // get location courier from DB
        Const.db.child(Const.CHILD_COURIER).addValueEventListener(mPositionCourierListener);

        // get current location of client
        mClientLocation = getCurrentLocation();

        // Устанавливаем колбек для найденного фрагмента, который сработает после загрузки карты
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bdPizza = BitmapDescriptorFactory.fromResource(R.drawable.local_pizza_map);

    }

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


    /** Получаем текущее положение */
    public LatLng getCurrentLocation(){
        double latitude;
        double longitude;
        LatLng position = null;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);

        // тут надо проверить разрешение на получение координат прежде чем пробовать получать их
        // или не ставить таржет СДК 23
        Location location = locationManager.getLastKnownLocation(bestProvider);

        // Определяем широту и долготу
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if (latitude != 0) {
            position = new LatLng(latitude, longitude);
        }
        return position;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if ((mClientLocation != null) && (mPositionCourier != null)){
            updateUi();
        }
    }

    /** Update all markers on the map */
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
}
