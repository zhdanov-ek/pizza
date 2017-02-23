package com.example.gek.pizza.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.services.CheckDeliveryService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity
        implements View.OnClickListener {

    CardView cvMenuOrder, cvNews, cvOrders, cvContacts, cvReservations;
    private static final String TAG = "List of settings ";

    @Override
    public void updateUI() {
        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_NULL:
                findViewById(R.id.cvOrders).setVisibility(View.GONE);
                break;
            case Const.AUTH_USER:
                findViewById(R.id.cvOrders).setVisibility(View.GONE);
                break;
            case Const.AUTH_SHOP:
                findViewById(R.id.cvOrders).setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Content inflate in VIEW and put in DrawerLayout
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.content_main, null, false);
        mDrawer.addView(contentView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        cvMenuOrder = (CardView) findViewById(R.id.cvMenuOrder);
        cvMenuOrder.setOnClickListener(this);
        cvNews = (CardView) findViewById(R.id.cvNews);
        cvNews.setOnClickListener(this);
        cvOrders = (CardView) findViewById(R.id.cvOrders);
        cvOrders.setOnClickListener(this);
        cvContacts = (CardView) findViewById(R.id.cvContacts);
        cvContacts.setOnClickListener(this);
        cvReservations = (CardView) findViewById(R.id.cvReservations);
        cvReservations.setOnClickListener(this);

        //todo move to settings
        findViewById(R.id.btnStartService).setOnClickListener(this);
        findViewById(R.id.btnStopService).setOnClickListener(this);


        //Получение настроек приложения
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        ValueEventListener settingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                Log.d(TAG, "Load all list Settings: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
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
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
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
            case R.id.cvMenuOrder:
                Intent menuIntent = new Intent(this, MenuOrdersActivity.class);
                startActivity(menuIntent);
                break;
            case R.id.cvNews:
                Intent newsIntent = new Intent(this, NewsActivity.class);
                startActivity(newsIntent);
                break;
            case R.id.cvOrders:
                startActivity(new Intent(this, DeliveriesActivity.class));
                break;
            case R.id.cvContacts:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.cvReservations:
                startActivity(new Intent(this, ReserveTableActivity.class));
                break;
            //todo переместить запуск сервиса в настройки куда-нибудь
            case R.id.btnStartService:
                startService(new Intent(this, CheckDeliveryService.class));
                break;
            case R.id.btnStopService:
                stopService(new Intent(this, CheckDeliveryService.class));
                break;

        }

    }

}
