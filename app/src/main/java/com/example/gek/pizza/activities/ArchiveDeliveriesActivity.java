package com.example.gek.pizza.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.DeliveriesAdapter;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

public class ArchiveDeliveriesActivity extends AppCompatActivity {
    private static final String TAG = "ArchiveDeliveries";
    private RecyclerView rv;
    private ArrayList<Delivery> list;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_deliveries);
        ctx = this;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_archive_deliveries);
        setSupportActionBar(myToolbar);

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    list.add(child.getValue(Delivery.class));
                }
                rv.setAdapter(new DeliveriesAdapter(list, ctx, Const.CHILD_DELIVERIES_ARCHIVE));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "read DB Cancelled", databaseError.toException());
            }
        };

        db.child(Const.CHILD_DELIVERIES_ARCHIVE).addValueEventListener(listener);

    }
}
