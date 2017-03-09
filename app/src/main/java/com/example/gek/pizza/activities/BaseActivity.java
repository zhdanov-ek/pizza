package com.example.gek.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Favorites;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    protected DrawerLayout mDrawer;
    protected TextView tvAuth;
    protected FirebaseAuth.AuthStateListener authListener;

    // In this method we will draw UI: hide or show menu, block activity and other
    public abstract void updateUI();

    private String TAG = this.getClass().getSimpleName();
    public static boolean signInAsAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Give header and find TextView
        View header = navigationView.getHeaderView(0);
        tvAuth = (TextView) header.findViewById(R.id.tvAuth);
        tvAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_NULL){
                    startActivity(new Intent(getBaseContext(), AuthenticationActivity.class));
                } else {
                    Connection.getInstance().signOut(getBaseContext());
                }
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
                    // Check user: is shop or other users

                    tvAuth.setText(user.getDisplayName() + "\n" +user.getEmail());
                    if (((user.getEmail().contentEquals(Connection.getInstance().getShopEmail()))
                            //убрать, для удобной отладки, потом убрать
                            && signInAsAdmin) || (signInAsAdmin)) {
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_SHOP);

                        // если admin то отписываемся от рассылки
                        Utils.subscribeOrUnsubscribeFromTopic(false);

                        Log.d(TAG, "FireBase authentication success (SHOP) " + user.getEmail());
                    } else {
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_USER);

                        Utils.subscribeOrUnsubscribeFromTopic(true);

                        Log.d(TAG, "FireBase authentication success (USER) " + user.getEmail());
                    }
                } else {
                    tvAuth.setText(R.string.common_signin_button_text);
                    Connection.getInstance().setCurrentAuthStatus(Const.AUTH_NULL);
                    Log.d(TAG, "FireBase authentication failed ");
                }

                // SHOW or HIDE items of menu
                switch (Connection.getInstance().getCurrentAuthStatus()){
                    case Const.AUTH_SHOP:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(false);
                        break;
                    case Const.AUTH_USER:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(true);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(true);
                        //initialize list of favorites dishes
                        Favorites.getInstance();
                        break;
                    case Const.AUTH_NULL:
                        navigationView.getMenu().findItem(R.id.nav_shop_group).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_delivery_status).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_pizza).setVisible(true);
                        break;
                }

                updateUI();
            }
        };

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
                startActivity(new Intent(this, MenuOrdersActivity.class));
                break;
            case R.id.nav_news:
                startActivity(new Intent(this, NewsActivity.class));
                break;
            case R.id.nav_favorite:
                Intent favoritesIntent = new Intent(this, DishesActivity.class);
                favoritesIntent.putExtra(Const.EXTRA_IS_FAVORITE, true);
                startActivity(favoritesIntent);
                break;
            case R.id.nav_pizza:
                startActivity(new Intent(this, MakePizzaActivity.class));
                break;
            case R.id.nav_deliveries:
                startActivity(new Intent(this, DeliveriesActivity.class));
                break;
            case R.id.nav_reservation:
                startActivity(new Intent(this, ReserveTableActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_delivery_status:
                startActivity(new Intent(this, DeliveryStatus.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.nav_push:
                startActivity(new Intent(this,PushActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
