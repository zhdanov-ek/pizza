package com.example.gek.pizza.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.MenuGroup;
import com.example.gek.pizza.helpers.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

import static com.example.gek.pizza.data.Const.db;

/**
 * Edit group of dishes
 * */

public class MenuOrdersEditActivity extends BaseActivity implements View.OnClickListener{

    private boolean isNewMenuGroup = true;
    private MenuGroup oldMenuGroup;
    private MenuGroup changedMenuGroup;
    private Uri uriPhoto;
    private int color;

    private LinearLayout llContainer;
    ProgressBar progressBar;
    EditText etName;
    TextView tvName;
    ImageView ivPhoto;
    Button btnOk, btnCancel;
    private StorageReference folderRef;
    private Boolean isNeedRemovePhoto = false;


    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_orders_edit);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Random color for background of item
        int[] colors = getResources().getIntArray(R.array.colors);
        color = colors[new Random().nextInt(colors.length - 1)];

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        llContainer = (LinearLayout) findViewById(R.id.llContainer);
        tvName = (TextView) findViewById(R.id.tvName);

        etName = (EditText) findViewById(R.id.etName);
        etName.addTextChangedListener(textWatcher);

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivPhoto.setOnClickListener(this);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);


        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.mes_no_internet, Toast.LENGTH_SHORT).show();
            btnOk.setVisibility(View.GONE);
        }

        // Определяем это новая запись или редактирование старой
        if (getIntent().hasExtra(Const.MODE) &&
                (getIntent().getIntExtra(Const.MODE, Const.MODE_NEW) == Const.MODE_EDIT)){
            isNewMenuGroup = false;
            oldMenuGroup = getIntent().getParcelableExtra(Const.EXTRA_MENU_GROUP);
            String title = getResources().getString(R.string.edit) + " - " + oldMenuGroup.getName();
            myToolbar.setTitle(title);
            fillValues(oldMenuGroup);
        } else {
            fillValues(null);
            myToolbar.setTitle(R.string.create_new);
        }

        // Получаем ссылку на наше хранилище
        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReferenceFromUrl(Const.STORAGE).child(Const.MENU_GROUP_IMAGES_FOLDER);
    }



    /** Получаем URI фото с галереи и показываем его в макете */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            if (requestCode == Const.REQUEST_LOAD_IMG ) {
                uriPhoto = data.getData();
                Glide.with(this)
                        .load(uriPhoto)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.menu_group_empty)
                        .into(ivPhoto);
                if ((!isNewMenuGroup) && (oldMenuGroup.getPhotoName().length() > 0)) {
                    isNeedRemovePhoto = true;
                }
            }
        } else {
            uriPhoto = null;
        }
    }

    private void fillValues(MenuGroup menuGroup){
        llContainer.setBackgroundColor(color);
        if (menuGroup == null) {
            tvName.setText("");
            uriPhoto = null;
            Glide.with(this)
                    .load(R.drawable.menu_group_empty)
                    .into(ivPhoto);
        } else {
            tvName.setText(menuGroup.getName());
            etName.setText(menuGroup.getName());
            if (menuGroup.getPhotoUrl().length() > 0){
                Glide.with(this)
                        .load(menuGroup.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.menu_group_empty)
                        .into(ivPhoto);
            } else {
                Glide.with(this)
                        .load(R.drawable.menu_group_empty)
                        .into(ivPhoto);
            }

        }
    }



    /** Запись на сервер данных */
    private void sendToServer(){
        final String name = tvName.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        // Если выбранно фото с галереи то сначало грузим фото, а потом запишем карточку в БД
        // Удаляем старое фото если оно было
        if (uriPhoto != null) {
            final String photoName = Utils.makePhotoName(tvName.getText().toString());
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
                    MenuGroup menuGroup = new MenuGroup(name, photoUrl.toString(), photoName);
                    if (isNewMenuGroup){
                        String key = db.child(Const.CHILD_MENU_GROUPS).push().getKey();
                        menuGroup.setKey(key);
                        db.child(Const.CHILD_MENU_GROUPS).child(key).setValue(menuGroup);
                    } else {
                        if (isNeedRemovePhoto) {
                            removeOldPhoto(oldMenuGroup.getPhotoName());
                        }
                        String oldKey = oldMenuGroup.getKey();
                        menuGroup.setKey(oldKey);
                        db.child(Const.CHILD_MENU_GROUPS).child(oldKey).setValue(menuGroup);
                        changedMenuGroup = menuGroup;
                    }
                    fillValues(null);
                    progressBar.setVisibility(View.GONE);
                    if (!isNewMenuGroup) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Const.EXTRA_MENU_GROUP, changedMenuGroup);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });

            // Если фото не выбирали то просто делаем запись в БД с изменениями
            // Удаляем старое фото если его удалил пользователь
        } else {
            MenuGroup menuGroup = new MenuGroup(name, "", "");
            if (isNewMenuGroup){
                String newKey = db.child(Const.CHILD_MENU_GROUPS).push().getKey();
                menuGroup.setKey(newKey);
                db.child(Const.CHILD_MENU_GROUPS).child(newKey).setValue(menuGroup);
            } else {
                if (isNeedRemovePhoto){
                    removeOldPhoto(oldMenuGroup.getPhotoName());
                    menuGroup.setPhotoName("");
                    menuGroup.setPhotoUrl("");
                } else {
                    menuGroup.setPhotoName(oldMenuGroup.getPhotoName());
                    menuGroup.setPhotoUrl(oldMenuGroup.getPhotoUrl());
                }
                String oldKey = oldMenuGroup.getKey();
                menuGroup.setKey(oldKey);
                db.child(Const.CHILD_MENU_GROUPS).child(oldKey).setValue(menuGroup);
                changedMenuGroup = menuGroup;
            }
            progressBar.setVisibility(View.GONE);
            if (!isNewMenuGroup) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.CHILD_MENU_GROUPS, changedMenuGroup);
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
            case R.id.ivPhoto:
                Utils.choosePhoto(this);
                break;
            case R.id.btnCancel:
                finish();
                break;

        }
    }

    private void removeOldPhoto(String namePhoto){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
        if (namePhoto.length() > 0){
            storageRef.child(Const.MENU_GROUP_IMAGES_FOLDER).child(namePhoto).delete();
        }
    }


    /** Переносим введенный текст в едит вью сразу в наш макет */
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //todo Проверяем ввод не допустимых символов
            tvName.setText(charSequence);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}
