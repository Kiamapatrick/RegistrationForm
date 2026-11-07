# Registration Form

A modern Android registration form built with **Java** and **Material Design 3** components. This app demonstrates best practices for form validation, date picking, ViewModel-based state management, and accessibility.

## Features

- **Name Field**: Full name input with person icon, minimum 2 letters, only letters/spaces/hyphens/apostrophes
- **Date of Birth Field**: Calendar picker using MaterialDatePicker (past dates only, max 18 years ago)
- **Email Field**: Email input with email icon and RFC 5322 format validation
- **Password Field**: Minimum 8 chars, at least 1 letter and 1 digit, no edge spaces, not a common password; strength indicator (Weak/Fair/Strong)
- **Confirm Password Field**: Must match password
- **Smart Button State**: "Next" button enables only when all fields are valid
- **Summary Screen**: Review entered data (name, DOB, email) before confirmation; password never displayed
- **Success State**: Confirmation card with Done button that clears the form
- **Accessibility**: Content descriptions for all interactive elements; error messages announced via TextInputLayout; password strength read by TalkBack
- **Dark Mode**: Full `values-night` color palette with WCAG AA contrast
- **Localization**: English (default) and Swahili (`values-sw`) — *Swahili translation is a first draft awaiting native-speaker review*

## Screenshots

> **[SCREENSHOTS PLACEHOLDER — ADD IMAGES HERE]**
> - Registration form (light & dark)
> - Date picker dialog
> - Summary screen
> - Success state

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java (Android) |
| Min SDK | 24 (Android 7.0 Nougat) |
| Compile / Target SDK | 36 (Android 14) |
| Build System | Gradle (Kotlin DSL) with version catalog (`gradle/libs.versions.toml`) |
| UI Framework | Material Design 3 / Jetpack (AppCompat, ConstraintLayout) |
| Date Picker | `com.google.android.material:material-datepicker` (via `espresso-contrib`) |
| Architecture | Single Activity (`MainActivity`) + `SummaryActivity`; `RegistrationViewModel` with LiveData; pure-Java validation/formatter units |

## Project Structure

```
RegistrationForm/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/registrationform/
│   │   │   ├── FormValidator.java           # Pure Java validation rules (no Android deps)
│   │   │   ├── PasswordStrength.java        # Pure Java password scoring
│   │   │   ├── RegistrationData.java        # Parcelable data holder for summary
│   │   │   ├── RegistrationFormatter.java   # Locale-aware date formatting
│   │   │   ├── RegistrationViewModel.java   # ViewModel with LiveData for form state
│   │   │   ├── MainActivity.java            # Form screen, input handling, date picker
│   │   │   └── SummaryActivity.java         # Review / confirm / success screen
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml        # Registration form layout
│   │   │   │   └── activity_summary.xml     # Summary / success layout
│   │   │   ├── values/
│   │   │   │   ├── strings.xml              # English strings
│   │   │   │   ├── colors.xml               # Light theme colors
│   │   │   │   ├── dimens.xml               # Spacing, sizes, radii
│   │   │   │   ├── styles.xml               # Custom styles
│   │   │   │   └── themes.xml               # Light theme
│   │   │   ├── values-night/
│   │   │   │   ├── colors.xml               # Dark theme colors
│   │   │   │   └── themes.xml               # Dark theme
│   │   │   ├── values-sw/
│   │   │   │   └── strings.xml              # Swahili strings (first draft)
│   │   │   ├── drawable/                    # Vector icons (person, email, lock, calendar, check)
│   │   │   └── xml/                         # Backup & data extraction rules
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml                   # Version catalog
│   └── wrapper/
├── .github/workflows/android.yml            # CI workflow
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- JDK 17 or later
- Android SDK 36

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Kiamapatrick/RegistrationForm.git
   cd RegistrationForm
   ```

2. Open in Android Studio:
   - File → Open → Select the `RegistrationForm` folder

3. Sync Gradle and run:
   - Click **Sync Project with Gradle Files**
   - Connect a device/emulator and click **Run** (▶)

## Validation Rules

