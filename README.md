# HackerTube for Android

An unofficial native Android client for [hackertube.org](https://hackertube.org), the collection of YouTube videos that Hacker News keeps recommending, created by Andrej Karpathy.

Built with Kotlin and Jetpack Compose. Video data, recommendations, categories, playlists, search and HN comments all come live from hackertube.org's public API, so the app shows the same content as the site.

## Features

- Personalised **For you** feed from your likes, "not interested" choices and watch history. No account: all state stays on the device.
- Longs, Recently recommended, Hidden gems, Categories and Playlists
- Search
- Watch page with YouTube's privacy-enhanced player, fullscreen, "Why Hacker News recommends it", HN comments and related videos
- Like, Not for me, Watch later, Share, Open in YouTube
- Mid-video frame thumbnails (switchable), dark / light / system theme

## Install

Download `HackerTube.apk` from the [latest release](../../releases/latest), open it on your phone and allow installs from that source. Requires Android 8.0 (API 26) or newer.

## Build from source

Requires JDK 17 and the Android SDK (platform 35).

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`. For a signed release build, create `signing/keystore.properties` with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`, then run `./gradlew assembleRelease`. The `signing/` folder is git-ignored. Without it, the release build falls back to the debug key.

## Notes

- The app needs internet access and depends on hackertube.org's API staying available and unchanged.
- This project is not affiliated with hackertube.org, YouTube, Google or Y Combinator. Hacker News comment data is licensed under the [Open Data Commons Attribution License](https://opendatacommons.org/licenses/by/1-0/).
- Videos are embedded through YouTube's player and remain subject to YouTube's terms.

## License

MIT, see [LICENSE](LICENSE). This covers the app's source code only, not the content it displays.
