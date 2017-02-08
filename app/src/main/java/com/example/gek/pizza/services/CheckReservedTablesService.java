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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ivleshch on 08.02.2017.
 */

public class CheckReservedTablesService extends Service {

    int notifId = 666;
    private NotificationManager notificationManager;
    private ArrayList<OrderTable> listOrderedTables;
    private Context ctx;
    private ValueEventListener newOrderedTableListener;
    SimpleDateFormat shortenedDateFormat;

    public CheckReservedTablesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        shortenedDateFormat = new SimpleDateFormat("yyyyMMdd");
        ctx = getBaseContext();
        listOrderedTables = new ArrayList<>();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setListenernewOrderedTableListener();
        return super.onStartCommand(intent, flags, startId);
    }



    private void setListenernewOrderedTableListener(){
        newOrderedTableListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();
                listOrderedTables.clear();


                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if(shortenedDateFormat.format(child.getValue(OrderTable.class).getDate())
                            .equals((shortenedDateFormat.format(new Date())))){

                            listOrderedTables.add(child.getValue(OrderTable.class));
                    }
                }
                if (listOrderedTables.size() != 0){
                    showNotification();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).addValueEventListener(newOrderedTableListener);
    }

    private void showNotification(){
        int count = 0;
        float totalPrice = 0;
        for (OrderTable orderedTable: listOrderedTables) {
            count++;
        }

        String title = getResources().getString(R.string.notification_reserved_table_title);
        String content = getResources().getString(R.string.notification_reserved_table_content) + ": " + count;

        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        ntfBuilder.setSmallIcon(R.drawable.table4);
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(content);


        ntfBuilder.setAutoCancel(true);
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));
        ntfBuilder.setTicker(title + " (" + count + ")");
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        Intent intent = new Intent(ctx, ReserveTableActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(notifId, ntfBuilder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).removeEventListener(newOrderedTableListener);
        super.onDestroy();
    }
}
