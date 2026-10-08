# Release checks — October 8, 2026

DirectOnly / DM만 0.5.9 · app.contactonly.sample · versionCode 10

- English default and Korean native UI resources have matching keys and format placeholders. Instagram’s own interface is not translated by this app.
- Release unit tests, Android Lint and release assembly passed in the distribution project. Reviewed source files were copied into this clean public package.
- Release APK signature verification passed; the signing identity and application ID match the previous sample.
- No WebView debugging code or native JavaScript bridge is included. HTTPS-only navigation, SSL-error cancellation, mixed-content blocking and disabled file access remain in place.
- INTERNET and POST_NOTIFICATIONS are the only declared app permissions. The notification listener is protected by BIND_NOTIFICATION_LISTENER_SERVICE.
- No developer-operated server, advertising SDK or analytics SDK is included. Instagram communications and local WebView data remain necessary.
- The public source package excludes signing keys, passwords, SDKs, caches, device captures and private machine paths.
- This update changes localization and documentation. The existing DM navigation and Reel filters remain unchanged.

This is an experimental prerelease with limited device testing. An English-language physical-device session has not been verified for this release. Device-specific installation and notification restrictions may apply.

A project-wide license has not been selected. Instagram terms compatibility still needs separate review. Technical checks are not legal clearance or platform approval.
