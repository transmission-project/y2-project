package com.example.huntertalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//new class for using firebase cloud messaging tutorial: https://www.youtube.com/watch?v=QXPgMUSfYFI
public class InviteNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("notification", "I received notification");
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }

    private void showNotification(String title, String body){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "com.example.huntertalk.test";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.d("TOKENFIREBASE", s);
    }
}
