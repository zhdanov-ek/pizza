package com.example.gek.pizza.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.OrdersAdapter;
import com.example.gek.pizza.helpers.Basket;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.StateLastDelivery;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.db;

/**  Show current order and make request for create delivery */

public class BasketActivity extends BaseActivity implements OrdersAdapter.RefreshTotalCallback{

    private RecyclerView rv;
    private TextView tvEmpty;
    private TextView tvTotal;
    private RelativeLayout rlOrderPanel, rlDeliveryPanel;
    private ValueEventListener mStateListener;
    private Boolean mIsSetListener = false;

    @Override
    public void updateUI() {
        if ((Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP) ||
                (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_COURIER)) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_basket);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_basket);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        tvTotal = (TextView) findViewById(R.id.tvTotal);
        rlOrderPanel = (RelativeLayout) findViewById(R.id.rlOrderPanel);
        rlDeliveryPanel = (RelativeLayout) findViewById(R.id.rlDeliveryPanel);
        findViewById(R.id.btnOrderNow).setOnClickListener(orderNowListener);
        findViewById(R.id.btnShowStatusDelivery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), DeliveryStatus.class));
            }
        });

        rv = (RecyclerView) findViewById(R.id.rv);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);

        // Check active delivery
        mStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StateLastDelivery stateLastDelivery = dataSnapshot.getValue(StateLastDelivery.class);
                if ((stateLastDelivery != null) &&
                        (stateLastDelivery.getDeliveryState() != Const.DELIVERY_STATE_ARCHIVE)){
                    rlOrderPanel.setVisibility(View.GONE);
                    rlDeliveryPanel.setVisibility(View.VISIBLE);
                } else {
                    rlDeliveryPanel.setVisibility(View.GONE);
                    if (tvEmpty.getVisibility() == View.VISIBLE) {
                        rlOrderPanel.setVisibility(View.GONE);
                    } else {
                        rlOrderPanel.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();

        // create divider for items
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this,DividerItemDecoration.VERTICAL );
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(dividerItemDecoration);
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER) {
            db.child(Const.CHILD_USERS)
                    .child(Connection.getInstance().getUserId())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .addValueEventListener(mStateListener);
            mIsSetListener = true;
        }

        // show list of dishes if have them
        if (Basket.getInstance().orders.size() > 0) {
            OrdersAdapter orderAdapter = new OrdersAdapter(this);
            rv.setAdapter(orderAdapter);
        } else {
            // Show message if basket empty
            rv.setVisibility(View.GONE);
            rlOrderPanel.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }


    /** Implement interface of adapter where we update total sum or hide RecyclerView */
    @Override
    public void refreshTotal() {
        if (Basket.getInstance().orders.size() == 0){
            rv.setVisibility(View.GONE);
            rlOrderPanel.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            String s = getResources().getString(R.string.total) + ": " + Utils.toPrice(Basket.getInstance().getTotalSum());
            tvTotal.setText(s);
        }
    }
    View.OnClickListener orderNowListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intentDelivery = new Intent(getBaseContext(), DeliveryCreationActivity.class);
            startActivity(intentDelivery);
        }
    };


    @Override
    protected void onPause() {
        if (mIsSetListener) {
            db.child(Const.CHILD_USERS)
                    .child(Connection.getInstance().getUserId())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .removeEventListener(mStateListener);
        }
        super.onPause();
    }
}
