package app.contactonly

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class InstagramNotificationListener : NotificationListenerService() {
    private val deduplicator = NotificationDeduplicator()
    override fun onListenerConnected() { connected = true; deduplicator.clear() }
    override fun onListenerDisconnected() { connected = false }
    override fun onDestroy() { connected = false; super.onDestroy() }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Reject other packages BEFORE accessing any notification extras.
        if (sbn.packageName != InstagramNotificationPolicy.PACKAGE || !enabled(this)) return
        val notification = sbn.notification
        val ranking = Ranking()
        val channelName = if (currentRanking.getRanking(sbn.key, ranking)) ranking.channel?.name?.toString() else null
        val messagingStyle = notification.extras.getString(Notification.EXTRA_TEMPLATE) == "android.app.Notification\$MessagingStyle" ||
            notification.extras.containsKey(Notification.EXTRA_MESSAGES)
        val isDm = InstagramNotificationPolicy.isDirectMessage(sbn.packageName, notification.category,
            messagingStyle, notification.channelId, channelName,
            notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
            notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        prefs(this).edit().putLong("last_instagram_event", System.currentTimeMillis()).putBoolean("last_event_dm", isDm).apply()
        if (!isDm || !deduplicator.isNew(sbn.key, sbn.postTime)) return
        if (DmNotifications.show(this)) {
            prefs(this).edit().putLong("last_dm_relay", System.currentTimeMillis()).apply()
            if (prefs(this).getBoolean("hide_original", false)) cancelNotification(sbn.key)
        }
    }
    companion object {
        @Volatile var connected = false
            private set
        fun prefs(context: Context) = context.getSharedPreferences("dm_notifications", Context.MODE_PRIVATE)
        fun enabled(context: Context) = prefs(context).getBoolean("relay_enabled", false)
        fun accessGranted(context: Context): Boolean {
            val component = ComponentName(context, InstagramNotificationListener::class.java)
            return if (Build.VERSION.SDK_INT >= 27) context.getSystemService(NotificationManager::class.java).isNotificationListenerAccessGranted(component)
            else Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                ?.split(':')?.any { ComponentName.unflattenFromString(it) == component } == true
        }
    }
}
