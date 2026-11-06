# Safety 4 Women privacy notice — draft for review

**Last updated:** 5 November 2026
**Publisher:** [Developer or organisation legal name]
**Contact:** [Public support email]
**Privacy policy URL:** [Publish this notice at a stable HTTPS address]

This draft describes the current source-code design. Confirm it against the deployed services, legal requirements, and the Play Data safety form before publication.

## Information the app handles

When you create an account, Firebase Authentication processes your email address and authentication credentials. The app stores your display name, account role, language and accessibility setting in Cloud Firestore. If you choose to add them, trusted-person names and phone numbers, journey destination/arrival/transport notes, check-in/help/SOS records, resolution status, companion permissions, and push-notification device tokens are also stored. Companion invitations contain the invitee email, access choices, status, and expiry. An accepted companion can see only the information allowed by the active permission and published Firestore rules.

The app does not request device location and does not track live location. It opens the phone dialler for emergency numbers, and opens a message draft for a selected trusted contact. It does not place calls or send SMS messages automatically. If the publisher configures the hosted notification API and the user has granted Android notification permission, recording a check-in, help alert, or SOS can send a minimal push notification to accepted companions who have consent to check-in/alert records. No contact receives a notification merely because it was saved.

## Service providers and transfers

Firebase Authentication, Cloud Firestore, and Firebase Cloud Messaging are Google/Firebase services. A separately hosted API may verify Firebase sign-in and deliver companion notifications. The publisher must list the actual API and blob-storage providers here, configure their regions and retention, and explain any international transfers before release. No blob upload feature is declared as operational in the current Spark configuration.

## Offline behavior

Firestore may cache account data and queue writes on the device for synchronization after reconnection. Keep the device protected and sign out on a shared device. Account creation and first sign-in require a network connection.

## Your choices and retention

You choose whether to add contacts, plan journeys, record safety events, invite companions, share information, grant notification permission, or use the dialler/message draft. You can revoke companion access in the app and withdraw Android notification permission in system settings. The publisher must add verified account deletion, data export, and retention/deletion instructions before a public release. Until those are implemented and tested, do not state that account deletion or export is available in the app.

## Security and children

Firebase Authentication manages passwords; the app does not store raw passwords. Firestore rules restrict records by account ownership and accepted companion permission. No online service can guarantee absolute security. The publisher must define the intended age group and complete Play Console target-audience/content-rating forms.

## Changes and contact

Update this notice when the app or service providers change. For privacy questions or requests, contact [public support email].
