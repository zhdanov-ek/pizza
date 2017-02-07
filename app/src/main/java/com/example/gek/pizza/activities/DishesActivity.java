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
import com.example.gek.pizza.data.MenuGroup;
import com.example.gek.pizza.data.News;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/** Отображение списка блюд в разделе */

public class DishesActivity extends AppCompatActivity {

    private TextView tvEmpty;
    private RecyclerView rv;
    private Context ctx = this;
    private FloatingActionButton fab;
    private MenuGroup menuGroup;
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
            menuGroup = intent.getParcelableExtra(Const.EXTRA_MENU_GROUP);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(menuGroup.getName());
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

                selectedDishes = Utils.selectGroup(allDishes, menuGroup.getKey());
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
            addDish.putExtra(Const.DISH_GROUP_KEY, menuGroup.getKey());
            startActivity(addDish);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Edit group");
        menu.add(0, 2, 0, "Remove group");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1:
                Intent editIntent = new Intent(this, MenuOrdersEditActivity.class);
                editIntent.putExtra(Const.MODE, Const.MODE_EDIT);
                editIntent.putExtra(Const.EXTRA_MENU_GROUP, menuGroup);
                startActivity(editIntent);
                break;
            case 2:
                removeGroupDishes();
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    // Remove group dishes if she's empty
    private void removeGroupDishes(){
        if (Utils.hasInternet(ctx)){
            if (selectedDishes.isEmpty()){
                Const.db.child(Const.CHILD_MENU_GROUPS).child(menuGroup.getKey()).setValue(null);
                Toast.makeText(ctx, "Group " + menuGroup.getName() + " removed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, "Group is not empty!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ctx, R.string.mes_no_internet, Toast.LENGTH_LONG).show();
        }
    }
}
