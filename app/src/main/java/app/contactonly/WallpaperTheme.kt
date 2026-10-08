package app.contactonly

/** Preserve the photograph's hue; select text by measured contrast. */
object WallpaperTheme {
    fun surface(rgb: Int): Int {
        fun channel(shift: Int) = ((((rgb ushr shift) and 255) * 0.82) + 24 * 0.18).toInt()
        return (0xff shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }
    fun foreground(rgb: Int): Int {
        fun channel(shift: Int): Double {
            val v = ((rgb ushr shift) and 255) / 255.0
            return if (v <= .04045) v / 12.92 else Math.pow((v + .055) / 1.055, 2.4)
        }
        val l = .2126 * channel(16) + .7152 * channel(8) + .0722 * channel(0)
        return if ((l + .05) / .05 >= 1.05 / (l + .05)) 0xff000000.toInt() else 0xffffffff.toInt()
    }
    fun scrimAlpha(visibility: Int) = ((100 - visibility.coerceIn(0, 100)) * 255 / 100)
}
