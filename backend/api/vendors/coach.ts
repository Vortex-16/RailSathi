import { memoryStore } from '../../src/db';
import { extractAndVerifyAuth } from '../../src/middleware/auth';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  const auth = extractAndVerifyAuth(req);
  if (auth.error || !auth.user) {
    return res.status(401).json(errorResponse(auth.error || 'Authentication required', auth.code || 'AUTH_REQUIRED'));
  }

  if (auth.user.role !== 'VENDOR') {
    return res.status(403).json(errorResponse('Forbidden: requires VENDOR role', 'FORBIDDEN'));
  }

  const { coach } = req.body || {};
  if (!coach) {
    return res.status(400).json(errorResponse('coach is required'));
  }

  const vendorId = auth.user.userId;
  let vendor = memoryStore.vendors.get(vendorId);
  if (!vendor) {
    vendor = {
      vendorId,
      name: auth.user.displayName || 'Hawker',
      isAvailable: true,
      currentCoach: coach,
      currentTrain: '31617',
      todaySalesCount: 0,
      todayEarnings: 0
    };
  } else {
    vendor.currentCoach = coach;
  }
  memoryStore.vendors.set(vendorId, vendor);

  return res.status(200).json(successResponse({
    vendorId,
    currentCoach: vendor.currentCoach
  }));
}
