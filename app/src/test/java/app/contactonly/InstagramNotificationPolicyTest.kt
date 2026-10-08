package app.contactonly

import org.junit.Assert.*
import org.junit.Test

class InstagramNotificationPolicyTest {
    private fun dm(pkg: String = "com.instagram.android", category: String? = null, style: Boolean = false,
                   id: String? = null, name: String? = null, group: Boolean = false, ongoing: Boolean = false) =
        InstagramNotificationPolicy.isDirectMessage(pkg, category, style, id, name, group, ongoing)
    @Test fun onlyInstagramIsAccepted() {
        assertFalse(dm(pkg = "com.whatsapp", category = "msg", style = true))
        assertFalse(dm(pkg = "com.instagram.android.fake", name = "Direct"))
        assertTrue(dm(category = "msg"))
        assertTrue(dm(style = true))
    }
    @Test fun directChannelsRecognizedWithoutReadingBodies() {
        listOf("Instagram Direct", "Messages", "direct_v2", "DM", "다이렉트 메시지", "채팅").forEach { assertTrue(it, dm(name = it)) }
        assertTrue(dm(id = "direct"))
    }
    @Test fun likesReelsAndAmbiguousSocialNotificationsIgnored() {
        listOf("Likes", "Reels", "Comments", "Live", "General", "directly", "administrator").forEach { assertFalse(it, dm(name = it, category = "social")) }
        assertFalse(dm())
        assertFalse(dm(category = "msg", group = true))
        assertFalse(dm(category = "msg", ongoing = true))
    }
    @Test fun duplicateUpdatesIgnoredButNewEventsDelivered() {
        val d = NotificationDeduplicator()
        assertTrue(d.isNew("a", 10))
        assertFalse(d.isNew("a", 10))
        assertTrue(d.isNew("a", 11))
        assertTrue(d.isNew("b", 11))
        d.clear()
        assertTrue(d.isNew("a", 11))
    }
}
