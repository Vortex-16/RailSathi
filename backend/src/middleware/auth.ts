import crypto from 'crypto';
import { memoryStore } from '../db';
import { errorResponse } from '../utils/response';

const JWT_SECRET = process.env.JWT_SECRET || 'railsaathi_production_super_secret_signing_key_2026';

export interface TokenPayload {
  userId: string;
  role: 'TRAVELER' | 'VENDOR' | 'GUEST';
  deviceId: string;
  displayName?: string;
  exp: number; // Unix timestamp in seconds
  iat: number;
}

// Generate base64url encoded string
function base64UrlEncode(str: string): string {
  return Buffer.from(str)
    .toString('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
}

function base64UrlDecode(str: string): string {
  let base64 = str.replace(/-/g, '+').replace(/_/g, '/');
  while (base64.length % 4) {
    base64 += '=';
  }
  return Buffer.from(base64, 'base64').toString('utf8');
}

export function generateJwt(payload: Omit<TokenPayload, 'exp' | 'iat'>, expiresInSeconds: number = 86400): string {
  const now = Math.floor(Date.now() / 1000);
  const fullPayload: TokenPayload = {
    ...payload,
    iat: now,
    exp: now + expiresInSeconds
  };

  const header = { alg: 'HS256', typ: 'JWT' };
  const encodedHeader = base64UrlEncode(JSON.stringify(header));
  const encodedPayload = base64UrlEncode(JSON.stringify(fullPayload));

  const signature = crypto
    .createHmac('sha256', JWT_SECRET)
    .update(`${encodedHeader}.${encodedPayload}`)
    .digest('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');

  return `${encodedHeader}.${encodedPayload}.${signature}`;
}

export function verifyJwt(token: string): { valid: boolean; payload?: TokenPayload; error?: string; code?: string } {
  if (!token || typeof token !== 'string') {
    return { valid: false, error: 'Token missing', code: 'AUTH_REQUIRED' };
  }

  const parts = token.split('.');
  if (parts.length !== 3) {
    return { valid: false, error: 'Malformed token structure', code: 'INVALID_TOKEN' };
  }

  const [encodedHeader, encodedPayload, signature] = parts;

  // Verify signature
  const expectedSignature = crypto
    .createHmac('sha256', JWT_SECRET)
    .update(`${encodedHeader}.${encodedPayload}`)
    .digest('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');

  if (signature !== expectedSignature) {
    return { valid: false, error: 'Invalid token signature', code: 'INVALID_SIGNATURE' };
  }

  try {
    const payload: TokenPayload = JSON.parse(base64UrlDecode(encodedPayload));
    const now = Math.floor(Date.now() / 1000);

    if (payload.exp && payload.exp < now) {
      return { valid: false, error: 'Token has expired', code: 'TOKEN_EXPIRED' };
    }

    return { valid: true, payload };
  } catch (err) {
    return { valid: false, error: 'Malformed token payload', code: 'INVALID_TOKEN' };
  }
}

export function extractAndVerifyAuth(req: any): { user?: TokenPayload; error?: string; code?: string } {
  const authHeader = req.headers?.authorization || req.headers?.Authorization;
  const sessionHeader = req.headers?.['x-session-token'];

  let token = '';
  if (authHeader && typeof authHeader === 'string' && authHeader.startsWith('Bearer ')) {
    token = authHeader.substring(7).trim();
  } else if (sessionHeader && typeof sessionHeader === 'string') {
    token = sessionHeader.trim();
  }

  if (!token) {
    return { error: 'Authorization header or x-session-token is required', code: 'AUTH_REQUIRED' };
  }

  // Check if it is a JWT
  if (token.includes('.')) {
    const verification = verifyJwt(token);
    if (!verification.valid || !verification.payload) {
      return { error: verification.error, code: verification.code };
    }
    return { user: verification.payload };
  }

  // Fallback: check sessionToken in memoryStore
  for (const u of memoryStore.users.values()) {
    if (u.sessionToken === token) {
      return {
        user: {
          userId: u.id,
          role: u.role,
          deviceId: u.deviceId,
          displayName: u.displayName,
          exp: Math.floor(Date.now() / 1000) + 86400,
          iat: Math.floor(Date.now() / 1000)
        }
      };
    }
  }

  return { error: 'Invalid or unknown session token', code: 'INVALID_TOKEN' };
}
