package tk.codme.chat24;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_message=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this,"default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Create an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,resultIntent, 0);

        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId=(int)System.currentTimeMillis();
        NotificationManager mNotifyMqr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotifyMqr.notify(mNotificationId,mBuilder.build());
    }

}
