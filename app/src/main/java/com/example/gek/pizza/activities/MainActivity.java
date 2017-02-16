package com.example.gek.pizza.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.services.CheckDeliveryService;
import com.example.gek.pizza.services.CheckReservedTablesService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity
        implements View.OnClickListener {

    CardView cvMenuOrder, cvNews, cvOrders, cvContacts, cvReservations;
    private static final String TAG = "List of settings ";

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER){
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            Toast.makeText(this, "Firebase auth: current user = " + email, Toast.LENGTH_SHORT).show();
        }

        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_NULL:
                findViewById(R.id.tvOrdersTitle).setVisibility(View.GONE);
                findViewById(R.id.cvOrders).setVisibility(View.GONE);
                break;
            case Const.AUTH_USER:
                findViewById(R.id.tvOrdersTitle).setVisibility(View.GONE);
                findViewById(R.id.cvOrders).setVisibility(View.GONE);
                break;
            case Const.AUTH_SHOP:
                findViewById(R.id.tvOrdersTitle).setVisibility(View.VISIBLE);
                findViewById(R.id.cvOrders).setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.content_main, null, false);
        mDrawer.addView(contentView, 0);

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
        findViewById(R.id.btnShowStatusOrder).setOnClickListener(this);




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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_basket:
                startActivity(new Intent(this, BasketActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.action_reserve_table:
                startActivity(new Intent(this, ReserveTableActivity.class));
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
                startService(new Intent(this, CheckReservedTablesService.class));
                break;
            case R.id.btnStopService:
                stopService(new Intent(this, CheckDeliveryService.class));
                stopService(new Intent(this, CheckReservedTablesService.class));
                break;
            case R.id.btnShowStatusOrder:
                startActivity(new Intent(this, DeliveryStatus.class));
                break;

        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return super.onNavigationItemSelected(item);
    }
}
