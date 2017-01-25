package com.example.gek.pizza.activities;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.News;
import com.example.gek.pizza.helpers.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import static com.example.gek.pizza.data.Const.db;

public class NewsEditActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isNewNews = true;
    private News oldNews;
    private News changedNews;
    private Uri uriPhoto;

    ProgressBar progressBar;
    EditText etTitle, etDescription;
    ImageView ivPhoto;
    Button btnRemovePhoto, btnOk;
    private StorageReference folderRef;
    private Boolean isNeedRemovePhoto = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_edit);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivPhoto.setOnClickListener(this);
        btnRemovePhoto = (Button) findViewById(R.id.btnRemovePhoto);
        btnRemovePhoto.setOnClickListener(this);
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);


        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.mes_no_internet, Toast.LENGTH_SHORT).show();
            btnOk.setVisibility(View.GONE);
        }

        // Определяем это новая новость или редактирование старой
        if (getIntent().hasExtra(Const.MODE) &&
                (getIntent().getIntExtra(Const.MODE, Const.MODE_NEW) == Const.MODE_EDIT)){
            isNewNews = false;
            oldNews = getIntent().getParcelableExtra(Const.EXTRA_NEWS);
            String title = getResources().getString(R.string.edit) + " - " + oldNews.getTitle();
            myToolbar.setTitle(title);
            fillValues(oldNews);
        } else {
            fillValues(null);
            myToolbar.setTitle(R.string.create_new);
        }

        // Получаем ссылку на наше хранилище
        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReferenceFromUrl(Const.STORAGE).child(Const.NEWS_IMAGES_FOLDER);
    }



    /** Получаем URI фото с галереи */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            if (requestCode == Const.REQUEST_LOAD_IMG ) {
                Uri uri = data.getData();
                ivPhoto.setImageURI(uri);
                uriPhoto = uri;

                if ((!isNewNews) && (oldNews.getPhotoName().length() > 0)) {
                    isNeedRemovePhoto = true;
                }
                btnRemovePhoto.setVisibility(View.VISIBLE);
            }
        } else {
            uriPhoto = null;
        }
    }

    private void fillValues(News news){
        if (news == null) {
            etTitle.setText("");
            etDescription.setText("");
            uriPhoto = null;
            ivPhoto.setImageResource(R.drawable.news_empty);
            btnRemovePhoto.setVisibility(View.INVISIBLE);
        } else {
            etTitle.setText(news.getTitle());
            etDescription.setText(news.getDescription());
            if (news.getPhotoUrl().length() > 0){
                Glide.with(this)
                        .load(news.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.news_empty)
                        .into(ivPhoto);
                btnRemovePhoto.setVisibility(View.VISIBLE);
            } else
                ivPhoto.setImageResource(R.drawable.news_empty);
        }
    }



    /** Запись на сервер данных */
    private void sendToServer(){
        final String title = etTitle.getText().toString();
        final String description = etDescription.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        // Если выбранно фото с галереи то сначало грузим фото, а потом запишем карточку в БД
        // Удаляем старое фото если оно было
        if (uriPhoto != null) {
            final String photoName = Utils.makePhotoName(etTitle.getText().toString());
            StorageReference currentImageRef = folderRef.child(photoName);
            UploadTask uploadTask = currentImageRef.putFile(uriPhoto);

            // Регистрируем слушатель для контроля загрузки файла на сервер.
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getBaseContext(), "Loading image to server: ERROR", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Получаем ссылку на закачанный файл и сохраняем ее
                    Uri photoUrl = taskSnapshot.getDownloadUrl();
                    News newNews = new News(title, description, photoUrl.toString(), photoName);
                    if (isNewNews){
                        String key = db.child(Const.CHILD_NEWS).push().getKey();
                        newNews.setKey(key);
                        db.child(Const.CHILD_NEWS).child(key).setValue(newNews);
                    } else {
                        if (isNeedRemovePhoto) {
                            removeOldPhoto(oldNews.getPhotoName());
                        }
                        String oldKey = oldNews.getKey();
                        newNews.setKey(oldKey);
                        db.child(Const.CHILD_NEWS).child(oldKey).setValue(newNews);
                        changedNews = newNews;
                    }
                    fillValues(null);
                    progressBar.setVisibility(View.GONE);
                    if (!isNewNews) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Const.EXTRA_NEWS, changedNews);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });

            // Если фото не выбирали то просто делаем запись в БД с изменениями
            // Удаляем старое фото если его удалил пользователь
        } else {
            News newNews = new News(title, description, "", "");
            if (isNewNews){
                String newKey = db.child(Const.CHILD_NEWS).push().getKey();
                newNews.setKey(newKey);
                db.child(Const.CHILD_NEWS).child(newKey).setValue(newNews);
            } else {
                if (isNeedRemovePhoto){
                    removeOldPhoto(oldNews.getPhotoName());
                    newNews.setPhotoName("");
                    newNews.setPhotoUrl("");
                } else {
                    newNews.setPhotoName(oldNews.getPhotoName());
                    newNews.setPhotoUrl(oldNews.getPhotoUrl());
                }
                String oldKey = oldNews.getKey();
                newNews.setKey(oldKey);
                db.child(Const.CHILD_NEWS).child(oldKey).setValue(newNews);
                changedNews = newNews;
            }
            progressBar.setVisibility(View.GONE);
            if (!isNewNews) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.EXTRA_NEWS, changedNews);
                setResult(RESULT_OK, resultIntent);
            }
            finish();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnOk:
                sendToServer();
                break;
            case R.id.btnRemovePhoto:
                if ((oldNews != null) &&(oldNews.getPhotoName().length() > 0)){
                    isNeedRemovePhoto = true;
                }
                uriPhoto = null;
                ivPhoto.setImageResource(R.drawable.news_empty);
                btnRemovePhoto.setVisibility(View.INVISIBLE);
                break;
            case R.id.ivPhoto:
                Utils.choosePhoto(this);
                break;
        }
    }

    private void removeOldPhoto(String namePhoto){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
        if (namePhoto.length() > 0){
            storageRef.child(Const.NEWS_IMAGES_FOLDER).child(namePhoto).delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


}
