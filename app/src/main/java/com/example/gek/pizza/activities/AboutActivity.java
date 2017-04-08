package com.example.gek.pizza.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.routes.Example;
import com.example.gek.pizza.data.routes.RetrofitMaps;
import com.example.gek.pizza.helpers.Connection;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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
 * About activity: contacts, map
 * Contacts could be change in Admin version
 */

public class AboutActivity extends BaseActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener
        {

    private TextView tvRouteInformationTime, tvRouteInformationDistance,tvCheckPermission;
    private ImageView ivArrowLeft, ivArrowRight;
    private LinearLayout llRouteInfo, llCheckPermission;
    private String textPhone, textEmail, textLatitude, textLongitude;
    private LatLng myLatLng;
    private SlidingUpPanelLayout slidingUpPanelLayout;

    private final String PERMISSION_DIALOG_OPEN_KEY = "permission_dialog_opened";
    private final String ROUTE_MARKERS = "route_markers";
    private final String ROUTES_IS_CALCULATED = "Routes_is_calculated";
    private final String FULLSCREEN = "fullscreen";
    private final String DRIVE = "drive";
    private final String WALK = "walk";
    private final String DISTANCE_MAP = "distance_map";
    private final String TIME_MAP = "time_map";
    private final String LAST_LOCATION_LAT = "last_location_lat";
    private final String LAST_LOCATION_LONG = "last_location_long";

    private final int REQUEST_CODE_GPS_PERMISSION = 1001;

    private boolean isOpenedPermissionDialog = false;
    private boolean isPermissionsGranted = false;
    private boolean isRoutesCalculated = false;
    private boolean isDrive = false;
    private boolean isWalk = false;
    private boolean isFullScreen = false;
    private boolean isPanelExpanded = false;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private ArrayList<LatLng> markerPoints;
    private Polyline route;
    private List<LatLng> listOfMarkers;
    private LatLng pizzeriaLocation;
    private Animation animationArrowRotation;
    private BitmapDescriptor bdPizza;

    @Override
    public void updateUI() {
        if (isPermissionsGranted){
            llCheckPermission.setVisibility(View.GONE);
            llRouteInfo.setVisibility(View.VISIBLE);
            tvCheckPermission.setVisibility(View.GONE);
        } else{
            llCheckPermission.setVisibility(View.VISIBLE);
            llRouteInfo.setVisibility(View.GONE);
            tvCheckPermission.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button btnCheckPermission;
        TextView tvPhone, tvEmail, tvAddress;
        RadioButton rbtnDrive;
        RadioGroup rgDriveWalk;

        String textAddress;

        inflateLayout(R.layout.activity_about);
        setToolbar(getString(R.string.title_about));

        bdPizza = BitmapDescriptorFactory.fromResource(R.drawable.local_pizza_map);
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
        tvCheckPermission = (TextView) findViewById(R.id.tvCheckPermission);

        llRouteInfo = (LinearLayout) findViewById(R.id.llRouteInfo);
        llCheckPermission = (LinearLayout) findViewById(R.id.llCheckPermission);

        findViewById(R.id.btnCheckPermission).setOnClickListener(this);

        rgDriveWalk = (RadioGroup) findViewById(R.id.rgDriveWalk);
        rbtnDrive = (RadioButton) findViewById(R.id.rbtnDrive);
        ivArrowLeft = (ImageView) findViewById(R.id.ivArrowLeft);
        ivArrowRight = (ImageView) findViewById(R.id.ivArrowRight);

        //landscape listener
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingUpPanel);
        if (slidingUpPanelLayout!=null){
            setSlidingUpPanelLayoutListeners();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // check google service availability
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);

        markerPoints = new ArrayList<>();

        if (status == ConnectionResult.SUCCESS) {
            try {
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fMapView);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } catch (NullPointerException exception) {
                exception.printStackTrace();
            }
        }

        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", textEmail, null));
                startActivity(intentEmail);
            }
        });

        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                callIntent.setData(Uri.parse("tel:" + textPhone));
                startActivity(callIntent);
            }
        });

        //type of route
        if (!isWalk && !isDrive) {
            isDrive = true;
            rbtnDrive.setChecked(true);
        }

        rgDriveWalk.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isDrive) {
                    isDrive = false;
                    isWalk = true;
                } else {
                    isDrive = true;
                    isWalk = false;
                }
                isRoutesCalculated = false;
                if (isPermissionsGranted) {
                    locationAndMapSettings(); // refresh route;
                }
            }
        });

        setSettingsToView(tvPhone, textPhone);
        setSettingsToView(tvEmail, textEmail);
        setSettingsToView(tvAddress, textAddress);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCheckPermission:
                openSettings();
                break;
        }
    }

    private void openSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        this.startActivityForResult(intent,REQUEST_CODE_GPS_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_GPS_PERMISSION){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                isPermissionsGranted = false;

            } else{
                isPermissionsGranted = true;
                if (googleMap != null){
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_about);
        item.setCheckable(true);
        item.setChecked(true);


    }

    private void setSlidingUpPanelLayoutListeners() {
        ivArrowLeft = (ImageView) findViewById(R.id.ivArrowLeft);
        ivArrowRight = (ImageView) findViewById(R.id.ivArrowRight);
        animationArrowRotation = AnimationUtils.loadAnimation(this, R.anim.panel_arrows);
        animationArrowRotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPanelExpanded) {
                    ivArrowLeft.setRotation(0);
                    ivArrowRight.setRotation(0);
                    isPanelExpanded = false;
                } else {
                    ivArrowLeft.setRotation(180);
                    ivArrowRight.setRotation(180);
                    isPanelExpanded = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                if ((isPanelExpanded && newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                        || (!isPanelExpanded && newState == SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    ivArrowLeft.startAnimation(animationArrowRotation);
                    ivArrowRight.startAnimation(animationArrowRotation);
                }
            }
        });
        slidingUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PERMISSION_DIALOG_OPEN_KEY, isOpenedPermissionDialog);
        outState.putBoolean(ROUTES_IS_CALCULATED, isRoutesCalculated);
        outState.putBoolean(DRIVE, isDrive);
        outState.putBoolean(WALK, isWalk);
        outState.putBoolean(FULLSCREEN, isFullScreen);
        outState.putString(DISTANCE_MAP, tvRouteInformationDistance.getText().toString());
        outState.putString(TIME_MAP, tvRouteInformationTime.getText().toString());
        outState.putParcelableArrayList(ROUTE_MARKERS, (ArrayList<LatLng>) listOfMarkers);
        if(lastLocation!=null){
            outState.putDouble(LAST_LOCATION_LAT,lastLocation.getLatitude());
            outState.putDouble(LAST_LOCATION_LONG,lastLocation.getLongitude());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            isOpenedPermissionDialog = savedInstanceState.getBoolean(PERMISSION_DIALOG_OPEN_KEY, false);
            isRoutesCalculated = savedInstanceState.getBoolean(ROUTES_IS_CALCULATED, false);
            isDrive = savedInstanceState.getBoolean(DRIVE, false);
            isWalk = savedInstanceState.getBoolean(WALK, false);
            isFullScreen = savedInstanceState.getBoolean(FULLSCREEN, false);
            listOfMarkers = savedInstanceState.getParcelableArrayList(ROUTE_MARKERS);
            tvRouteInformationDistance.setText(savedInstanceState.getString(DISTANCE_MAP, ""));
            tvRouteInformationTime.setText(savedInstanceState.getString(TIME_MAP, ""));

            Double lastLat = savedInstanceState.getDouble(LAST_LOCATION_LAT,0);
            Double lastLong = savedInstanceState.getDouble(LAST_LOCATION_LONG,0);
            if (lastLat!=0 && lastLong!=0){
                lastLocation = new Location("");
                lastLocation.setLatitude(lastLat);
                lastLocation.setLongitude(lastLong);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        if (map != null && !textLatitude.equals("") && !textLongitude.equals("")) {
            googleMap = map;

            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                        Toast.makeText(AboutActivity.this, R.string.mes_enable_gps, Toast.LENGTH_SHORT).show();
                    } else{
                        locationAndMapSettings();
                    }
                    return false;
                }
            });

            // check permissions
            verifyLocationPermissions();
            pizzeriaLocation = new LatLng(Double.parseDouble(textLatitude), Double.parseDouble(textLongitude));
            addPizzeriaOnMap();
        }
    }

    public void addPizzeriaOnMap() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pizzeriaLocation, Const.ZOOM_MAP));

        // add pizzeria marker
        googleMap.addMarker(new MarkerOptions()
                .icon(bdPizza)
                .title(getString(R.string.hint_on_map))
                .snippet(getString(R.string.snipset_on_map))
                .position(pizzeriaLocation));

        // add route on map
        if (listOfMarkers != null) {
            if (listOfMarkers.size() > 0) {
                route = googleMap.addPolyline(new PolylineOptions()
                        .addAll(listOfMarkers)
                        .width(15)
                        .color(Color.BLUE)
                        .geodesic(true)
                );

                // all route is visible on map
                if (lastLocation != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), Const.ZOOM_MAP));

                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    boundsBuilder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                    boundsBuilder.include(pizzeriaLocation);

                    LatLngBounds bounds = boundsBuilder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Const.OFFSET_FROM_EDGES_OF_THE_MAP);
                    googleMap.moveCamera(cu);
                }


            }
        } else {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
                        .setInterval(Const.LOCATION_INTERVAL_UPDATE * 1000);  // interval update
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } else {
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

            if (lastLocation == null) {
                lastLocation = location;
            }
            double dist = lastLocation.distanceTo(location);

            if ((dist > Const.LOCATION_DISTANCE_UPDATE && googleMap != null) || !isRoutesCalculated) {

                lastLocation = location;

                myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                markerPoints.add(myLatLng);
                markerPoints.add(pizzeriaLocation);

                if (isWalk) {
                    getRoutes(Const.TYPE_ROUTE_WALK);
                } else {
                    getRoutes(Const.TYPE_ROUTE_DRIVE);
                }


                isRoutesCalculated = true;
            }
        }
    }

    // google api for routes
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

        Call<Example> call = service.getDistanceDuration(Const.TYPE_METRIC, myLatLng.latitude + "," + myLatLng.longitude, pizzeriaLocation.latitude + "," + pizzeriaLocation.longitude, type);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                try {
                    if (route != null) {
                        route.remove();
                    }
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String distance = getResources().getString(R.string.dist) + response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = getResources().getString(R.string.time) + response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        tvRouteInformationDistance.setText(distance);
                        tvRouteInformationTime.setText(time);
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

    // decode response about route
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

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_NULL) ||
                (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER)) {
            menu.add(0, Const.ACTION_BASKET, 0, R.string.action_basket)
                    .setIcon(R.drawable.ic_basket)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Const.ACTION_BASKET:
                startActivity(new Intent(this, BasketActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
