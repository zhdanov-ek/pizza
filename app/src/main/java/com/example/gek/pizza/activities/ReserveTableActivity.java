package com.example.gek.pizza.activities;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.OrderTable;
import com.example.gek.pizza.data.StateTableReservation;
import com.example.gek.pizza.data.Table;
import com.example.gek.pizza.helpers.RotationGestureDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.example.gek.pizza.data.Const.db;

/**
 * Класс для расстановки столов, и их заказов
 */

public class ReserveTableActivity extends BaseActivity implements RotationGestureDetector.OnRotationGestureListener {

    private ImageView ivAddTable6, ivAddTable8, ivAddTable4;
    private String textPhone, textEmail, textAddress;
    private TextView tvPhone, tvEmail, tvAddress, tvTitleSettings;
    private RelativeLayout rlReserveTable, rlSettingsReserveTable;
    private LinearLayout llAboutUs;
    private ImageView ivTable, ivOldTable;
    private android.widget.RelativeLayout.LayoutParams layoutParams;
    private String msg = "DRAGDROP";
    private int xCoordinate, yCootdinate;
    private int windowWidth, windowHeight;
    public int idTable;
    public String tableName;
    private DisplayMetrics displayMetrics;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Animation animationArrowRotation;
    private boolean isPanelExpanded = false, isPanelCollapsedDragAndDrop = false;
    boolean isNewTable;
    private ArrayList<Table> allTables;
    private ArrayList<OrderTable> allReservedTables;
    private Button btnOk, btnRemove;
    private Toolbar myToolbar;
    private ImageButton ibTrash;
    private SimpleDateFormat shortenedDateFormat;
    private FloatingActionButton fabSaveSchema, fabReserveTable, fabConfirm, fabCancel;
    private Toast toastReserved;
//    private String tableKeyNotification;
    private HashMap<String, Boolean> hmReservedConfirmed;

    private final String IS_PANEL_EXPANDED = "is_panel_expanded";
    private final String IS_PANEL_COLLAPSED_DRAG_AND_DROP = "is_panel_collapsed_drag_and_drop";
    private final String TABLES_MARKERS = "tables_markers";
    private final String TABLE_RESERVED = "table_reserved";
    private final String TABLE_RESERVATION_CONFIRMED = "table_reservation_confirmed";
    private RotationGestureDetector mRotationDetector;

    public static boolean activeReserveTableActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activeReserveTableActivity = false;

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_reserve_table, null, false);
        mDrawer.addView(contentView, 0);

