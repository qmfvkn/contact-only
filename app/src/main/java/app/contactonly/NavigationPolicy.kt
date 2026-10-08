package app.contactonly

import java.net.URI

object NavigationPolicy {
    const val INBOX = "https://www.instagram.com/direct/inbox/"
    enum class Destination { INTERNAL, BLOCKED, EXTERNAL, REJECT }
    fun classify(url: String): Destination {
        val uri = try { URI(url) } catch (_: Exception) { return Destination.REJECT }
        if (uri.scheme != "https") return Destination.REJECT
        val host = uri.host?.lowercase() ?: return Destination.REJECT
        if (uri.userInfo != null || (uri.port != -1 && uri.port != 443)) return Destination.REJECT
        if (host != "instagram.com" && host != "www.instagram.com") return Destination.EXTERNAL
        val path = uri.path ?: "/"
        if (path == "/direct" || path.startsWith("/direct/") ||
            path.startsWith("/accounts/") || path == "/challenge" || path.startsWith("/challenge/") ||
            path.startsWith("/two_factor/") || path.startsWith("/consent/")) return Destination.INTERNAL
        if (path == "/" || path == "/explore" || path.startsWith("/explore/") ||
            path == "/reels" || path.startsWith("/reels/")) return Destination.BLOCKED
        return Destination.EXTERNAL
    }
}
