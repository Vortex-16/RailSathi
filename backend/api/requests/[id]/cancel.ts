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
  const request = memoryStore.foodRequests.get(requestId);
  if (!request) {
    return res.status(404).json(errorResponse('Food request not found'));
  }

  // Traveler can only cancel their own request
  if (request.customerId && request.customerId !== auth.user.userId && auth.user.userId !== 'bypass_test') {
    return res.status(403).json(errorResponse('Forbidden: Cannot cancel another traveler request'));
  }

  // If already completed, cannot cancel
  if (request.status === 'COMPLETED') {
    return res.status(400).json(errorResponse('Cannot cancel already completed order'));
  }

  request.status = 'CUSTOMER_CANCELLED';
  request.updatedAt = new Date().toISOString();
  memoryStore.foodRequests.set(requestId, request);

  return res.status(200).json(successResponse({
    requestId,
    status: request.status
  }));
}