//        tableKeyNotification = "";
//        if (getIntent().hasExtra(Const.OPEN_FROM_NOTIFICATION)) {
//            tableKeyNotification = getIntent().getStringExtra(Const.OPEN_FROM_NOTIFICATION);
//        }


        shortenedDateFormat = new SimpleDateFormat("yyyyMMdd");

        fabSaveSchema = (FloatingActionButton) findViewById(R.id.fabSaveSchema);
        fabSaveSchema.setOnClickListener(onClickListenerBtn);

        fabReserveTable = (FloatingActionButton) findViewById(R.id.fabReserveTable);
        fabReserveTable.setOnClickListener(onClickListenerBtn);

        fabConfirm = (FloatingActionButton) findViewById(R.id.fabConfirm);
        fabConfirm.setOnClickListener(onClickListenerBtn);

        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        fabCancel.setOnClickListener(onClickListenerBtn);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        textAddress = sharedPreferences.getString(Const.SETTINGS_ADDRESS_KEY, "");
        textEmail = sharedPreferences.getString(Const.SETTINGS_EMAIL_KEY, "");
        textPhone = sharedPreferences.getString(Const.SETTINGS_PHONE_KEY, "");

        tvPhone = (TextView) findViewById(R.id.tvAboutPhone);
        tvEmail = (TextView) findViewById(R.id.tvAboutEmail);
        tvAddress = (TextView) findViewById(R.id.tvAboutAddress);
        tvTitleSettings = (TextView) findViewById(R.id.tvTitleSettings);

        setSettingsToView(tvPhone, textPhone);
        setSettingsToView(tvEmail, textEmail);
        setSettingsToView(tvAddress, textAddress);

        myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_reserve_table);
        setSupportActionBar(myToolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        ibTrash = (ImageButton) findViewById(R.id.ibTrash);

        ibTrash.setVisibility(View.GONE);
        fabSaveSchema.setVisibility(View.GONE);
        fabReserveTable.setVisibility(View.GONE);
        fabCancel.setVisibility(View.GONE);
        fabConfirm.setVisibility(View.GONE);

        // устанавливае слушатель на тулбар, для удаления столов
        myToolbar.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DROP:
                        deleteTable();
                        myToolbar.setTitle(R.string.title_reserve_table);
                        myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                            fabSaveSchema.setVisibility(View.VISIBLE);
                        }
                        fabReserveTable.setVisibility(View.GONE);
                        fabCancel.setVisibility(View.GONE);
                        fabConfirm.setVisibility(View.GONE);

                        ibTrash.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        // устанавливаем слушатель на добавления новых столов
        ValueEventListener tablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();

                allTables.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Table currentTable = child.getValue(Table.class);
                    allTables.add(currentTable);
                }
                updateTables();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        Const.db.child(Const.CHILD_TABLES).addValueEventListener(tablesListener);

        // устанавливаем слушатель на добавления новых заказов
        ValueEventListener tablesReservedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                allReservedTables.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    OrderTable orderedTable = child.getValue(OrderTable.class);
                    orderedTable.setKey(child.getKey());
                    if (shortenedDateFormat.format(child.getValue(OrderTable.class).getDate())
                            .equals((shortenedDateFormat.format(new Date())))) {
                        allReservedTables.add(orderedTable);
                    }
                }
                updateOrderedTable();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).addValueEventListener(tablesReservedListener);

        allTables = new ArrayList<>();
        allReservedTables = new ArrayList<>();

        mRotationDetector = new RotationGestureDetector(this);

        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        ivAddTable4 = (ImageView) findViewById(R.id.ivAddTable4);
        ivAddTable6 = (ImageView) findViewById(R.id.ivAddTable6);
        ivAddTable8 = (ImageView) findViewById(R.id.ivAddTable8);
        rlReserveTable = (RelativeLayout) findViewById(R.id.activity_reserve_table);
        rlSettingsReserveTable = (RelativeLayout) findViewById(R.id.settings_reserve_table);
        llAboutUs = (LinearLayout) findViewById(R.id.llAboutUs);

        // отрисовываем столы, только после того как можем определить размеры экрана
        ViewTreeObserver greenObserver = rlReserveTable.getViewTreeObserver();
        greenObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                rlReserveTable.getViewTreeObserver().removeOnPreDrawListener(this);
                updateTables();
                updateOrderedTable();
                return true;
            }
        });

        ivAddTable4.setOnTouchListener(onClickListenerNewTable);
        ivAddTable6.setOnTouchListener(onClickListenerNewTable);
        ivAddTable8.setOnTouchListener(onClickListenerNewTable);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingUpPanel);
        setSlidingUpPanelLayoutListeners();

        rlSettingsReserveTable.setOnDragListener(onDragListenerSettings);
        rlReserveTable.setOnDragListener(onDragListenerTable);
    }

    public void setSettingsToView(TextView view, String setting) {
        if (setting.isEmpty()) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(setting);
        }
    }

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP) {
            rlSettingsReserveTable.setVisibility(View.GONE);
            llAboutUs.setVisibility(View.VISIBLE);
            tvTitleSettings.setText(R.string.title_reserve_table_settings_user);
        } else {
            rlSettingsReserveTable.setVisibility(View.VISIBLE);
            llAboutUs.setVisibility(View.GONE);
            tvTitleSettings.setText(R.string.title_reserve_table_settings);
        }
        fabSaveSchema.setVisibility(View.GONE);
        fabConfirm.setVisibility(View.GONE);
        fabCancel.setVisibility(View.GONE);
        fabReserveTable.setVisibility(View.GONE);

        updateOrderedTable();
    }


    @Override
    protected void onStart() {
        activeReserveTableActivity = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        activeReserveTableActivity = false;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTables();
        updateOrderedTable();
    }

    private int isPortraitMode() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PANEL_EXPANDED, isPanelExpanded);
        outState.putBoolean(IS_PANEL_COLLAPSED_DRAG_AND_DROP, isPanelCollapsedDragAndDrop);
        outState.putParcelableArrayList(TABLES_MARKERS, (ArrayList<Table>) allTables);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            isPanelExpanded = savedInstanceState.getBoolean(IS_PANEL_EXPANDED, false);
            isPanelCollapsedDragAndDrop = savedInstanceState.getBoolean(IS_PANEL_COLLAPSED_DRAG_AND_DROP, false);
            allTables = savedInstanceState.getParcelableArrayList(TABLES_MARKERS);
            if (allTables.size() != 0) {
                updateTables();
            }
        }
    }

    // получение нового id
    public int getNewId() {
        int counter;
        counter = 1;
        int newId = allTables.size() + counter;
        while (findViewById(newId) != null) {
            counter = counter + 1;
            newId = allTables.size() + counter;
        }
        return newId;
    }

    // слушатель перетаскивания столов по залу
    private View.OnDragListener onDragListenerTable = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    layoutParams = (RelativeLayout.LayoutParams) ivTable.getLayoutParams();
                    Log.d(msg, "Action is DragEvent.ACTION_DRAG_STARTED");
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    myToolbar.setTitle(R.string.title_reserve_table_delete);
                    myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.delete_toolbar_pink_200));

                    fabSaveSchema.setVisibility(View.GONE);
                    fabReserveTable.setVisibility(View.GONE);
                    fabCancel.setVisibility(View.GONE);
                    fabConfirm.setVisibility(View.GONE);
                    if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                        ibTrash.setVisibility(View.VISIBLE);
                    }

                    if (isPanelExpanded) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        isPanelCollapsedDragAndDrop = true;
                        isPanelExpanded = false;
                    }
                    Log.d(msg, "Action is DragEvent.ACTION_DRAG_LOCATION");
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d(msg, "Action is DragEvent.ACTION_DRAG_DROPPED");

                    myToolbar.setTitle(R.string.title_reserve_table);
                    myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

                    if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                        fabSaveSchema.setVisibility(View.VISIBLE);
                    }
                    fabReserveTable.setVisibility(View.GONE);
                    fabCancel.setVisibility(View.GONE);
                    fabConfirm.setVisibility(View.GONE);
                    ibTrash.setVisibility(View.GONE);

                    xCoordinate = (int) event.getX() - (ivTable.getWidth() / 2);
                    yCootdinate = (int) event.getY() - (ivTable.getHeight() / 2);
                    if ((xCoordinate + ivTable.getWidth()) > windowWidth) {
                        xCoordinate = windowWidth - ivTable.getWidth();
                    } else if (xCoordinate < 0) {
                        xCoordinate = 0;
                    }
                    if ((yCootdinate + ivTable.getHeight()) > windowHeight) {
                        yCootdinate = windowHeight - ivTable.getHeight();
                    }

                    if (isNewTable) {

                        final ImageView newTable = new ImageView(getApplicationContext());
                        idTable = getResources().getIdentifier(tableName, "drawable", getPackageName());
                        newTable.setImageResource(idTable);
                        LayoutParams lp = new LayoutParams(Math.round(120 * displayMetrics.density), Math.round(75 * displayMetrics.density));
                        lp.leftMargin = xCoordinate;
                        lp.topMargin = yCootdinate;
                        newTable.setLayoutParams(lp);
                        int newId = getNewId();

                        newTable.setId(newId);
                        rlReserveTable.addView(newTable);


                        allTables.add(new Table(newId, xCoordinate, yCootdinate, windowWidth, windowHeight, idTable, 0.0f, isPortraitMode(), tableName));

                        isNewTable = false;
                        newTable.setOnClickListener(onClickListener);

                        if (isPanelCollapsedDragAndDrop) {
                            isPanelCollapsedDragAndDrop = false;
                            isPanelExpanded = true;
                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                        }

                    } else {
                        layoutParams.leftMargin = xCoordinate;
                        layoutParams.topMargin = yCootdinate;
                        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                        ivTable.setLayoutParams(layoutParams);
                        ivTable.setVisibility(View.VISIBLE);
                        modifyTable(ivTable.getId(), xCoordinate, yCootdinate);

                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    // изменение параметров стола
    public void modifyTable(int id, int xCoordinate, int yCootdinate) {
        for (Table table : allTables) {
            if (table.getTableId() == id) {
                table.setxCoordinate(xCoordinate);
                table.setxResolution(windowWidth);
                table.setyCoordinate(yCootdinate);
                table.setyResolution(windowHeight);
                table.setPortraitMode(isPortraitMode());
            }
        }
    }

    // обработка нажатия и долгого нажатия по столу. При долгом нажатии апускаем перетаскивание
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ivTable = (ImageView) v;
                    if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                        ClipData data = ClipData.newPlainText("", "");
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(ivTable);
                        ivTable.startDrag(data, shadowBuilder, ivTable, 0);
                        ivTable.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
            });

            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (ivTable != null) {
//                        if (!isReserved(ivTable.getId(), false)) {
                        ivTable.setBackgroundColor(Color.TRANSPARENT);
//                        }
                    }
                    ivTable = (ImageView) v;

                    hmReservedConfirmed = isReserved(ivTable.getId(), true);

                    ivTable.setBackgroundColor(Color.GREEN);
                    if (hmReservedConfirmed.get(TABLE_RESERVED)) {
                        fabReserveTable.setVisibility(View.GONE);
                        if (!hmReservedConfirmed.get(TABLE_RESERVATION_CONFIRMED)) {
                            if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                                fabConfirm.setVisibility(View.VISIBLE);
                                fabCancel.setVisibility(View.VISIBLE);
                            }
                        } else {
                            fabConfirm.setVisibility(View.GONE);
                            fabCancel.setVisibility(View.GONE);
                        }
                    } else {
                        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
                            fabReserveTable.setVisibility(View.VISIBLE);
                        }
                        fabConfirm.setVisibility(View.GONE);
                        fabCancel.setVisibility(View.GONE);
                    }

                    return false;
                }
            });
        }
    };

    // проверка заказ столик или нет, если заказн то заказ подтвержден или нет
    private HashMap<String, Boolean> isReserved(int id, boolean showInfo) {
        HashMap<String, Boolean> hmTableReserverConfirmed;
        hmTableReserverConfirmed = new HashMap<>();
        Boolean isReserved, isConfirmed;
        String mesReserved = "";
        isReserved = false;
        isConfirmed = false;

        for (Table table : allTables) {
            if (table.getTableId() == id) {
                if(table.getKey()!=null) {
                    for (OrderTable orderedTable : allReservedTables) {
                        if (table.getKey().equals(orderedTable.getTableKey())) {
                            isReserved = true;
                            if (orderedTable.getIsCheckedByAdmin() == 1) {
                                isConfirmed = true;
                            }
                            mesReserved = orderedTable.getClientName() + " (" + orderedTable.getPhoneClient() + ")";
                            break;
                        }
                    }
                }
                break;
            }
        }
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
            if (isReserved && !mesReserved.equals("") && showInfo) {
                if (toastReserved != null) {
                    toastReserved.cancel();
                }
                toastReserved = Toast.makeText(getApplicationContext(), mesReserved, Toast.LENGTH_SHORT);
                toastReserved.show();

            }
        }
        hmTableReserverConfirmed.put(TABLE_RESERVED, isReserved);
        hmTableReserverConfirmed.put(TABLE_RESERVATION_CONFIRMED, isConfirmed);
        return hmTableReserverConfirmed;
    }

    // перетаскивание в панели столов
    private View.OnDragListener onDragListenerSettings = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            return true;
        }
    };


    private View.OnClickListener onClickListenerBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast btnToast;
            switch (view.getId()) {
                case R.id.fabSaveSchema:
                    btnToast = Toast.makeText(getApplicationContext(), R.string.mes_save_table_shema, Toast.LENGTH_SHORT);
                    btnToast.show();
                    sendToServer();
                    break;
                case R.id.fabReserveTable:
                    btnToast = Toast.makeText(getApplicationContext(), R.string.mes_reserve_table, Toast.LENGTH_SHORT);
                    btnToast.show();
                    if (ivTable != null) {
                        reserveTable();
                    }
                    break;
                case R.id.fabConfirm:
                    btnToast = Toast.makeText(getApplicationContext(), R.string.mes_confirm_reservetion, Toast.LENGTH_SHORT);
                    btnToast.show();
                    if (ivTable != null) {
                        confirmCancelReservation(true);
                    }
                    break;
                case R.id.fabCancel:
                    btnToast = Toast.makeText(getApplicationContext(), R.string.mes_cancel_reservation, Toast.LENGTH_SHORT);
                    btnToast.show();
                    if (ivTable != null) {
                        confirmCancelReservation(false);
                    }
                    break;
            }
        }
    };


    public void confirmCancelReservation(boolean confirmReservation) {
        for (Table table : allTables) {
            if (table.getTableId() == ivTable.getId()) {
                for (OrderTable orderedTable : allReservedTables) {
                    if (table.getKey().equals(orderedTable.getTableKey())) {
                        if (confirmReservation) {
                            orderedTable.setIsCheckedByAdmin(1);
                            db.child(Const.CHILD_RESERVED_TABLES_NEW).child(orderedTable.getKey()).setValue(orderedTable);
                        } else {
                            Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).child(orderedTable.getKey()).removeValue();
                            int pictureId = getResources().getIdentifier(table.getPictureName(), "drawable", getPackageName());
                            ivTable.setImageResource(pictureId);
                        }

                        StateTableReservation stateTableReservation = new StateTableReservation();
                        stateTableReservation.setReservationKey(orderedTable.getKey());
                        if(confirmReservation){
                            stateTableReservation.setReservationState(Const.RESERVATION_TABLE_STATE_CONFIRMED);
                        } else{
                            stateTableReservation.setReservationState(Const.RESERVATION_TABLE_STATE_CANCEL);
                        }
                        db.child(Const.CHILD_USERS)
                                .child(orderedTable.getUserId())
                                .child(Const.CHILD_USER_RESERVATION_STATE)
                                .child(orderedTable.getKey())
                                .setValue(stateTableReservation);

                        break;
                    }
                }
            }
        }

        fabConfirm.setVisibility(View.GONE);
        fabCancel.setVisibility(View.GONE);
    }

    public void reserveTable() {
        for (Table table : allTables) {
            if (table.getTableId() == ivTable.getId()) {
                Intent intentReserveTable = new Intent(this, ReserveTableCreationActivity.class);
                intentReserveTable.putExtra(Const.EXTRA_TABLE, table);
                intentReserveTable.putExtra(Const.EXTRA_TABLE_KEY, table.getKey());
                startActivityForResult(intentReserveTable, Const.REQUEST_RESERVE_TABLE);
                break;
            }
        }

        fabReserveTable.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == Const.REQUEST_RESERVE_TABLE) && (resultCode == RESULT_OK) && (data != null)) {
            if (data.hasExtra(Const.EXTRA_TABLE)) {
                Table table = data.getParcelableExtra(Const.EXTRA_TABLE);
                ivTable = (ImageView) findViewById(table.getTableId());
//                ivTable.setBackgroundColor(Color.GRAY);
            }
        }
    }

    public void deleteTable() {
        if (ivTable != null) {
            for (Table table : allTables) {
                if (table.getTableId() == ivTable.getId()) {
                    rlReserveTable.removeView(ivTable);
                    if (table.getKey() != null) {
                        Const.db.child(Const.CHILD_TABLES).child(table.getKey()).removeValue();
                    }
                    allTables.remove(table);
                    break;
                }
            }
        }
    }

    public int findCoordinates(int coordinate, int sizeWidthHeight, int resolution) {
        return (int) (coordinate * sizeWidthHeight) / resolution;
    }

    //отрисока уже заказанных столов по двум состояниям заказан, подтвержден
    private void updateOrderedTable() {
        getRelativeLayoutInfo();
        if (windowWidth != 0 && windowHeight != 0) {
            String userId;
            if(Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_NULL){
                userId = "";
            } else{
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            for (OrderTable orderedTable : allReservedTables) {
                for (Table table : allTables) {
                    if (table.getKey().equals(orderedTable.getTableKey())) {
                        ImageView reservedTable = (ImageView) findViewById(table.getTableId());
                        if (reservedTable != null) {
//                            reservedTable.setBackgroundColor(Color.GRAY);

                            Drawable[] layers = new Drawable[2];
                            layers[0] = ContextCompat.getDrawable(getApplicationContext(), getResources().getIdentifier(table.getPictureName(), "drawable", getPackageName()));

                            hmReservedConfirmed = isReserved(reservedTable.getId(), false);
                            if (hmReservedConfirmed.get(TABLE_RESERVATION_CONFIRMED)) {
                                if(userId.equals(orderedTable.getUserId())){
                                    layers[1] = ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_table_confirmation);
                                } else{
                                    layers[1] = ContextCompat.getDrawable(getApplicationContext(), R.drawable.table_confirmation);
                                }
                            } else {
                                if(userId.equals(orderedTable.getUserId())){
                                    layers[1] = ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_table_reserved);
                                } else{
                                    layers[1] = ContextCompat.getDrawable(getApplicationContext(), R.drawable.table_reserved);
                                }
                            }
                            LayerDrawable layerDrawable = new LayerDrawable(layers);


                            reservedTable.setImageDrawable(layerDrawable);
                        }
                    }
                }
            }
        }
    }

    // отрисовка столов
    private void updateTables() {
        getRelativeLayoutInfo();
        if (windowWidth != 0 && windowHeight != 0) {
            for (Table table : allTables) {
                if (findViewById(table.getTableId()) == null) {
                    final ImageView newTable = new ImageView(getApplicationContext());

//                    получения индекса картинки, при ребилде проэекта индексы могут изменяться
                    int pictureId = getResources().getIdentifier(table.getPictureName(), "drawable", getPackageName());
                    ;

                    newTable.setImageResource(pictureId);
                    LayoutParams lp = new LayoutParams(Math.round(120 * displayMetrics.density), Math.round(75 * displayMetrics.density));
                    if (table.getPortraitMode() == isPortraitMode()) {
                        lp.leftMargin = findCoordinates(table.getxCoordinate(), windowWidth, table.getxResolution());
                        lp.topMargin = findCoordinates(table.getyCoordinate(), windowHeight, table.getyResolution());
                    } else {
                        lp.leftMargin = findCoordinates(table.getyCoordinate(), windowWidth, table.getyResolution());
                        lp.topMargin = findCoordinates(table.getxCoordinate(), windowHeight, table.getxResolution());
                    }
                    newTable.setLayoutParams(lp);
                    newTable.setId(table.getTableId());
                    rlReserveTable.addView(newTable);
                    if (table.getPortraitMode() == isPortraitMode()) {
                        newTable.setRotation(table.getRotation());
                    } else {
                        newTable.setRotation(table.getRotation() + 90);
                    }
                    newTable.setOnClickListener(onClickListener);
                }
            }
        }

    }

    private void sendToServer() {

        for (Table table : allTables) {
            String newKey;
            ImageView ivTable = (ImageView) findViewById(table.getTableId());
            float rotation = ivTable.getRotation();
            if (table.getKey() == null) {
                newKey = db.child(Const.CHILD_TABLES).push().getKey();
                table.setKey(newKey);
            } else {
                newKey = table.getKey();
            }
            table.setRotation(rotation);

            db.child(Const.CHILD_TABLES).child(newKey).setValue(table);
        }
        fabSaveSchema.setVisibility(View.GONE);

//        finish();
    }


    // добавления нового стола из нижней панели
    private View.OnTouchListener onClickListenerNewTable = new View.OnTouchListener() {
        ImageView ivShadow;

        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            switch (v.getId()) {
                case R.id.ivAddTable4:
                    idTable = R.drawable.table4;
                    tableName = getString(R.string.table_name_4);
                    ivShadow = ivAddTable4;
                    break;
                case R.id.ivAddTable6:
                    idTable = R.drawable.table6;
                    tableName = getString(R.string.table_name_6);
                    ivShadow = ivAddTable6;
                    break;
                case R.id.ivAddTable8:
                    idTable = R.drawable.table8;
                    tableName = getString(R.string.table_name_8);
                    ivShadow = ivAddTable8;
                    break;
            }

            if (ivTable != null) {
//                if (!isReserved(ivTable.getId(), false)) {
                ivTable.setBackgroundColor(Color.TRANSPARENT);
//                }
            }
            ivTable = new ImageView(getApplicationContext());
            ivTable.setImageResource(idTable);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(Math.round(120 * displayMetrics.density), Math.round(75 * displayMetrics.density));
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);
//            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
            ivTable.setLayoutParams(lp);
            rlSettingsReserveTable.addView(ivTable);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(ivShadow);
            ivTable.startDrag(data, shadowBuilder, ivTable, 0);
            ivTable.setVisibility(View.INVISIBLE);
            isNewTable = true;

            return true;
        }
    };

    private void setSlidingUpPanelLayoutListeners() {
        final ImageView left = (ImageView) findViewById(R.id.ivArrowLeft);
        final ImageView right = (ImageView) findViewById(R.id.ivArrowRight);
        animationArrowRotation = AnimationUtils.loadAnimation(this, R.anim.panel_arrows);
        animationArrowRotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPanelExpanded) {
                    left.setRotation(0);
                    right.setRotation(0);
                    isPanelExpanded = false;
                } else {
                    left.setRotation(180);
                    right.setRotation(180);
                    isPanelExpanded = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                if ((isPanelExpanded && newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                        || (!isPanelExpanded && newState == SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    left.startAnimation(animationArrowRotation);
                    right.startAnimation(animationArrowRotation);
                }
            }
        });
        slidingUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        slidingUpPanelLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mRotationDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getRelativeLayoutInfo();
    }

    private void getRelativeLayoutInfo() {
        rlReserveTable = (RelativeLayout) findViewById(R.id.activity_reserve_table);
        windowWidth = rlReserveTable.getWidth();
        windowHeight = rlReserveTable.getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mRotationDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector) {
        if (ivTable != null) {
            if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                float angle = rotationDetector.getAngle();
                ivTable.setRotation(ivTable.getRotation() + (-angle));
                Log.d("RotationGestureDetector", "Rotation: " + Float.toString(angle));
                if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP){
                    fabSaveSchema.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
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
}


