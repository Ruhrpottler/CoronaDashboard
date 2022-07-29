package com.blackviper.coronadashboard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/** @apiNote API Level 26 or higher required
 *
 */
public class NotificationSvc
{
    private int notificationId = 1;
    private static String defaultChannelId = "defaultChannel";
    private final Context context;
    private final NotificationManager manager;

    public NotificationSvc(Context context)
    {
        this.context = context;
        this.manager = context.getSystemService(NotificationManager.class);
        if(getNotificationChannel(defaultChannelId) == null)
        {
            createNotificationChannel(defaultChannelId, "Allgemein", "Allgemeine Benachrichtigungen");
        }
    }

    private void createNotificationChannel(String channelId, String channelName, String description)
    {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        manager.createNotificationChannel(channel);
    }

    private NotificationChannel getNotificationChannel(String channelId) //TODO notwendig?
    {
        return manager.getNotificationChannel(channelId);
    }

    /** Benachrichtigung senden oder aktualisieren
     * @param priority z.B. NotificationCompat.PRIORITY_DEFAULT
     */

    /**
     * With default priority
     */
    public int sendNotification(String title, String description) //TODO aktuell immer über defualt, entscheiden für eins und dann entsprechend anpassen
    {
        return sendNotification(getNotificationId(), title, description, NotificationCompat.PRIORITY_DEFAULT);
    }

    public int sendNotification(String title, String description, int priority) //TODO aktuell immer über defualt, entscheiden für eins und dann entsprechend anpassen
    {
        return sendNotification(getNotificationId(), title, description, priority);
    }

    private int sendNotification(int notificationId, String title, String description, int priority)
    {
        if(notificationId < 0)
        {
            Log.w(this.getClass().toString(), "Die Notification-ID " + notificationId + " ist ungültig. Die Benachrichtigung wird nicht gesendet");
            return -1;
        }

        //User gelangt in die App, wenn er auf die Benachrichtigung tippt.
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, this.defaultChannelId)
                .setSmallIcon(R.mipmap.appicon) //TODO Von Manifest abhängig machen
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(priority)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE); //private zeigt nur gurndlegende Informationen auf dem Sperrbildschirm

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());

        Log.i(this.getClass().toString(), "Benachrichtigung mit ID " + notificationId + " gesendet.");
        return notificationId;
    }

    /** Aktualisiert die Benachrichtigung, falls noch vorhanden. Wenn nicht, wird sie als neue Benachrichtigung angezeigt.
     *
     */
    public void updateNotification(int notificationId, String title, String descritpion, int priority)
    {
        sendNotification(notificationId, title, descritpion, priority);
    }

    public void removeNotification(int notificationId)
    {
        manager.cancel(notificationId);
    }

    private int getNotificationId()
    {
        return this.notificationId++; //TODO Prüfen ob er wirklich erst erhöht und dann returned.
    }


}
