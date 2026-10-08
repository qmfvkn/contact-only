package app.contactonly

object InstagramNotificationPolicy {
    const val PACKAGE = "com.instagram.android"
    fun isDirectMessage(packageName: String, category: String?, messagingStyle: Boolean,
                        channelId: String?, channelName: String?, groupSummary: Boolean, ongoing: Boolean): Boolean {
        if (packageName != PACKAGE || groupSummary || ongoing) return false
        if (category == "msg" || messagingStyle) return true
        // Only channel metadata is examined. Social notification body text is never parsed.
        val channel = "${channelId.orEmpty()} ${channelName.orEmpty()}".lowercase()
        return Regex("(^|[^a-z])(direct|dm|messages?|messaging|inbox)([^a-z]|$)|다이렉트|메시지|채팅").containsMatchIn(channel)
    }
}

class NotificationDeduplicator {
    private val seen = LinkedHashMap<String, Long>()
    fun isNew(key: String, postTime: Long): Boolean {
        if (seen[key] == postTime) return false
        seen[key] = postTime
        if (seen.size > 200) seen.remove(seen.keys.first())
        return true
    }
    fun clear() = seen.clear()
}
