# Safety 4 Women 1.0.0 — Part 3 development build

## Added in this iteration

- Firebase Authentication registration/login and signed-in session restoration.
- Cloud Firestore storage for profiles, settings, journey plans, incidents, trusted contacts and companion consent records.
- Firestore security rules and composite indexes for private user data and consent-limited journey sharing.
- Optimistic UI updates for offline journey/contact/incident writes; Firestore queues writes for synchronization after reconnection.
- Firebase Android Gradle plugin and product dependencies.

## Still required before final submission

- Publish `firestore.rules` and deploy `firestore.indexes.json` to Firebase project `opsc-5af15`.
- Validate sign-up/sign-in, all Firestore reads/writes, companion invitation/acceptance/revocation and offline synchronization on an Android phone.
- Firebase Cloud Storage uploads. The project remains on Spark; Cloud Storage needs Blaze billing or an instructor-approved alternative.
- Push-notification delivery, full isiZulu translation, Android CI verification, signed release bundle, store material and narrated demonstration video.

This development build has not been signed, published, or verified on a physical device.
