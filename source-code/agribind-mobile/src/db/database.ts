import * as SQLite from "expo-sqlite";

const db = SQLite.openDatabaseSync("agribind_mobile.db");

export const initializeDatabase = () => {
  db.execSync(`
    CREATE TABLE IF NOT EXISTS farmer_profile (
      id TEXT PRIMARY KEY NOT NULL,
      name TEXT NOT NULL,
      phone_number TEXT NOT NULL,
      preferred_language TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS market_price_cache (
      id TEXT PRIMARY KEY NOT NULL,
      commodity TEXT NOT NULL,
      market TEXT NOT NULL,
      currency TEXT NOT NULL,
      price REAL NOT NULL,
      updated_at TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS cached_announcements (
      id TEXT PRIMARY KEY NOT NULL,
      title TEXT NOT NULL,
      language TEXT NOT NULL,
      audio_url TEXT NOT NULL,
      downloaded INTEGER NOT NULL DEFAULT 0,
      updated_at TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS weather_cache (
      id TEXT PRIMARY KEY NOT NULL,
      region TEXT NOT NULL,
      payload TEXT NOT NULL,
      updated_at TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS offline_transaction_queue (
      client_outbox_id TEXT PRIMARY KEY NOT NULL,
      action_type TEXT NOT NULL,
      payload TEXT NOT NULL,
      status TEXT NOT NULL,
      created_at TEXT NOT NULL,
      attempts INTEGER NOT NULL DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS notifications_cache (
      id TEXT PRIMARY KEY NOT NULL,
      category TEXT NOT NULL,
      title TEXT NOT NULL,
      body TEXT NOT NULL,
      read INTEGER NOT NULL DEFAULT 0,
      created_at TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS microcredit_cache (
      id TEXT PRIMARY KEY NOT NULL,
      cooperative_id TEXT NOT NULL,
      cooperative_name TEXT NOT NULL,
      total_amount REAL NOT NULL,
      amount_repaid REAL NOT NULL,
      currency TEXT NOT NULL,
      start_date TEXT NOT NULL,
      end_date TEXT NOT NULL,
      purpose TEXT NOT NULL,
      status TEXT NOT NULL,
      repayments_json TEXT NOT NULL,
      cached_at TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS plant_health_cache (
      id TEXT PRIMARY KEY NOT NULL,
      crop_name TEXT NOT NULL,
      disease_name TEXT NOT NULL,
      severity TEXT NOT NULL,
      affected_area_ha REAL NOT NULL,
      recommendation TEXT NOT NULL,
      reported_at TEXT NOT NULL
    );
  `);
};

export default db;
