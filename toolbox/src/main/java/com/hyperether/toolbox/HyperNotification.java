package com.hyperether.toolbox;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;

/*
 * HyperNotification.java
 *
 * Created by Slobodan on 12/11/2017
 */

public class HyperNotification {

    private static HyperNotification instance;
    private int notificationId = 0;
    private String channelId = "hyper_channel_1";

    public static HyperNotification getInstance() {
        if (instance == null)
            instance = new HyperNotification();
        return instance;
    }

    public Notification getForegroundServiceNotification(Context context,
                                                         String title,
                                                         String status,
                                                         int iconSmall,
                                                         int iconLarge,
                                                         PendingIntent intent) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(status)
                .setSmallIcon(iconSmall)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconLarge));
        if (intent != null)
            builder.setContentIntent(intent);
        return builder.build();
    }

    protected void setNotification(Context context, Notification notification) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            notificationId++;
            mNotificationManager.notify(notificationId, notification);
        }
    }

    protected void removeNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(notificationId);
            notificationId--;
        }
    }
}
