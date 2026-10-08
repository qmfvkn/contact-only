# DirectOnly

### Distraction-free Instagram DMs for Android

**No feed. No endless Reels. Just messages.**

[한국어 · DM만](README.ko.md) · [Download APK](https://github.com/qmfvkn/contact-only/releases/tag/v0.5.9-sample) · [Privacy](PRIVACY.md)

Ever opened Instagram to reply to one message, then ended up scrolling?

DirectOnly opens your Instagram inbox directly so you can reply, stay connected, and get back to your day. Built for people who want the conversation without the content feed.

## What it does

- Opens Instagram DMs directly.
- Restricts navigation to the feed, posts and Explore.
- Stops endless Reels browsing: you can watch a Reel shared in a DM, then swipe up or down to return to the conversation.
- Hides Notes from the inbox.
- Offers a simple light or dark theme.
- Lets you choose photos and videos using Android’s media picker.
- Optionally relays DM alerts from the official Instagram app.

“No endless Reels” refers to browsing restrictions, not removing videos sent by friends. The app does not measure or guarantee a particular amount of time saved.

## Download and install

**[Download DirectOnly 0.5.9 for Android](https://github.com/qmfvkn/contact-only/releases/download/v0.5.9-sample/directonly-0.5.9-android.apk)**

[Release notes, source archive and SHA-256 checksums](https://github.com/qmfvkn/contact-only/releases/tag/v0.5.9-sample)

- Requires Android 8.0 or newer.
- Sign in directly on Instagram’s website inside the app.
- Native app controls follow your phone’s language: English by default, Korean as **DM만**. Instagram’s own interface follows its language/account settings.
- Updating an existing distribution sample keeps the same application ID and signing identity.
- The official Instagram app is not required for viewing DMs. It **is required for notification relay**, with login, DM alerts and the relevant Android permissions enabled. Use the same account in both apps.

This is an **experimental prerelease**, with limited device testing. Instagram website changes may break filters or messaging behavior. Voice/video calls are not guaranteed. Some devices may block installation or notification access under Play Protect or restricted-settings policies; disabling security protections is not a requirement.

DirectOnly is an unofficial web client. It is not affiliated with, endorsed by, or approved by Instagram, Meta or Samsung.

## Privacy

The app has no developer-operated server, advertising SDK or analytics SDK. Instagram handles login and messaging. Cookies and web data may remain in the app’s private device storage until cleared. Photo access is limited to items you select.

Notification relay is off by default. Android notification access is a broad permission; the app processes only Instagram notifications and does not copy sender names or message contents into its own alerts. See the [privacy explanation](PRIVACY.md) for details, including third-party data handling and how to revoke access.

## Build from source

Use JDK 17 and Android SDK platform 35. Open this folder in Android Studio, or configure ANDROID_HOME and run:

```sh
# Windows
gradlew.bat testReleaseUnitTest lintRelease assembleRelease

# macOS / Linux
sh gradlew testReleaseUnitTest lintRelease assembleRelease
```

The generated release APK is unsigned. Distribution signing keys are private; an update to an installed app requires the same signature.

JavaScript regression checks:

```sh
node tests/guard-script.test.cjs
node tests/reel-script.test.cjs
node tests/wallpaper-script.test.cjs
```

English strings live in app/src/main/res/values/strings.xml; Korean strings live in values-ko/strings.xml.

## Rights and limitations

The bell, gear and app icons are project-created vectors. Icons collected from Samsung apps through OneUIProject are not included.

A project-wide open-source license has not been selected. Public availability does not grant unrestricted reuse. [Third-party notices](THIRD-PARTY-NOTICES.md) apply to their respective components, not to the entire project.

Compatibility with Instagram’s terms for this interface-modification approach still needs separate review. Publication is not a guarantee of legal clearance or Meta approval.

Feedback: [GitHub Issues](https://github.com/qmfvkn/contact-only/issues). Please do not post passwords, authentication codes or private conversations.
