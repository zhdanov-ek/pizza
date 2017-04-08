package com.example.gek.pizza.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.Basket;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.helpers.Favorites;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.db;

public class DishShowActivity extends BaseActivity implements View.OnClickListener{
    private TextView tvName, tvPrice, tvDescription;
    private ImageView ivPhoto;
    private ImageView ivFavorites;
    private Boolean isFavorite;
    private Button btnAdd;
    private Button btnRemove;
    private Button btnEdit;
    private LinearLayout llCounter;
    private TextView tvCounter;
    private Dish dishOpen;
    private Boolean isSetListenerFavorites;

    private Boolean isOpenDialog = false;
    private static final String EXTRA_OPEN_DIALOG = "is_open_dialog";

    @Override
    public void updateUI() {
        switch (Connection.getInstance().getCurrentAuthStatus()){
            case Const.AUTH_USER:
                btnEdit.setVisibility(View.GONE);
                btnRemove.setVisibility(View.GONE);
                ivFavorites.setVisibility(View.VISIBLE);
                btnAdd.setEnabled(true);
                break;
            case Const.AUTH_SHOP:
                btnEdit.setVisibility(View.VISIBLE);
                btnRemove.setVisibility(View.VISIBLE);
                ivFavorites.setVisibility(View.GONE);
                btnAdd.setEnabled(false);
                break;
            case Const.AUTH_COURIER:
                btnEdit.setVisibility(View.GONE);
                btnRemove.setVisibility(View.GONE);
                ivFavorites.setVisibility(View.GONE);
                btnAdd.setEnabled(false);
                break;
            default:
                btnEdit.setVisibility(View.GONE);
                btnRemove.setVisibility(View.GONE);
                ivFavorites.setVisibility(View.GONE);
                btnAdd.setEnabled(true);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_dish_show);
        setToolbar("");

        tvName = (TextView) findViewById(R.id.tvAuthName);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivFavorites = (ImageView) findViewById(R.id.ivFavorites);
        ivFavorites.setOnClickListener(this);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnRemove = (Button) findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(this);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(this);

        llCounter = (LinearLayout) findViewById(R.id.llCounter);
        tvCounter = (TextView) findViewById(R.id.tvCounter);
        findViewById(R.id.ivMinus).setOnClickListener(this);
        findViewById(R.id.ivPlus).setOnClickListener(this);

        if (getIntent().hasExtra(Const.EXTRA_DISH)){
            dishOpen = getIntent().getParcelableExtra(Const.EXTRA_DISH);
            fillValues(dishOpen);
            mToolbar.setTitle(dishOpen.getName());
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isOpenDialog){
            outState.putBoolean(EXTRA_OPEN_DIALOG, true);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            isOpenDialog = savedInstanceState.getBoolean(EXTRA_OPEN_DIALOG);
            if (isOpenDialog){
                removeDish(dishOpen);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_dishes);
        item.setCheckable(true);
        item.setChecked(true);

        // Search dish in basket. Need for set count
        int countDishInOrder = Utils.findInBasket(dishOpen);
        if (countDishInOrder != 0) {
            btnAdd.setVisibility(View.GONE);
            llCounter.setVisibility(View.VISIBLE);
            tvCounter.setText(String.valueOf(countDishInOrder));
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            llCounter.setVisibility(View.GONE);
        }

        // listen change in favorites for USER auth
        isSetListenerFavorites = false;
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER) {
            ivFavorites.setVisibility(View.VISIBLE);
            db.child(Const.CHILD_USERS)
                    .child(Connection.getInstance().getUserId())
                    .child(Const.CHILD_USER_FAVORITES)
                    .addValueEventListener(favoritesListener);

        } else {
            ivFavorites.setVisibility(View.GONE);
        }
    }

