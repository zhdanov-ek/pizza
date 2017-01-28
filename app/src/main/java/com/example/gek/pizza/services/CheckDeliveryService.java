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

import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.AboutActivity;
import com.example.gek.pizza.activities.MainActivity;
import com.example.gek.pizza.adapters.DishesAdapter;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CheckDeliveryService extends Service {
    private static String TAG = "CheckDeliveryService";
    // По этому ID мы сможем обращаться к нашему уведомлению, что бы изменить его например
    // Для изменения уведомления достаточно повторно создать его с тем же номером ID
    int notifId = 555;
    NotificationManager notificationManager;
    Context ctx;

    public CheckDeliveryService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getBaseContext();
        // Получаем системный менеджер уведомлений
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setListenerNewDeliveries();
        return super.onStartCommand(intent, flags, startId);
    }


    private void setListenerNewDeliveries(){
        // Описываем слушатель, который мониторит новые заказы на доставку,
        // которые находятся в child(CHILD_ORDERS_NEW)
        ValueEventListener newDeliveriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long num = dataSnapshot.getChildrenCount();

                // Перебираем все новые заказы и выводим для каждого нотификейшн
                Log.d(TAG, "Load new deliveries: total Children objects:" + num);
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Delivery currentDelivery = child.getValue(Delivery.class);
                    showNotification(currentDelivery);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "newDeliveriesListener: onCancelled", databaseError.toException());
            }
        };

        // устанавливаем слушатель на изменения в разделе новых заявок на доставку
        Const.db.child(Const.CHILD_ORDERS_NEW).addValueEventListener(newDeliveriesListener);
    }

    private void showNotification(Delivery delivery){

        // Создаем наше уведомление
        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        // Формируем его наполняя информацией
        // Следующие три параметра являются ОБЯЗАТЕЛЬНЫМИ
        ntfBuilder.setSmallIcon(R.drawable.ic_warning);
        ntfBuilder.setContentTitle("my title ");
        ntfBuilder.setContentText("уведомление");


        // ************   Не обязательные параметры  *************
        ntfBuilder.setContentInfo("my Content Info");
        // ставим флаг, чтобы уведомление пропало после нажатия
        ntfBuilder.setAutoCancel(true);
        // Устанавливаем большую картинку в само уведомление
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo));
        // В екшен баре появляется на секунду строка вместе со значком
        ntfBuilder.setTicker("New delivery!");

        // Устанавливаем параметры для уведомления (звук, вибро, подсветка и т.д.)
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // Указываем явный интент для запуска окна по нажатию на уведомление
        Intent intent = new Intent(ctx, AboutActivity.class);
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
}
