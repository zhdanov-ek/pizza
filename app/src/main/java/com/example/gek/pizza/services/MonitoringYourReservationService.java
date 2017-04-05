package com.example.gek.pizza.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.ReserveTableActivity;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.StateTableReservation;
import com.example.gek.pizza.helpers.Connection;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.db;


/**
 * Monitoring reservation table
 * Run automatic after creation new request on reservation tha table
 * Stop after SHOP change status of our requests (accept/reject)
 */

public class MonitoringYourReservationService extends Service {
    public static final String TAG = "RESERVED";
    private int notifyId;
    private Boolean mIsSetListener;
    private ValueEventListener mStateListener;
    private Context ctx;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        notifyId = Const.RESERVED_TABLE_USER_NOTIFY_ID;
        super.onCreate();
        mIsSetListener = false;
        ctx = getBaseContext();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        setListener();

        // The mode in which the service stopped by the system will be restarted automatically
        // until it  stopped correctly from the program itself
        return START_STICKY;
    }


    /** Check all reserved tables, and send notification if state of reservation is changed
     * Stopped service when all reservations processed*/
    private void setListener(){
        mStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                boolean isDataNotNull = false;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    isDataNotNull = true;
                    StateTableReservation stateTableReservation = child.getValue(StateTableReservation.class);
                    count++;
                    if (stateTableReservation != null) {
                        String state = "";
                        switch (stateTableReservation.getReservationState()) {
                            case Const.RESERVATION_TABLE_STATE_CANCEL:
                                state = getResources().getString(R.string.mes_cancel_reservetion_user);
                                break;
                            case Const.RESERVATION_TABLE_STATE_CONFIRMED:
                                state = getResources().getString(R.string.mes_confirm_reservetion_user);
                                break;
                        }
                        if (state.length() > 0 ) {
                            count--;
                            showNotification(state);
                            db.child(Const.CHILD_USERS)
                                    .child(Connection.getInstance().getUserId())
                                    .child(Const.CHILD_USER_RESERVATION_STATE)
                                    .child(child.getKey())
                                    .removeValue();
                        }
                    }
                }
                if (count==0 && isDataNotNull){
                    Log.d(TAG, "ServiceStopped");
                    stopSelf();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        db.child(Const.CHILD_USERS)
                .child(Connection.getInstance().getUserId())
                .child(Const.CHILD_USER_RESERVATION_STATE)
                .addValueEventListener(mStateListener);
        mIsSetListener = true;
    
    }

    private void showNotification(String state){
        String title = getResources().getString(R.string.notification_monitoring_table_reservation);

        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ntfBuilder.setSmallIcon(R.drawable.verified);
        } else{
            ntfBuilder.setSmallIcon(R.drawable.table4);
        }
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(state);
        ntfBuilder.setAutoCancel(true);
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));

        // icon in action bar
        ntfBuilder.setTicker(title + ": " + state);

        // Set notification properties
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // Notification pressed action
        Intent intent = new Intent(ctx, ReserveTableActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);
        Notification notification = ntfBuilder.build();
        mNotificationManager.notify(notifyId++, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        if (mIsSetListener) {
            db.child(Const.CHILD_USERS)
                    .child(Connection.getInstance().getUserId())
                    .child(Const.CHILD_USER_RESERVATION_STATE)
                    .removeEventListener(mStateListener);
        }
        super.onDestroy();
    }
}
