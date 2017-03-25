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
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.DeliveriesActivity;
import com.example.gek.pizza.activities.ReserveTableActivity;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.OrderTable;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

// https://developer.android.com/guide/topics/ui/notifiers/notifications.html?hl=ru


public class ShopService extends Service {
    private static String TAG = "SHOP_SERVICE";
    private Boolean mIsSetListener;
    // По этому ID мы сможем обращаться к нашему уведомлению, что бы изменить его например
    // Для изменения уведомления достаточно повторно создать его с тем же номером ID
    int orderNotifId = Const.ODRED_NOTIFY_ID;
    int tableNotifyId = Const.RESERVED_TABLE_NOTIFY_ID;

    private ArrayList<OrderTable> listOrderedTablesRemove;

    private NotificationManager notificationManager;
    private Context ctx;
    private ChildEventListener newDeliveriesListener, newOrderedTableListener;
    private ValueEventListener archiveOrderedTableListener;

    private SimpleDateFormat shortenedDateFormat;
    private HashMap<String, Date> hmOrderTable;
    private HashMap<String, Date> hmDeliveries;
    private Timer timer;
    private int countOfNotificationRepeat;
    private boolean isTimerStarted;


    public ShopService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        ctx = getBaseContext();

        // определяем формат даты
        shortenedDateFormat = new SimpleDateFormat("yyyyMMdd");

        // храним заказы столов которые нужно переместить в архив
        listOrderedTablesRemove = new ArrayList<>();

        // список заказнных и не обработанных столов
        hmOrderTable = new HashMap<>();
        hmOrderTable.clear();

        // список не обработанных доставок
        hmDeliveries = new HashMap<>();
        hmDeliveries.clear();

        // храним информацию о состаяюнию фонового задания
        isTimerStarted = false;

