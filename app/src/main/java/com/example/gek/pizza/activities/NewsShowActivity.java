package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.News;

public class NewsShowActivity extends AppCompatActivity {
    TextView tvTitle, tvDescription;
    ImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_show);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);

        if (getIntent().hasExtra(Const.EXTRA_NEWS)){
            News openNews = getIntent().getParcelableExtra(Const.EXTRA_NEWS);
            tvTitle.setText(openNews.getTitle());
            tvDescription.setText(openNews.getDescription());
            if ((openNews.getPhotoUrl() != null) && (openNews.getPhotoUrl().length() > 0)){
                Glide.with(this)
                        .load(openNews.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.news_icon)
                        .into(ivPhoto);
            } else {
                ivPhoto.setImageResource(R.drawable.news_icon);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_edit:
                Toast.makeText(this, "edit", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ab_remove:
                Toast.makeText(this, "remove", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
