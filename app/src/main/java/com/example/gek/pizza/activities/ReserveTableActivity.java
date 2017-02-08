package com.example.gek.pizza.activities;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.OrderTable;
import com.example.gek.pizza.data.Table;
import com.example.gek.pizza.helpers.RotationGestureDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.gek.pizza.data.Const.db;


public class ReserveTableActivity extends AppCompatActivity implements RotationGestureDetector.OnRotationGestureListener {

    private ImageView ivAddTable6, ivAddTable8, ivAddTable4;
    private RelativeLayout rlReserveTable, rlSettingsReserveTable;
    private ImageView ivTable, ivOldTable;
    private android.widget.RelativeLayout.LayoutParams layoutParams;
    private String msg = "DRAGDROP";
    private int xCoordinate, yCootdinate;
    private int windowWidth, windowHeight;
    public int idTable;
    private DisplayMetrics displayMetrics;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Animation animationArrowRotation;
    private boolean isPanelExpanded = false, isPanelCollapsedDragAndDrop = false;
    boolean isNewTable;
    private ArrayList<Table> allTables;
    private ArrayList<OrderTable> allReservedTables;
    Button btnOk, btnRemove;
    Toolbar myToolbar;
    ImageButton ibTrash, ibOk, ibReserveTable;
    TextView tvOk, tvReserveTable;
    SimpleDateFormat shortenedDateFormat;

    private final String IS_PANEL_EXPANDED = "is_panel_expanded";
    private final String IS_PANEL_COLLAPSED_DRAG_AND_DROP = "is_panel_collapsed_drag_and_drop";
    private final String TABLES_MARKERS = "tables_markers";

