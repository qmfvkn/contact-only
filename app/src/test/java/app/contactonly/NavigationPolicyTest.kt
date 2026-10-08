package app.contactonly
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationPolicyTest {
    @Test fun directAndLoginStayInside() {
        listOf("/direct/inbox/", "/direct/t/123/", "/accounts/login/?next=%2Fdirect%2Finbox%2F", "/challenge/123/").forEach {
            assertEquals(NavigationPolicy.Destination.INTERNAL, NavigationPolicy.classify("https://www.instagram.com$it"))
        }
    }
    @Test fun distractionsBlocked() {
        listOf("/", "/explore/", "/reels", "/reels/").forEach {
            assertEquals(NavigationPolicy.Destination.BLOCKED, NavigationPolicy.classify("https://www.instagram.com$it"))
        }
    }
    @Test fun postsAndOtherSitesNeedExternalBrowser() {
        listOf("https://www.instagram.com/p/123/", "https://www.instagram.com/reel/123/", "https://instagram.com.evil.example/direct/inbox/", "https://example.com").forEach {
            assertEquals(NavigationPolicy.Destination.EXTERNAL, NavigationPolicy.classify(it))
        }
    }
    @Test fun dangerousSchemesAndCredentialsRejected() {
        listOf("javascript:alert(1)", "file:///etc/passwd", "intent://test", "http://instagram.com/direct/", "https://user@instagram.com/direct/", "https://instagram.com:8080/direct/", "bad url").forEach {
            assertEquals(NavigationPolicy.Destination.REJECT, NavigationPolicy.classify(it))
        }
    }
}
