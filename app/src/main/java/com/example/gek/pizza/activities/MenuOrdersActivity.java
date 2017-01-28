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
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.MenuOrdersAdapter;
import com.example.gek.pizza.adapters.NewsAdapter;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.MenuGroup;
import com.example.gek.pizza.data.News;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MenuOrdersActivity extends AppCompatActivity {

    private static final String TAG = "Menu orders ";
    private RecyclerView rv;
    private Context ctx = this;
    private FloatingActionButton fab;
    private MenuOrdersAdapter menuOrdersAdapter;
    private ArrayList<MenuGroup> listMenuGroup;
    private GridLayoutManager lLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_orders);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_menu_order);
        setSupportActionBar(myToolbar);

        rv = (RecyclerView) findViewById(R.id.rv);

        // задаем лаяют с твумя столбцами
        lLayout = new GridLayoutManager(MenuOrdersActivity.this, 2);
        rv.setLayoutManager(lLayout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
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
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
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

}
