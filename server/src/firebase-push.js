import jwt from "jsonwebtoken";
import { createPublicKey, createSign } from "node:crypto";

const TOKEN_URL = "https://oauth2.googleapis.com/token";
const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging https://www.googleapis.com/auth/datastore";

/** Firebase Admin REST integration. Service-account material is supplied only by the host environment. */
export function createFirebasePush({ projectId, serviceAccount }) {
  if (!projectId || !serviceAccount?.client_email || !serviceAccount?.private_key) {
    throw new Error("Firebase project ID and service account are required for push delivery");
  }
  let accessToken;
  let accessTokenExpiry = 0;
  let firebaseKeys;
  let firebaseKeysExpiry = 0;

  async function adminAccessToken() {
    if (accessToken && Date.now() < accessTokenExpiry - 60_000) return accessToken;
    const now = Math.floor(Date.now() / 1000);
    const unsigned = `${Buffer.from(JSON.stringify({ alg: "RS256", typ: "JWT" })).toString("base64url")}.${Buffer.from(JSON.stringify({ iss: serviceAccount.client_email, scope: FCM_SCOPE, aud: TOKEN_URL, iat: now, exp: now + 3600 })).toString("base64url")}`;
    const signer = createSign("RSA-SHA256");
    signer.update(unsigned);
    const assertion = `${unsigned}.${signer.sign(serviceAccount.private_key, "base64url")}`;
    const response = await fetch(TOKEN_URL, {
      method: "POST", headers: { "content-type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer", assertion }),
    });
    if (!response.ok) throw new Error(`Firebase service authorization failed (${response.status})`);
    const data = await response.json();
    accessToken = data.access_token;
    accessTokenExpiry = Date.now() + Number(data.expires_in ?? 3600) * 1000;
    return accessToken;
  }

  async function firestore(path, options = {}) {
    const route = path.startsWith(":") ? path : `/${path}`;
    const response = await fetch(`https://firestore.googleapis.com/v1/projects/${encodeURIComponent(projectId)}/databases/(default)/documents${route}`, {
      ...options,
      headers: { authorization: `Bearer ${await adminAccessToken()}`, "content-type": "application/json", ...options.headers },
    });
    if (!response.ok) throw new Error(`Firebase data request failed (${response.status})`);
    return response.status === 204 ? null : response.json();
  }

  async function verifyIdToken(token) {
    if (!firebaseKeys || Date.now() >= firebaseKeysExpiry) {
      const response = await fetch("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com");
      if (!response.ok) throw new Error("Firebase token verification keys are unavailable");
      firebaseKeys = await response.json();
      firebaseKeysExpiry = Date.now() + 60 * 60 * 1000;
    }
    const key = firebaseKeys.keys.find((item) => item.kid === jwt.decode(token, { complete: true })?.header?.kid);
    if (!key) throw new Error("Unknown Firebase token signing key");
    const claims = jwt.verify(token, createPublicKey({ key, format: "jwk" }), { algorithms: ["RS256"], audience: projectId, issuer: `https://securetoken.google.com/${projectId}` });
    if (typeof claims.sub !== "string" || !claims.sub) throw new Error("Invalid Firebase token subject");
    return { uid: claims.sub };
  }

  async function sendCompanionAlert(uid, level, incidentId) {
    const profile = await firestore(`users/${encodeURIComponent(uid)}`);
    if (profile.fields?.role?.stringValue !== "PRIMARY") return { notified: false, sent: 0 };
    const query = {
      structuredQuery: {
        from: [{ collectionId: "companionLinks" }],
        where: { compositeFilter: { op: "AND", filters: [
          ["primaryUid", { stringValue: uid }], ["status", { stringValue: "ACTIVE" }], ["allowCheckIns", { booleanValue: true }],
        ].map(([field, value]) => ({ fieldFilter: { field: { fieldPath: field }, op: "EQUAL", value } })) } },
        limit: 11,
      },
    };
    const rows = await firestore(":runQuery", { method: "POST", body: JSON.stringify(query) });
    const companionUids = [...new Set((rows ?? []).map((row) => row.document?.fields?.companionUid?.stringValue).filter(Boolean))].slice(0, 10);
    const access = await adminAccessToken();
    const messages = [];
    for (const companionUid of companionUids) {
      const [profileData, tokens] = await Promise.all([
        firestore(`users/${encodeURIComponent(companionUid)}`),
        firestore(`users/${encodeURIComponent(companionUid)}/deviceTokens`),
      ]);
      const language = profileData.fields?.language?.stringValue ?? "en";
      const copy = notificationCopy(level, language);
      for (const tokenDoc of tokens.documents ?? []) {
        const token = tokenDoc.fields?.token?.stringValue;
        if (token) messages.push({ token, ...copy });
      }
    }
    let sent = 0;
    for (const item of messages) {
      const result = await fetch(`https://fcm.googleapis.com/v1/projects/${encodeURIComponent(projectId)}/messages:send`, {
        method: "POST", headers: { authorization: `Bearer ${access}`, "content-type": "application/json" },
        body: JSON.stringify({ message: { token: item.token, notification: { title: item.title, body: item.body }, data: { level, incidentId } } }),
      });
      if (result.ok) sent += 1;
      else if (result.status !== 404 && result.status !== 400) throw new Error(`Firebase notification delivery failed (${result.status})`);
    }
    return { notified: sent > 0, sent };
  }

  return { verifyIdToken, sendCompanionAlert };
}

function notificationCopy(level, language) {
  const copy = {
    CHECK_IN: ["Safety check-in", "A person who trusts you checked in. Please contact them if appropriate."],
    ALERT: ["Safety help requested", "A person who trusts you asked for support. Please contact them if appropriate."],
    SOS: ["SOS safety alert", "A person who trusts you activated SOS. Please contact them if appropriate."],
  }[level];
  const zu = {
    CHECK_IN: ["Ukuqinisekisa ukuphepha", "Umuntu okuthembayo uqinisekisile ukuthi uphephile. Mthinte uma kufanele."],
    ALERT: ["Kucelwe usizo lokuphepha", "Umuntu okuthembayo ucele ukwesekwa. Mthinte uma kufanele."],
    SOS: ["Isexwayiso se-SOS", "Umuntu okuthembayo uvule i-SOS. Mthinte uma kufanele."],
  }[level];
  const selected = language === "zu" ? zu : copy;
  return { title: selected[0], body: selected[1] };
}
