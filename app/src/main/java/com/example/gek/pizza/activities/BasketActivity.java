package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.OrderAdapter;
import com.example.gek.pizza.data.Basket;

/**  Отображает корзину заказов и отправляет заказ */

public class BasketActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_basket);
        setSupportActionBar(myToolbar);

        rv = (RecyclerView) findViewById(R.id.rv);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);

        // если заказы выбраны то показываем список
        if (Basket.getInstance().orders.size() > 0) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            OrderAdapter orderAdapter = new OrderAdapter(this);
            rv.setAdapter(orderAdapter);
        } else {
            rv.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }

    }
}
