package app.contactonly

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object DmNotifications {
    const val CHANNEL = "instagram_dm"
    const val OPEN_INBOX = "app.contactonly.OPEN_INBOX"
    private const val NOTIFICATION_ID = 200
    private fun manager(context: Context) = context.getSystemService(NotificationManager::class.java)
    fun createChannel(context: Context) {
        manager(context).createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.inbox), NotificationManager.IMPORTANCE_HIGH).apply {
            description = context.getString(R.string.channel_description)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        })
    }
    fun enabled(context: Context): Boolean {
        createChannel(context)
        return (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            manager(context).areNotificationsEnabled() && manager(context).getNotificationChannel(CHANNEL).importance != NotificationManager.IMPORTANCE_NONE
    }
    @android.annotation.SuppressLint("MissingPermission") // enabled() checks runtime permission and channel state before notify().
    fun show(context: Context, test: Boolean = false): Boolean {
        if (!enabled(context)) return false
        val open = Intent(context, MainActivity::class.java).apply {
            action = OPEN_INBOX
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(context, 200, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(context, CHANNEL)
            .setSmallIcon(app.contactonly.R.drawable.ic_notification)
            .setContentTitle(if (test) context.getString(R.string.notification_test_title) else context.getString(R.string.notification_new_title))
            .setContentText(if (test) context.getString(R.string.notification_test_body) else context.getString(R.string.notification_new_body))
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        // Never put sender names or message contents into our notifications.
        manager(context).notify(NOTIFICATION_ID, notification)
        return true
    }
    fun clear(context: Context) { manager(context).cancel(NOTIFICATION_ID) }
}
