package com.example.gek.pizza.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * О заведении: контакты, карта
 * Контактные данные должны иметь возможность корректироваться через админскую часть программы
 * Данные хранить в БД в отдельной ветке
 */

public class AboutActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView tvPhone, tvEmail, tvAddress, tvLatitude, tvLongitude;
    String textPhone, textEmail, textAddress, textLatitude, textLongitude;
    public static final String TAG = "Init map :";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        try {
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fMapView);
            if(mapFragment !=null) {
                mapFragment.getMapAsync(this);
            }
        } catch (NullPointerException exception){
            Log.e(TAG, exception.toString());
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_about);
        setSupportActionBar(myToolbar);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        textAddress   = sharedPreferences.getString(Const.SETTINGS_ADDRESS_KEY, "");
        textEmail     = sharedPreferences.getString(Const.SETTINGS_EMAIL_KEY, "");
        textLatitude  = sharedPreferences.getString(Const.SETTINGS_LATITUDE_KEY, "");
        textLongitude = sharedPreferences.getString(Const.SETTINGS_LONGITUDE_KEY, "");
        textPhone     = sharedPreferences.getString(Const.SETTINGS_PHONE_KEY, "");

        tvPhone = (TextView) findViewById(R.id.tvAboutPhone);
        tvEmail = (TextView) findViewById(R.id.tvAboutEmail);
        tvAddress = (TextView) findViewById(R.id.tvAboutAddress);
//        tvLatitude = (TextView) findViewById(R.id.tvSettingsLatitude);
//        tvLongitude = (TextView) findViewById(R.id.tvSettingsLongitude);

        setSettingsToView(tvPhone, textPhone);
        setSettingsToView(tvEmail, textEmail);
        setSettingsToView(tvAddress, textAddress);
//        setSettingsToView(tvLatitude, textLatitude);
//        setSettingsToView(tvLongitude, textLongitude);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (map != null) {
            LatLng pizza = new LatLng(Double.parseDouble(textLatitude), Double.parseDouble(textLongitude));

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pizza, Const.ZOOM_MAP));
            map.addMarker(new MarkerOptions()
                    .title(getString(R.string.hint_on_map))
                    .snippet(getString(R.string.snipset_on_map))
                    .position(pizza));
        }
    }

    public void setSettingsToView(TextView view, String setting) {
        if (setting.isEmpty()) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(setting);
        }
    }

}
