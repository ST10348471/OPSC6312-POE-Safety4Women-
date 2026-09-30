import bcrypt from "bcryptjs";
import cors from "cors";
import express from "express";
import jwt from "jsonwebtoken";
import { createHash, randomBytes } from "node:crypto";
import { z } from "zod";

const ISSUER = "safety4women-api";
const AUDIENCE = "safety4women-android";
const ACCESS_TOKEN_SECONDS = 30 * 60;

const registerInput = z.object({
  name: z.string().trim().min(2).max(80),
  email: z.string().trim().email().max(254),
  password: z.string().min(10).max(128).refine((value) => Buffer.byteLength(value, "utf8") <= 72, "Password must be 72 UTF-8 bytes or fewer"),
  role: z.enum(["PRIMARY", "COMPANION"]),
}).strict();
const loginInput = z.object({ email: z.string().trim().email().max(254), password: z.string().min(1).max(128) }).strict();
const journeyInput = z.object({ destination: z.string().trim().min(2).max(160), expectedArrivalMinutes: z.number().int().min(1).max(360), transportDetails: z.string().trim().max(300).nullable().optional() }).strict();
const incidentInput = z.object({ journeyId: z.string().uuid().nullable().optional(), level: z.enum(["CHECK_IN", "ALERT", "SOS"]), latitude: z.number().gte(-90).lte(90).nullable().optional(), longitude: z.number().gte(-180).lte(180).nullable().optional() }).strict().superRefine((data, context) => {
  if ((data.latitude == null) !== (data.longitude == null)) context.addIssue({ code: "custom", message: "Latitude and longitude must be supplied together", path: ["latitude"] });
});
const resolveInput = z.object({ status: z.enum(["RESOLVED", "CANCELLED"]).default("RESOLVED") }).strict();
const journeyUpdateInput = z.object({ status: z.enum(["COMPLETED", "CANCELLED"]) }).strict();
const settingsInput = z.object({ language: z.enum(["en", "zu"]).optional(), highContrast: z.boolean().optional() }).strict().refine((data) => Object.keys(data).length > 0, "At least one setting is required");
const contactInput = z.object({ name: z.string().trim().min(2).max(80), phone: z.string().trim().min(7).max(32), allowCheckIns: z.boolean().default(true), allowHelpMessages: z.boolean().default(true) }).strict();
const inviteInput = z.object({ email: z.string().trim().email().max(254), allowJourneys: z.boolean().default(true), allowCheckIns: z.boolean().default(true) }).strict();
const acceptInviteInput = z.object({ invitationToken: z.string().min(32).max(256) }).strict();

function validationFailure(res, error) {
  return res.status(400).json({ error: "Invalid request", details: error.flatten().fieldErrors });
}

