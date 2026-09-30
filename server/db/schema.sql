-- Idempotent PostgreSQL schema. Apply with: psql "$DATABASE_URL" -f db/schema.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  display_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('PRIMARY','COMPANION')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS user_settings (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  language TEXT NOT NULL DEFAULT 'en' CHECK (language IN ('en','zu')),
  high_contrast BOOLEAN NOT NULL DEFAULT false,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS trusted_contacts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  phone TEXT NOT NULL,
  allow_check_ins BOOLEAN NOT NULL DEFAULT true,
  allow_help_messages BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS companion_invitations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  primary_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  companion_email TEXT NOT NULL,
  token_hash TEXT NOT NULL UNIQUE,
  allow_journeys BOOLEAN NOT NULL DEFAULT true,
  allow_check_ins BOOLEAN NOT NULL DEFAULT true,
  status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACCEPTED','REVOKED','EXPIRED')),
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  accepted_at TIMESTAMPTZ
);
CREATE TABLE IF NOT EXISTS companion_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  primary_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  companion_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  allow_journeys BOOLEAN NOT NULL DEFAULT true,
  allow_check_ins BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  revoked_at TIMESTAMPTZ,
  UNIQUE (primary_user_id, companion_user_id)
);
CREATE TABLE IF NOT EXISTS journeys (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  destination TEXT NOT NULL,
  expected_arrival_minutes INTEGER NOT NULL CHECK (expected_arrival_minutes BETWEEN 1 AND 360),
  transport_details TEXT,
  status TEXT NOT NULL CHECK (status IN ('ACTIVE','COMPLETED','CANCELLED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ
);
CREATE TABLE IF NOT EXISTS incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  journey_id UUID REFERENCES journeys(id) ON DELETE SET NULL,
  actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  level TEXT NOT NULL CHECK (level IN ('CHECK_IN','ALERT','SOS')),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  status TEXT NOT NULL CHECK (status IN ('ACTIVE','RESOLVED','CANCELLED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at TIMESTAMPTZ,
  CHECK ((latitude IS NULL AND longitude IS NULL) OR (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180))
);
CREATE INDEX IF NOT EXISTS journeys_user_created_idx ON journeys(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS incidents_user_created_idx ON incidents(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS companion_link_companion_idx ON companion_links(companion_user_id) WHERE revoked_at IS NULL;

-- Migrate existing demo databases without dropping user data.
ALTER TABLE journeys ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE incidents ALTER COLUMN latitude DROP NOT NULL;
ALTER TABLE incidents ALTER COLUMN longitude DROP NOT NULL;
ALTER TABLE incidents DROP CONSTRAINT IF EXISTS incidents_status_check;
ALTER TABLE incidents ADD CONSTRAINT incidents_status_check CHECK (status IN ('ACTIVE','RESOLVED','CANCELLED'));
