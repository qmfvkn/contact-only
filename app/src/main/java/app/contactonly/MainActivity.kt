package app.contactonly

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.webkit.*
import android.widget.*

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private lateinit var notice: TextView
    private lateinit var wallpaper: ImageView
    private lateinit var wallpaperScrim: View
    private lateinit var header: LinearLayout
    private lateinit var footer: LinearLayout
    private lateinit var heading: TextView
    private var wallpaperBitmap: android.graphics.Bitmap? = null
    private val icons = mutableListOf<ImageButton>()
    private var revealGeneration = 0
    private var themed = false
    private var wallpaperStamp: String? = null
    private var wallpaperLoadGeneration = 0
    private var wallpaperColor = Color.rgb(21, 62, 54)
    private val wallpaperScript by lazy { assets.open("wallpaper-theme.js").bufferedReader().use { it.readText() } }
    private var picker: ValueCallback<Array<Uri>>? = null
    private val green = Color.rgb(21, 62, 54)
    private val reelScript by lazy { assets.open("dm-reel.js").bufferedReader().use { it.readText() } }
    private val guard by lazy { assets.open("dm-guard.js").bufferedReader().use { it.readText() } }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val uiPrefs=getSharedPreferences("wallpaper_ui",MODE_PRIVATE)
        if(uiPrefs.getString("background_mode","light")!="dark")uiPrefs.edit().putString("background_mode","light").apply()
        uiPrefs.edit().putBoolean("cards_enabled",false).apply()
        window.statusBarColor = green
        window.navigationBarColor = green
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.TRANSPARENT) }
        val background = FrameLayout(this)
        wallpaper = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        background.addView(wallpaper, FrameLayout.LayoutParams(-1, -1))
        wallpaperScrim = View(this)
        background.addView(wallpaperScrim, FrameLayout.LayoutParams(-1, -1))
        background.addView(root, FrameLayout.LayoutParams(-1, -1))
        root.setOnApplyWindowInsetsListener { v, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else v.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
        header = LinearLayout(this).apply { gravity = android.view.Gravity.CENTER_VERTICAL; setPadding(dp(18), dp(4), dp(8), dp(4)); setBackgroundColor(green) }
        heading = TextView(this).apply { text = getString(R.string.app_name); textSize = 21f; gravity = android.view.Gravity.CENTER_VERTICAL; setTextColor(Color.WHITE) }
        header.addView(heading, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(icon(R.drawable.ic_contact_bell, getString(R.string.notifications)) { notificationSettings() }, LinearLayout.LayoutParams(dp(48), dp(48)))
        header.addView(icon(R.drawable.ic_contact_gear, getString(R.string.settings)) { settings() }, LinearLayout.LayoutParams(dp(48), dp(48)))
        root.addView(header)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        root.addView(progress, LinearLayout.LayoutParams(-1, dp(3)))
        notice = TextView(this).apply { textSize = 13f; setPadding(dp(16), dp(8), dp(16), dp(8)); visibility = View.GONE; setTextColor(green) }
        root.addView(notice)
        web = WebView(this)
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        footer = LinearLayout(this).apply { gravity = android.view.Gravity.CENTER; setBackgroundColor(Color.rgb(235, 246, 240)) }
        footer.addView(button(getString(R.string.inbox)) { inbox() }, LinearLayout.LayoutParams(0, dp(50), 1f))
        footer.addView(button(getString(R.string.refresh)) { notice.visibility = View.GONE; web.reload() }, LinearLayout.LayoutParams(0, dp(50), 1f))
        root.addView(footer)
        setContentView(background)
        configureWeb()
        DmNotifications.createChannel(this)
        inbox()
        if (!getPreferences(0).getBoolean("introduced", false)) {
            AlertDialog.Builder(this).setTitle(getString(R.string.intro_title))
                .setMessage(getString(R.string.intro_body))
                .setPositiveButton(getString(R.string.get_started)) { _, _ -> getPreferences(0).edit().putBoolean("introduced", true).apply() }.show()
        }
    }
    private fun button(label: String, action: () -> Unit) = Button(this).apply { text = label; isAllCaps = false; setOnClickListener { action() } }
    private fun icon(resource: Int, label: String, action: () -> Unit) = ImageButton(this).apply {
        setImageResource(resource); contentDescription = label; setPadding(dp(12), dp(12), dp(12), dp(12))
        val value = android.util.TypedValue(); theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true)
        setBackgroundResource(value.resourceId); imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        setOnClickListener { action() }; icons.add(this)
    }
    private fun refreshWallpaper() { applyWallpaperColors() }
    private fun applyWallpaperColors() {
        val mode=getSharedPreferences("wallpaper_ui",MODE_PRIVATE).getString("background_mode","light")
        val dark=mode=="dark"
        themed = false
        wallpaper.visibility=if(themed)View.VISIBLE else View.GONE
        val color = if(dark)Color.rgb(18,18,18) else if(mode=="light")Color.rgb(245,245,245) else wallpaperColor
        val foreground = if(dark)Color.WHITE else if(mode=="light")Color.BLACK else if (themed) WallpaperTheme.foreground(color) else Color.WHITE
        header.setBackgroundColor(color); heading.setTextColor(foreground)
        icons.forEach { it.imageTintList = android.content.res.ColorStateList.valueOf(foreground) }
        footer.setBackgroundColor(if (themed || dark || mode=="light") color else Color.rgb(235, 246, 240))
        for (i in 0 until footer.childCount) (footer.getChildAt(i) as? Button)?.apply {
            setTextColor(if (themed || dark || mode=="light") foreground else Color.rgb(30, 34, 40))
            // Native Button backgrounds used to cover the footer with their own default theme tint.
            backgroundTintList = null
            background = android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x30ffffff), android.graphics.drawable.ColorDrawable(Color.TRANSPARENT), null)
            elevation = 0f; stateListAnimator = null
        }
        window.statusBarColor = color; window.navigationBarColor = color
        window.decorView.systemUiVisibility = if (foreground == Color.BLACK) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
        wallpaper.setBackgroundColor(Color.WHITE)
        wallpaperScrim.setBackgroundColor(if(dark)Color.rgb(18,18,18) else Color.argb(if (themed) WallpaperTheme.scrimAlpha(65) else 255,255,255,255))
        web.setBackgroundColor(if (themed || dark) Color.TRANSPARENT else Color.WHITE)
        applyWebTheme()
    }
    private fun applyWebTheme() {
        if (NavigationPolicy.classify(web.url ?: "") != NavigationPolicy.Destination.INTERNAL) return
        // Only a boolean crosses into Instagram. The photograph stays in native private storage.
        val prefs=getSharedPreferences("wallpaper_ui",MODE_PRIVATE)
        val dark=prefs.getString("background_mode","light")=="dark"
        val config=org.json.JSONObject().put("dark",dark).put("enabled",prefs.getBoolean("cards_enabled",false)).put("tone",prefs.getInt("card_tone",if(dark)1 else 0)).put("opacity",prefs.getInt("card_opacity",78).coerceIn(0,100)).put("radius",prefs.getInt("card_radius",18).coerceIn(0,32))
        web.evaluateJavascript("window.__contactWallpaper=${if (themed || dark) "true" else "false"};window.__contactCardConfig=$config;" + wallpaperScript, null)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == DmNotifications.OPEN_INBOX) { inbox(); DmNotifications.clear(this) }
    }
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 201)
        } else {
            startActivity(Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName))
        }
    }
    private fun testNotification() {
        if (!DmNotifications.show(this, test = true)) {
            Toast.makeText(this, getString(R.string.notification_permission_hint), Toast.LENGTH_LONG).show()
            requestNotificationPermission()
        }
    }
    private fun notificationSettings() {
        val preferences = InstagramNotificationListener.prefs(this)
        val active = InstagramNotificationListener.enabled(this)
        fun status(value: Boolean) = if (value) getString(R.string.on) else getString(R.string.off)
        val last = preferences.getLong("last_dm_relay", 0)
        val lastText = if (last == 0L) getString(R.string.none_yet) else java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(last))
        val lastEvent = preferences.getLong("last_instagram_event", 0)
        val lastEventText = if (lastEvent == 0L) getString(R.string.none_yet) else if (preferences.getBoolean("last_event_dm", false)) getString(R.string.dm_recognized) else getString(R.string.dm_skipped)
        val message = getString(R.string.notification_status,
            status(active), status(InstagramNotificationListener.accessGranted(this)),
            status(DmNotifications.enabled(this)),
            getString(if (InstagramNotificationListener.connected) R.string.connected else R.string.connection_pending),
            lastText, lastEventText)
        AlertDialog.Builder(this).setTitle(getString(R.string.dm_notifications))
            .setMessage(message).setPositiveButton(getString(R.string.configure)) { _, _ -> notificationActions() }
            .setNegativeButton(getString(R.string.close), null).show()
    }
    private fun notificationActions() {
        val enabled = InstagramNotificationListener.enabled(this)
        val preferences = InstagramNotificationListener.prefs(this)
        val hide = preferences.getBoolean("hide_original", false)
        AlertDialog.Builder(this).setTitle(getString(R.string.notification_settings))
            .setItems(arrayOf(if (enabled) getString(R.string.relay_off) else getString(R.string.relay_on), getString(R.string.notification_permission), getString(R.string.notification_access), getString(R.string.test_notification),
                if (hide) getString(R.string.keep_original) else getString(R.string.hide_original))) { _, which ->
                when (which) {
                    0 -> if (enabled) {
                        preferences.edit().putBoolean("relay_enabled", false).apply()
                        DmNotifications.clear(this)
                        Toast.makeText(this, getString(R.string.relay_disabled), Toast.LENGTH_LONG).show()
                    } else explainNotificationAccess()
                    1 -> requestNotificationPermission()
                    2 -> explainNotificationAccess()
                    3 -> testNotification()
                    4 -> {
                        preferences.edit().putBoolean("hide_original", !hide).apply()
                        Toast.makeText(this, if (!hide) getString(R.string.original_hidden_hint) else getString(R.string.original_kept_hint), Toast.LENGTH_LONG).show()
                    }
                }
            }.show()
    }
    private fun explainNotificationAccess() {
        AlertDialog.Builder(this).setTitle(getString(R.string.access_title))
            .setMessage(getString(R.string.access_body))
            .setNegativeButton(getString(R.string.cancel), null).setPositiveButton(getString(R.string.connect_settings)) { _, _ ->
                InstagramNotificationListener.prefs(this).edit().putBoolean("relay_enabled", true).apply()
                if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 202)
                } else openNotificationAccess()
            }.show()
    }
    private fun openNotificationAccess() {
        try { startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        catch (_: Exception) { Toast.makeText(this, getString(R.string.access_hint), Toast.LENGTH_LONG).show() }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 202) {
            if (grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) openNotificationAccess()
            else Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }
    private fun inbox() { notice.visibility = View.GONE; web.visibility = View.INVISIBLE; web.loadUrl(NavigationPolicy.INBOX) }
    private fun revealWhenReady(url: String, generation: Int, attempt: Int = 0) {
        if(generation != revealGeneration || web.url != url || isFinishing || isDestroyed)return
        progress.visibility=View.VISIBLE
        web.evaluateJavascript("Boolean(window.__contactOnlyPrepare && window.__contactOnlyPrepare())") { ready ->
            if(generation != revealGeneration || web.url != url || isFinishing || isDestroyed)return@evaluateJavascript
            if(ready == "true") {
                web.visibility=View.VISIBLE; progress.visibility=View.GONE
            } else if(attempt < 120) {
                web.postDelayed({revealWhenReady(url,generation,attempt+1)},100)
            } else {
                notice.text=getString(R.string.prepare_delayed)
                notice.visibility=View.VISIBLE; progress.visibility=View.GONE
            }
        }
    }
    @Suppress("SetJavaScriptEnabled")
    private fun configureWeb() {
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mediaPlaybackRequiresUserGesture = true
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame) return false
                return navigate(request.url.toString())
            }
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                revealGeneration++
                web.visibility = View.INVISIBLE
                when (NavigationPolicy.classify(url)) {
                    NavigationPolicy.Destination.INTERNAL -> Unit
                    else -> { view.stopLoading(); inbox() }
                }
            }
            override fun onPageFinished(view: WebView, url: String) {
                if (NavigationPolicy.classify(url) == NavigationPolicy.Destination.INTERNAL) {
                    view.evaluateJavascript(guard + reelScript) { applyWebTheme(); revealWhenReady(url, ++revealGeneration) }
                }
                CookieManager.getInstance().flush()
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    notice.text = getString(R.string.connection_error)
                    notice.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                }
            }
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                if (request.isForMainFrame && response.statusCode >= 400) {
                    notice.text = getString(R.string.http_error, response.statusCode)
                    notice.visibility = View.VISIBLE
                }
            }
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: android.net.http.SslError) {
                handler.cancel()
                notice.text = getString(R.string.ssl_error)
                notice.visibility = View.VISIBLE
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, value: Int) {
                progress.progress = value
                progress.visibility = if (value == 100 && web.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                picker?.onReceiveValue(null)
                picker = callback
                val types = params.acceptTypes.flatMap { it.split(',') }.map { it.trim().lowercase() }
                    .filter { it.contains('/') && !it.contains(';') }.distinct()
                val multiple = params.mode == FileChooserParams.MODE_OPEN_MULTIPLE
                val visual = types.isEmpty() || types.all { it == "*/*" || it.startsWith("image/") || it.startsWith("video/") }
                val visualType = when {
                    types.size == 1 && types[0] != "*/*" -> types[0]
                    types.isNotEmpty() && types.all { it.startsWith("image/") } -> "image/*"
                    types.isNotEmpty() && types.all { it.startsWith("video/") } -> "video/*"
                    else -> null // Photo picker defaults to both images and videos.
                }
                if (visual && android.os.Build.VERSION.SDK_INT >= 33) {
                    val photos = Intent(android.provider.MediaStore.ACTION_PICK_IMAGES).apply {
                        if (visualType != null) type = visualType
                        if (multiple) putExtra(android.provider.MediaStore.EXTRA_PICK_IMAGES_MAX,
                            minOf(20, android.provider.MediaStore.getPickImagesMaxLimit()))
                    }
                    try { startActivityForResult(photos, 100); return true }
                    catch (_: ActivityNotFoundException) { /* Use installed gallery apps below. */ }
                }
                val intent = Intent(if (visual) Intent.ACTION_GET_CONTENT else Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = if (visual) visualType ?: "*/*" else if (types.size == 1) types[0] else "*/*"
                    if (visual && visualType == null) putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                    else if (!visual && types.size > 1) putExtra(Intent.EXTRA_MIME_TYPES, types.toTypedArray())
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
                }
                return try {
                    startActivityForResult(if (visual) Intent.createChooser(intent, getString(R.string.select_media)) else intent, 100)
                    true
                }
                catch (_: ActivityNotFoundException) { picker?.onReceiveValue(null); picker = null; false }
            }
            override fun onPermissionRequest(request: PermissionRequest) { request.deny() }
        }
    }
    private fun navigate(url: String): Boolean = when (NavigationPolicy.classify(url)) {
        NavigationPolicy.Destination.INTERNAL -> false
        NavigationPolicy.Destination.BLOCKED -> { Toast.makeText(this, getString(R.string.dm_only), Toast.LENGTH_SHORT).show(); true }
        NavigationPolicy.Destination.REJECT -> { Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show(); true }
        NavigationPolicy.Destination.EXTERNAL -> {
            AlertDialog.Builder(this).setTitle(getString(R.string.external_title))
                .setMessage(getString(R.string.external_body, Uri.parse(url).host ?: ""))
                .setNegativeButton(getString(R.string.cancel), null).setPositiveButton(getString(R.string.open)) { _, _ ->
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)) }
                    catch (_: ActivityNotFoundException) { Toast.makeText(this, getString(R.string.no_browser), Toast.LENGTH_SHORT).show() }
                }.show()
            true
        }
    }
    private fun themeSettings() {
        val prefs=getSharedPreferences("wallpaper_ui",MODE_PRIVATE)
        val dark=prefs.getString("background_mode","light")=="dark"
        AlertDialog.Builder(this).setTitle(getString(R.string.theme)).setSingleChoiceItems(arrayOf(getString(R.string.light_theme),getString(R.string.dark_theme)),if(dark)1 else 0){dialog,index->prefs.edit().putString("background_mode",if(index==1)"dark" else "light").apply();applyWallpaperColors();dialog.dismiss()}.setNegativeButton(getString(R.string.close),null).show()
    }
    private fun settings() {
        AlertDialog.Builder(this).setTitle(getString(R.string.settings_title, getString(R.string.app_name), packageManager.getPackageInfo(packageName, 0).versionName ?: ""))
            .setItems(arrayOf(getString(R.string.return_dm), getString(R.string.logout_data), getString(R.string.about), getString(R.string.dm_notifications), getString(R.string.theme))) { _, which ->
                when (which) {
                    0 -> inbox()
                    1 -> AlertDialog.Builder(this).setTitle(getString(R.string.logout_title)).setMessage(getString(R.string.logout_body))
                        .setNegativeButton(getString(R.string.cancel), null).setPositiveButton(getString(R.string.delete)) { _, _ ->
                            InstagramNotificationListener.prefs(this).edit().putBoolean("relay_enabled", false).apply()
                            DmNotifications.clear(this)
                            web.stopLoading()
                            CookieManager.getInstance().removeAllCookies {
                                CookieManager.getInstance().flush()
                                WebStorage.getInstance().deleteAllData()
                                web.clearCache(true); web.clearHistory(); web.clearFormData(); inbox()
                            }
                        }.show()
                    2 -> AlertDialog.Builder(this).setTitle(getString(R.string.about)).setMessage(getString(R.string.about_body)).setPositiveButton(getString(R.string.ok), null).show()
                    3 -> notificationSettings()
                    4 -> themeSettings()

                }
            }.show()
    }
    @Deprecated("Legacy activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val result = if (resultCode != RESULT_OK || data == null) null
            else data.clipData?.let { clip -> Array(clip.itemCount) { clip.getItemAt(it).uri } } ?: data.data?.let { arrayOf(it) }
            val selected = result?.filter { it.scheme == "content" }?.toTypedArray()?.takeIf { it.isNotEmpty() }
            picker?.onReceiveValue(selected); picker = null
        }
    }
    @Deprecated("Legacy back navigation")
    override fun onBackPressed() {
        val path = web.url?.let { Uri.parse(it).path } ?: ""
        if (path.startsWith("/direct/") && path != "/direct/inbox/") inbox()
        else if (path.startsWith("/accounts/") || path.startsWith("/challenge/")) inbox()
        else super.onBackPressed()
    }
    override fun onPause() { CookieManager.getInstance().flush(); web.onPause(); super.onPause() }
    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) { web.onResume(); refreshWallpaper() }
        if (InstagramNotificationListener.enabled(this) && InstagramNotificationListener.accessGranted(this) && !InstagramNotificationListener.connected) {
            android.service.notification.NotificationListenerService.requestRebind(android.content.ComponentName(this, InstagramNotificationListener::class.java))
        }
    }
    override fun onDestroy() { picker?.onReceiveValue(null); picker = null; web.destroy(); wallpaper.setImageDrawable(null); wallpaperBitmap?.recycle(); super.onDestroy() }
}
