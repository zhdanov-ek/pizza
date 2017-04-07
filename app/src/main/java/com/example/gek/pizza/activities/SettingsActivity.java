package com.example.gek.pizza.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;

/**
 * Settings for SHOP user. Configure of global settings
 */

public class SettingsActivity extends BaseActivity {

    // If user is not have the SHOP status - close Activity
    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP) {
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_settings);
        setToolbar(getString(R.string.title_settings));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_settings);
        item.setCheckable(true);
        item.setChecked(true);
    }
}
