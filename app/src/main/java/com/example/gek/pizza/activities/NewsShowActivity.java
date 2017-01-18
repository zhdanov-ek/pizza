package com.example.gek.pizza.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class NewsShowActivity extends AppCompatActivity {
    TextView tvTitle, tvDescription;
    ImageView ivPhoto;
    News openNews;

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
            openNews = getIntent().getParcelableExtra(Const.EXTRA_NEWS);
            fillValues(openNews);
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
                editNews();
                break;
            case R.id.ab_remove:
                removeNews();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Редактирование существующей записи */
    private void editNews(){
        Intent editNewsIntent = new Intent(this, NewsEditActivity.class);
        editNewsIntent.putExtra(Const.MODE, Const.MODE_EDIT);
        editNewsIntent.putExtra(Const.EXTRA_NEWS, openNews);
        startActivityForResult(editNewsIntent, Const.REQUEST_EDIT_NEWS);
    }

    /** обновляем информацию в активити если ее успешно отредактировали */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && (requestCode == Const.REQUEST_EDIT_NEWS) && (resultCode == RESULT_OK)){
            openNews = data.getParcelableExtra(Const.EXTRA_NEWS);
            fillValues(openNews);
        }
    }

    /** Заполняем все поля активити */
    private void fillValues(News news){
        tvTitle.setText(news.getTitle());
        tvDescription.setText(news.getDescription());
        if ((news.getPhotoUrl() != null) && (news.getPhotoUrl().length() > 0)){
            Glide.with(this)
                    .load(news.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.news_icon)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.news_icon);
        }
    }

    /** Удаление новости из базы и фото с хранилища */
    private void removeNews(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove);
        builder.setIcon(R.drawable.ic_warning);
        String message = getResources().getString(R.string.confirm_remove_item);
        builder.setMessage(message + "\n" + openNews.getTitle());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Получаем ссылку на наше хранилище и удаляем фото по названию
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
                if (openNews.getPhotoUrl().length() > 0){
                    storageRef.child(Const.NEWS_IMAGES_FOLDER).child(openNews.getPhotoName()).delete();
                }

                // Получаем ссылку на базу данных и удаляем новость по ключу открытой новости
                Const.db.child(Const.CHILD_NEWS).child(openNews.getKey()).removeValue();
                finish();
            }
        });
        builder.show();
    }
}
