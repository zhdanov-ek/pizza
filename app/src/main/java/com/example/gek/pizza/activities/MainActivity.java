package com.example.gek.pizza.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.services.ShopService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity
        implements View.OnClickListener {

    private LinearLayout llMenuOrder, llNews, llOrders, llContacts, llReservations;
    private LinearLayout llOrdersDevider;
    private Button btnStartService, btnStopService;
    private static final String TAG = "List of settings ";

    // todo убрать кнопки запуска сервиса, а сервис запускать после аутентификации ЗАВЕДЕНИЯ. Выключать
    // при логауте
    @Override
    public void updateUI() {
        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_NULL:
                llOrdersDevider.setVisibility(View.GONE);
                llOrders.setVisibility(View.GONE);
                btnStartService.setVisibility(View.GONE);
                btnStopService.setVisibility(View.GONE);
                break;
            case Const.AUTH_USER:
                llOrdersDevider.setVisibility(View.GONE);
                llOrders.setVisibility(View.GONE);
                btnStartService.setVisibility(View.GONE);
                btnStopService.setVisibility(View.GONE);
                break;
            case Const.AUTH_SHOP:
                llOrdersDevider.setVisibility(View.VISIBLE);
                llOrders.setVisibility(View.VISIBLE);
                btnStartService.setVisibility(View.VISIBLE);
                btnStopService.setVisibility(View.VISIBLE);
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

        llMenuOrder = (LinearLayout) findViewById(R.id.llMenuOrder);
        llMenuOrder.setOnClickListener(this);
        llNews = (LinearLayout) findViewById(R.id.llNews);
        llNews.setOnClickListener(this);
        llOrdersDevider = (LinearLayout) findViewById(R.id.llOrdersDevider);
        llOrders = (LinearLayout) findViewById(R.id.llOrders);
        llOrders.setOnClickListener(this);
        llContacts = (LinearLayout) findViewById(R.id.llContacts);
        llContacts.setOnClickListener(this);
        llReservations = (LinearLayout) findViewById(R.id.llReservations);
        llReservations.setOnClickListener(this);

        //todo move to settings
        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnStartService.setOnClickListener(this);
        btnStopService.setOnClickListener(this);


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
            case R.id.llMenuOrder:
                Intent menuIntent = new Intent(this, MenuOrdersActivity.class);
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


            case R.id.btnStartService:
                startService(new Intent(this, ShopService.class));
                break;
            case R.id.btnStopService:
                stopService(new Intent(this, ShopService.class));
                break;

        }

    }

}
