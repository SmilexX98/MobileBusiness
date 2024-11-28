package martinez.javier.chat.Notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import martinez.javier.chat.Chat.ChatActivity
import martinez.javier.chat.R

class MyFcmService : FirebaseMessagingService() {

    companion object{
        private const val NOTIFICATION_CHANNEL_ID = "CHAT_CHANNEL_ID"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null){
            val senderUid = remoteMessage.data["senderUid"]
            showNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                senderUid ?: "Unknown"

            )
        }


    }

    private fun showNotification(title: String?, body: String?, senderUid: String) {
        val notificationId = java.util.Random().nextInt(3000)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        configurarCanalNotificacion(notificationManager)

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("uid", senderUid)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title ?: "Sin titulo")
            .setContentText(body ?: "Sin contenido")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())

    }

    private fun configurarCanalNotificacion(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Chat_Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Show Chat Notification"
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }
}