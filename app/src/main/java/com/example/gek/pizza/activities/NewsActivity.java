package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.News;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private ArrayList<News> allNews;
    private RecyclerView rv;
//    private UsersAdapter usersAdapter;
    private Context ctx = this;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_activities);

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabListener);

        allNews = new ArrayList<>();


    }

    /** Запуск активити на добавление новости */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addNews = new Intent(ctx, NewsEditActivity.class);
            addNews.putExtra(Const.MODE, Const.MODE_NEW);
            startActivity(addNews);
        }
    };
}
