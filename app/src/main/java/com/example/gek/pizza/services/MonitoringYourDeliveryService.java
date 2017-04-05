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
import com.example.gek.pizza.activities.MainActivity;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.StateLastDelivery;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.db;

/**
 * Monitoring client delivery
 * Run automatic after creation new delivery
 * Stop after delivery change state to ARCHIVE
 */


public class MonitoringYourDeliveryService extends Service {
    public static final String TAG = "DELIVERY";
    private Boolean mIsSetListener;
    private ValueEventListener mStateListener;
    private Context ctx;
    private NotificationManager mNotificationManager;
    private final static int mNotifyId = 777;

    @Override
    public void onCreate() {
        mIsSetListener = false;
        ctx = getBaseContext();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String idDelivery = intent.getStringExtra(Const.EXTRA_DELIVERY_ID);
        setListener(idDelivery);

        // The mode in which the content that was submitted to the startService will be transmitted
        // In onStartCommand again after the destruction during the restart of the service
        return START_REDELIVER_INTENT;
    }


    /** Listen state of order, and show notification if state is changed */
    private void setListener(final String idDelivery){
        mStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StateLastDelivery stateLastDelivery = dataSnapshot.getValue(StateLastDelivery.class);
                if (stateLastDelivery != null) {
                    String state = "";
                    switch (stateLastDelivery.getDeliveryState()) {
                        case Const.DELIVERY_STATE_NEW:
                            state = getResources().getString(R.string.mes_pass_new);
                            break;
                        case Const.DELIVERY_STATE_COOKING:
                            state = getResources().getString(R.string.mes_pass_kitchen);
                            break;
                        case Const.DELIVERY_STATE_TRANSPORT:
                            state = getResources().getString(R.string.mes_pass_courier);
                            break;
                        case Const.DELIVERY_STATE_ARCHIVE:
                            state = getResources().getString(R.string.mes_pass_archive);
                            break;
                    }

                    if (stateLastDelivery.getDeliveryState() != Const.DELIVERY_STATE_ARCHIVE){
                        showNotification(state);
                    } else {
                        if  (stateLastDelivery.getDeliveryId().contentEquals(idDelivery)) {
                            showNotification(state);
                            stopSelf();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        };
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if ((user != null) && (user.getUid() != null)){
            db.child(Const.CHILD_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .addValueEventListener(mStateListener);
            mIsSetListener = true;
        }

    }


    /** Show notification */
    private void showNotification(String state){
        String title = getResources().getString(R.string.notification_monitoring_delivery_title);

        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ntfBuilder.setSmallIcon(R.drawable.currency_usd);
        } else{
            ntfBuilder.setSmallIcon(R.drawable.ic_money);
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
        Intent intent = new Intent(ctx, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);
        Notification notification = ntfBuilder.build();
        mNotificationManager.notify(mNotifyId, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mIsSetListener) {
            db.child(Const.CHILD_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .removeEventListener(mStateListener);
        }
        mIsSetListener = false;
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        if (mIsSetListener) {
            db.child(Const.CHILD_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .removeEventListener(mStateListener);
        }
        super.onDestroy();
    }

}
