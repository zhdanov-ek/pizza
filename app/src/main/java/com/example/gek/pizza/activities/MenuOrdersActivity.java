package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.MenuOrdersAdapter;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.MenuGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Показывает список групп меню. По клику на айтеме открывается другое окно со списком блюд
 * Размер картинки 500х300 пикселей
 * */

public class MenuOrdersActivity extends BaseActivity {

    private static final String TAG = "Menu orders ";
    private RecyclerView rv;
    private Context ctx = this;
    private MenuOrdersAdapter menuOrdersAdapter;
    private ArrayList<MenuGroup> listMenuGroup;

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER){
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            Toast.makeText(this, "Firebase auth: current user = " + email, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_menu_dishes, null, false);
        mDrawer.addView(contentView, 0);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_menu_order);
        setSupportActionBar(myToolbar);

        rv = (RecyclerView) findViewById(R.id.rv);

        // задаем лаяют с твумя или тремя столбцами в зависимости от поворота экрана
        GridLayoutManager lLayout;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            lLayout = new GridLayoutManager(MenuOrdersActivity.this, 2);
        } else {
            lLayout = new GridLayoutManager(MenuOrdersActivity.this, 3);
        }
        rv.setLayoutManager(lLayout);

        // Выравниваем отступы между айтемами
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ctx, R.dimen.item_menu_orders_offset);
        rv.addItemDecoration(itemDecoration);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);

        listMenuGroup = new ArrayList<>();

        // Описываем слушатель, который возвращает в программу весь список данных,
        // которые находятся в child(CHILD_MENU_GROUPS)
        // В итоге при любом изменении вся база перезаливается с БД в программу
        ValueEventListener contactCardListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                listMenuGroup.clear();

                Log.d(TAG, "Load all list menu: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    MenuGroup menuGroup = child.getValue(MenuGroup.class);
                    listMenuGroup.add(menuGroup);
                }
                if (listMenuGroup.size() == 0) {
                    Toast.makeText(ctx, R.string.mes_no_records, Toast.LENGTH_LONG).show();
                }
                menuOrdersAdapter = new MenuOrdersAdapter(ctx, listMenuGroup);
                rv.setAdapter(menuOrdersAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на изменения в нашей базе в разделе контактов
        Const.db.child(Const.CHILD_MENU_GROUPS).addValueEventListener(contactCardListener);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, R.string.action_archive);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1:
                showRemovedDishes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Запуск активити на добавление группы меню */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addMenuGroup = new Intent(ctx, MenuOrdersEditActivity.class);
            addMenuGroup.putExtra(Const.MODE, Const.MODE_NEW);
            startActivity(addMenuGroup);
        }
    };


    /** С помощью этого класса регулируем отступы между айтемами */
    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    /** Открываем активити со списком удаленных блюд */
    private void showRemovedDishes(){
        MenuGroup groupRemoved = new MenuGroup();
        groupRemoved.setName(getResources().getString(R.string.title_archive_dishes));
        groupRemoved.setKey(Const.DISH_GROUP_VALUE_REMOVED);
        Intent archiveIntent = new Intent(this, DishesActivity.class);
        archiveIntent.putExtra(Const.EXTRA_MENU_GROUP, groupRemoved);
        startActivity(archiveIntent);
    }
}
