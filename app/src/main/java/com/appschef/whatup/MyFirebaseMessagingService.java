package com.appschef.whatup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String CHANNEL_ID = "Message";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("newToken", s);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        createNotificationChannel();

        String title = remoteMessage.getData().get("title") != null ? remoteMessage.getData().get("title") : "What Up";
        String body = remoteMessage.getData().get("body") != null ? remoteMessage.getData().get("body") : "You Got a message!";
        String data=remoteMessage.getData().get("action");
//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle(remoteMessage.getNotification().getTitle())
//                .setContentText(remoteMessage.getNotification().getBody())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .build();
//        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
//        manager.notify(123, notification);
        postNotification(title, body,data);
//        super.onMessageReceived(remoteMessage);
    }

    public static String getToken(Context context) {
//        return null;
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //making normal notification channel
            CharSequence name = "Notification Channel";
            String description = "Default Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            //registering channels
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void postNotification(String title, String text,String data) {
        Intent intent = null;

        intent = new Intent(this, MainActivity2.class);
        intent.putExtra("who",data);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, Math.round(System.currentTimeMillis() / 10000), intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap icon =BitmapFactory.decodeResource(getResources(),R.drawable.icon);
//                BitmapFactory.decodeResource(getResources().getDrawable(R.drawable.icon));
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
.setLargeIcon(icon)
                        .setColor(getColor(R.color.yellow))
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(Math.round(System.currentTimeMillis() / 10000), notificationBuilder.build());
        }
    }
}