| Field | Rules |
|-------|-------|
| **Full Name** | Required; min 2 letters (combining marks don't count); only Unicode letters, spaces, hyphens (`-`), apostrophes (`'`, `’`), and dots (`.`) |
| **Date of Birth** | Required; selected via MaterialDatePicker; must not be in the future; must be ≥ 18 years ago (max date = today − 18 years, in UTC) |
| **Email** | Required; RFC 5322 simplified regex (`local-part@domain`) |
| **Password** | Required; min 8 chars; ≥ 1 letter; ≥ 1 digit; no leading/trailing spaces; not in common-password list |
| **Confirm Password** | Required; must exactly match Password |

## Key Implementation Details

### Date Picker
Uses `MaterialDatePicker` with `DateValidatorPointBackward` capped at 18 years ago (UTC). Selected date stored as UTC milliseconds. Formatted as `dd MMM yyyy` (e.g., "15 Jan 1990" in English; month name localized to device locale, e.g., Swahili "15 Mac 1990").

### Validation Strategy
- Pure Java `FormValidator` (no Android dependencies) — unit-testable on JVM.
- Errors appear **after** user interacts (focus loss or typing).
- Real-time validation while typing (after first interaction).
- All fields validated on "Next" button click.
- Button enabled only when all fields pass validation.

### Architecture
- **FormValidator** – static validation methods returning `ValidationResult`; `Error` enum for all error types.
- **PasswordStrength** – scores password (0–100) → Level (WEAK/FAIR/STRONG) and progress value.
- **RegistrationViewModel** – holds field values, touched flags, LiveData for errors/strength/validity; survives rotation.
- **RegistrationFormatter** – `formatDob(Long utcMillis, Locale)` using `SimpleDateFormat("dd MMM yyyy", locale)` in UTC.
- **MainActivity** – binds UI to ViewModel; drives date picker; observes LiveData for errors/strength/button state.
- **SummaryActivity** – receives `RegistrationData` parcelable; shows review card, edit/confirm buttons, success state.

### Accessibility
- Every interactive element has `android:contentDescription`.
- `TextInputLayout` with `app:errorEnabled="true"` announces errors.
- Password strength container has `android:importantForAccessibility="yes"`, progress bar has `contentDescription`, label has `android:accessibilityLiveRegion="polite"`.
- Touch targets ≥ 48 dp (button height 56 dp).

### Dark Mode
Complete `values-night` palette (Material 3 tonal pairs). All text/background contrasts ≥ 4.5:1 (WCAG AA). Verified:
- Title (onSurface on background): 13.23:1
- Subtitle/Hint (onSurfaceVariant on background): 10.02:1
- Error text (error on surface): 10.00:1
- Strength label (onSurfaceVariant on surface): 10.02:1
- Button text (onPrimary on primary): 7.71:1

## Dependencies (from `gradle/libs.versions.toml`)

```toml
[versions]
agp = "8.12.3"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.8.0"
material = "1.14.0"
activity = "1.13.0"
constraintlayout = "2.2.2"
coreLibraryDesugaring = "2.0.4"
lifecycle = "2.8.4"
coreTesting = "2.2.0"
espressoIntents = "3.7.0"
espressoContrib = "3.7.0"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
espresso-intents = { group = "androidx.test.espresso", name = "espresso-intents", version.ref = "espressoIntents" }
espresso-contrib = { group = "androidx.test.espresso", name = "espresso-contrib", version.ref = "espressoContrib" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
core-library-desugaring = { group = "com.android.tools", name = "desugar_jdk_libs", version.ref = "coreLibraryDesugaring" }
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel", version.ref = "lifecycle" }
lifecycle-livedata = { group = "androidx.lifecycle", name = "lifecycle-livedata", version.ref = "lifecycle" }
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime", version.ref = "lifecycle" }
core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "coreTesting" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

## Building

### Debug APK
```bash
./gradlew assembleDebug
```

### Release APK (requires signing config in local.properties / keystore)
```bash
./gradlew assembleRelease
```

## Testing

```bash
# Unit tests (JVM, no device)
./gradlew testDebugUnitTest

# Instrumented tests (requires running emulator or device)
./gradlew connectedDebugAndroidTest
```

Instrumented tests cover:
- Next button state transitions
- Field validation (name, email, password, confirm, DOB)
- Rotation preserves all values
- Summary screen data integrity
- Edit details flow
- Confirm → success → Done clears form
- Keyboard Next/Done flow (focus order, invalid form blocks submit)

*Instrumented tests are not run in CI because they require an Android emulator. They are executed locally.*

## CI

GitHub Actions workflow (`.github/workflows/android.yml`) runs on every push and PR:
- Checkout, setup JDK 17 (Temurin), setup Gradle
- `./gradlew assembleDebug testDebugUnitTest lintDebug`
- Uploads Debug APK, unit test XML results, and Lint HTML report as artifacts

[![Android CI](https://github.com/Kiamapatrick/RegistrationForm/actions/workflows/android.yml/badge.svg)](https://github.com/Kiamapatrick/RegistrationForm/actions/workflows/android.yml)

## Future Improvements

- [x] Multi-step registration flow (SummaryActivity with edit/confirm)
- [x] ViewModel + LiveData for state management
- [x] Password field with strength indicator
- [x] Phone number field with country code picker (not implemented; DOB picker only)
- [x] Unit tests for validation logic (FormValidator, PasswordStrength, RegistrationFormatter, RegistrationViewModel)
- [x] UI tests with Espresso (14 tests)
- [x] Dark mode optimizations (full values-night palette, WCAG AA)
- [x] Localization support (Swahili `values-sw/strings.xml` — first draft)
- [ ] Additional localizations
- [ ] Automated screenshot tests

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Kiamapatrick** - [GitHub](https://github.com/Kiamapatrick)

---

*Built with ❤️ using Android Studio and Material Design 3*