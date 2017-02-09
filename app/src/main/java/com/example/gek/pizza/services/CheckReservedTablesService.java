package com.example.gek.pizza.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.ReserveTableActivity;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.OrderTable;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Ivleshch on 08.02.2017.
 */

public class CheckReservedTablesService extends Service {

    int notifyId = 101;
    private NotificationManager notificationManager;
    private ArrayList<OrderTable>  listOrderedTablesRemove;
    private Context ctx;
    private ChildEventListener newOrderedTableListener;
    private ValueEventListener ArchiveOrderedTableListener;
    private SimpleDateFormat shortenedDateFormat;

    public CheckReservedTablesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        shortenedDateFormat = new SimpleDateFormat("yyyyMMdd");
        ctx = getBaseContext();
        listOrderedTablesRemove = new ArrayList<>();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setListenerNewOrderedTableListener();
        setListenerArchiveOrderedTableListener();

        return super.onStartCommand(intent, flags, startId);
    }


    private void setListenerArchiveOrderedTableListener() {
        ValueEventListener ArchiveOrderedTableListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listOrderedTablesRemove.clear();

                Date date = new Date();
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                date = cal.getTime();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    OrderTable orderedTable = child.getValue(OrderTable.class);
                     if (date.after(child.getValue(OrderTable.class).getDate())) {
                        orderedTable.setKey(child.getKey());
                        listOrderedTablesRemove.add(orderedTable);
                    }
                }

                if (listOrderedTablesRemove.size() != 0) {
                    for (OrderTable orderedTable : listOrderedTablesRemove) {
                        orderedTable.setIsNotificated(1);
                        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).child(orderedTable.getKey()).removeValue();
                        Const.db.child(Const.CHILD_RESERVED_TABLES_ARCHIVE).child(orderedTable.getKey()).setValue(orderedTable);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        };
        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).addValueEventListener(ArchiveOrderedTableListener);

    }


    private void setListenerNewOrderedTableListener() {
        newOrderedTableListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                OrderTable orderedTable = dataSnapshot.getValue(OrderTable.class);
                orderedTable.setKey(dataSnapshot.getKey());

                if (shortenedDateFormat.format(orderedTable.getDate()).equals((shortenedDateFormat.format(new Date())))) {
                    if (orderedTable.getIsNotificated() == 0) {
                        orderedTable.setIsNotificated(1);
                        showNotification(orderedTable);
                        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).child(orderedTable.getKey()).setValue(orderedTable);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).addChildEventListener(newOrderedTableListener);
    }

    private void showNotification(OrderTable orderedTable) {

        String time = Utils.formatDate(orderedTable.getDate());
        String title = getResources().getString(R.string.notification_reserved_table_title) + " (" + time + ")";
        String content = orderedTable.getClientName() + " (" + orderedTable.getPhoneClient() + ")";

        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        ntfBuilder.setSmallIcon(R.drawable.table4);
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(content);


        ntfBuilder.setAutoCancel(true);
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        Intent intent = new Intent(ctx, ReserveTableActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(notifyId++, ntfBuilder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).removeEventListener(newOrderedTableListener);
        super.onDestroy();
    }
}
