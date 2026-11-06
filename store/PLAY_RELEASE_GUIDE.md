# Play release preparation

## Current package

- Public app name: Safety 4 Women
- Android package: `za.co.safety4women`
- Current source version: `1.1.0` (`versionCode` 2)
- Target SDK: 36
- Output for Play: Android App Bundle (`.aab`), not the debug APK used for phone testing.

## Create and keep the upload key private

Use Android Studio's **Build > Generate Signed Bundle / APK > Android App Bundle > Create new** flow, or `keytool` locally. Keep the keystore and passwords in a private backup. Never commit the keystore, passwords, signing properties, or service-account JSON. Google Play App Signing is recommended: retain the upload key securely and let Play manage the app-signing key.

The Gradle release variant reads these environment values when set:

```text
SAFETY4WOMEN_KEYSTORE
SAFETY4WOMEN_KEYSTORE_PASSWORD
SAFETY4WOMEN_KEY_ALIAS
SAFETY4WOMEN_KEY_PASSWORD
```

Build with `.\gradlew.bat bundleRelease`. Before uploading, verify the AAB is signed with the upload key and that package/version values match the Play Console app. Do not upload a locally generated unsigned artifact.

## Listing draft

**Short description (draft):** Plan safer journeys, check in with trusted people, and find South African emergency options.

**Full description (draft):** Safety 4 Women helps you plan a journey, keep trusted contacts, record a check-in or request for help, and open South African emergency call options. You can invite a companion and choose what journey and check-in information they may see. If push delivery is configured, a check-in, help alert, or SOS can notify an accepted companion who has permission. Message drafts and emergency calls remain under your control. The app does not track live location or contact emergency services automatically. English and isiZulu are available. Review this description against the final tested build and Play policy before publishing.

**Category suggestion:** Tools / Lifestyle (confirm the category that best matches the final app and Play Console choices).

## Account details still needed

The developer display name, public support email, privacy policy URL, content rating answers, Data safety form, target audience, and store screenshots must be entered or confirmed by the Play Console account owner. Do not publish this draft as a legal or final store statement without verifying the actual release behavior and privacy policy.

## Screenshot capture checklist

Capture from the final signed candidate on a physical phone. Include onboarding with both language options, registration/sign-in, home, journey planning, consent-based trusted people, check-in/alert, SOS options, settings in English and isiZulu, and (only after host delivery is configured) a companion push notification. Use test accounts and fictional contact details. Crop or obscure email addresses, phone numbers, invitation codes, notifications from other apps, and any real location data. Do not imply that an emergency service call or SMS was placed automatically.

## Privacy policy draft

See [`PRIVACY_POLICY_DRAFT.md`](PRIVACY_POLICY_DRAFT.md). Replace the owner/contact placeholders, verify Firebase and host retention settings, publish at a stable HTTPS URL, and use the same URL in Play Console before release.
