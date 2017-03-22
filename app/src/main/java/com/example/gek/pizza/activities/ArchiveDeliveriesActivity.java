package com.example.gek.pizza.activities;

import android.content.Context;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

public class ArchiveDeliveriesActivity extends BaseActivity {
    private static final String TAG = "ARCHIVE_DELIVERIES";
    private RecyclerView rv;
    private ArrayList<Delivery> list;
    private Context ctx;

    @Override
    public void updateUI() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Content inflate in VIEW and put in DrawerLayout
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_archive_deliveries, null, false);
        mDrawer.addView(contentView, 0);
        ctx = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_archive_deliveries);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    list.add(child.getValue(Delivery.class));
                }
                rv.setAdapter(new DeliveriesAdapter(list, ctx, Const.CHILD_DELIVERIES_ARCHIVE));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "read DB Cancelled", databaseError.toException());
            }
        };

        db.child(Const.CHILD_DELIVERIES_ARCHIVE).addValueEventListener(listener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_deliveries);
        item.setCheckable(true);
        item.setChecked(true);
    }
}
