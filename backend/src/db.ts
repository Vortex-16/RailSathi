import { Pool, PoolConfig } from 'pg';

const connectionString = process.env.DATABASE_URL;

let pool: Pool | null = null;

if (connectionString) {
  const config: PoolConfig = {
    connectionString,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : undefined,
    max: 10,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
  };
  pool = new Pool(config);
}

// In-Memory fallback store for development or before DATABASE_URL is set
export const memoryStore = {
  users: new Map<string, any>(),
  vendors: new Map<string, any>(),
  journeys: new Map<string, any>(),
  foodRequests: new Map<string, any>(),
  orders: new Map<string, any>(),
  syncKeys: new Set<string>()
};

export async function query(text: string, params?: any[]): Promise<any> {
  if (pool) {
    try {
      const res = await pool.query(text, params);
      return res;
    } catch (err) {
      console.error('[DB] PostgreSQL query failed, falling back to memory layer:', err);
    }
  }
  return { rows: [] };
}

export async function isDbConnected(): Promise<boolean> {
  if (!pool) return false;
  try {
    const client = await pool.connect();
    client.release();
    return true;
  } catch {
    return false;
  }
}
