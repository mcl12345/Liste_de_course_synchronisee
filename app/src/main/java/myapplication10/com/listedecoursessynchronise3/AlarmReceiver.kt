package myapplication10.com.listedecoursessynchronise3

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.toast

class AlarmReceiver : BroadcastReceiver() {

    val NOTIFICATION_MON_MESSAGE : String = "NOTIFICATION_MON_MESSAGE"

    override fun onReceive(context: Context, intent: Intent) {
        context.toast("Alarm")

        // Configuration de la notification
        // ------------------------------------------------------------
        // Code de compatibilité avec les versions supérieures
        if(Build.VERSION.SDK_INT >= 26) {
            val mgr = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel( NOTIFICATION_MON_MESSAGE, context.getString(R.string.categorie_notif), NotificationManager.IMPORTANCE_DEFAULT)
            mgr.createNotificationChannel(channel)
        }

        val notificationId = 2
        val mgr = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notifIntent = Intent(context, ActualisationActivity::class.java)
        notifIntent.putExtra("notif_intent", "notif_intent")
        notifIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notifPendingIntent = PendingIntent.getActivity(context, 42, notifIntent, PendingIntent.FLAG_IMMUTABLE)

        //val stackBuilder = TaskStackBuilder.create(context)
        //stackBuilder.addParentStack(NotifActivity::class.java)
        //stackBuilder.addNextIntent(notifIntent)

        //val notifPendingIntent = stackBuilder.getPendingIntent(42, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(context, NOTIFICATION_MON_MESSAGE)
            .setSmallIcon(R.drawable.bosse)
            .setContentTitle(context.getString(R.string.notification))
            .setContentText(context.getString(R.string.notif_nouv_message))
            .setContentIntent(notifPendingIntent)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
            .addAction(R.drawable.ic_search_white_24dp, "Action", notifPendingIntent)
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.bosse))

        mgr.notify(notificationId, notif.build())
    }
}