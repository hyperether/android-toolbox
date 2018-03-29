package com.hyperether.toolbox;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

/*
 * HyperNotification.java
 *
 * Created by Slobodan on 12/11/2017
 */

public class HyperNotification {

    private static final String TAG = HyperNotification.class.getSimpleName();
    private static HyperNotification instance;
    private int notificationId = 0;
    private String channelId = "hyper_channel_1";
    private final Map<String, Integer> pendingNotificationIds = new HashMap<>();

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

    /**
     * Set Notification
     *
     * @param builder builder
     * @param intent intent
     * @param flag notification intent flag
     * @param type notification action
     * @param singleType show only one notification for single type
     */
    public void setNotification(Notification.Builder builder,
                                Intent intent,
                                int flag,
                                String type,
                                boolean singleType) {
        Context context = HyperApp.getInstance().getApplicationContext();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            HyperLog.getInstance().d(TAG, "setNotification", "type: " + type +
                    " notificationID = " + notificationId);
            notificationId++;

            if (singleType && hasPendingNotificationId(type)) {
                return;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                    flag);

            if (pendingIntent != null)
                builder.setContentIntent(pendingIntent);

            mNotificationManager.notify(notificationId, builder.build());

            if (singleType) {
                pendingNotificationIds.put(type, notificationId);
            } else {
                pendingNotificationIds.put(notificationId + "", notificationId);
            }
        }
    }

    /**
     * Get notification id by server orderID. If we do not have notificationID for server order ID
     * return -1;
     *
     * @param key notification key
     *
     * @return notification id
     */
    private int getPendingNotificationId(String key) {
        int returnValue = -1;
        if (key != null && !key.isEmpty()) {
            Integer notID = pendingNotificationIds.get(key);
            if (notID != null)
                returnValue = notID;
        }

        return returnValue;
    }

    /**
     * Has Pending Notification Id
     *
     * @param key notification key
     *
     * @return boolean
     */
    private boolean hasPendingNotificationId(String key) {
        for (int i = 0; i < pendingNotificationIds.size(); i++) {
            if (pendingNotificationIds.get(key) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cancel Notification - Method that cancels the notification with msgKey
     *
     * @param msgKey msgKey
     */
    public void cancelNotification(String msgKey) {
        int notId = getPendingNotificationId(msgKey);
        Context ctxt = HyperApp.getInstance().getApplicationContext();
        NotificationManager nMgr = (NotificationManager) ctxt
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancel(notId);
        }
        pendingNotificationIds.remove(msgKey);
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
