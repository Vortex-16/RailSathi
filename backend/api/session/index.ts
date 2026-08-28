import { memoryStore } from '../../src/db';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  const { deviceId, installationId, role, displayName, phone, language, isSeniorMode } = req.body || {};

  if (!deviceId || !installationId) {
    return res.status(400).json(errorResponse('deviceId and installationId are required'));
  }

  const sessionToken = `rss_${Math.random().toString(36).substring(2)}_${Date.now()}`;
  const userId = `usr_${Math.random().toString(36).substring(2, 10)}`;

  const user = {
    id: userId,
    deviceId,
    installationId,
    role: role || 'TRAVELER',
    displayName: displayName || 'Commuter',
    phone: phone || '',
    language: language || 'ENGLISH',
    isSeniorMode: Boolean(isSeniorMode),
    sessionToken,
    createdAt: new Date().toISOString()
  };

  memoryStore.users.set(userId, user);

  return res.status(200).json(successResponse({
    user,
    sessionToken
  }));
}
