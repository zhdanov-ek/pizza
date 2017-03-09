package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.DishesAdapter;
import com.example.gek.pizza.data.AllDishes;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.FavoriteDish;
import com.example.gek.pizza.data.MenuGroup;
import com.example.gek.pizza.helpers.GridSpacingItemDecoration;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

/** Отображение списка блюд в разделе */

public class DishesActivity extends BaseActivity {

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
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_dishes, null, false);
        mDrawer.addView(contentView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        rv = (RecyclerView) findViewById(R.id.rv);

        // Задаем лаяют с твумя или тремя столбцами в зависимости от поворота экрана
        // и устанавливаем расстояния между айтемами
        GridLayoutManager gridLayoutManager;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(DishesActivity.this, 2);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_dish_offset);
            rv.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
        } else {
            gridLayoutManager = new GridLayoutManager(DishesActivity.this, 3);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_dish_offset_land);
            rv.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));
        }

        rv.setLayoutManager(gridLayoutManager);

        // Слушаем либо избранное либо общий список блюд
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Const.EXTRA_IS_FAVORITE)) {
                db.child(Const.CHILD_USERS)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(Const.CHILD_USER_FAVORITES)
                        .addValueEventListener(favoriteDishesListener);
                toolbar.setTitle(R.string.title_favorites);
            } else {
                menuGroup = intent.getParcelableExtra(Const.EXTRA_MENU_GROUP);
                toolbar.setTitle(menuGroup.getName());
                db.child(Const.CHILD_DISHES).addValueEventListener(allDishesListener);
            }
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);
    }

    // Слушаем юзерскую папку с ключами избранных блюд и передаем в адаптер
    private ValueEventListener favoriteDishesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (selectedDishes == null){
                selectedDishes = new ArrayList<>();
            } else {
                selectedDishes.clear();
            }

            Dish currentDish;
            // По ключам формируем список блюд
            for (DataSnapshot child: dataSnapshot.getChildren()) {
                FavoriteDish favoriteDish = child.getValue(FavoriteDish.class);
                currentDish = AllDishes.getInstance().getDish(favoriteDish.getKeyOfDish());
                if (!currentDish.getKeyGroup().contentEquals(Const.DISH_GROUP_VALUE_REMOVED)){
                    selectedDishes.add(currentDish);
                }
            }

            if (selectedDishes.isEmpty()){
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

        }
    };



    // Описываем слушатель, который возвращает в программу весь список данных,
    // которые находятся в child(CHILD_DISHES)
    // Из этого списка выбираем блюда с нашего раздела
    private ValueEventListener allDishesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            allDishes.clear();
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
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP) {
            menu.add(0, Const.ACTION_EDIT, 0, R.string.action_edit_group);
            menu.add(0, Const.ACTION_REMOVE, 0, R.string.action_remove_group);
        } else {
            menu.add(0, Const.ACTION_BASKET, 0, R.string.action_basket)
                    .setIcon(R.drawable.ic_basket)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Const.ACTION_EDIT:
                Intent editIntent = new Intent(this, MenuOrdersEditActivity.class);
                editIntent.putExtra(Const.MODE, Const.MODE_EDIT);
                editIntent.putExtra(Const.EXTRA_MENU_GROUP, menuGroup);
                startActivity(editIntent);
                break;
            case Const.ACTION_REMOVE:
                removeGroupDishes();
                finish();
                break;
            case Const.ACTION_BASKET:
                startActivity(new Intent(this, BasketActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    // Remove group dishes if she's empty
    private void removeGroupDishes(){
        if (Utils.hasInternet(ctx)){
            if (selectedDishes.isEmpty()){
                db.child(Const.CHILD_MENU_GROUPS).child(menuGroup.getKey()).setValue(null);
                Toast.makeText(ctx, "Group " + menuGroup.getName() + " removed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, "Group is not empty!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ctx, R.string.mes_no_internet, Toast.LENGTH_LONG).show();
        }
    }
}
