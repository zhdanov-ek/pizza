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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.DishesAdapter;
import com.example.gek.pizza.helpers.AllDishes;
import com.example.gek.pizza.helpers.Connection;
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

/** Show list of dishes */

public class DishesActivity extends BaseActivity {

    private TextView tvEmpty;
    private RecyclerView rv;
    private Context ctx = this;
    private FloatingActionButton fab;
    private MenuGroup menuGroup;
    private DishesAdapter mDishesAdapter;
    private ArrayList<Dish> allDishes = new ArrayList<>();
    private ArrayList<Dish> mSelectedDishes;
    private final String TAG = "DISHES ACTIVITY";
    private ValueEventListener mFavoriteDishesListener;
    private ValueEventListener mAllDishesListener;

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_dishes);

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

        // Set layout with 2 or 3 column, set offset between them
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

        // Listen favorites or dishes list
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Const.EXTRA_IS_FAVORITE)) {
                initFavoriteDishesListener();
                db.child(Const.CHILD_USERS)
                        .child(Connection.getInstance().getUserId())
                        .child(Const.CHILD_USER_FAVORITES)
                        .addValueEventListener(mFavoriteDishesListener);
                Log.d(TAG, "onCreate: set listener FAVORITES");
                toolbar.setTitle(R.string.title_favorites);
            } else {
                initAllDishesListener();
                menuGroup = intent.getParcelableExtra(Const.EXTRA_MENU_GROUP);
                toolbar.setTitle(menuGroup.getName());
                db.child(Const.CHILD_DISHES).addValueEventListener(mAllDishesListener);
                Log.d(TAG, "onCreate: set listener ALL DISHES");
            }
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_dishes);
        item.setCheckable(true);
        item.setChecked(true);
    }


    /**
     * Initialize listener of user folder with keys of favorites dishes
     */
    private void initFavoriteDishesListener(){
        mFavoriteDishesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mSelectedDishes == null){
                    mSelectedDishes = new ArrayList<>();
                } else {
                    mSelectedDishes.clear();
                }
                Dish currentDish;
                // Use keys for make list of dishes
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    FavoriteDish favoriteDish = child.getValue(FavoriteDish.class);
                    currentDish = AllDishes.getInstance().getDish(favoriteDish.getKeyOfDish());
                    if (!currentDish.getKeyGroup().contentEquals(Const.DISH_GROUP_VALUE_REMOVED)){
                        mSelectedDishes.add(currentDish);
                    }
                }
                if (mSelectedDishes.isEmpty()){
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    if (mDishesAdapter == null) {
                        mDishesAdapter = new DishesAdapter(ctx, mSelectedDishes);
                        rv.setAdapter(mDishesAdapter);
                    } else {
                        mDishesAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

    }



    /**
     *  Listener get all dishes from  child(CHILD_DISHES)
     *  Take dishes only from one group
     */
    private void initAllDishesListener(){
        mAllDishesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allDishes.clear();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Dish currentDish = child.getValue(Dish.class);
                    allDishes.add(currentDish);
                }
                mSelectedDishes = Utils.selectGroup(allDishes, menuGroup.getKey());
                if (mSelectedDishes.size() == 0){
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    if (mDishesAdapter == null) {
                        mDishesAdapter = new DishesAdapter(ctx, mSelectedDishes);
                        rv.setAdapter(mDishesAdapter);
                    } else {
                        mDishesAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
    }


    /** Start activity for add new Dish */
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
        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_SHOP:
                menu.add(0, Const.ACTION_EDIT, 0, R.string.action_edit_group);
                menu.add(0, Const.ACTION_REMOVE, 0, R.string.action_remove_group);
                break;
            case Const.AUTH_NULL:
            case Const.AUTH_USER:
                menu.add(0, Const.ACTION_BASKET, 0, R.string.action_basket)
                        .setIcon(R.drawable.ic_basket)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                break;
            default:
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Const.ACTION_EDIT:
                Intent editIntent = new Intent(this, MenuGroupEditActivity.class);
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
            if (mSelectedDishes.isEmpty()){
                db.child(Const.CHILD_MENU_GROUPS).child(menuGroup.getKey()).setValue(null);
                Toast.makeText(ctx, "Group " + menuGroup.getName() + " removed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, "Group is not empty!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ctx, R.string.mes_no_internet, Toast.LENGTH_LONG).show();
        }
    }


    // remove FireBase listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAllDishesListener != null) {
            db.child(Const.CHILD_DISHES).removeEventListener(mAllDishesListener);
            Log.d(TAG, "onDestroy: remove listener ALL DISHES");
        }
        if ((mFavoriteDishesListener != null) && (FirebaseAuth.getInstance().getCurrentUser() != null)) {
            db.child(Const.CHILD_USERS)
                    .child(Connection.getInstance().getUserId())
                    .child(Const.CHILD_USER_FAVORITES)
                    .removeEventListener(mFavoriteDishesListener);
            Log.d(TAG, "onDestroy: remove listener FAVORITES");
        }
    }
}
