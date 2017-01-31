package com.example.gek.pizza.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.RetrofitMaps;
import com.example.gek.pizza.data.routes.Example;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.gek.pizza.data.Const.REQUEST_CODE_LOCATION;

/**
 * О заведении: контакты, карта
 * Контактные данные должны иметь возможность корректироваться через админскую часть программы
 * Данные хранить в БД в отдельной ветке
 */

public class AboutActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    TextView tvPhone, tvEmail, tvAddress, tvLatitude, tvLongitude, tvRouteInformationTime,tvRouteInformationDistance;
    RadioButton rbtnDrive, rbtnWalk;
    RadioGroup rgDriveWalk;
    ImageView ivFullScreen;
    ScrollView svAboutUs;
    String textPhone, textEmail, textAddress, textLatitude, textLongitude;
    LatLng myLatLng;

    public static final String TAG = "Init map :";
    private final String PERMISSION_DIALOG_OPEN_KEY = "permission_dialog_opened";
    private final String ROUTE_MARKERS = "route_markers";
    private final String ROUTES_IS_CALCULATED = "Routes_is_calculated";
    private final String FULLSCREEN = "fullscreen";
    private final String DRIVE = "drive";
    private final String WALK = "walk";
    private final String DISTANCE_MAP = "distance_map";
    private final String TIME_MAP = "time_map";

    private boolean isOpenedPermissionDialog = false;
    private boolean isPermissionsGranted = false;
    private boolean isRoutesCalculated = false;
    private boolean isDrive = false;
    private boolean isWalk = false;
    private boolean isFullScreen = false;

    public GoogleMap googleMap;
    public GoogleApiClient googleApiClient;
    public Location lastLocation;
    ArrayList<LatLng> markerPoints;
    Polyline route;
    public List<LatLng> listOfMarkers;
    LatLng pizzeriaLocation;
    private Animation animationArrowRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_about);
        setSupportActionBar(myToolbar);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        textAddress = sharedPreferences.getString(Const.SETTINGS_ADDRESS_KEY, "");
        textEmail = sharedPreferences.getString(Const.SETTINGS_EMAIL_KEY, "");
        textLatitude = sharedPreferences.getString(Const.SETTINGS_LATITUDE_KEY, "");
        textLongitude = sharedPreferences.getString(Const.SETTINGS_LONGITUDE_KEY, "");
        textPhone = sharedPreferences.getString(Const.SETTINGS_PHONE_KEY, "");

        tvPhone = (TextView) findViewById(R.id.tvAboutPhone);
        tvEmail = (TextView) findViewById(R.id.tvAboutEmail);
        tvAddress = (TextView) findViewById(R.id.tvAboutAddress);
        tvRouteInformationDistance = (TextView) findViewById(R.id.tvRouteInformationDistance);
        tvRouteInformationTime = (TextView) findViewById(R.id.tvRouteInformationTime);

        rgDriveWalk = (RadioGroup) findViewById(R.id.rgDriveWalk);
        rbtnDrive = (RadioButton) findViewById(R.id.rbtnDrive);
        rbtnWalk  = (RadioButton) findViewById(R.id.rbtnWalk);
        ivFullScreen  = (ImageView) findViewById(R.id.ivArrow);
        svAboutUs = (ScrollView) findViewById(R.id.svAboutUs);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // проверека доступности гугл сервисов
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);

        markerPoints = new ArrayList<>();

        if (status == ConnectionResult.SUCCESS) {
            try {
                // отрисовка карты
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fMapView);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } catch (NullPointerException exception) {
                Log.e(TAG, exception.toString());
            }
        }



        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentEmail = new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto", textEmail, null));
                startActivity(intentEmail);
            }
        });

        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                callIntent.setData(Uri.parse("tel:"+textPhone));
                startActivity(callIntent);
            }
        });
