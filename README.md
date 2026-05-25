<div align="center">

<img src="docs/icon.svg" width="120" alt="Rep Attack icon" />

<img src="docs/title.svg" width="420" alt="Rep Attack" />

**A clean, offline gym tracker for Android — with a Wear OS companion app.**

Plan your workouts, log every set in seconds, and watch your numbers go up.
No accounts. No ads. No tracking. Ever.

![Android](https://img.shields.io/badge/Android-7%2B-3DDC84?logo=android&logoColor=white)
![Wear OS](https://img.shields.io/badge/Wear%20OS-3%2B-4285F4?logo=wearos&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-Expressive-6750A4?logo=materialdesign&logoColor=white)
[![License](https://img.shields.io/badge/License-GPL%20v3-A42E2B?logo=gnu&logoColor=white)](LICENSE)

<a href="https://github.com/yashbellam97/RepAttack/releases/latest">
  <img alt="Download APK" src="https://img.shields.io/badge/Download-APK-181717?logo=github&logoColor=white&style=for-the-badge" />
</a>

</div>

---

## Why Rep Attack?

Most gym apps want your email, your subscription, and your attention. Rep Attack just wants to help you lift.

- 🏋️ **Built for the gym floor.** Big buttons, plus/minus steppers, one-tap set completion — log a full session without ever touching the keyboard.
- 📈 **See your progress.** Per-exercise charts for max weight and volume make plateaus and PRs obvious at a glance.
- 📅 **Plan ahead.** Group exercises into workouts and workouts into programs. Edit workouts or switch programs anytime without losing exercise history.
- ⌚ **Glance at your routine.** A companion Wear OS app shows your workout on your wrist.
- 🔒 **Private by design.** Everything is stored locally. Back up and restore via JSON whenever you want.
- 🎨 **Looks great.** Material 3 Expressive, dynamic color on Android 12+, and spring-physics motion throughout.
- 📳 **Feels great.** Tuned haptic feedback on every meaningful action — set completion, swipes, drag handles, delete confirmations.

---

## Screenshots

<div align="center">

### On your phone

<table>
  <tr>
    <td><img src="docs/screenshots/workouts.png" width="240" /></td>
    <td><img src="docs/screenshots/log-session.png" width="240" /></td>
    <td><img src="docs/screenshots/stats.png" width="240" /></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/workout-detail.png" width="240" /></td>
    <td><img src="docs/screenshots/settings.png" width="240" /></td>
    <td><img src="docs/screenshots/programs.png" width="240" /></td>
  </tr>
</table>

### On your wrist

<table>
  <tr>
    <td><img src="docs/screenshots/wear-list.png" width="200" /></td>
    <td><img src="docs/screenshots/wear-detail.png" width="200" /></td>
    <td><img src="docs/screenshots/wear-detail-scroll.png" width="200" /></td>
  </tr>
</table>

</div>

---

## Building from source

Want to hack on it or build your own APK? You'll need a recent Android Studio and JDK 11.

```bash
git clone https://github.com/yashbellam97/RepAttack.git
cd RepAttack
./gradlew :app:installDebug    # phone app
./gradlew :wear:installDebug   # wear os companion
```

<details>
<summary><b>Tech stack (for the curious)</b></summary>

- **Kotlin** 2.1 + **Jetpack Compose** with **Material 3 Expressive**
- **Room** for the local database (MVVM, `StateFlow` everywhere)
- **Vico** for charts
- **sh.calvin.reorderable** for drag-and-drop
- **kotlinx.serialization** for the backup format
- **Wear Data Layer** for phone → watch sync

Modules:

- `app/` — phone app
- `shared/` — DTOs shared with the watch
- `wear/` — Wear OS companion

</details>

---

## License

Rep Attack is released under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for the full text.

In short: you're free to use, modify, and distribute the app, but any distributed fork must also be open source under the same license. This keeps Rep Attack — and any version of it that reaches users — free forever.
