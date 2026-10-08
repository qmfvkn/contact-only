package app.contactonly
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow
class WallpaperThemeTest {
    @Test fun tonalSurfacesKeepSelectedTextReadableAcrossRgbSpace() {
        fun luminance(rgb: Int): Double {
            fun c(s: Int): Double { val v=((rgb ushr s) and 255)/255.0; return if(v<=.04045)v/12.92 else ((v+.055)/1.055).pow(2.4) }
            return .2126*c(16)+.7152*c(8)+.0722*c(0)
        }
        for(r in 0..255 step 17) for(g in 0..255 step 17) for(b in 0..255 step 17) {
            val surface=WallpaperTheme.surface((r shl 16) or (g shl 8) or b)
            val text = WallpaperTheme.foreground(surface)
            val s=luminance(surface); val t=luminance(text)
            assertTrue((maxOf(s,t)+.05)/(minOf(s,t)+.05)>=4.5)
        }
    }
    @Test fun visibilityMapsToWhiteOverlayAndClamps() {
        org.junit.Assert.assertEquals(255,WallpaperTheme.scrimAlpha(0))
        org.junit.Assert.assertEquals(0,WallpaperTheme.scrimAlpha(100))
        org.junit.Assert.assertEquals(0,WallpaperTheme.scrimAlpha(150))
        org.junit.Assert.assertEquals(255,WallpaperTheme.scrimAlpha(-10))
    }
}