//        устанавливаем тип маршрута по умолчанию
        if (isWalk==false && isDrive==false){
            isDrive = true;
            rbtnDrive.setChecked(true);
        }
        ivFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFullScreen){
                    svAboutUs.setVisibility(View.VISIBLE);
                    isFullScreen = false;
                    ivFullScreen.setRotation(0);

                } else{
                    svAboutUs.setVisibility(View.GONE);
                    isFullScreen = true;
                    ivFullScreen.setRotation(180);
                }

            }
        });
        rgDriveWalk.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(isDrive==true){
                    isDrive = false;
                    isWalk = true;
                } else{
                    isDrive = true;
                    isWalk = false;
                }
                isRoutesCalculated=false;
                if (isPermissionsGranted) {
                    locationAndMapSettings(); // при изменении типа маршрута обновляем карту
                }
            }
        });

        setSettingsToView(tvPhone, textPhone);
        setSettingsToView(tvEmail, textEmail);
        setSettingsToView(tvAddress, textAddress);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // записываем значение при повороте экрана
        outState.putBoolean(PERMISSION_DIALOG_OPEN_KEY, isOpenedPermissionDialog);
        outState.putBoolean(ROUTES_IS_CALCULATED, isRoutesCalculated);
        outState.putBoolean(DRIVE, isDrive);
        outState.putBoolean(WALK, isWalk);
        outState.putBoolean(FULLSCREEN, isFullScreen);
        outState.putString(DISTANCE_MAP, tvRouteInformationDistance.getText().toString());
        outState.putString(TIME_MAP, tvRouteInformationTime.getText().toString());
        outState.putParcelableArrayList(ROUTE_MARKERS, (ArrayList<LatLng>) listOfMarkers);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            // востанавливаем значение при повороте экрана
            isOpenedPermissionDialog = savedInstanceState.getBoolean(PERMISSION_DIALOG_OPEN_KEY, false);
            isRoutesCalculated = savedInstanceState.getBoolean(ROUTES_IS_CALCULATED, false);
            isDrive = savedInstanceState.getBoolean(DRIVE, false);
            isWalk = savedInstanceState.getBoolean(WALK, false);
            isFullScreen = savedInstanceState.getBoolean(FULLSCREEN, false);
            listOfMarkers = savedInstanceState.getParcelableArrayList(ROUTE_MARKERS);
            tvRouteInformationDistance.setText(savedInstanceState.getString(DISTANCE_MAP,""));
            tvRouteInformationTime.setText(savedInstanceState.getString(TIME_MAP,""));
            if (isFullScreen){
                isFullScreen = false;
                ivFullScreen.callOnClick();
            }
        }
    }

    @Override
    public void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (isPermissionsGranted) {
//            locationAndMapSettings();
//        }
//    }

    @Override
    public void onConnected(Bundle bundle) {
        if (isPermissionsGranted) {
            locationAndMapSettings();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION: {
                isOpenedPermissionDialog = false;
                if ((grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    isPermissionsGranted = true;
                    googleApiClient.connect();
                    locationAndMapSettings();
                } else {
                    isPermissionsGranted = false;
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (map != null) {
            googleMap = map;

            // провека разрешений
            verifyLocationPermissions();
//            if(textLatitude!="" && textLongitude!=""){
                pizzeriaLocation = new LatLng(Double.parseDouble(textLatitude), Double.parseDouble(textLongitude));

                // добавление данных на карту
                addPizzeriaOnMap();
//            }
        }
    }

    public void addPizzeriaOnMap(){
        // добавление маркера с пиццерией на карту
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pizzeriaLocation, Const.ZOOM_MAP));
        googleMap.addMarker(new MarkerOptions()
                .title(getString(R.string.hint_on_map))
                .snippet(getString(R.string.snipset_on_map))
                .position(pizzeriaLocation));

        // добавление маршрута на карту
        if(listOfMarkers!=null){
            if (listOfMarkers.size() > 0) {
                route = googleMap.addPolyline(new PolylineOptions()
                        .addAll(listOfMarkers)
                        .width(15)
                        .color(Color.BLUE)
                        .geodesic(true)
                );

//                позиционирование карты, чтоб старт и финиш были видны на экране
                if (lastLocation !=null){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()), Const.ZOOM_MAP));

                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    boundsBuilder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                    boundsBuilder.include(pizzeriaLocation);

                    LatLngBounds bounds = boundsBuilder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Const.OFFSET_FROM_EDGES_OF_THE_MAP);
                    googleMap.moveCamera(cu);
                }

            }
        } else{
            isRoutesCalculated = false;
        }

    }

    public void setSettingsToView(TextView view, String setting) {
        if (setting.isEmpty()) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(setting);
        }
    }


    private void verifyLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (isOpenedPermissionDialog) {
                return;
            }
            isOpenedPermissionDialog = true;
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Const.REQUEST_CODE_LOCATION);
        } else {
            isPermissionsGranted = true;
            googleApiClient.connect();
            locationAndMapSettings();
        }
    }

    private void locationAndMapSettings() {
        if (googleApiClient.isConnected()) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    || (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {

                        googleMap.setMyLocationEnabled(true);
                        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        LocationRequest locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                                .setInterval(Const.LOCATION_INTERVAL_UPDATE * 1000);  // проверка положение каждые 10 сек.
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                }
         }else {
            googleApiClient.connect();
         }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (markerPoints.size() > 1) {
//            googleMap.clear();
            markerPoints.clear();
            markerPoints = new ArrayList<>();
        }

        if (location != null) {

            if(lastLocation == null){
                lastLocation = location;
            }
            double dist = lastLocation.distanceTo(location);

            if ((dist > Const.LOCATION_DISTANCE_UPDATE && googleMap !=null) || !isRoutesCalculated) {

                lastLocation = location;

                myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                markerPoints.add(myLatLng);
                markerPoints.add(pizzeriaLocation);

                if (isWalk==true){
                    getRoutes("walking");
                } else{
                    getRoutes("driving");
                }


                isRoutesCalculated = true;
            }
        }
    }

    // отправка запроса для получения маршрута от А до В
    private void getRoutes(String type) {

        String url = Const.GOOGLE_DIRECTIONS_API;

        OkHttpClient.Builder builderHttp = new OkHttpClient.Builder();
        builderHttp.readTimeout(15, TimeUnit.SECONDS);
        builderHttp.writeTimeout(15, TimeUnit.SECONDS);
        OkHttpClient client = builderHttp.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);

        Call<Example> call = service.getDistanceDuration("metric", myLatLng.latitude + "," + myLatLng.longitude,pizzeriaLocation.latitude + "," + pizzeriaLocation.longitude, type);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                try {
                    if (route != null) {
                        route.remove();
                    }
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        tvRouteInformationDistance.setText("Dist.:" + distance);
                        tvRouteInformationTime.setText("Time:" + time);
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                        listOfMarkers = decodeRoutes(encodedString);
                        googleMap.clear();
                        addPizzeriaOnMap();

                    }
                } catch (Exception e) {
                    isRoutesCalculated = false;
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                isRoutesCalculated = false;
            }
        });

    }

    // разбор ответа о маршруте
    private List<LatLng> decodeRoutes(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }


}
