# Safety 4 Women — Part 3 project

Safety 4 Women is a Kotlin/Jetpack Compose Android app for personal journey planning and trusted companion support. The app now uses Firebase Authentication for accounts and Cloud Firestore for account profiles, settings, journeys, incidents, trusted contacts and consent-based companion links.

## Firebase project status

- Firebase project: `opsc-5af15` (OPSC).
- Android application ID: `za.co.safety4women`.
- Email/Password Authentication is enabled.
- The default Cloud Firestore database has been created in `africa-south1` (Johannesburg).
- `app/google-services.json` is present locally and is excluded from Git.
- The Android Firebase plugin and Auth/Firestore/Storage/Messaging SDKs are declared. Authentication and Firestore app flows are implemented.
- **Required next setup:** publish the included `firestore.rules` and deploy `firestore.indexes.json`. Until the rules are published, Firestore production mode denies app data reads and writes.
- The project is on Spark. Cloud Storage for Firebase is not usable on this plan; the profile/blob upload requirement is pending. The Firebase Storage SDK dependency is present but no upload UI/flow is claimed.
- Push notification delivery is not implemented. The messaging SDK is present only as setup groundwork.

## Features

- Firebase email/password account registration and login. Firebase Authentication manages password credentials; the app does not store passwords.
- Primary and companion roles; one-time, email-bound invitation codes with expiry, permission choices, explicit acceptance and revocation.
- Journey planning with expected arrival and transport details, plus consent-gated companion journey view.
- Trusted contacts, check-in/alert/SOS incident records, and resolution.
- Firestore Android offline cache and queued writes; local changes synchronize when the device reconnects. Initial account creation and first-time sign-in still require a network connection.
- English/isiZulu language selection. Only onboarding is currently translated into isiZulu.
- South African emergency dialler shortcuts. The app does not call automatically, send SMS automatically or send push notifications.

## Publish the Firestore rules

The database was created in production mode, which denies mobile client access until secure rules are published. From the project root, deploy the included rules and indexes using the Firebase CLI:

```powershell
npm install --global firebase-tools
firebase login
firebase --project opsc-5af15 deploy --only firestore:rules,firestore:indexes
```

Alternatively, copy `firestore.rules` into the Firebase Console under **Firestore → Rules**, then publish. The Firebase CLI also deploys the composite indexes defined in `firestore.indexes.json`. Never replace these rules with open/test-mode rules.

## Open and run the Android app

1. Open this folder in Android Studio.
2. Use JDK 21 and install Android SDK 36.
3. Confirm `app/google-services.json` exists locally, then sync Gradle.
4. Run the `app` configuration on an Android phone or emulator with Google Play services.

The Firebase configuration file contains project/app identifiers and is ignored by Git. For GitHub Actions, create an Actions secret named `GOOGLE_SERVICES_JSON` whose value is the full JSON file contents. Do not commit signing keys, service-account keys, or database credentials.

The included `server/` folder is the REST/PostgreSQL backend from the earlier project version. The current Android app uses Firebase Auth/Firestore directly, so it does not need a hosted REST API URL to run. The API source and tests remain available separately.

## Checks

Run Android tests and a debug build locally:

```powershell
./gradlew testDebugUnitTest assembleDebug
```

Run the retained REST API tests from `server/`:

```powershell
pnpm install --frozen-lockfile
pnpm test
```

GitHub Actions runs both checks. For Android CI, configure the `GOOGLE_SERVICES_JSON` repository secret first. A successful emulator/build check does not replace physical-phone testing.

## Remaining Part 3 work

- Firebase Cloud Storage blob upload. Firebase now requires the Blaze pay-as-you-go plan for Storage; this project remains on Spark, so uploads are blocked until the account owner chooses a billing plan or the lecturer accepts a different blob-storage service.
- FCM token registration, secure server-side notification delivery and notification handling.
- Full isiZulu translation, account deletion/export and privacy/retention controls.
- Test Firebase rules and all flows on a physical phone; run Android CI after dependencies can be downloaded.
- Final app screenshots, store listing/privacy materials, signed Android App Bundle, Play Console evidence and the required narrated phone demonstration video.

For safety, the app clearly describes local drafts and user-confirmed dialler actions; no emergency contact or companion receives an alert from a saved incident. Do not use an unverified build for real safety-critical use.

## Release notes — 1.0.0 Part 3 development build

- Registered Android app `za.co.safety4women` with Firebase project `opsc-5af15`.
- Added Firebase Authentication and Firestore for account and app data.
- Added consent-scoped Firestore rules and indexes for user records and companion links.
- Added Firestore-backed journeys, incidents, settings, contacts and companion consent flows with optimistic offline writes.
- Retained the existing Express/PostgreSQL source and its CI tests, while removing the app's dependency on a separately hosted API.
- Known release blockers are listed above; this is not a signed or Play Store-ready release.

## AI assistance disclosure

Complete this section according to course policy. Identify any AI tools used and describe the specific coding, debugging or documentation work they assisted with. Do not claim unverified tests or features.
