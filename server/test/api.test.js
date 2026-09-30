import request from "supertest";
import { beforeEach, describe, expect, it } from "vitest";
import { createApp } from "../src/app.js";

const user = { id: "11d5db7a-73dd-4a11-aaf5-cdecc3f9ff12", email: "member@example.org", display_name: "Test Member", role: "PRIMARY" };
let pool;
let app;
let insertedPasswordHash;
let insertedInvitationHash;

beforeEach(() => {
  insertedPasswordHash = undefined;
  insertedInvitationHash = undefined;
  pool = { query: async (sql, params = []) => {
    if (sql.startsWith("INSERT INTO users")) { insertedPasswordHash = params[2]; return { rows: [{ ...user, role: params[3] }] }; }
    if (sql.startsWith("SELECT id,email,display_name,role,password_hash")) return { rows: [{ ...user, password_hash: "$2a$12$fakehash" }] };
    if (sql.startsWith("INSERT INTO user_settings")) return { rows: [{ language: "en", high_contrast: false }] };
    if (sql.startsWith("SELECT language,high_contrast")) return { rows: [{ language: "zu", high_contrast: true }] };
    if (sql.startsWith("INSERT INTO journeys")) return { rows: [{ id: "bbedb3a3-3984-4f8b-92a8-3642947db9bc", destination: "Home", expected_arrival_minutes: 45, transport_details: null, status: "ACTIVE" }] };
    if (sql.startsWith("INSERT INTO incidents")) return { rows: [{ id: "a1aa93fe-350a-48c1-b257-124b26347719", level: "SOS", status: "ACTIVE" }] };
    if (sql.startsWith("INSERT INTO companion_invitations")) { insertedInvitationHash = params[2]; return { rows: [] }; }
    if (sql.startsWith("WITH accepted AS")) return { rows: [{ id: "66b42149-e901-4a82-95a8-7c45604915da", primary_user_id: user.id, allow_journeys: true, allow_check_ins: false }] };
    if (sql.startsWith("UPDATE incidents")) return { rows: [{ id: "a1aa93fe-350a-48c1-b257-124b26347719", status: "RESOLVED" }] };
    return { rows: [], rowCount: 0 };
  } };
  app = createApp({ pool, jwtSecret: "test-secret-with-more-than-thirty-two-bytes-long" });
});

async function token(role = "PRIMARY", email = user.email) {
  const response = await request(app).post("/v1/auth/register").send({ name: user.display_name, email, password: "correct horse battery", role });
  return response.body.accessToken;
}

describe("authenticated API", () => {
  it("registers with a non-plaintext password flow and issues a short-lived token", async () => {
    const response = await request(app).post("/v1/auth/register").send({ name: user.display_name, email: user.email, password: "correct horse battery", role: "PRIMARY" });
    expect(response.status).toBe(201);
    expect(response.body.accessToken).toBeTruthy();
    expect(response.body.expiresInSeconds).toBe(1800);
    expect(JSON.stringify(response.body)).not.toContain("password");
    expect(insertedPasswordHash).toMatch(/^\$2[aby]\$/);
    expect(insertedPasswordHash).not.toBe("correct horse battery");
  });

  it("rejects protected requests without a token", async () => {
    expect((await request(app).get("/v1/journeys")).status).toBe(401);
    expect((await request(app).post("/v1/incidents").send({ userId: user.id, level: "SOS" })).status).toBe(401);
  });

  it("returns a clear 400 for malformed JSON", async () => {
    const response = await request(app).post("/v1/auth/login").set("Content-Type", "application/json").send("{not-json");
    expect(response.status).toBe(400);
    expect(response.body.error).toBe("Malformed JSON request body");
  });

  it("validates registration input and disallows caller-selected user IDs", async () => {
    expect((await request(app).post("/v1/auth/register").send({ name: "A", email: "nope", password: "short", role: "PRIMARY" })).status).toBe(400);
    const auth = await token();
    expect((await request(app).post("/v1/journeys").set("Authorization", `Bearer ${auth}`).send({ userId: user.id, destination: "Home", expectedArrivalMinutes: 45 })).status).toBe(400);
  });

  it("creates authenticated journeys and reports safety alerts as recorded only", async () => {
    const auth = await token();
    const journey = await request(app).post("/v1/journeys").set("Authorization", `Bearer ${auth}`).send({ destination: "Home", expectedArrivalMinutes: 45 });
    expect(journey.status).toBe(201);
    expect(journey.body.journey.status).toBe("ACTIVE");
    const incident = await request(app).post("/v1/incidents").set("Authorization", `Bearer ${auth}`).send({ level: "SOS" });
    expect(incident.status).toBe(201);
    expect(incident.body.delivery).toBe("recorded_only");
  });

  it("persists settings for the authenticated user", async () => {
    const auth = await token();
    const response = await request(app).get("/v1/settings").set("Authorization", `Bearer ${auth}`);
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ language: "zu", high_contrast: true });
  });

  it("creates an expiring companion invitation and stores only its hash", async () => {
    const auth = await token();
    const response = await request(app).post("/v1/companions/invitations").set("Authorization", `Bearer ${auth}`).send({ email: "friend@example.org", allowJourneys: true, allowCheckIns: false });
    expect(response.status).toBe(201);
    expect(response.body.delivery).toBe("manual_share_only");
    expect(response.body.expiresInHours).toBe(72);
    expect(response.body.invitationToken).toHaveLength(43);
    expect(insertedInvitationHash).not.toBe(response.body.invitationToken);
    expect(insertedInvitationHash).toHaveLength(64);
  });

  it("requires a companion account to accept a companion invitation", async () => {
    const primaryToken = await token();
    const forbidden = await request(app).post("/v1/companions/accept").set("Authorization", `Bearer ${primaryToken}`).send({ invitationToken: "x".repeat(43) });
    expect(forbidden.status).toBe(403);
    const companionToken = await token("COMPANION", "friend@example.org");
    const accepted = await request(app).post("/v1/companions/accept").set("Authorization", `Bearer ${companionToken}`).send({ invitationToken: "x".repeat(43) });
    expect(accepted.status).toBe(201);
    expect(accepted.body.companionLink.allow_journeys).toBe(true);
    expect(accepted.body.companionLink.allow_check_ins).toBe(false);
  });
});
