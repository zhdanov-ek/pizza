package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.DeliveriesAdapter;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

/**
 * Show list od deliveries from one group:
 * 1) transport for courier
 * 2) archive for shop
 *
 * */

public class OneGroupDeliveriesActivity extends BaseActivity {
    private static final String TAG = "ONE_GROUP_DELIVERIES";
    private RecyclerView rv;
    private ArrayList<Delivery> mList;
    private Context ctx;
    private Boolean isArchive;  // true - archive, false - transport
    private ValueEventListener mDeliveriesListener;
    private DeliveriesAdapter mDeliveriesAdapter;

    @Override
    public void updateUI() {
        if ((Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_NULL) ||
        (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER)) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Content inflate in VIEW and put in DrawerLayout
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_group_deliveries, null, false);
        mDrawer.addView(contentView, 0);
        ctx = this;

        // Define mList of deliveries for output from extras from intent
        Intent intent = getIntent();
        if ((intent == null) || !(intent.hasExtra(Const.MODE))) {
            finish();
        } else {
            if (intent.getIntExtra(Const.MODE, Const.MODE_ARCHIVE_DELIVERIES) == Const.MODE_ARCHIVE_DELIVERIES){
                isArchive = true;
            } else {
                isArchive = false;
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        if (isArchive) {
            toolbar.setTitle(R.string.title_archive_deliveries);
        } else {
            toolbar.setTitle(R.string.title_transport_deliveries);
        }
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        mList = new ArrayList<>();
        initListener();
        if (isArchive) {
            db.child(Const.CHILD_DELIVERIES_ARCHIVE).addValueEventListener(mDeliveriesListener);
        } else {
            db.child(Const.CHILD_DELIVERIES_TRANSPORT).addValueEventListener(mDeliveriesListener);
        }

    }


    /** Initialize listener of FireBase */
    private void initListener(){
        mDeliveriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    mList.add(child.getValue(Delivery.class));
                }
                if (mDeliveriesAdapter ==  null){
                    String group;
                    if (isArchive) {
                        group = Const.CHILD_DELIVERIES_ARCHIVE;
                    } else {
                        group = Const.CHILD_DELIVERIES_TRANSPORT;
                    }
                    mDeliveriesAdapter = new DeliveriesAdapter(mList, ctx, group);
                    rv.setAdapter(mDeliveriesAdapter);
                } else {
                    mDeliveriesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "read DB Cancelled", databaseError.toException());
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_deliveries);
        item.setCheckable(true);
        item.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        if (mDeliveriesListener != null){
            db.child(Const.CHILD_DELIVERIES_ARCHIVE).removeEventListener(mDeliveriesListener);
        }
        super.onDestroy();
    }
}
