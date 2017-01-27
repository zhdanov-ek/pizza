package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.DishesAdapter;
import com.example.gek.pizza.adapters.NewsAdapter;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.News;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DishesActivity extends AppCompatActivity {

    private TextView tvEmpty;
    private RecyclerView rv;
    private Context ctx = this;
    private FloatingActionButton fab;
    private String keyGroup = "";
    private String nameGroup = "";
    private DishesAdapter dishesAdapter;
    private ArrayList<Dish> allDishes = new ArrayList<>();
    private ArrayList<Dish> selectedDishes;
    private final String TAG = "DISHES ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_dishes);


        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        rv = (RecyclerView) findViewById(R.id.rv);
        // задаем лаяют с твумя столбцами
        GridLayoutManager lLayout = new GridLayoutManager(DishesActivity.this, 2);
        rv.setLayoutManager(lLayout);

        Intent intent = getIntent();
        if (intent != null) {
            keyGroup = intent.getStringExtra(Const.DISH_GROUP_KEY);
            nameGroup = intent.getStringExtra(Const.DISH_GROUP_NAME);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(nameGroup);
        setSupportActionBar(myToolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);

        // Описываем слушатель, который возвращает в программу весь список данных,
        // которые находятся в child(CHILD_DISHES)
        // В итоге при любом изменении вся база перезаливается с БД в программу
        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                allDishes.clear();

                Log.d(TAG, "Load all list ContactCards: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Dish currentDish = child.getValue(Dish.class);
                    allDishes.add(currentDish);
                }

                selectedDishes = Utils.selectGroup(allDishes, keyGroup);
                if (selectedDishes.size() == 0){
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    dishesAdapter = new DishesAdapter(ctx, selectedDishes);
                    rv.setAdapter(dishesAdapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на изменения в нашей базе в разделе блюд
        Const.db.child(Const.CHILD_DISHES).addValueEventListener(contactCardListener);
    }

    /** Запуск активити на добавление блюда в текущей группе */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addDish = new Intent(ctx, DishEditActivity.class);
            addDish.putExtra(Const.MODE, Const.MODE_NEW);
            addDish.putExtra(Const.DISH_GROUP_KEY, keyGroup);
            startActivity(addDish);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_basket:
                startActivity(new Intent(this, BasketActivity.class));
                break;
            case R.id.action_settings:
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
