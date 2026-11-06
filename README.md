# Safety 4 Women — Part 3

Safety 4 Women is a Kotlin/Jetpack Compose Android safety-planning app for South African users. This repository contains the complete Android source, Firebase configuration templates/rules, a Node.js/PostgreSQL REST API, tests, CI workflow, and Play release preparation material.

## Implemented in the app

- Firebase Authentication with email/password registration and sign-in. Passwords are handled by Firebase Authentication and are not stored by the app.
- Google single sign-on through Android Credential Manager and Firebase Auth. Enable the Google provider, register the app's SHA-1 and SHA-256 fingerprints, and download a refreshed `google-services.json` containing a Web OAuth client ID before testing this path.
- Firestore profile, journey, safety incident, contact, and consent-based companion data. Firestore's Android offline persistence queues writes and syncs them when connectivity returns.
- English and isiZulu interface copy, including settings, safety flow, validation, emergency explanations, and status messages. The translation is a project draft and must be reviewed by a fluent isiZulu speaker before publication.
- South African emergency dialler shortcuts, trusted contacts, journey planning, companion invitations/acceptance/revocation, and activity history. Calls and SMS are not placed or sent automatically.
- FCM token registration and Android notification display. Recording a check-in/help alert/SOS can request a push to an accepted companion only when the hosted API is configured and the companion granted the check-in/alert permission. SMS remains a user-reviewed draft.
- A private, server-side push sender. The Android client sends its Firebase ID token to `/v1/notifications/companion-alert`; the service validates the token, checks Firestore consent, and sends a minimal FCM alert. Firebase service-account credentials belong only in the host's private environment settings.
- Release version `1.1.0` / version code `2`; release signing is enabled only when all four `SAFETY4WOMEN_*` signing environment variables are provided.

## Firebase setup and plan constraints

The Android package is `za.co.safety4women`; Firebase project ID is `opsc-5af15`; Firestore is in `africa-south1`. Put your downloaded `google-services.json` at `app/google-services.json`. It is ignored by Git and must not be committed. Email/password and Firestore are set up in the Firebase project. The Firestore rules and indexes are in `firestore.rules` and `firestore.indexes.json`; deploy changes with:

```powershell
firebase login
firebase --project opsc-5af15 deploy --only firestore:rules,firestore:indexes
```

The project is intentionally staying on Firebase Spark. Firebase Cloud Storage uploads are not available on this plan, so the assignment's blob-storage learning outcome is not yet met. Do not enable billing without the account owner's decision. No app-side upload feature is claimed as working.

## API and push deployment

The existing Express API uses PostgreSQL for its REST resources and requires private `DATABASE_URL`, `JWT_SECRET` (at least 32 bytes), and `PORT`. Apply `server/db/schema.sql` to that PostgreSQL database. To enable FCM delivery, also set private host variables `FIREBASE_PROJECT_ID=opsc-5af15` and `FIREBASE_SERVICE_ACCOUNT_JSON` (the complete service-account JSON). Do not put a service-account key in Android, GitHub source, screenshots, or this README. Use a host supporting HTTPS Node services and PostgreSQL. Health check: `GET /health` should return `{"status":"ok"}`.

Set the public API root for Android builds with a Gradle property, for example:

```powershell
.\gradlew.bat assembleDebug -PsafetyApiBaseUrl=https://YOUR-API-HOST
```

The current Android app uses this REST endpoint for companion push requests; its account/profile/journey/incident data remains in Firestore. The PostgreSQL REST endpoints are available separately. A deployed host URL and service-account secret are required for end-to-end push delivery; source code alone cannot deliver notifications.

## Build, test, and CI

Requirements: JDK 21, Android SDK 36, and network access for Gradle/Maven dependencies. In Android Studio, sync Gradle, then run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

The repository's GitHub Actions workflow runs Android unit tests/debug assembly and the API tests on pushes and pull requests. It reads `GOOGLE_SERVICES_JSON` from a repository Actions secret. The secret must contain the exact JSON file contents; never paste service-account credentials there unless a workflow specifically needs them (this workflow only needs the Android Firebase config).

## Play Store preparation

See [`store/PLAY_RELEASE_GUIDE.md`](store/PLAY_RELEASE_GUIDE.md) and [`store/PRIVACY_POLICY_DRAFT.md`](store/PRIVACY_POLICY_DRAFT.md). The Gradle release build reads signing values from these private environment variables: `SAFETY4WOMEN_KEYSTORE`, `SAFETY4WOMEN_KEYSTORE_PASSWORD`, `SAFETY4WOMEN_KEY_ALIAS`, and `SAFETY4WOMEN_KEY_PASSWORD`. Create and back up your upload key locally; never commit it or send it in chat. The guide includes draft store listing copy and a screenshot checklist. Add publisher identity, support contact, privacy-policy URL, verified data-retention details, screenshots, and signed AAB/APK evidence before submission.

A signed production artifact, Play Console upload, physical-device acceptance test, and narrated phone demonstration still require the account owner's local key, Play Console access, phone, and recording. The current privacy draft is not a published policy. The app does not yet implement Firebase Blob Storage, account deletion/export, or comprehensive retention controls.

## Part 3 evidence checklist

For the demonstration, use a physical Android phone; show registration/login and Google SSO, settings, invalid-input handling, offline write followed by reconnection/sync, a consented companion notification after the API is deployed, the hosted API health endpoint and PostgreSQL data, and both languages. Explain clearly that the app does not share live location or automatically place calls/SMS. Include professional narration and add the video link, screenshots, release notes, GitHub Actions result, and final `final PoE` tag to the submission evidence. Never use real sensitive contact details in screenshots.

## Feedback and references

Review the isiZulu safety wording with a fluent speaker. Firebase, Android Credential Manager, and FCM implementation details follow their official Android/Firebase documentation. Keep comments near security-sensitive rules and server-side token validation when extending the app.
