package com.hyperether.toolbox;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/*
 * HyperNotification.java
 *
 * Created by Slobodan on 12/11/2017
 */

public class HyperNotification {

    private static HyperNotification instance;
    private int notificationId = 0;
    private String channelId = "hyper_channel_1";
    private String channelName = "hyper_channel";
    private String channelDesc = "hyper_channel_desc";
    private NotificationManager notificationManager;

    public static HyperNotification getInstance() {
        if (instance == null)
            instance = new HyperNotification();
        return instance;
    }

    private HyperNotification() {
    }

    public Notification getForegroundServiceNotification(Context context,
                                                         String title,
                                                         String status,
                                                         int iconSmall,
                                                         int iconLarge,
                                                         PendingIntent intent) {
        // Create channel if needed
        getNotificationManager(context);
        // create notification with proper builder
        return getNotification(context, title, status, iconSmall, iconLarge, intent);
    }

    protected void setNotification(Context context, Notification notification) {
        NotificationManager mNotificationManager = getNotificationManager(context);
        if (mNotificationManager != null) {
            notificationId++;
            mNotificationManager.notify(notificationId, notification);
        }
    }

    protected void removeNotification(Context context) {
        NotificationManager mNotificationManager = getNotificationManager(context);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(notificationId);
            notificationId--;
        }
    }

    private NotificationManager getNotificationManager(Context context) {
        if (notificationManager == null) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(channelId, channelName,
                        importance);
                channel.setDescription(channelDesc);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            } else {
                notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
            }
        }
        return notificationManager;
    }

    @SuppressWarnings("deprecation")
    private Notification getNotification(Context context,
                                         String title,
                                         String status,
                                         int iconSmall,
                                         int iconLarge,
                                         PendingIntent intent) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setContentTitle(title)
                .setContentText(status)
                .setSmallIcon(iconSmall)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconLarge));
        if (intent != null)
            builder.setContentIntent(intent);
        return builder.build();
    }
}
