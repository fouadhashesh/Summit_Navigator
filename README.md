# Summit Navigator

Summit Navigator is a premium, offline-first Android application designed for tech conferences and summits. It features a modern, beautifully spaced UI, tiered user access (Admin, VIP, Attendee), and a seamless offline caching architecture using Room and Firebase.

## Core Features
- **Offline-First Room Caching**: Robust local database caching that instantly syncs with Firestore, allowing attendees to view the schedule and their personalized agenda entirely offline.
- **Firebase Auth & Tiered Roles**: Secure authentication with role-based access control. Admins can create, edit, and delete sessions dynamically, while VIPs get exclusive access to premium sessions.
- **Live FCM Announcements**: Receive real-time push notifications and announcements from Firebase Cloud Messaging (FCM).
- **Skeleton Loading UI & Empty States**: Beautifully polished visual experience utilizing custom skeleton screens for loading and intuitive empty states for unpopulated views.

## Setup Instructions

### Prerequisites
- **Minimum SDK**: API 26 (Android 8.0+)
- **IDE**: Android Studio (Latest)
- **Language**: Kotlin

### Installation
1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. **MANDATORY**: Add the `google-services.json` file.
   - Go to your Firebase Console -> Project Settings -> General.
   - Download the `google-services.json` file.
   - Place this file directly inside the `app/` directory of the project.
4. Sync your Gradle project files.
5. Build and run the application on an emulator or physical device running Android 8.0+.

## API Keys & Secrets

> [!WARNING]
> **NEVER commit real API keys or secrets to version control.**

*Placeholder for any 3rd party API keys (e.g., Maps, Analytics)*
- `API_KEY_PLACEHOLDER_1` = `[REPLACE_ME]`
- `API_KEY_PLACEHOLDER_2` = `[REPLACE_ME]`

Ensure that any sensitive keys are stored securely in `local.properties` and referenced dynamically in your `build.gradle` file.

---
*Built with Kotlin Coroutines, StateFlow, Room, and Firebase.*
