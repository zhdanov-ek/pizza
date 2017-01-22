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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
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


        rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new ClickListener() {

            @Override
            public void onClick(View view, int position) {
                //Values are passing to activity & to fragment as well
                Toast.makeText(MenuOrdersActivity.this, "Single Click on position : " + position,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(MenuOrdersActivity.this, "Long press on position : " + position,
                        Toast.LENGTH_LONG).show();
            }

        }));

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

    /** Запуск активити на добавление группы меню */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addMenuGroup = new Intent(ctx, MenuOrdersEditActivity.class);
            addMenuGroup.putExtra(Const.MODE, Const.MODE_NEW);
            startActivity(addMenuGroup);
        }
    };



    public interface ClickListener{
         void onClick(View view,int position);
         void onLongClick(View view,int position);
    }


    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }


        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

}
