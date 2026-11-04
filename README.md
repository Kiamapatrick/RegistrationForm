# Registration Form

A modern Android registration form built with **Kotlin** (using Java for this version) and **Material Design 3** components. This app demonstrates best practices for form validation, date picking, and accessibility.

## Features

- **Name Field**: Full name input with person icon, minimum 2 characters validation
- **Date of Birth Field**: Calendar picker using MaterialDatePicker (past dates only)
- **Email Field**: Email input with email icon and format validation
- **Real-time Validation**: Fields validate on focus loss and as user types (after first interaction)
- **Smart Button State**: "Next" button enables only when all fields are valid
- **Accessibility**: Content descriptions for all interactive elements
- **Material Design 3**: Uses Material Components for modern, consistent UI

## Screenshots

*(Add screenshots here when available)*

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java (Android) |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 34 (Android 14) |
| Build System | Gradle (Kotlin DSL) |
| UI Framework | Material Design 3 / Jetpack |
| Date Picker | `com.google.android.material:material-datepicker` |
| Architecture | Single Activity with ViewBinding |

## Project Structure

```
RegistrationForm/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/registrationform/
│   │   │   └── MainActivity.java          # Main activity with form logic
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml      # Registration form layout
│   │   │   ├── values/
│   │   │   │   ├── strings.xml            # String resources
│   │   │   │   ├── colors.xml             # Color definitions
│   │   │   │   ├── dimens.xml             # Dimension resources
│   │   │   │   ├── styles.xml             # Custom styles
│   │   │   │   └── themes.xml             # App themes
│   │   │   ├── drawable/                  # Vector icons (person, email, calendar)
│   │   │   └── xml/                       # Backup & data extraction rules
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml                 # Version catalog
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

## Getting Started

### Prerequisites

- Android Studio Koala (2024.1.1) or later
- JDK 17 or later
- Android SDK 34

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
| **Full Name** | Required, minimum 2 characters |
| **Date of Birth** | Required, must be a past date (not future) |
| **Email** | Required, valid email format |

## Key Implementation Details

### Date Picker
Uses `MaterialDatePicker` with `DateValidatorPointBackward.now()` to restrict selection to past dates only. The selected date is stored as UTC milliseconds and formatted as `dd MMM yyyy` (e.g., "15 Jan 1990").

### Validation Strategy
- Errors appear **after** user interacts with a field (focus loss or typing)
- Real-time validation while typing (after first interaction)
- All fields validated on "Next" button click
- Button enabled only when all fields pass validation

### Accessibility
- All interactive elements have `contentDescription`
- Proper `inputType` for each field (textPersonName, textEmailAddress, etc.)
- Semantic layout with proper labeling

## Dependencies (from `gradle/libs.versions.toml`)

```toml
[versions]
agp = "8.5.0"
kotlin = "1.9.22"
coreKtx = "1.13.1"
appcompat = "1.7.0"
material = "1.12.0"
activity = "1.9.0"
constraintlayout = "2.1.4"
junit = "4.13.2"
espresso = "3.5.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
google-material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
```

## Building

### Debug APK
```bash
./gradlew assembleDebug
```

### Release APK (requires signing config)
```bash
./gradlew assembleRelease
```

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Future Improvements

- [ ] Add multi-step registration flow (additional screens)
- [ ] Implement ViewModel + LiveData/Flow for state management
- [ ] Add password field with strength indicator
- [ ] Add phone number field with country code picker
- [ ] Unit tests for validation logic
- [ ] UI tests with Espresso
- [ ] Dark mode optimizations
- [ ] Localization support (add more `values-*/strings.xml`)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Kiamapatrick** - [GitHub](https://github.com/Kiamapatrick)

---

*Built with ❤️ using Android Studio and Material Design 3*