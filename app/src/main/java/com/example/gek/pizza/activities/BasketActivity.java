package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

public class BasketActivity extends AppCompatActivity implements OrderAdapter.RefreshTotalCallback{

    private RecyclerView rv;
    private TextView tvEmpty;
    private TextView tvTotal;
    private RelativeLayout rlOrderPanel;
    private Button btnOrderNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_basket);
        setSupportActionBar(myToolbar);

        tvTotal = (TextView) findViewById(R.id.tvTotal);
        btnOrderNow = (Button) findViewById(R.id.btnOrderNow);
        btnOrderNow.setOnClickListener(orderNowListener);
        rlOrderPanel = (RelativeLayout) findViewById(R.id.rlOrderPanel);

        rv = (RecyclerView) findViewById(R.id.rv);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);

        // если заказы выбраны то показываем список
        if (Basket.getInstance().orders.size() > 0) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            // определяем разделители для айтемов
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(this,DividerItemDecoration.VERTICAL );
            rv.addItemDecoration(dividerItemDecoration);
            OrderAdapter orderAdapter = new OrderAdapter(this);
            rv.setAdapter(orderAdapter);
        } else {
            rv.setVisibility(View.GONE);
            rlOrderPanel.setVisibility(View.GONE);
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
            String s = getResources().getString(R.string.total) + ": ";
            float sum = 0;
            for (Order order: Basket.getInstance().orders) {
                sum = sum + order.getSum();
            }
            s = s + Utils.toPrice(sum);
            tvTotal.setText(s);
        }
    }

    View.OnClickListener orderNowListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //todo send data to FireBase
            Toast.makeText(getBaseContext(), "Sending order to FireBase", Toast.LENGTH_LONG).show();
        }
    };
}
