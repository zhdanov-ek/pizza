package com.example.gek.pizza.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.DeliveryStatus;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.StateLastDelivery;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.gek.pizza.data.Const.db;

/**
 * Monitoring client delivery
 * Run automatic after creation new delivery
 * Stop after delivery change state to ARCHIVE
 */

//todo Сервис работает паралельно программе. При выключении проги он вырубается тоже. Оставлять или делать независимо?

public class MonitoringYourDeliveryService extends Service {
    private Boolean mIsSetListener;
    private int mLastState;
    private ValueEventListener mStateListener;
    private Context ctx;
    private NotificationManager mNotificationManager;
    private final static int mNotifyId = 777;

    @Override
    public void onCreate() {
        super.onCreate();
        mIsSetListener = false;
        mLastState = Const.DELIVERY_STATE_NEW;
        ctx = getBaseContext();
        // Получаем системный менеджер уведомлений
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setListener();
        return super.onStartCommand(intent, flags, startId);
    }


    /** Смотрим состояние выполнения заказа и выводим уведомления о каждой смене состояния */
    private void setListener(){
        mStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StateLastDelivery stateLastDelivery = dataSnapshot.getValue(StateLastDelivery.class);
                if (stateLastDelivery != null) {
                    String state = "";
                    switch (stateLastDelivery.getDeliveryState()) {
                        case Const.DELIVERY_STATE_NEW:
                            state = "";
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
                    if (state.length() > 0 ) {
                        showNotification(state);
                        // stop service after delivery moved to archive
                        if (stateLastDelivery.getDeliveryState() == Const.DELIVERY_STATE_ARCHIVE) {
                            stopSelf();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER){
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
        ntfBuilder.setSmallIcon(R.drawable.ic_money);
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(state);
        ntfBuilder.setAutoCancel(true);
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));

        // В екшен баре появляется на секунду строка вместе со значком
        ntfBuilder.setTicker(title + ": " + state);

        // Устанавливаем параметры для уведомления (звук, вибро, подсветка и т.д.)
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // Указываем явный интент для запуска окна по нажатию на уведомление
        Intent intent = new Intent(ctx, DeliveryStatus.class);
        // Формируем ОЖИДАЮЩИЙ интент на основе обычного и задаем его в билдере
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