    /** Listen favorites of user and update UI */
    ValueEventListener favoritesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (Favorites.getInstance().searchDish(dishOpen.getKey()) != null) {
                isFavorite = true;
                ivFavorites.setImageResource(R.drawable.ic_star_solid);
            } else {
                isFavorite = false;
                ivFavorites.setImageResource(R.drawable.ic_star_empty);
            }
            ivFavorites.setVisibility(View.VISIBLE);
            ivFavorites.setClickable(true);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRemove:
                removeDish(dishOpen);
                break;
            case R.id.btnEdit:
                editDish(dishOpen);
                break;
            case R.id.btnAdd:
                addNewDish();
                showInfoOrder(true);
                break;
            case R.id.ivMinus:
                pressMinus();
                showInfoOrder(false);
                break;
            case R.id.ivPlus:
                pressPlus();
                showInfoOrder(true);
                break;
            case R.id.ivFavorites:
                pressFavorites();
                break;
        }
    }

    private void showInfoOrder(Boolean isOperationAdd){
        String operation;
        if (isOperationAdd){
            operation = dishOpen.getName() + " " + getString(R.string.added_to_basket);
        } else {
            operation = dishOpen.getName() + " " + getString(R.string.removed_from_basket);
        }
        String sum = operation + "\n" + getResources().getString(R.string.total_sum) + ": " +
                Utils.toPrice(Basket.getInstance().getTotalSum());
        Snackbar.make(tvName, sum, Snackbar.LENGTH_SHORT).show();
    }

    /** Add or remove dish from favorites */
    private void pressFavorites(){
        if (isFavorite){
            Favorites.getInstance().removeDish(dishOpen.getKey());
            Toast.makeText(this,
                    String.format(getResources().getString(R.string.mes_remove_from_favorites),
                            dishOpen.getName()),
                    Toast.LENGTH_SHORT).show();
        } else {
            Favorites.getInstance().addDish(dishOpen.getKey());
            Toast.makeText(this,
                    String.format(getResources().getString(R.string.mes_add_to_favorites),
                            dishOpen.getName()),
                    Toast.LENGTH_SHORT).show();
        }
        ivFavorites.setClickable(false);
    }

    /** Increase count on 1 */
    private void pressPlus(){
        int count = Integer.parseInt(tvCounter.getText().toString()) + 1;
        tvCounter.setText(String.valueOf(count));
        Basket.getInstance().changeCount(dishOpen.getKey(), count);
    }

    /** Decrease count on -1 or show button if 0 */
    private void pressMinus(){
        int count = Integer.parseInt(tvCounter.getText().toString()) - 1;
        Basket.getInstance().changeCount(dishOpen.getKey(), count);
        if (count == 0) {
            llCounter.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            tvCounter.setText(String.valueOf(count));
        }
    }


    /** Add dish to Basket and replace button */
    private void addNewDish(){
        Basket.getInstance().addDish(dishOpen);
        tvCounter.setText("1");
        btnAdd.setVisibility(View.GONE);
        llCounter.setVisibility(View.VISIBLE);
    }

    /** Update Dish after edit */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == Const.REQUEST_EDIT_DISH) && (resultCode == RESULT_OK) && (data != null)) {
            Dish changedDish = data.getParcelableExtra(Const.EXTRA_DISH);
            dishOpen = changedDish;
            fillValues(changedDish);
        }
    }


    private void fillValues(Dish dish){
        if ((dish.getPhotoUrl() != null) && (dish.getPhotoUrl().length() > 0)){
            Glide.with(this)
                    .load(dish.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.dish_empty)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.dish_empty);
        }

        // Don't remove if dish in archive
        if (dish.getKeyGroup().contentEquals(Const.DISH_GROUP_VALUE_REMOVED)){
            btnRemove.setVisibility(View.GONE);
        }
        tvName.setText(dish.getName());
        tvPrice.setText(Utils.toPrice(dish.getPrice()));
        tvDescription.setText(dish.getDescription());
    }


    /** Open activity for edit dish */
    private void editDish(Dish dish){
        Intent editIntent = new Intent(this, DishEditActivity.class);
        editIntent.putExtra(Const.MODE, Const.MODE_EDIT);
        editIntent.putExtra(Const.EXTRA_DISH, dish);
        startActivityForResult(editIntent, Const.REQUEST_EDIT_DISH);
    }





    /** For remove dish we set group to REMOVED */
    private void removeDish(final Dish dish){
        isOpenDialog = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove);
        builder.setIcon(R.drawable.ic_warning);
        String message = getResources().getString(R.string.confirm_remove_item);
        builder.setMessage(message + "\n" + dish.getName());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isOpenDialog = false;
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dish.setKeyGroup(Const.DISH_GROUP_VALUE_REMOVED);
                db.child(Const.CHILD_DISHES).child(dish.getKey()).setValue(dish);
                isOpenDialog = false;
                finish();
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_NULL) ||
                (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER)){
            menu.add(0, Const.ACTION_BASKET, 0, R.string.action_basket)
                    .setIcon(R.drawable.ic_basket)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Const.ACTION_BASKET:
                startActivity(new Intent(this, BasketActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (isSetListenerFavorites){
            db.child(Const.CHILD_USERS)
                    .child(Connection.getInstance().getUserId())
                    .child(Const.CHILD_USER_FAVORITES)
                    .removeEventListener(favoritesListener);
        }
        super.onPause();
    }


}
