package com.example.gek.pizza.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.services.CourierService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity
        implements View.OnClickListener {

    private LinearLayout llOrders, llReservations;
    private LinearLayout llOrdersDevider, llReservationsDevider;
    private static final String TAG = "MAIN_MENU";

    @Override
    public void updateUI() {
        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_NULL:
            case Const.AUTH_USER:
                llOrdersDevider.setVisibility(View.GONE);
                llOrders.setVisibility(View.GONE);
                llReservations.setVisibility(View.VISIBLE);
                llReservationsDevider.setVisibility(View.VISIBLE);
                break;
            case Const.AUTH_COURIER:
                llOrdersDevider.setVisibility(View.GONE);
                llOrders.setVisibility(View.GONE);
                llReservations.setVisibility(View.GONE);
                llReservationsDevider.setVisibility(View.GONE);
                break;
            case Const.AUTH_SHOP:
                llOrdersDevider.setVisibility(View.VISIBLE);
                llOrders.setVisibility(View.VISIBLE);
                llReservations.setVisibility(View.VISIBLE);
                llReservationsDevider.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.content_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        findViewById(R.id.llMenuOrder).setOnClickListener(this);
        findViewById(R.id.llNews).setOnClickListener(this);
        findViewById(R.id.llContacts).setOnClickListener(this);
        llOrdersDevider = (LinearLayout) findViewById(R.id.llOrdersDevider);
        llReservationsDevider = (LinearLayout) findViewById(R.id.llReservationsDevider);
        llOrders = (LinearLayout) findViewById(R.id.llOrders);
        llOrders.setOnClickListener(this);
        llReservations = (LinearLayout) findViewById(R.id.llReservations);
        llReservations.setOnClickListener(this);

        // Fetch preferences of app
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        ValueEventListener settingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    // get admin email
                    if (child.getKey().equals(Const.SETTINGS_ADMIN_EMAIL_KEY)) {
                        String currentAdminEmail = sharedPreferences.getString(
                                Const.SETTINGS_ADMIN_EMAIL_KEY,
                                Const.ADMIN_EMAIL_BY_DEFAULT);
                        if (!currentAdminEmail.equals(child.getValue().toString())){
                            Connection.getInstance().setShopEmail(child.getValue().toString());
                        }
                    }
                    // get courier email
                    if (child.getKey().equals(Const.SETTINGS_COURIER_EMAIL_KEY)) {
                        String currentCourierEmail = sharedPreferences.getString(
                                Const.SETTINGS_COURIER_EMAIL_KEY,
                                Const.COURIER_EMAIL_BY_DEFAULT);
                        if (!currentCourierEmail.equals(child.getValue().toString())){
                            Connection.getInstance().setCourierEmail(child.getValue().toString());
                        }
                    }
                    editor.putString(child.getKey(), child.getValue().toString()).apply();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        Const.db.child(Const.CHILD_SETTINGS).addValueEventListener(settingsListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_NULL) ||
                (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER)){
            menu.add(0, Const.ACTION_BASKET, 0, R.string.action_basket)
                    .setIcon(R.drawable.ic_basket)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Const.ACTION_BASKET:
                startActivity(new Intent(this, BasketActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llMenuOrder:
                Intent menuIntent = new Intent(this, MenuGroupsActivity.class);
                startActivity(menuIntent);
                break;
            case R.id.llNews:
                Intent newsIntent = new Intent(this, NewsActivity.class);
                startActivity(newsIntent);
                break;
            case R.id.llOrders:
                startActivity(new Intent(this, DeliveriesActivity.class));
                break;
            case R.id.llContacts:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.llReservations:
                startActivity(new Intent(this, ReserveTableActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_COURIER){
            stopService(new Intent(this, CourierService.class));
        }
        super.onDestroy();
    }
}
