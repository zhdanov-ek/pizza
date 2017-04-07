package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.News;
import com.example.gek.pizza.adapters.NewsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NewsActivity extends BaseActivity{

    private static final String TAG = "LIST_NEWS";
    private ArrayList<News> allNews;
    private RecyclerView rv;
    private NewsAdapter newsAdapter;
    private FloatingActionButton fab;
    private ValueEventListener mNewsValueListener;
    private Query mGetNewsSorted;
    private Context ctx;

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_news);
        item.setCheckable(true);
        item.setChecked(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_news);
        setToolbar(getString(R.string.title_news));

        ctx = this;
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);

        allNews = new ArrayList<>();

        // Make request for retrieve news with sorting with timeStamp
        initNewsListener();
        mGetNewsSorted = Const.db.child(Const.CHILD_NEWS).orderByChild("timeStamp");
        mGetNewsSorted.addValueEventListener(mNewsValueListener);
    }

    /** Initialization news listener */
    private void initNewsListener(){
        mNewsValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allNews.clear();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    News currentNews = child.getValue(News.class);
                    allNews.add(currentNews);
                }
                if (allNews.size() == 0) {
                    Toast.makeText(ctx, R.string.mes_no_records, Toast.LENGTH_LONG).show();
                }
                newsAdapter = new NewsAdapter(ctx, allNews);
                rv.setAdapter(newsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ", databaseError.toException() );
            }
        };
    }

    /** Add new news */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addNews = new Intent(ctx, NewsEditActivity.class);
            addNews.putExtra(Const.MODE, Const.MODE_NEW);
            startActivity(addNews);
        }
    };


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
    protected void onDestroy() {
        super.onDestroy();
        if ((mNewsValueListener != null) && (mGetNewsSorted != null)){
            mGetNewsSorted.removeEventListener(mNewsValueListener);
        }
    }
}
