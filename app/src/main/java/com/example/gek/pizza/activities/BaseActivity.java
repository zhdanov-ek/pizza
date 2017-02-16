package com.example.gek.pizza.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    protected DrawerLayout mDrawer;
    protected TextView tvAuth;
    protected FirebaseAuth.AuthStateListener authListener;

    // In this method we draw UI: hide or show menu, block activity and other
    public abstract void updateUI();

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Находим наше меню и привязываем его к тулбару
        // todo нужно тулбар вынести с активити сюда
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Give header and find TextView
        View header = navigationView.getHeaderView(0);
        tvAuth = (TextView) header.findViewById(R.id.tvAuth);


        // Callback change state of auth Firebase.
        // After change state of auth FireBase we update UI in current Activity
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    // Check user: is shop or other users
                    tvAuth.setText(user.getEmail());
                    if (user.getEmail().contentEquals(Connection.getInstance().getShopEmail())){
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_SHOP);
                        Log.d(TAG, "FireBase authentication success (SHOP) " + user.getEmail());
                    } else {
                        Connection.getInstance().setCurrentAuthStatus(Const.AUTH_USER);
                        Log.d(TAG, "FireBase authentication success (USER) " + user.getEmail());
                    }
                } else {
                    tvAuth.setText(R.string.common_signin_button_text);
                    Connection.getInstance().setCurrentAuthStatus(Const.AUTH_NULL);
                    Log.d(TAG, "FireBase authentication failed ");
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
        int id = item.getItemId();

        if (id == R.id.nav_dishes) {
            startActivity(new Intent(this, MenuOrdersActivity.class));
        } else if (id == R.id.nav_news) {
            startActivity(new Intent(this, NewsActivity.class));
        } else if (id == R.id.nav_deliveries) {
            startActivity(new Intent(this, DeliveriesActivity.class));
        } else if (id == R.id.nav_reservation) {
            startActivity(new Intent(this, ReserveTableActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_signin) {
            startActivity(new Intent(this, AuthenticationActivity.class));
        } else if (id == R.id.nav_signout) {
            Connection.getInstance().signOut();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
