package com.example.gek.pizza.activities;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.OrderAdapter;
import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Order;
import com.example.gek.pizza.data.StateLastDelivery;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.db;

/**  Отображает корзину заказов и отправляет заказ */

public class BasketActivity extends BaseActivity implements OrderAdapter.RefreshTotalCallback{

    private RecyclerView rv;
    private TextView tvEmpty;
    private TextView tvTotal;
    private RelativeLayout rlOrderPanel;
    private Button btnOrderNow;
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

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_basket, null, false);
        mDrawer.addView(contentView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_basket);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        tvTotal = (TextView) findViewById(R.id.tvTotal);
        btnOrderNow = (Button) findViewById(R.id.btnOrderNow);
        btnOrderNow.setOnClickListener(orderNowListener);
        rlOrderPanel = (RelativeLayout) findViewById(R.id.rlOrderPanel);

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
                    Snackbar.make(rv, "You have active delivery now", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Show", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(getBaseContext(), DeliveryStatus.class));
                                }
                            }).show();
                } else {
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

        // определяем разделители для айтемов
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this,DividerItemDecoration.VERTICAL );
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(dividerItemDecoration);

        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_USER:
                // смотрим состояние последней доставки и если она не закрыта то показываем сообщение
                db.child(Const.CHILD_USERS)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(Const.CHILD_USER_DELIVERY_STATE)
                        .addValueEventListener(mStateListener);
                mIsSetListener = true;
                // если в корзине есть, что-то то показываем список блюд
                if (Basket.getInstance().orders.size() > 0) {
                    OrderAdapter orderAdapter = new OrderAdapter(this);
                    rv.setAdapter(orderAdapter);
                } else {
                    // Если корзина пуста то информируем об этом
                    rv.setVisibility(View.GONE);
                    rlOrderPanel.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                break;
            default:
                //AUTH_NULL
                // если в корзине есть, что-то то показываем
                if (Basket.getInstance().orders.size() > 0) {
                    OrderAdapter orderAdapter = new OrderAdapter(this);
                    rv.setAdapter(orderAdapter);
                } else {
                    // Если корзина пуста то информируем об этом
                    rlOrderPanel.setVisibility(View.GONE);
                    rv.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                break;
        }

    }


    /** Реализация интерфейса адаптера в которой мы обновляем итоговую сумму или скрываем РВ */
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
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .removeEventListener(mStateListener);
        }
        super.onPause();
    }
}
