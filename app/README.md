# app — Android application (PDM)

**Empty on purpose.** The PDM class has not yet confirmed the stack — **native Kotlin vs Expo** — and the
explicit decision was *not* to scaffold either guess in advance.

When it starts:

- **Kotlin path** → the Gradle project is rooted here (`app/` matches the standard Android module name), with
  `build/`, `.gradle/` and `local.properties` already ignored.
- **Expo path** → the Expo project root is here, `node_modules/` already ignored.

Working agreement for the mobile part: the agent cannot see a live emulator — review happens through
screenshots (sync folder + vision), and an `adb screencap` helper comes later.
