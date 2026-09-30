import "dotenv/config";
import pg from "pg";
import { createApp } from "./app.js";

const { Pool } = pg;
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.NODE_ENV === "production" ? { rejectUnauthorized: true } : false,
});
const jwtSecret = process.env.JWT_SECRET;
if (!process.env.DATABASE_URL) throw new Error("DATABASE_URL is required");
if (!jwtSecret || Buffer.byteLength(jwtSecret, "utf8") < 32) throw new Error("Set JWT_SECRET to a random value of at least 32 bytes");

const app = createApp({ pool, jwtSecret, allowedOrigins: process.env.CORS_ORIGIN?.split(",").map((origin) => origin.trim()).filter(Boolean) ?? [] });
const server = app.listen(Number(process.env.PORT) || 8080, () => console.log("Safety 4 Women API listening"));

async function shutdown(signal) {
  console.log(`Received ${signal}; closing API`);
  server.close(async () => {
    await pool.end();
    process.exit(0);
  });
}
process.once("SIGTERM", () => shutdown("SIGTERM"));
process.once("SIGINT", () => shutdown("SIGINT"));
