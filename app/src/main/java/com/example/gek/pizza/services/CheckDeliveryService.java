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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.AboutActivity;
import com.example.gek.pizza.activities.DeliveriesActivity;
import com.example.gek.pizza.activities.MainActivity;
import com.example.gek.pizza.adapters.DishesAdapter;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.Order;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// https://developer.android.com/guide/topics/ui/notifiers/notifications.html?hl=ru


public class CheckDeliveryService extends Service {
    private static String TAG = "CheckDeliveryService";
    // По этому ID мы сможем обращаться к нашему уведомлению, что бы изменить его например
    // Для изменения уведомления достаточно повторно создать его с тем же номером ID
    int notifId = 1;
    private NotificationManager notificationManager;
    private Context ctx;
    private ChildEventListener newDeliveriesListener;

    public CheckDeliveryService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getBaseContext();
        // Получаем системный менеджер уведомлений
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setListenerNewDeliveries();
        return super.onStartCommand(intent, flags, startId);
    }



    private void setListenerNewDeliveries(){
        // Описываем слушатель, который мониторит новые заказы на доставку,
        // которые находятся в child(CHILD_DELIVERIES_NEW)
        newDeliveriesListener = new ChildEventListener(){

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                showNotification(dataSnapshot.getValue(Delivery.class));
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
                Log.w(TAG, "load data from FireBase: onCancelled", databaseError.toException());
            }
        };

        Const.db.child(Const.CHILD_DELIVERIES_NEW).addChildEventListener(newDeliveriesListener);
    }

    private void showNotification(Delivery delivery){
        float totalPrice = delivery.getTotalSum();
        String time = Utils.formatDate(delivery.getDateNew());
        String title = getResources().getString(R.string.notification_delivery_title) + " (" + time + ")";
        String content = delivery.getAddressClient() +" (" + delivery.getNameClient() + ")";
        String contentInfo = Utils.toPrice(totalPrice);

        // Создаем наше уведомление
        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        // Формируем его наполняя информацией
        // Следующие три параметра являются ОБЯЗАТЕЛЬНЫМИ
        ntfBuilder.setSmallIcon(R.drawable.ic_money);
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(content);


        // ************   Не обязательные параметры (для Notification) *************
        ntfBuilder.setContentInfo(contentInfo);
        // ставим флаг, чтобы уведомление пропало после нажатия. Потом надо убрать его и снимать
        // нотификейшн после того реально обработается заказ и переместится с папки NEW
        ntfBuilder.setAutoCancel(true);
        // Устанавливаем большую картинку в само уведомление
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));
        // В екшен баре появляется на секунду строка вместе со значком
        ntfBuilder.setTicker(title + " (" + contentInfo + ")");

        // Устанавливаем параметры для уведомления (звук, вибро, подсветка и т.д.)
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // Указываем явный интент для запуска окна по нажатию на уведомление
        Intent intent = new Intent(ctx, DeliveriesActivity.class);
        // Формируем ОЖИДАЮЩИЙ интент на основе обычного и задаем его в билдере
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);

        // Даем ему команду отобразить наше уведомление
        // айди инкрементируем, что бы были на каждый заказ свой нотификейшн
        notificationManager.notify(notifId++, ntfBuilder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        // перед уничтожением сервиса убираем наш лисенер. Иначе он будет отрабатывать и без службы
        Const.db.child(Const.CHILD_DELIVERIES_NEW).removeEventListener(newDeliveriesListener);
        super.onDestroy();
    }

}