        // Получаем системный менеджер уведомлений
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: setListeners");
        setListenerNewDeliveries();
        setListenerArchiveOrderedTableListener();
        setListenerNewOrderedTableListener();
        Connection.getInstance().setServiceRunning(true);
        return START_STICKY;
    }

    private void setListenerNewOrderedTableListener() {
        newOrderedTableListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                boolean isShowTableNotification;
                isShowTableNotification = false;

                OrderTable orderedTable = dataSnapshot.getValue(OrderTable.class);
                orderedTable.setKey(dataSnapshot.getKey());

                // если дата заказа стола сегодня, и он не обработан администратором то выводим уведомление
                if (shortenedDateFormat.format(orderedTable.getDate()).equals((shortenedDateFormat.format(new Date())))) {
                    if (orderedTable.getIsCheckedByAdmin() == 0) {
                        // Добавляем в список не обработанных заказов
                        if (hmOrderTable.get(orderedTable) == null) {
                            hmOrderTable.put(orderedTable.getKey(), orderedTable.getDate());
                        }
                        isShowTableNotification = true;

                        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).child(orderedTable.getKey()).setValue(orderedTable);

                        // проверяем запущено ли фоновое задание, для уведомление
                        checkIsTimerStarted();
                    }

                    if (isShowTableNotification) {
                        showNotificationTables(false);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                OrderTable orderedTable = dataSnapshot.getValue(OrderTable.class);
                orderedTable.setKey(dataSnapshot.getKey());
                // при мзменении состояния заказа удаляем его из списка не обработанных
                // и при необходимости запускаем, останавливаем фоновое задание уведомлений
                if (shortenedDateFormat.format(orderedTable.getDate()).equals((shortenedDateFormat.format(new Date())))) {
                    if (orderedTable.getIsCheckedByAdmin() == Const.STATUS_CHECKED_BY_ADMIN) {
                        if (hmOrderTable.get(orderedTable.getKey()) != null) {
                            hmOrderTable.remove(orderedTable.getKey());

                            checkIsTimerStarted();
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // при мзменении состояния заказа удаляем его из списка не обработанных
                // и при необходимости запускаем, останавливаем фоновое задание уведомлений

                OrderTable orderedTable = dataSnapshot.getValue(OrderTable.class);
                orderedTable.setKey(dataSnapshot.getKey());
                if (hmOrderTable.get(orderedTable.getKey()) != null) {
                    hmOrderTable.remove(orderedTable.getKey());
                }

                checkIsTimerStarted();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // при мзменении состояния заказа удаляем его из списка не обработанных
                // и при необходимости запускаем, останавливаем фоновое задание уведомлений

                OrderTable orderedTable = dataSnapshot.getValue(OrderTable.class);
                orderedTable.setKey(dataSnapshot.getKey());
                if (hmOrderTable.get(orderedTable.getKey()) != null) {
                    hmOrderTable.remove(orderedTable.getKey());
                }
                checkIsTimerStarted();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).addChildEventListener(newOrderedTableListener);
        mIsSetListener = true;

    }

    private void setListenerArchiveOrderedTableListener() {
        archiveOrderedTableListener = new ValueEventListener() {
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
                        if (hmOrderTable.get(orderedTable.getKey()) != null) {
                            hmOrderTable.remove(orderedTable.getKey());
                        }

                        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).child(orderedTable.getKey()).removeValue();
                        Const.db.child(Const.CHILD_RESERVED_TABLES_ARCHIVE).child(orderedTable.getKey()).setValue(orderedTable);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        };
        Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).addValueEventListener(archiveOrderedTableListener);
        mIsSetListener = true;
    }

    /** фоновое задание для повторения не обработанных уведомлений */
    public void notificationRepeat() {
        isTimerStarted = true;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!ReserveTableActivity.activeReserveTableActivity && !DeliveriesActivity.activeDeliveriesActivity){
                    if (countOfNotificationRepeat > Const.COUNT_OF_NOTIFICATION_REPEAT_ALARM) {
                        if (hmOrderTable.size() > 0) {
                            showNotificationTables(true);
                        }
                        if (hmDeliveries.size() > 0) {
                            showNotification(true);
                        }
                    } else {
                        if (hmOrderTable.size() > 0) {
                            showNotificationTables(false);
                        }
                        if (hmDeliveries.size() > 0) {
                            showNotification(false);
                        }
                    }
                }
                countOfNotificationRepeat++;
            }
        }, Const.TIME_INTERVAL_NOTIFICATION_START, Const.TIME_INTERVAL_NOTIFICATION_REPEAT);
    }

    // запуск, остановка фонового уведомления в зависимости от наличия необработанных заказов столов или доставок
    private void checkIsTimerStarted() {
        if ((hmOrderTable.size() > 0 || hmDeliveries.size() > 0) && !isTimerStarted) {
            timer = new Timer();
            notificationRepeat();
        } else if ((hmOrderTable.size() == 0 && hmDeliveries.size() == 0) && isTimerStarted) {
            isTimerStarted = false;
            timer.cancel();
            timer = null;
            countOfNotificationRepeat = 1;
        }
    }

    private void showNotificationTables(boolean alert) {
        String title = getResources().getString(R.string.notification_reserved_table_title);
        String content = getResources().getString(R.string.notification_reserved_table_content);

        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ntfBuilder.setSmallIcon(R.drawable.verified);
        } else {
            ntfBuilder.setSmallIcon(R.drawable.table4);
        }
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(content);

        ntfBuilder.setAutoCancel(true);
        // заперт удаления уведомления
        ntfBuilder.setOngoing(true);
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        Intent intent = new Intent(ctx, ReserveTableActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);
        Notification notification = ntfBuilder.build();
        // если на оповещение никто нериагирует, запускаем более навящевый сигнал
        if (alert) {
            notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        }

        notificationManager.notify(tableNotifyId, notification);
    }

    private void setListenerNewDeliveries() {
        // Описываем слушатель, который мониторит новые заказы на доставку,
        // которые находятся в child(CHILD_DELIVERIES_NEW)
        newDeliveriesListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Добавление в список не обработанных доставок
                if (hmDeliveries.get(dataSnapshot.getValue(Delivery.class)) == null) {
                    hmDeliveries.put(dataSnapshot.getValue(Delivery.class).getKey(), dataSnapshot.getValue(Delivery.class).getDateNew());
                }

//                showNotification(dataSnapshot.getValue(Delivery.class),false);
                checkIsTimerStarted();
                showNotification(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // при мзменении состояния заказа удаляем его из списка не обработанных
                // и при необходимости запускаем, останавливаем фоновое задание уведомлений
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                if (hmDeliveries.get(delivery.getKey()) != null) {
                    hmDeliveries.remove(delivery.getKey());
                }
                checkIsTimerStarted();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load data from FireBase: onCancelled", databaseError.toException());
            }
        };

        Log.d(TAG, "setListenerNewDeliveries: SET LISTENER FOR CONTROL DELIVERIES");
        Const.db.child(Const.CHILD_DELIVERIES_NEW).addChildEventListener(newDeliveriesListener);
        mIsSetListener = true;

    }

    private void showNotification(boolean alert) {
//        float totalPrice = delivery.getTotalSum();
//        String time = Utils.formatDate(delivery.getDateNew());
//        String title = getResources().getString(R.string.notification_delivery_title) + " (" + time + ")";
//        String content = delivery.getAddressClient() +" (" + delivery.getNameClient() + ")";

        String title = getResources().getString(R.string.notification_delivery_title);
        String content = getResources().getString(R.string.notification_delivery_content);

        // Создаем наше уведомление
        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(ctx);
        // Формируем его наполняя информацией
        // Следующие три параметра являются ОБЯЗАТЕЛЬНЫМИ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ntfBuilder.setSmallIcon(R.drawable.currency_usd);
        } else {
            ntfBuilder.setSmallIcon(R.drawable.ic_money);
        }
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(content);


        ntfBuilder.setAutoCancel(true);
        // заперт удаления уведомления
        ntfBuilder.setOngoing(true);
        // Устанавливаем большую картинку в само уведомление
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification));
        // В екшен баре появляется на секунду строка вместе со значком
        // ntfBuilder.setTicker(title + " (" + contentInfo + ")");
        ntfBuilder.setTicker(title);

        // Устанавливаем параметры для уведомления (звук, вибро, подсветка и т.д.)
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // Указываем явный интент для запуска окна по нажатию на уведомление
        Intent intent = new Intent(ctx, DeliveriesActivity.class);
        // Формируем ОЖИДАЮЩИЙ интент на основе обычного и задаем его в билдере
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);

        Notification notification = ntfBuilder.build();
        // если на оповещение никто нериагирует, запускаем более навящевый сигнал
        if (alert) {
            notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        }
        notificationManager.notify(orderNotifId, notification);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: ");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        // перед уничтожением сервиса убираем наш лисенер. Иначе он будет отрабатывать и без службы
        if (mIsSetListener) {
            Const.db.child(Const.CHILD_DELIVERIES_NEW).removeEventListener(newDeliveriesListener);
            Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).removeEventListener(newOrderedTableListener);
            Const.db.child(Const.CHILD_RESERVED_TABLES_NEW).removeEventListener(archiveOrderedTableListener);
        }

        // отключаем фоновое задание
        if (isTimerStarted && timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

}