    private RotationGestureDetector mRotationDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_table);

        shortenedDateFormat = new SimpleDateFormat("yyyyMMdd");

        myToolbar = (Toolbar) findViewById(R.id.toolBar_reserve_table);
        myToolbar.setTitle(R.string.title_reserve_table);
        setSupportActionBar(myToolbar);

        ibOk = (ImageButton) findViewById(R.id.ibOk);
        tvOk = (TextView) findViewById(R.id.tvOk);
        ibTrash = (ImageButton) findViewById(R.id.ibTrash);
        ibReserveTable = (ImageButton) findViewById(R.id.ibReserveTable);
        tvReserveTable = (TextView) findViewById(R.id.tvReserveTable);


        ibOk.setOnClickListener(onClickListenerBtn);
        tvOk.setOnClickListener(onClickListenerBtn);
        ibReserveTable.setOnClickListener(onClickListenerBtn);
        tvReserveTable.setOnClickListener(onClickListenerBtn);

        ibTrash.setVisibility(View.GONE);
        ibOk.setVisibility(View.GONE);
        tvOk.setVisibility(View.GONE);
        ibReserveTable.setVisibility(View.GONE);
        tvReserveTable.setVisibility(View.GONE);


        myToolbar.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DROP:
                        deleteTable();
                        myToolbar.setTitle(R.string.title_reserve_table);
                        myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        ibOk.setVisibility(View.VISIBLE);
                        tvOk.setVisibility(View.VISIBLE);
                        ibReserveTable.setVisibility(View.GONE);
                        tvReserveTable.setVisibility(View.GONE);

                        ibTrash.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        allTables = new ArrayList<>();
        allReservedTables = new ArrayList<>();

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

        ValueEventListener tablesReservedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                allReservedTables.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    OrderTable orderedTable = child.getValue(OrderTable.class);
                    if(shortenedDateFormat.format(child.getValue(OrderTable.class).getDate())
                            .equals((shortenedDateFormat.format(new Date())))){
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

        mRotationDetector = new RotationGestureDetector(this);

        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        ivAddTable4 = (ImageView) findViewById(R.id.ivAddTable4);
        ivAddTable6 = (ImageView) findViewById(R.id.ivAddTable6);
        ivAddTable8 = (ImageView) findViewById(R.id.ivAddTable8);
        rlReserveTable = (RelativeLayout) findViewById(R.id.activity_reserve_table);
        rlSettingsReserveTable = (RelativeLayout) findViewById(R.id.settings_reserve_table);

        ivAddTable4.setOnTouchListener(onClickListenerNewTable);
        ivAddTable6.setOnTouchListener(onClickListenerNewTable);
        ivAddTable8.setOnTouchListener(onClickListenerNewTable);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingUpPanel);
        setSlidingUpPanelLayoutListeners();

        rlSettingsReserveTable.setOnDragListener(onDragListenerSettings);
        rlReserveTable.setOnDragListener(onDragListenerTable);
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
                    myToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

                    ibOk.setVisibility(View.GONE);
                    tvOk.setVisibility(View.GONE);
                    ibReserveTable.setVisibility(View.GONE);
                    tvReserveTable.setVisibility(View.GONE);
                    ibTrash.setVisibility(View.VISIBLE);

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

                    ibOk.setVisibility(View.VISIBLE);
                    tvOk.setVisibility(View.VISIBLE);
                    ibReserveTable.setVisibility(View.GONE);
                    tvReserveTable.setVisibility(View.GONE);
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
                        newTable.setImageResource(idTable);
                        LayoutParams lp = new LayoutParams(Math.round(120 * displayMetrics.density), Math.round(75 * displayMetrics.density));
                        lp.leftMargin = xCoordinate;
                        lp.topMargin = yCootdinate;
                        newTable.setLayoutParams(lp);
                        int newId = getNewId();

                        newTable.setId(newId);
                        rlReserveTable.addView(newTable);


                        allTables.add(new Table(newId, xCoordinate, yCootdinate, windowWidth, windowHeight, idTable, 0.0f, isPortraitMode()));

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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ivTable = (ImageView) v;
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(ivTable);
                    ivTable.startDrag(data, shadowBuilder, ivTable, 0);
                    ivTable.setVisibility(View.INVISIBLE);
                    return true;
                }
            });

            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (ivTable != null) {
                        ivTable.setBackgroundColor(Color.TRANSPARENT);
                    }
                    ivTable = (ImageView) v;
                    ivTable.setBackgroundColor(Color.GREEN);
                    tvReserveTable.setVisibility(View.VISIBLE);
                    ibReserveTable.setVisibility(View.VISIBLE);

                    return false;
                }
            });
        }
    };

    private View.OnDragListener onDragListenerSettings = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            return true;
        }
    };


    private View.OnClickListener onClickListenerBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ibOk:
                    sendToServer();
                    break;
                case R.id.tvOk:
                    sendToServer();
                    break;
                case R.id.ibReserveTable:
                    if(ivTable!=null){
                        reserveTable();
                    }
                    break;
                case R.id.tvReserveTable:
                    if(ivTable!=null) {
                        reserveTable();
                    }
                    break;
            }
        }
    };

    public void reserveTable(){
        for (Table table : allTables) {
            if (table.getTableId() == ivTable.getId()) {
                Intent intentReserveTable = new Intent(this, ReserveTableCreationActivity.class);
                intentReserveTable.putExtra(Const.EXTRA_TABLE, table);
                intentReserveTable.putExtra(Const.EXTRA_TABLE_KEY, table.getKey());
                startActivityForResult(intentReserveTable, Const.REQUEST_RESERVE_TABLE);
                break;
            }
        }

        tvReserveTable.setVisibility(View.GONE);
        ibReserveTable.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == Const.REQUEST_RESERVE_TABLE) && (resultCode == RESULT_OK) && (data != null)) {
            if (data.hasExtra(Const.EXTRA_TABLE)) {
                Table table = data.getParcelableExtra(Const.EXTRA_TABLE);
                ivTable = (ImageView) findViewById(table.getTableId());
                ivTable.setBackgroundColor(Color.GRAY);
            }
        }
    }

    public void deleteTable(){
        if (ivTable!=null){
            for (Table table : allTables) {
                if (table.getTableId() == ivTable.getId()) {
                    rlReserveTable.removeView(ivTable);
                    if(table.getKey()!=null){
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



    private void updateOrderedTable(){
        for (OrderTable orderedTable : allReservedTables) {
            for (Table table : allTables) {
                if(table.getKey().equals(orderedTable.getTableKey())){
                    ImageView reservedTable = (ImageView) findViewById(table.getTableId());
                    reservedTable.setBackgroundColor(Color.GRAY);
                }
            }
        }
    }

    private void updateTables() {
        getRelativeLayoutInfo();
        if (windowWidth != 0 && windowHeight != 0) {
            for (Table table : allTables) {
                if (findViewById(table.getTableId()) == null) {
                    final ImageView newTable = new ImageView(getApplicationContext());
                    newTable.setImageResource(table.getPictureId());
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
        ibOk.setVisibility(View.GONE);
        tvOk.setVisibility(View.GONE);

//        finish();
    }

    private View.OnTouchListener onClickListenerNewTable = new View.OnTouchListener() {
        ImageView ivShadow;

        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            switch (v.getId()) {
                case R.id.ivAddTable4:
                    idTable = R.drawable.table4;
                    ivShadow = ivAddTable4;
                    break;
                case R.id.ivAddTable6:
                    idTable = R.drawable.table6;
                    ivShadow = ivAddTable6;
                    break;
                case R.id.ivAddTable8:
                    idTable = R.drawable.table8;
                    ivShadow = ivAddTable8;
                    break;
            }

            if (ivTable != null) {
                ivTable.setBackgroundColor(Color.TRANSPARENT);
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
            float angle = rotationDetector.getAngle();
            ivTable.setRotation(ivTable.getRotation() + (-angle));
            Log.d("RotationGestureDetector", "Rotation: " + Float.toString(angle));
            ibOk.setVisibility(View.VISIBLE);
            tvOk.setVisibility(View.VISIBLE);
        }
    }
}


