import { query, memoryStore } from '../../src/db';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  const userId = req.headers['x-user-id'] || req.query.userId || req.body?.userId;

  if (!userId) {
    return res.status(401).json(errorResponse('Unauthorized: User ID required', 'UNAUTHORIZED'));
  }

  const uId = String(userId);

  if (req.method === 'GET') {
    // Read Profile
    try {
      const resDb = await query(
        `SELECT u.id, u.display_name, u.phone, u.role, u.language, u.is_senior_mode,
                p.full_name, p.email, p.bio, p.preferred_station, p.regular_commute_route
         FROM users u
         LEFT JOIN profiles p ON u.id = p.user_id
         WHERE u.id = $1`,
        [uId]
      );

      if (resDb && resDb.rows && resDb.rows.length > 0) {
        return res.status(200).json(successResponse(resDb.rows[0]));
      }

      const memUser = memoryStore.users.get(uId);
      if (memUser) {
        return res.status(200).json(successResponse(memUser));
      }

      return res.status(200).json(successResponse({
        id: uId,
        displayName: 'Commuter',
        role: 'TRAVELER',
        language: 'ENGLISH',
        isSeniorMode: false
      }));
    } catch (err: any) {
      return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
    }
  } else if (req.method === 'POST' || req.method === 'PUT') {
    // Update Profile
    const { displayName, phone, language, isSeniorMode, bio, preferredStation, regularRoute } = req.body || {};

    try {
      await query(
        `UPDATE users
         SET display_name = COALESCE($2, display_name),
             phone = COALESCE($3, phone),
             language = COALESCE($4, language),
             is_senior_mode = COALESCE($5, is_senior_mode),
             updated_at = CURRENT_TIMESTAMP
         WHERE id = $1`,
        [uId, displayName, phone, language, isSeniorMode]
      );

      await query(
        `INSERT INTO profiles (user_id, full_name, bio, preferred_station, regular_commute_route)
         VALUES ($1, $2, $3, $4, $5)
         ON CONFLICT (user_id) DO UPDATE
         SET full_name = COALESCE($2, profiles.full_name),
             bio = COALESCE($3, profiles.bio),
             preferred_station = COALESCE($4, profiles.preferred_station),
             regular_commute_route = COALESCE($5, profiles.regular_commute_route),
             updated_at = CURRENT_TIMESTAMP`,
        [uId, displayName, bio, preferredStation, regularRoute]
      );

      const memUser = memoryStore.users.get(uId) || {};
      const updated = {
        ...memUser,
        id: uId,
        displayName: displayName || memUser.displayName,
        phone: phone || memUser.phone,
        language: language || memUser.language,
        isSeniorMode: isSeniorMode ?? memUser.isSeniorMode,
        bio: bio || memUser.bio
      };
      memoryStore.users.set(uId, updated);

      return res.status(200).json(successResponse(updated));
    } catch (err: any) {
      return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
    }
  } else if (req.method === 'DELETE') {
    // Delete Profile / Clear Account Data
    try {
      await query('DELETE FROM profiles WHERE user_id = $1', [uId]);
      await query('DELETE FROM users WHERE id = $1', [uId]);
      memoryStore.users.delete(uId);

      return res.status(200).json(successResponse({ deleted: true, userId: uId }));
    } catch (err: any) {
      return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
    }
  } else {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }
}
