# Release notes — Safety 4 Women 1.1.0

## Added for Part 3

- Firebase email/password account registration and sign-in with Firestore profile and safety data.
- Consent-based companion invitations, accepted/revocable links, trusted contacts, journeys, check-in/help/SOS records, and Firestore offline write synchronization.
- English and isiZulu interface selection, expanded across app screens, safety guidance, form validation, and status copy.
- Google single sign-on app flow using Credential Manager and Firebase Authentication; requires Google provider, registered app SHA fingerprints, and refreshed Firebase Android configuration.
- FCM token registration, Android 13+ notification permission request, and notification display service.
- An authenticated server endpoint that validates Firebase ID tokens, checks active companion consent, and sends push messages through FCM HTTP v1.
- Release version metadata and optional environment-based signing configuration, store listing draft, privacy-policy draft, and release evidence checklist.
- GitHub Actions Android build/unit-test and API test workflow.

## Release setup still required

- Deploy the Node/PostgreSQL API and apply `server/db/schema.sql`; configure its private database/JWT/Firebase service-account environment variables. Configure Android's `safetyApiBaseUrl` Gradle property for the hosted API.
- Firebase remains on Spark by user choice. Firebase Cloud Storage/blob upload is therefore unavailable and is not represented as implemented.
- Create/retain the upload signing key locally; provide publisher/support/privacy details, build and test a signed release artifact, and complete the Play Console process.
- Have a fluent isiZulu speaker review the safety translations, test the finished release on a physical phone, and record the required narrated demonstration.
