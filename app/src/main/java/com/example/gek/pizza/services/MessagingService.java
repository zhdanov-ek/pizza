package com.example.gek.pizza.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.NewsActivity;
import com.example.gek.pizza.data.Const;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service catch incoming masseges. FCM notifications
 */

public class MessagingService extends FirebaseMessagingService {

    private static int notifyId = Const.PUSH_NITIFICATIONS_ID;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        NotificationManager mNotificationManager;

        if (remoteMessage==null){
            return;
        }

        String title = remoteMessage.getData().get(Const.FCM_TITLE);
        String body = remoteMessage.getData().get(Const.FCM_BODY);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ntfBuilder = new NotificationCompat.Builder(getBaseContext());
        ntfBuilder.setSmallIcon(R.drawable.push_icon);
        ntfBuilder.setContentTitle(title);
        ntfBuilder.setContentText(body);
        ntfBuilder.setAutoCancel(true);
        ntfBuilder.setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_notification));
        ntfBuilder.setTicker(title + ": " + body);
        ntfBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE |
                Notification.DEFAULT_LIGHTS);

        Intent intent = new Intent(getBaseContext(), NewsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
        ntfBuilder.setContentIntent(pendingIntent);
        Notification notification = ntfBuilder.build();
        mNotificationManager.notify(notifyId++, notification);
    }
}
