package com.example.huntertalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//new class for using firebase cloud messaging tutorial: https://www.youtube.com/watch?v=QXPgMUSfYFI
public class InviteNotificationService extends FirebaseMessagingService {

    private DatabaseReference userDb;
    private FirebaseAuth mAuth;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendRegistrationToServer();
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        Log.d("notification", "I received notification");
    }



    private void showNotification(String title, String body){


        final int id=Integer.MAX_VALUE / 13+1;
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"MyNotifications")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setContentText(body);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(id, builder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.d("TOKENFIREBASE", s);

    }

    public void sendRegistrationToServer(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("fail", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        mAuth= FirebaseAuth.getInstance();
                        FirebaseUser currentUser= mAuth.getCurrentUser();
                        final String uid= currentUser.getUid();
                        userDb = FirebaseDatabase.getInstance().getReference("users");
                        userDb.child(uid).child("fcmToken").setValue(token);

                        // Log and toast
                        String msg = "token"+token;
                        Log.d("token", msg);
                    }
                });

    }
}