export function createApp({ pool, jwtSecret, allowedOrigins = [] }) {
  if (!pool?.query) throw new Error("A database pool is required");
  if (typeof jwtSecret !== "string" || Buffer.byteLength(jwtSecret, "utf8") < 32) throw new Error("JWT_SECRET must contain at least 32 bytes");

  const app = express();
  app.disable("x-powered-by");
  app.use(cors({ origin: allowedOrigins.length ? allowedOrigins : false }));
  app.use(express.json({ limit: "32kb" }));

  const issueToken = (user) => jwt.sign({ role: user.role }, jwtSecret, {
    subject: user.id,
    issuer: ISSUER,
    audience: AUDIENCE,
    expiresIn: ACCESS_TOKEN_SECONDS,
  });

  const authenticated = (req, res, next) => {
    const [scheme, token, extra] = (req.get("authorization") ?? "").split(" ");
    if (scheme !== "Bearer" || !token || extra) return res.status(401).json({ error: "Authentication required" });
    try {
      const claims = jwt.verify(token, jwtSecret, { issuer: ISSUER, audience: AUDIENCE });
      if (typeof claims !== "object" || typeof claims.sub !== "string" || !["PRIMARY", "COMPANION"].includes(claims.role)) throw new Error("Invalid token claims");
      req.user = { id: claims.sub, role: claims.role };
      return next();
    } catch {
      return res.status(401).json({ error: "Session is invalid or expired. Please sign in again." });
    }
  };

  app.get("/health", (_req, res) => res.json({ status: "ok" }));

  app.post("/v1/auth/register", async (req, res, next) => {
    const parsed = registerInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    const { name, password, role } = parsed.data;
    const email = parsed.data.email.toLowerCase();
    try {
      const passwordHash = await bcrypt.hash(password, 12);
      const { rows } = await pool.query(
        "INSERT INTO users(email,display_name,password_hash,role) VALUES($1,$2,$3,$4) RETURNING id,email,display_name,role",
        [email, name, passwordHash, role],
      );
      const user = rows[0];
      await pool.query("INSERT INTO user_settings(user_id) VALUES($1) ON CONFLICT(user_id) DO NOTHING", [user.id]);
      return res.status(201).json({ accessToken: issueToken(user), tokenType: "Bearer", expiresInSeconds: ACCESS_TOKEN_SECONDS, user });
    } catch (error) {
      if (error.code === "23505") return res.status(409).json({ error: "An account already exists for that email" });
      return next(error);
    }
  });

  app.post("/v1/auth/login", async (req, res, next) => {
    const parsed = loginInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const { rows } = await pool.query(
        "SELECT id,email,display_name,role,password_hash FROM users WHERE email=$1",
        [parsed.data.email.toLowerCase()],
      );
      const stored = rows[0];
      const matches = stored ? await bcrypt.compare(parsed.data.password, stored.password_hash) : false;
      if (!stored || !matches) return res.status(401).json({ error: "Email or password is incorrect" });
      const user = { id: stored.id, email: stored.email, display_name: stored.display_name, role: stored.role };
      return res.json({ accessToken: issueToken(user), tokenType: "Bearer", expiresInSeconds: ACCESS_TOKEN_SECONDS, user });
    } catch (error) { return next(error); }
  });

  app.get("/v1/me", authenticated, async (req, res, next) => {
    try {
      const { rows } = await pool.query("SELECT id,email,display_name,role FROM users WHERE id=$1", [req.user.id]);
      if (!rows[0]) return res.status(401).json({ error: "Account no longer exists" });
      return res.json({ user: rows[0] });
    } catch (error) { return next(error); }
  });

  app.get("/v1/settings", authenticated, async (req, res, next) => {
    try {
      const { rows } = await pool.query("SELECT language,high_contrast FROM user_settings WHERE user_id=$1", [req.user.id]);
      return res.json(rows[0] ?? { language: "en", high_contrast: false });
    } catch (error) { return next(error); }
  });
  app.patch("/v1/settings", authenticated, async (req, res, next) => {
    const parsed = settingsInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const { rows } = await pool.query(
        "INSERT INTO user_settings(user_id,language,high_contrast) VALUES($1,COALESCE($2,'en'),COALESCE($3,false)) ON CONFLICT(user_id) DO UPDATE SET language=COALESCE($2,user_settings.language), high_contrast=COALESCE($3,user_settings.high_contrast), updated_at=now() RETURNING language,high_contrast",
        [req.user.id, parsed.data.language ?? null, parsed.data.highContrast ?? null],
      );
      return res.json(rows[0]);
    } catch (error) { return next(error); }
  });

  app.get("/v1/journeys", authenticated, async (req, res, next) => {
    try {
      const { rows } = await pool.query("SELECT id,destination,expected_arrival_minutes,transport_details,status,created_at FROM journeys WHERE user_id=$1 ORDER BY created_at DESC LIMIT 100", [req.user.id]);
      return res.json({ journeys: rows });
    } catch (error) { return next(error); }
  });
  app.post("/v1/journeys", authenticated, async (req, res, next) => {
    const parsed = journeyInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const { destination, expectedArrivalMinutes, transportDetails } = parsed.data;
      const { rows } = await pool.query(
        "INSERT INTO journeys(user_id,destination,expected_arrival_minutes,transport_details,status) VALUES($1,$2,$3,$4,'ACTIVE') RETURNING id,destination,expected_arrival_minutes,transport_details,status,created_at",
        [req.user.id, destination, expectedArrivalMinutes, transportDetails ?? null],
      );
      return res.status(201).json({ journey: rows[0] });
    } catch (error) { return next(error); }
  });
  app.patch("/v1/journeys/:id", authenticated, async (req, res, next) => {
    const parsed = journeyUpdateInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const { rows } = await pool.query(
        "UPDATE journeys SET status=$3,completed_at=CASE WHEN $3='COMPLETED' THEN now() ELSE completed_at END WHERE id=$1 AND user_id=$2 RETURNING id,status,completed_at",
        [req.params.id, req.user.id, parsed.data.status],
      );
      if (!rows[0]) return res.status(404).json({ error: "Journey not found" });
      return res.json({ journey: rows[0] });
    } catch (error) { return next(error); }
  });

  app.get("/v1/incidents", authenticated, async (req, res, next) => {
    try {
      const { rows } = await pool.query("SELECT id,journey_id,level,latitude,longitude,status,created_at,resolved_at FROM incidents WHERE user_id=$1 ORDER BY created_at DESC LIMIT 100", [req.user.id]);
      return res.json({ incidents: rows });
    } catch (error) { return next(error); }
  });
  app.post("/v1/incidents", authenticated, async (req, res, next) => {
    const parsed = incidentInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const item = parsed.data;
      const { rows } = await pool.query(
        "INSERT INTO incidents(user_id,journey_id,level,latitude,longitude,status) VALUES($1,$2,$3,$4,$5,'ACTIVE') RETURNING id,journey_id,level,latitude,longitude,status,created_at",
        [req.user.id, item.journeyId ?? null, item.level, item.latitude ?? null, item.longitude ?? null],
      );
      return res.status(201).json({ incident: rows[0], delivery: "recorded_only" });
    } catch (error) { return next(error); }
  });
  app.patch("/v1/incidents/:id/resolve", authenticated, async (req, res, next) => {
    const parsed = resolveInput.safeParse(req.body ?? {});
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const { rows } = await pool.query(
        "UPDATE incidents SET status=$3,resolved_at=now() WHERE id=$1 AND user_id=$2 RETURNING id,status,resolved_at",
        [req.params.id, req.user.id, parsed.data.status],
      );
      if (!rows[0]) return res.status(404).json({ error: "Incident not found" });
      return res.json({ incident: rows[0] });
    } catch (error) { return next(error); }
  });

  app.get("/v1/contacts", authenticated, async (req, res, next) => {
    try {
      const { rows } = await pool.query("SELECT id,name,phone,allow_check_ins,allow_help_messages,created_at FROM trusted_contacts WHERE user_id=$1 ORDER BY name LIMIT 100", [req.user.id]);
      return res.json({ contacts: rows });
    } catch (error) { return next(error); }
  });
  app.post("/v1/contacts", authenticated, async (req, res, next) => {
    const parsed = contactInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const item = parsed.data;
      const { rows } = await pool.query(
        "INSERT INTO trusted_contacts(user_id,name,phone,allow_check_ins,allow_help_messages) VALUES($1,$2,$3,$4,$5) RETURNING id,name,phone,allow_check_ins,allow_help_messages,created_at",
        [req.user.id, item.name, item.phone, item.allowCheckIns, item.allowHelpMessages],
      );
      return res.status(201).json({ contact: rows[0], delivery: "not_invited" });
    } catch (error) { return next(error); }
  });
  app.delete("/v1/contacts/:id", authenticated, async (req, res, next) => {
    try {
      const { rowCount } = await pool.query("DELETE FROM trusted_contacts WHERE id=$1 AND user_id=$2", [req.params.id, req.user.id]);
      if (!rowCount) return res.status(404).json({ error: "Contact not found" });
      return res.status(204).end();
    } catch (error) { return next(error); }
  });

  app.post("/v1/companions/invitations", authenticated, async (req, res, next) => {
    if (req.user.role !== "PRIMARY") return res.status(403).json({ error: "Only primary users can invite companions" });
    const parsed = inviteInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const token = randomBytes(32).toString("base64url");
      const tokenHash = createHash("sha256").update(token).digest("hex");
      await pool.query("INSERT INTO companion_invitations(primary_user_id,companion_email,token_hash,allow_journeys,allow_check_ins,expires_at) VALUES($1,$2,$3,$4,$5,now()+interval '72 hours')", [req.user.id, parsed.data.email.toLowerCase(), tokenHash, parsed.data.allowJourneys, parsed.data.allowCheckIns]);
      return res.status(201).json({ invitationToken: token, expiresInHours: 72, delivery: "manual_share_only" });
    } catch (error) { return next(error); }
  });
  app.post("/v1/companions/accept", authenticated, async (req, res, next) => {
    if (req.user.role !== "COMPANION") return res.status(403).json({ error: "Use a companion account to accept an invitation" });
    const parsed = acceptInviteInput.safeParse(req.body);
    if (!parsed.success) return validationFailure(res, parsed.error);
    try {
      const tokenHash = createHash("sha256").update(parsed.data.invitationToken).digest("hex");
      const { rows } = await pool.query(
        `WITH accepted AS (
           UPDATE companion_invitations i SET status='ACCEPTED',accepted_at=now()
           FROM users companion
           WHERE i.token_hash=$1 AND i.companion_email=companion.email AND companion.id=$2
             AND i.status='PENDING' AND i.expires_at>now()
           RETURNING i.primary_user_id,i.allow_journeys,i.allow_check_ins
         )
         INSERT INTO companion_links(primary_user_id,companion_user_id,allow_journeys,allow_check_ins)
         SELECT primary_user_id,$2,allow_journeys,allow_check_ins FROM accepted
         ON CONFLICT(primary_user_id,companion_user_id) DO UPDATE SET allow_journeys=EXCLUDED.allow_journeys,allow_check_ins=EXCLUDED.allow_check_ins,revoked_at=NULL
         RETURNING id,primary_user_id,allow_journeys,allow_check_ins`, [tokenHash, req.user.id],
      );
      if (!rows[0]) return res.status(400).json({ error: "Invitation is invalid, expired, already used, or addressed to another account" });
      return res.status(201).json({ companionLink: rows[0] });
    } catch (error) { return next(error); }
  });
  app.get("/v1/companions", authenticated, async (req, res, next) => {
    try {
      const sql = req.user.role === "PRIMARY"
        ? "SELECT l.id,u.id AS user_id,u.display_name,u.email,l.allow_journeys,l.allow_check_ins,l.created_at FROM companion_links l JOIN users u ON u.id=l.companion_user_id WHERE l.primary_user_id=$1 AND l.revoked_at IS NULL ORDER BY l.created_at DESC"
        : "SELECT l.id,u.id AS user_id,u.display_name,u.email,l.allow_journeys,l.allow_check_ins,l.created_at FROM companion_links l JOIN users u ON u.id=l.primary_user_id WHERE l.companion_user_id=$1 AND l.revoked_at IS NULL ORDER BY l.created_at DESC";
      const { rows } = await pool.query(sql, [req.user.id]);
      return res.json({ companions: rows });
    } catch (error) { return next(error); }
  });
  app.delete("/v1/companions/:id", authenticated, async (req, res, next) => {
    if (req.user.role !== "PRIMARY") return res.status(403).json({ error: "Only the primary user can revoke this link" });
    try {
      const { rows } = await pool.query("UPDATE companion_links SET revoked_at=now() WHERE id=$1 AND primary_user_id=$2 AND revoked_at IS NULL RETURNING id", [req.params.id, req.user.id]);
      if (!rows[0]) return res.status(404).json({ error: "Companion link not found" });
      return res.status(204).end();
    } catch (error) { return next(error); }
  });
  app.get("/v1/companions/journeys", authenticated, async (req, res, next) => {
    if (req.user.role !== "COMPANION") return res.status(403).json({ error: "This view is for companion accounts" });
    try {
      const { rows } = await pool.query("SELECT j.id,j.user_id,j.destination,j.expected_arrival_minutes,j.transport_details,j.status,j.created_at FROM companion_links l JOIN journeys j ON j.user_id=l.primary_user_id WHERE l.companion_user_id=$1 AND l.revoked_at IS NULL AND l.allow_journeys=true AND j.status='ACTIVE' ORDER BY j.created_at DESC LIMIT 100", [req.user.id]);
      return res.json({ journeys: rows });
    } catch (error) { return next(error); }
  });

  app.use((error, _req, res, _next) => {
    // Avoid logging request bodies: they can contain passwords and private safety data.
    console.error("API request failed:", error?.name ?? "Error", error?.code ?? "internal");
    if (error?.type === "entity.too.large") return res.status(413).json({ error: "Request body is too large" });
    if (error instanceof SyntaxError && Object.hasOwn(error, "body")) return res.status(400).json({ error: "Malformed JSON request body" });
    return res.status(500).json({ error: "Unexpected server error" });
  });
  return app;
}

export const apiContract = { issuer: ISSUER, audience: AUDIENCE, accessTokenSeconds: ACCESS_TOKEN_SECONDS };
