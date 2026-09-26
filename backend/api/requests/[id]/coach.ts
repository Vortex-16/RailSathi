import { memoryStore } from '../../../src/db';
import { extractAndVerifyAuth } from '../../../src/middleware/auth';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  const auth = extractAndVerifyAuth(req);
  if (auth.error || !auth.user) {
    return res.status(401).json(errorResponse(auth.error || 'Authentication required', auth.code || 'AUTH_REQUIRED'));
  }

  if (auth.user.role !== 'TRAVELER') {
    return res.status(403).json(errorResponse('Forbidden: requires TRAVELER role', 'FORBIDDEN'));
  }

  const requestId = (req.query?.id || '').toString();
  const { newCoach } = req.body || {};
  if (!newCoach) {
    return res.status(400).json(errorResponse('newCoach is required'));
  }

  const request = memoryStore.foodRequests.get(requestId);
  if (!request) {
    return res.status(404).json(errorResponse('Food request not found'));
  }

  // Coach can only be changed before vendor acceptance
  if (request.status !== 'REQUESTED' && request.status !== 'MATCHING') {
    return res.status(409).json(errorResponse('Cannot change coach after vendor has accepted or order is in-flight'));
  }

  request.coachNumber = newCoach;
  request.updatedAt = new Date().toISOString();
  memoryStore.foodRequests.set(requestId, request);

  return res.status(200).json(successResponse({
    requestId,
    coachNumber: request.coachNumber
  }));
}
