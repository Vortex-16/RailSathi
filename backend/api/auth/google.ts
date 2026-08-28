import { query, memoryStore } from '../../src/db';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  const { idToken, googleId, email, displayName, photoUrl, role, language } = req.body || {};

  if (!googleId && !email && !idToken) {
    return res.status(400).json(errorResponse('Authentication credentials (idToken or email/googleId) required'));
  }

  const effectiveEmail = email || `user_${(googleId || 'anon').substring(0, 8)}@railsaathi.in`;
  const effectiveName = displayName || 'Commuter';
  const effectiveRole = (role === 'VENDOR') ? 'VENDOR' : 'TRAVELER';
  const effectiveLang = language || 'ENGLISH';
  const sessionToken = `rss_${Math.random().toString(36).substring(2)}_${Date.now()}`;
  const userId = `usr_${(googleId || Math.random().toString(36).substring(2, 10))}`;

  try {
    // Upsert user into PostgreSQL if DB is connected
    const dbRes = await query(
      `INSERT INTO users (device_id, installation_id, role, display_name, language, session_token)
       VALUES ($1, $2, $3, $4, $5, $6)
       ON CONFLICT (device_id) DO UPDATE
       SET role = EXCLUDED.role, display_name = EXCLUDED.display_name, session_token = EXCLUDED.session_token, updated_at = CURRENT_TIMESTAMP
       RETURNING id`,
      [effectiveEmail, `inst_${userId}`, effectiveRole, effectiveName, effectiveLang, sessionToken]
    );

    const actualDbId = dbRes?.rows?.[0]?.id ? String(dbRes.rows[0].id) : userId;

    // Upsert into profile table
    await query(
      `INSERT INTO profiles (user_id, full_name, email)
       VALUES ($1, $2, $3)
       ON CONFLICT (user_id) DO UPDATE
       SET full_name = EXCLUDED.full_name, email = EXCLUDED.email, updated_at = CURRENT_TIMESTAMP`,
      [actualDbId, effectiveName, effectiveEmail]
    );

    const userObj = {
      id: actualDbId,
      email: effectiveEmail,
      displayName: effectiveName,
      photoUrl: photoUrl || null,
      role: effectiveRole,
      language: effectiveLang,
      sessionToken,
      createdAt: new Date().toISOString()
    };

    memoryStore.users.set(actualDbId, userObj);

    return res.status(200).json(successResponse({
      user: userObj,
      sessionToken
    }));
  } catch (err: any) {
    console.error('[Auth API] Failed to authenticate user:', err);
    // Return session even if DB is transiently offline
    const userObj = {
      id: userId,
      email: effectiveEmail,
      displayName: effectiveName,
      photoUrl: photoUrl || null,
      role: effectiveRole,
      language: effectiveLang,
      sessionToken,
      createdAt: new Date().toISOString()
    };
    return res.status(200).json(successResponse({
      user: userObj,
      sessionToken
    }));
  }
}
