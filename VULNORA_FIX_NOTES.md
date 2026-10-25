# Vulnora scanner update

The selected profile uses one self-contained asset wordlist. Each platform-specific wordlist includes the general discovery list plus paths commonly associated with that platform. The scanner reads all unique, safe entries from that asset; there is no fixed 300-path cap.

The scanner filters absolute URLs, query and fragment characters, control characters, backslashes, encoded separators, and path-traversal forms from wordlists. It uses a small number of concurrent requests for stability, but still processes every retained wordlist entry.

HTTP 200 alone is not treated as a confirmed exposure. The target's base URL is displayed as a baseline row, while confirmed exposure checks look for specific content patterns such as readable environment values, Git metadata, database dumps, private-key headers, PHP information pages, and directory listings. Missing security headers are shown as low-severity recommendations rather than being counted as exposed paths.

The Home screen now displays the selected profile, progress, total tested paths, confirmed exposures, security recommendations, and every tested path without a UI row cap. A 200 response uses a neutral status style unless a confirmed exposure pattern is detected.

A full Gradle build was not possible in the editing environment because Gradle 9.6.0 was not cached, the environment could not reach services.gradle.org, and no Android SDK was installed. The scanner/model/repository sources and HomeViewModel were additionally checked with the installed Kotlin compiler and local dependency stubs; open the project in Android Studio and run `:app:assembleDebug` to complete the Android build check.
