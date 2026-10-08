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
        manager(context).createNotificationChannel(NotificationChannel(CHANNEL, "인스타 DM", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "새 인스타 DM을 알리고 연락만에서 엽니다."
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
            .setContentTitle(if (test) "DM 알림 테스트" else "새 인스타 DM이 있어요")
            .setContentText(if (test) "누르면 연락만의 DM 화면이 열립니다." else "눌러서 받은편지함을 확인하세요.")
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
