package com.example.gek.pizza.activities;

import android.content.Intent;
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
import com.example.gek.pizza.data.Order;
import com.example.gek.pizza.helpers.Utils;

/**  Отображает корзину заказов и отправляет заказ */

public class BasketActivity extends BaseActivity implements OrderAdapter.RefreshTotalCallback{

    private RecyclerView rv;
    private TextView tvEmpty;
    private TextView tvTotal;
    private RelativeLayout rlOrderPanel;
    private Button btnOrderNow;

    @Override
    public void updateUI() {

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

    }


    @Override
    protected void onResume() {
        super.onResume();
        // если в корзине есть, что-то то показываем
        if (Basket.getInstance().orders.size() > 0) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            // определяем разделители для айтемов
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(this,DividerItemDecoration.VERTICAL );
            rv.addItemDecoration(dividerItemDecoration);
            OrderAdapter orderAdapter = new OrderAdapter(this);
            rv.setAdapter(orderAdapter);

        } else {
            // Если корзина пуста то проверяем нет ли действующей доставки
            rv.setVisibility(View.GONE);
            rlOrderPanel.setVisibility(View.GONE);
            if (Basket.getInstance().getNumberDelivery().length() > 0) {
                String mes = getResources().getString(R.string.mes_your_order_performed) + "\n" +
                        Basket.getInstance().getNumberDelivery();
                tvEmpty.setText(mes);
            }
            tvEmpty.setVisibility(View.VISIBLE);
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
}
