package com.example.gek.pizza.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.helpers.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import static com.example.gek.pizza.data.Const.db;

public class DishEditActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isNewDish = true;
    private Dish oldDish;
    private Dish changedDish;
    private Uri uriPhoto;

    ProgressBar progressBar;
    EditText etName, etDescription, etPrice;
    ImageView ivPhoto;
    Button btnRemovePhoto, btnOk;
    private StorageReference folderRef;
    private Boolean isNeedRemovePhoto = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_edit);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etName = (EditText) findViewById(R.id.etName);
        etPrice = (EditText) findViewById(R.id.etPrice);
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

        // Определяем это новое блюдо или редактирование старого
        if (getIntent().hasExtra(Const.MODE) &&
                (getIntent().getIntExtra(Const.MODE, Const.MODE_NEW) == Const.MODE_EDIT)){
            isNewDish = false;
            oldDish = getIntent().getParcelableExtra(Const.EXTRA_DISH);
            String title = getResources().getString(R.string.edit) + " - " + oldDish.getName();
            myToolbar.setTitle(title);
            fillValues(oldDish);
        } else {
            fillValues(null);
            myToolbar.setTitle(R.string.create_new);
        }

        // Получаем ссылку на наше хранилище
        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReferenceFromUrl(Const.STORAGE).child(Const.DISHES_IMAGES_FOLDER);
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

                if ((!isNewDish) && (oldDish.getPhotoName().length() > 0)) {
                    isNeedRemovePhoto = true;
                }
                btnRemovePhoto.setVisibility(View.VISIBLE);
            }
        } else {
            uriPhoto = null;
        }
    }

    private void fillValues(Dish dish){
        if (dish == null) {
            etName.setText("");
            etDescription.setText("");
            etPrice.setText("");
            uriPhoto = null;
            ivPhoto.setImageResource(R.drawable.news_icon);
            btnRemovePhoto.setVisibility(View.INVISIBLE);
        } else {
            etName.setText(dish.getName());
            etDescription.setText(dish.getDescription());
            etPrice.setText(Float.toString(dish.getPrice()));
            if (dish.getPhotoUrl().length() > 0){
                Glide.with(this)
                        .load(dish.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.news_icon)
                        .into(ivPhoto);
                btnRemovePhoto.setVisibility(View.VISIBLE);
            } else
                ivPhoto.setImageResource(R.drawable.news_icon);
        }
    }



    /** Запись на сервер данных */
    private void sendToServer(){
        final String title = etName.getText().toString();
        final String description = etDescription.getText().toString();
        //todo сделать проверку корректности ввода значения
        final float price = Float.parseFloat(etPrice.getText().toString());

        progressBar.setVisibility(View.VISIBLE);

        // Если выбранно фото с галереи то сначало грузим фото, а потом запишем карточку в БД
        // Удаляем старое фото если оно было
        if (uriPhoto != null) {
            final String photoName = makePhotoName();
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
                    Dish newDish = new Dish(title, description, price, photoUrl.toString(), photoName);
                    if (isNewDish){
                        String key = db.child(Const.CHILD_DISHES).push().getKey();
                        newDish.setKey(key);
                        db.child(Const.CHILD_DISHES).child(key).setValue(newDish);
                    } else {
                        if (isNeedRemovePhoto) {
                            removeOldPhoto(oldDish.getPhotoName());
                        }
                        String oldKey = oldDish.getKey();
                        newDish.setKey(oldKey);
                        db.child(Const.CHILD_DISHES).child(oldKey).setValue(newDish);
                        changedDish = newDish;
                    }
                    fillValues(null);
                    progressBar.setVisibility(View.GONE);
                    if (!isNewDish) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Const.CHILD_DISHES, changedDish);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });

            // Если фото не выбирали то просто делаем запись в БД с изменениями
            // Удаляем старое фото если его удалил пользователь
        } else {
            Dish newDish = new Dish(title, description, price, "", "");
            if (isNewDish){
                String newKey = db.child(Const.CHILD_DISHES).push().getKey();
                newDish.setKey(newKey);
                db.child(Const.CHILD_DISHES).child(newKey).setValue(newDish);
            } else {
                if (isNeedRemovePhoto){
                    removeOldPhoto(oldDish.getPhotoName());
                    newDish.setPhotoName("");
                    newDish.setPhotoUrl("");
                } else {
                    newDish.setPhotoName(oldDish.getPhotoName());
                    newDish.setPhotoUrl(oldDish.getPhotoUrl());
                }
                String oldKey = oldDish.getKey();
                newDish.setKey(oldKey);
                db.child(Const.CHILD_DISHES).child(oldKey).setValue(newDish);
                changedDish = newDish;
            }
            progressBar.setVisibility(View.GONE);
            if (!isNewDish) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.CHILD_DISHES, changedDish);
                setResult(RESULT_OK, resultIntent);
            }
            finish();
        }
    }



    /** Формируем имя для фотки из данных пользователя. Убираем нежелательные символы */
    //todo довести до ума код и вынести его в класс Utils
    private String makePhotoName(){
        String time = Calendar.getInstance().getTime().toString();
        String name = etName.getText().toString() + time;
        name = name.replace(".", "");
        name = name.replace("@", "");
        name = name.replace(" ", "");
        name = name.replace("#", "");
        name = name + ".jpg";
        return  name;
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnOk:
                sendToServer();
                break;
            case R.id.btnRemovePhoto:
                if ((oldDish != null) &&(oldDish.getPhotoName().length() > 0)){
                    isNeedRemovePhoto = true;
                }
                uriPhoto = null;
                ivPhoto.setImageResource(R.drawable.news_icon);
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
            storageRef.child(Const.DISHES_IMAGES_FOLDER).child(namePhoto).delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


}
