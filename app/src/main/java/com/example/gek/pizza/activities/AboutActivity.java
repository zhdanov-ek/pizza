package com.example.gek.pizza.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.helpers.Utils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fMapView);
        mapFragment.getMapAsync(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_about);
        setSupportActionBar(myToolbar);

        textAddress = Utils.getSetting(this, Const.SETTINGS_ADDRESS_KEY);
        textEmail = Utils.getSetting(this, Const.SETTINGS_EMAIL_KEY);
        textLatitude = Utils.getSetting(this, Const.SETTINGS_LATITUDE_KEY);
        textLongitude = Utils.getSetting(this, Const.SETTINGS_LONGITUDE_KEY);
        textPhone = Utils.getSetting(this, Const.SETTINGS_PHONE_KEY);

        tvPhone = (TextView) findViewById(R.id.tvSettingsPhone);
        tvEmail = (TextView) findViewById(R.id.tvSettingsEmail);
        tvAddress = (TextView) findViewById(R.id.tvSettingsAddress);
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
        LatLng pizza = new LatLng(Double.parseDouble(textLatitude),Double.parseDouble(textLongitude));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pizza, Const.ZOOM_MAP));
        map.addMarker(new MarkerOptions()
                .title(getString(R.string.hint_on_map))
                .snippet(getString(R.string.snipset_on_map))
                .position(pizza));
    }

    public void setSettingsToView(TextView view, String setting) {
        if (setting.isEmpty()) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(setting);
        }
    }

}
