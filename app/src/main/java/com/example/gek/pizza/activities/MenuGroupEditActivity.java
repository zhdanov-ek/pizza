package com.example.gek.pizza.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import com.example.gek.pizza.helpers.Connection;
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

public class MenuGroupEditActivity extends BaseActivity implements View.OnClickListener{

    private static final String STATE_BUTTON_OK = "button_ok";
    private static final String STATE_URI_PHOTO = "uri_photo";
    private static final String STATE_URI_HAVE = "uri_have";

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
    private Boolean isSelectPhoto = false;


    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_menu_orders_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // Random color for background of item
        int[] colors = getResources().getIntArray(R.array.colors);
        color = colors[new Random().nextInt(colors.length - 1)];

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        llContainer = (LinearLayout) findViewById(R.id.llContainer);
        tvName = (TextView) findViewById(R.id.tvAuthName);

        etName = (EditText) findViewById(R.id.etName);
        etName.addTextChangedListener(textWatcher);
        etName.setFilters(new InputFilter[] {Utils.getInputFilterSymbol()});

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

        // New or edit old group?
        if (getIntent().hasExtra(Const.MODE) &&
                (getIntent().getIntExtra(Const.MODE, Const.MODE_NEW) == Const.MODE_EDIT)){
            isNewMenuGroup = false;
            btnOk.setEnabled(true);
            isSelectPhoto = true;
            oldMenuGroup = getIntent().getParcelableExtra(Const.EXTRA_MENU_GROUP);
            String title = getResources().getString(R.string.edit) + " - " + oldMenuGroup.getName();
            toolbar.setTitle(title);
            fillValues(oldMenuGroup);
        } else {
            fillValues(null);
            toolbar.setTitle(R.string.create_new);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        folderRef = storage.getReferenceFromUrl(Const.STORAGE).child(Const.MENU_GROUP_IMAGES_FOLDER);

        if (savedInstanceState != null){
            if (savedInstanceState.getBoolean(STATE_BUTTON_OK)){
                btnOk.setEnabled(true);
            } else {
                btnOk.setEnabled(false);
            }
            if (savedInstanceState.getBoolean(STATE_URI_HAVE)){
                isSelectPhoto = true;
                uriPhoto = Uri.parse(savedInstanceState.getString(STATE_URI_PHOTO));
                Glide.with(this)
                        .load(uriPhoto)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.menu_group_empty)
                        .into(ivPhoto);
            }
        }
    }



    /** Fetch URI image choosed in gallery and show in preview */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            if (requestCode == Const.REQUEST_LOAD_IMG ) {
                uriPhoto = data.getData();
                isSelectPhoto = true;
                if (etName.length() > 0) {
                    btnOk.setEnabled(true);
                }
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


    /** Write data to server */
    private void sendToServer(){
        final String name = tvName.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        if (uriPhoto != null) {
            final String photoName = Utils.makePhotoName(tvName.getText().toString());
            StorageReference currentImageRef = folderRef.child(photoName);
            UploadTask uploadTask = currentImageRef.putFile(uriPhoto);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getBaseContext(),
                            getBaseContext().getResources().getString(R.string.mes_error_load_image),
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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
                    }
                    finish();
                }
            });

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


    /** Copy text in preview */
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            tvName.setText(charSequence);
            if ((charSequence.length() > 0 ) && (isSelectPhoto)){
                btnOk.setEnabled(true);
            } else {
                btnOk.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_BUTTON_OK, btnOk.isEnabled());
        if (uriPhoto != null) {
            savedInstanceState.putBoolean(STATE_URI_HAVE, true);
            savedInstanceState.putString(STATE_URI_PHOTO, uriPhoto.toString());
        } else {
            savedInstanceState.putBoolean(STATE_URI_HAVE, false);
        }
    }

}
