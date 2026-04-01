# Cultivation

Advanced Android application built for intelligent cultivation workflows with modern mobile architecture, on-device ML capabilities, cloud sync, and robust data visualization.

## Overview

Cultivation is a native Android project that combines practical field-oriented workflows with a scalable engineering foundation. It emphasizes clean architecture, responsive UI behavior, and real-time cloud-backed user experiences.

The application demonstrates production-style mobile development with integrated Firebase services, device capabilities, and analytics-ready modules.

## Key Capabilities

- Clean, modular Android architecture for maintainable feature growth.
- Cloud-backed authentication and data persistence with Firebase.
- Location-aware functionality with Google Play Services Location.
- Camera-based input workflows via CameraX.
- On-device inference support through LiteRT dependencies.
- Data charts and trend visualization using MPAndroidChart.
- Local persistence patterns with Room.
- Network API consumption with Retrofit + Gson.

## Technology Stack

- Language: Java/Kotlin-compatible Android ecosystem
- SDK: Android SDK 36 (min SDK 26)
- Build: Gradle Kotlin DSL
- UI: Material Components, AppCompat, ConstraintLayout
- Cloud: Firebase Auth, Firestore, Storage
- Device APIs: CameraX, Location Services
- ML Runtime: LiteRT support + GPU + select TF ops
- Data: Room, Retrofit, Gson
- Visualization: MPAndroidChart

## Architecture

The project follows a clean architecture style with separation across:

- Presentation Layer: Activity/Fragment UI and user interactions
- Domain Layer: Core app logic and business rules
- Data Layer: Cloud/local data sources and repositories

Design goals:

- Easy testing and extension of feature modules
- Low coupling between UI and data operations
- Stable performance under asynchronous workloads

## Project Structure

```
CULTIVATION/
|- app/
|  |- src/main/
|  |- build.gradle.kts
|  |- google-services.json
|- gradle/
|- build.gradle.kts
|- settings.gradle.kts
|- README.md
```

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 11+
- Firebase project setup

### Setup

1. Clone the repository.
2. Open in Android Studio.
3. Ensure `app/google-services.json` is configured.
4. Sync Gradle dependencies.
5. Run on device/emulator.

## Build and Run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Why This Project Stands Out

- Demonstrates modern Android engineering beyond simple CRUD apps.
- Blends cloud, local, analytics, and device-native capabilities.
- Shows ability to build scalable mobile systems with clean design.
- Recognized at Smart India Hackathon for innovation and practical impact.

## Author

Krishna Choudhary

## Repository

https://github.com/Byte-Maste/Cultivation

