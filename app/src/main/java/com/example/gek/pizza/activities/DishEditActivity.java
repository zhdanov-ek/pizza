package com.example.gek.pizza.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.MenuGroup;
import com.example.gek.pizza.helpers.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

public class DishEditActivity extends BaseActivity implements View.OnClickListener{

    private static final String STATE_URI_PHOTO = "uri_photo";
    private static final String STATE_URI_HAVE = "uri_have";
    private Context ctx;
    private boolean isNewDish = true;
    private Dish oldDish;
    private Dish changedDish;
    private Uri uriPhoto;
    private ProgressBar progressBar;
    private Spinner spinnerGroup;
    private EditText etName, etDescription, etPrice;
    private ImageView ivPhoto;
    private Button btnOk;
    private ImageButton ibRemovePhoto;
    private StorageReference folderRef;
    private Boolean isNeedRemovePhoto = false;
    private String keyGroup = "";
    public static final String TAG = "DishEditActivity ";
    private ArrayList<MenuGroup> listMenuGroups;
    private ArrayList<String> listNameGroups;
    private ArrayAdapter<String> adapter;


    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Content inflate in VIEW and put in DrawerLayout
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_dish_edit, null, false);
        mDrawer.addView(contentView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        ctx = this;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        spinnerGroup = (Spinner) findViewById(R.id.spinnerGroup);
        etName = (EditText) findViewById(R.id.etName);
        etPrice = (EditText) findViewById(R.id.etPrice);
        etDescription = (EditText) findViewById(R.id.etDescription);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivPhoto.setOnClickListener(this);
        ibRemovePhoto = (ImageButton) findViewById(R.id.ibRemovePhoto);
        ibRemovePhoto.setOnClickListener(this);
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        etName.addTextChangedListener(textWatcher);
        etPrice.addTextChangedListener(textWatcher);

        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.mes_no_internet, Toast.LENGTH_SHORT).show();
            btnOk.setVisibility(View.GONE);
        }


        // Определяем это новое блюдо или редактирование старого
        if (getIntent().hasExtra(Const.MODE) &&
                (getIntent().getIntExtra(Const.MODE, Const.MODE_NEW) == Const.MODE_EDIT)){
            isNewDish = false;
            oldDish = getIntent().getParcelableExtra(Const.EXTRA_DISH);
            keyGroup = oldDish.getKeyGroup();
            String title = getResources().getString(R.string.edit) + " - " + oldDish.getName();
            toolbar.setTitle(title);
            spinnerGroup.setVisibility(View.VISIBLE);
            loadListGroupsMenu();
            fillValues(oldDish);
        } else {
            fillValues(null);
            keyGroup = getIntent().getStringExtra(Const.DISH_GROUP_KEY);
            toolbar.setTitle(R.string.create_new);
        }

        // Получаем ссылку на наше хранилище
        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReferenceFromUrl(Const.STORAGE).child(Const.DISHES_IMAGES_FOLDER);

        // анализируем переданные данные в активити если таковые были (при повороте, скрытии и т.д.)
        if (savedInstanceState != null){
            if (savedInstanceState.getBoolean(STATE_URI_HAVE)){
                ibRemovePhoto.setVisibility(View.VISIBLE);
                uriPhoto = Uri.parse(savedInstanceState.getString(STATE_URI_PHOTO));
                Glide.with(this)
                        .load(uriPhoto)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.dish_empty)
                        .into(ivPhoto);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_dishes);
        item.setCheckable(true);
        item.setChecked(true);
    }

    private void loadListGroupsMenu(){
        // Описываем слушатель, который вернет один раз в программу весь список данных,
        // которые находятся в child(CHILD_MENU_GROUPS)
        // Полученные данные грузим в Spinner для того, что бы можно было блюдо восстановить
        // или переместить в другую группу меню
        ValueEventListener groupMenuListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Добавляем первой заглушку-группу для удаленных блюд
                MenuGroup removedGroup = new MenuGroup();
                removedGroup.setName(getResources().getString(R.string.title_archive_dishes));
                removedGroup.setKey(Const.DISH_GROUP_VALUE_REMOVED);
                listNameGroups = new ArrayList<>();
                listNameGroups.add(removedGroup.getName());
                listMenuGroups = new ArrayList<>();
                listMenuGroups.add(removedGroup);

                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    MenuGroup menuGroup = child.getValue(MenuGroup.class);
                    listMenuGroups.add(menuGroup);
                    listNameGroups.add(menuGroup.getName());
                }

                adapter = new ArrayAdapter<>(
                        ctx,
                        android.R.layout.simple_spinner_item,
                        listNameGroups);
                spinnerGroup.setAdapter(adapter);

                // Показываем в спинере текущую группу
                for (int i = 0; i < listMenuGroups.size(); i++) {
                    if (keyGroup.contentEquals(listMenuGroups.get(i).getKey())){
                        spinnerGroup.setSelection(i);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        // Сам заброс данных из базы
        Const.db.child(Const.CHILD_MENU_GROUPS).addListenerForSingleValueEvent(groupMenuListener);
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
                ibRemovePhoto.setVisibility(View.VISIBLE);
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
            ivPhoto.setImageResource(R.drawable.dish_empty);
            ibRemovePhoto.setVisibility(View.INVISIBLE);
            btnOk.setEnabled(false);
        } else {
            etName.setText(dish.getName());
            etDescription.setText(dish.getDescription());
            etPrice.setText(Float.toString(dish.getPrice()));
            if (dish.getPhotoUrl().length() > 0){
                Glide.with(this)
                        .load(dish.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.dish_empty)
                        .into(ivPhoto);
                ibRemovePhoto.setVisibility(View.VISIBLE);
            } else {
                ivPhoto.setImageResource(R.drawable.dish_empty);
                ibRemovePhoto.setVisibility(View.INVISIBLE);
            }
        }
    }



    /** Запись на сервер данных */
    private void sendToServer(){
        final String title = etName.getText().toString();
        final String description = etDescription.getText().toString();
        //todo сделать проверку корректности ввода значения
        final float price = Float.parseFloat(etPrice.getText().toString());
        if (listMenuGroups != null) {
            keyGroup = listMenuGroups.get(spinnerGroup.getSelectedItemPosition()).getKey();
        }

        progressBar.setVisibility(View.VISIBLE);

        // Если выбранно фото с галереи то сначало грузим фото, а потом запишем карточку в БД
        // Удаляем старое фото если оно было
        if (uriPhoto != null) {
            final String photoName = Utils.makePhotoName(etName.getText().toString());
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
                    Dish newDish = new Dish(title, description, price, photoUrl.toString(), photoName, keyGroup);
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
                        resultIntent.putExtra(Const.EXTRA_DISH, changedDish);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });

            // Если фото не выбирали то просто делаем запись в БД с изменениями
            // Удаляем старое фото если его удалил пользователь
        } else {
            Dish newDish = new Dish(title, description, price, "", "", keyGroup);
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
                resultIntent.putExtra(Const.EXTRA_DISH, changedDish);
                setResult(RESULT_OK, resultIntent);
            }
            finish();

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCancel:
                finish();
                break;
            case R.id.btnOk:
                sendToServer();
                break;
            case R.id.ibRemovePhoto:
                if ((oldDish != null) &&(oldDish.getPhotoName().length() > 0)){
                    isNeedRemovePhoto = true;
                }
                uriPhoto = null;
                ivPhoto.setImageResource(R.drawable.dish_empty);
                ibRemovePhoto.setVisibility(View.INVISIBLE);
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


    /** Сохраняем данные на случай переворота дисплея или сворачивания окна */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (uriPhoto != null) {
            savedInstanceState.putBoolean(STATE_URI_HAVE, true);
            savedInstanceState.putString(STATE_URI_PHOTO, uriPhoto.toString());
        } else {
            savedInstanceState.putBoolean(STATE_URI_HAVE, false);
        }
    }

    /** Отслеживаем изменения в полях Name и Price */
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if ((etPrice.length() > 0 ) && (etName.length() > 0)){
                btnOk.setEnabled(true);
            } else {
                btnOk.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


}
