# Safety 4 Women

Safety 4 Women is an Android app concept for personal journeys and trusted companion support. The screens follow the supplied Figma onboarding direction and Part 1 requirements. The project contains a Kotlin/Jetpack Compose app, an Express REST API, a PostgreSQL schema, API tests, and a GitHub Actions workflow.

## Part 2 feature checklist

| Requirement | Current implementation | Remaining work |
|---|---|---|
| Sign-in and password protection | Account registration and login use the REST API. The API stores bcrypt hashes and issues 30-minute JWT access tokens. Tokens remain in app memory. | Deploy the API/database, configure the app URL, test password reset and account deletion. Google sign-in is not configured. |
| Settings | Account language and high-contrast preference endpoints; English/isiZulu selector and sign-out screen. | Only onboarding is translated into isiZulu. High contrast is stored but is not yet applied to every screen. |
| REST API and database | Express routes, PostgreSQL schema, authenticated user-scoped operations for journeys, incidents, settings, trusted contacts, and companions. | Create the hosted service/database and apply `server/db/schema.sql`. This repository does not include a live server or database. |
| Professional UI | Compose onboarding, account forms, home, journey planner, companion management, safety tiers, emergency options, settings, and activity screens. | Exercise on the target phone, refine accessibility and layouts, and capture evidence for the assignment. |
| Three user-defined features | (1) journey planning with saved destination/ETA/transport; (2) invitation code, explicit acceptance, optional journey/check-in permissions, revocation, and companion journey view; (3) Check-in/Alert/SOS records with resolution API and local SMS draft/emergency dialer options. | Safe-arrival reminders, overdue/deviation detection, notification delivery, companion check-in visibility, and companion escalation are not implemented. |
| Source, README, tests, CI | This is the complete source project. API tests are included. GitHub Actions runs API tests plus Android unit tests and a debug build. | Publish to the student’s GitHub repository and confirm the Actions run there. Android CI has not been run from this environment. |
| Demonstration video | Suggested script below. | Record on a phone with narration and add the video URL here before submission. |

The Part 1 design also lists live location, FCM notifications, offline Room/WorkManager sync, full bilingual translation, and more extensive incident and privacy audit workflows. Those are not claimed as working features in this build. In particular, saving an SOS record does not send an SOS; a message draft is not sent until the user sends it; an emergency dialer shortcut does not place a call automatically.

## Android app setup

1. Open this project folder in Android Studio and allow Gradle sync (JDK 21 and Android SDK 36).
2. Deploy/configure the API as described below before using account features.
3. Set the hosted HTTPS API URL as a Gradle user property on the development computer. On Windows, add this line to `%USERPROFILE%\.gradle\gradle.properties`:

   ```properties
   SAFETY_API_BASE_URL=https://YOUR_API_HOST/
   ```

   The app intentionally rejects non-HTTPS API URLs. The default build URL is a reserved `.invalid` placeholder; account actions will not work until this is configured.
4. Sync Gradle again, run the `app` configuration on a phone/emulator, create a primary account and a separate companion account, then try the journeys below.

Passwords must contain at least 10 characters and at most 72 UTF-8 bytes. The app keeps the access token in memory and asks the user to sign in again after the session is lost or expires.

## API setup and deployment

The backend requires Node.js 22+, PostgreSQL, and a random `JWT_SECRET` containing at least 32 bytes.

```powershell
cd server
Copy-Item .env.example .env
# Edit .env with DATABASE_URL and JWT_SECRET. Never commit .env.
pnpm install --frozen-lockfile
psql "$env:DATABASE_URL" -f db/schema.sql
pnpm start
```

The API listens on port 8080 by default. `GET /health` is a liveness response. For a hosted deployment, create a Node web service and managed PostgreSQL database with your chosen provider, set `DATABASE_URL`, `JWT_SECRET`, and `PORT` as service environment variables, then apply the schema once. Configure `SAFETY_API_BASE_URL` in Android Studio to the service's HTTPS address. Keep all secrets in the hosting provider’s secret settings.

### Main API routes

| Route | Purpose |
|---|---|
| `POST /v1/auth/register`, `POST /v1/auth/login` | Create account and start session |
| `GET /v1/me`, `GET/PATCH /v1/settings` | Current user and preferences |
| `GET/POST /v1/journeys`, `PATCH /v1/journeys/:id` | Own journey plans |
| `GET/POST /v1/incidents`, `PATCH /v1/incidents/:id/resolve` | Safety-event history and resolution |
| `GET/POST/DELETE /v1/contacts` | Own trusted phone contacts |
| `POST /v1/companions/invitations`, `POST /v1/companions/accept` | Create/accept an email-bound, expiring companion invitation |
| `GET/DELETE /v1/companions`, `GET /v1/companions/journeys` | Inspect/revoke consent links and view permitted active journeys |

Protected routes use `Authorization: Bearer <accessToken>`. The invitation endpoint returns a one-time code for the primary user to share privately; the app does not send email/SMS. Only the matching companion account can accept. API event creation returns `delivery: "recorded_only"` because push/SMS notification delivery is not configured.

## Tests and GitHub Actions

From `server/`, run `pnpm install --frozen-lockfile` and `pnpm test`. The eight API tests cover bcrypt hashing in registration, validation, authentication requirements, user-ID spoofing rejection, journey/incident creation, settings retrieval, malformed JSON, invitation-token hashing, and role-gated invitation acceptance. The GitHub Actions workflow also runs `testDebugUnitTest assembleDebug` for Android. Run the workflow after pushing the source to GitHub and fix any failures before submission.

## Suggested demonstration recording

Record on a real Android phone with voice-over. Show the app launch, English/isiZulu selector, account registration, companion account registration, invitation creation and acceptance, settings, journey creation and the companion’s permitted view, each safety tier, an emergency dialer option, the server/database records, and a rejected invalid form. State that SMS drafts need a manual send and that live tracking and notification delivery are not enabled. Do not show real people’s contact details or passwords.

## Assignment mapping and status

| Part 1 item | Evidence in the current build |
|---|---|
| Primary/Companion roles | Account role chosen at registration and enforced for companion invite actions |
| Explicit companion consent | Email-bound invitation code, 72-hour expiry, matching-account acceptance, permission toggles, primary-user revocation |
| Safe journey | Destination, ETA, transport notes; stored through authenticated API and visible to accepted companions with journey permission |
| Three safety levels | Check-in, Alert, SOS records; SMS drafts for check-in/help and emergency number dialer options |
| Incident resolution | Resolution endpoint; “I’m safe” closes the SOS screen. Companion resolution workflow is not implemented. |
| Settings | Language preference, high-contrast preference, sign-out |
| Authentication and API | bcrypt password hashing, JWT-protected PostgreSQL operations, input validation and API tests |

## Before calling this production-ready

Deploy and exercise the service, run Android CI and device testing, finish the full isiZulu translation and high-contrast behavior, persist trusted contacts in the UI, show API-backed incident history, and implement safe-arrival reminders and actual companion notifications. Review location consent, privacy policy, data deletion/export, retention, security, and South African emergency contact details before handling real safety data. This assignment build is not a substitute for emergency services.

## Use of AI

If AI assistance is used in the submission, follow the course’s disclosure rules. Cite the tools and describe which code, debugging, or documentation tasks were assisted; do not claim unverified requirements or test results.
