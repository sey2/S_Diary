package com.today.diary.fcm;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.today.diary.MainActivity;
import com.today.diary.R;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public MyFirebaseMessagingService() {
    }

    @Override
    public void onNewToken(String token){   // 새로운 토큰을 확인했을 때 호출되는 메서드
        super.onNewToken(token);    // token은 앱의 등록 id를 의미
        Log.e("FMS", "onNewToken 호출됨: " + token);
    }

    @Override // 푸시 메시지를 받았을 떄 그 내용 확인한 후 액티비티 쪽으로 보내는 메서드 호출
    public void onMessageReceived(RemoteMessage remoteMessage){ // 새로운 메시지를 받았을 때 호출되는 메서드
        Log.d("FMS", "onMessageReceived 호출됨.");

        if (remoteMessage.getNotification() != null) {                      //포어그라운드
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }else if (remoteMessage.getData().size() > 0) {                           //백그라운드
            sendNotification(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"));
            /* 백그라운드 작동 내용 */
        }
    }

    private void sendNotification(String messageBody, String messageTitle)  {
        Log.d("FCM Log", "알림 메시지: " + messageBody);

        /* 알림의 탭 작업 설정 */
        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "Channel ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /* 알림 만들기 */
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_main2_round)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setFullScreenIntent(pendingIntent, true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /* 새로운 인텐트로 앱 열기 */
        Intent newintent = new Intent(this, MainActivity.class);
        newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( newintent );

        /* 채널 만들기*/
        /* Android 8.0 이상에서 알림을 게시하려면 알림을 만들어야 함 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Channel Name";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

}
