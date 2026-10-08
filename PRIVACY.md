# Privacy explanation

Applies to DirectOnly / DM만 0.5.9 · Updated October 8, 2026

[한국어](PRIVACY.ko.md)

- Login, messaging and media uploads communicate with Instagram’s web service. Instagram’s privacy policy and terms apply.
- The app has no developer-operated server, advertising SDK, analytics SDK or separate feature that sends passwords/messages to the developer.
- Login cookies, WebView cache/storage and theme preferences may remain in the app’s private storage on your device. **Settings → Sign out · Clear web data** clears the session and web data. Uninstalling removes app-private data. Android backup is disabled.
- The Android media picker provides only the content URIs you select. Selected media is uploaded to Instagram when you send it. The app does not request permission to read your entire gallery.
- Notification relay is off by default. After you enable it and grant Android notification access, the app processes notifications from the official Instagram package only. The Android permission itself is broad and can expose notifications from other apps.
- DM detection uses notification category, channel and messaging-style metadata. Sender names and message bodies are not copied into the app’s notifications. Last-event time, DM classification and last-relay time are stored locally; duplicate-event tracking stays in memory.
- Turn off relay and revoke Android notification access to stop further processing. Web sign-out and notification-access revocation are separate controls.
- JavaScript identifies Notes labels and page layout to modify the web interface. There is no feature that sends screen content to a developer server.
- Communications and diagnostics performed by Android, WebView and Instagram follow those providers’ policies and your settings.

Distribution operator: GitHub **qmfvkn**. Questions: [GitHub Issues](https://github.com/qmfvkn/contact-only/issues). Do not post private messages, passwords, verification codes or conversation screenshots in public issues.

This explanation is based on code review and does not replace legal advice.
