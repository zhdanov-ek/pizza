package com.example.gek.pizza.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.helpers.Favorites;
import com.example.gek.pizza.helpers.Utils;
import com.example.gek.pizza.services.CourierService;
import com.example.gek.pizza.services.ShopService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Basic activity need for implement NavigationDrawer and FirebaseAuth
 * */

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    protected DrawerLayout mDrawer;
    protected TextView tvAuthEmail;
    protected FirebaseAuth.AuthStateListener authListener;
    protected NavigationView navigationView;
    protected Toolbar mToolbar;

    // In this method we will draw UI: hide or show menu, block activity and other
    public abstract void updateUI();

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!Utils.hasInternet(this)){
            Toast.makeText(this, getString(R.string.mes_no_internet), Toast.LENGTH_SHORT).show();
        }

        // Give header and find View
        View header = navigationView.getHeaderView(0);
        header.findViewById(R.id.ivLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(MainActivity.class);
            }
        });
        tvAuthEmail = (TextView) header.findViewById(R.id.tvAuthEmail);
        tvAuthEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startActivity(new Intent(getBaseContext(), AuthenticationActivity.class));
            }
        });


        // Callback change state of auth Firebase.
        // After change state of auth FireBase we update UI in current Activity
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // refresh menu
                invalidateOptionsMenu();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {

                    // save userId for correct close service and destroy listeners
                    Connection.getInstance().setUserId(user.getUid());
                    Connection.getInstance().setUserName(user.getDisplayName());
                    if (user.getPhotoUrl() != null){
                        Connection.getInstance().setUserPhotoUrl(user.getPhotoUrl().toString());
                    }

                    // Check user: is shop or other users
                    tvAuthEmail.setText(Html.fromHtml(
                            String.format(getString(R.string.sign_status),
                                Connection.getInstance().getUserName(),
                                user.getEmail())));

                    // admin
                    if ((user.getEmail() != null) &&
                            (user.getEmail().contentEquals(Connection.getInstance().getShopEmail()))){
                        // remove subscribe for get message
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_SHOP, getApplicationContext());
                        Utils.subscribeOrUnsubscribeFromTopic(false);
                        if (!Connection.getInstance().getServiceRunning()){
                            startService(new Intent(getBaseContext(), ShopService.class));
                        }
                        Log.d(TAG, "FireBase authentication success (SHOP) " + user.getEmail());

                    // courier
                    } else if (user.getEmail().contentEquals(Connection.getInstance().getCourierEmail())){
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_COURIER, getApplicationContext());
                        Utils.subscribeOrUnsubscribeFromTopic(true);
                        if (!Connection.getInstance().getServiceRunning()){
                            // todo check permissions
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getBaseContext(),
                                        getBaseContext().getString(R.string.mes_enable_permission),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                startService(new Intent(getBaseContext(), CourierService.class));
                            }

                        }
                        Log.d(TAG, "FireBase authentication success (COURIER) " + user.getEmail());

                    // auth client
                    } else {
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_USER, getApplicationContext());
                        Utils.subscribeOrUnsubscribeFromTopic(true);
                        Log.d(TAG, "FireBase authentication success (USER) " + user.getEmail());
                    }

                // guest client
                } else {
                    tvAuthEmail.setText(R.string.common_signin_button_text);
                    Connection.getInstance().setCurrentAuthStatus(Const.AUTH_NULL, getApplicationContext());
                    Log.d(TAG, "FireBase authentication failed ");
                }

                // SHOW or HIDE items of menu
                switch (Connection.getInstance().getCurrentAuthStatus()){
                    case Const.AUTH_SHOP:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_courier_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_reservation).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(false);
                        break;
                    case Const.AUTH_COURIER:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_courier_group).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_reservation).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(false);
                        break;
                    case Const.AUTH_USER:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_courier_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_reservation).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(true);
                        //initialize list of favorites dishes
                        Favorites.getInstance();
                        break;
                    case Const.AUTH_NULL:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_courier_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_reservation).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(true);
                        break;
                }
                updateUI();
            }
        };
    }

    // Inflate layout and put in DrawerLayout
    protected void inflateLayout(int resIdLayout){
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(resIdLayout, null, false);
        mDrawer.addView(contentView, 0);
    }


    // Config toolbar and add button for open DrawerLayout
    protected void setToolbar(String title){
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //set listener FireBase auth
        Log.d(TAG, "FireBase authentication: setListener");
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //remove listener FireBase auth
        Log.d(TAG, "FireBase authentication: removeListener");
        FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }



    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_dishes:
                openActivity(MenuGroupsActivity.class);
                break;
            case R.id.nav_news:
                openActivity(NewsActivity.class);
                break;
            case R.id.nav_favorite:
                Intent favoritesIntent = new Intent(this, DishesActivity.class);
                favoritesIntent.putExtra(Const.EXTRA_IS_FAVORITE, true);
                TaskStackBuilder.create(this)
                        .addParentStack(DishesActivity.class)
                        .addNextIntent(favoritesIntent)
                        .startActivities();
                break;
            case R.id.nav_list_addresses:
                Intent addressesIntent = new Intent(this, OneGroupDeliveriesActivity.class);
                addressesIntent.putExtra(Const.MODE, Const.MODE_TRANSPORT_DELIVERIES);
                TaskStackBuilder.create(this)
                        .addParentStack(OneGroupDeliveriesActivity.class)
                        .addNextIntent(addressesIntent)
                        .startActivities();
                break;
            case R.id.nav_map_with_addresses:
                // TODO: 24.03.17 open map with clients
                break;
            case R.id.nav_pizza:
                openActivity(MakePizzaActivity.class);
                break;
            case R.id.nav_deliveries:
                openActivity(DeliveriesActivity.class);
                break;
            case R.id.nav_reservation:
                openActivity(ReserveTableActivity.class);
                break;
            case R.id.nav_settings:
                openActivity(SettingsActivity.class);
                break;
            case R.id.nav_delivery_status:
                openActivity(DeliveryStatus.class);
                break;
            case R.id.nav_about:
                openActivity(AboutActivity.class);
                break;
            case R.id.nav_push:
                openActivity(PushActivity.class);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // if user click in menu item for run current open activity don't change stack
    private void openActivity(Class<?> c){
        Intent intent = new Intent(this, c);
        if (getClass() != c){
            TaskStackBuilder.create(this)
                    .addParentStack(c)
                    .addNextIntent(new Intent(this, c))
                    .startActivities();
        } else{
            startActivity(intent);
        }
    }

}
