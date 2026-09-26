import { memoryStore } from '../../src/db';
import { extractAndVerifyAuth } from '../../src/middleware/auth';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  const auth = extractAndVerifyAuth(req);
  if (auth.error || !auth.user) {
    return res.status(401).json(errorResponse(auth.error || 'Authentication required', auth.code || 'AUTH_REQUIRED'));
  }

  if (auth.user.role !== 'VENDOR') {
    return res.status(403).json(errorResponse('Forbidden: requires VENDOR role', 'FORBIDDEN'));
  }

  const vendorId = auth.user.userId;
  const deviceId = auth.user.deviceId || 'unknown_device';

  let vendor = memoryStore.vendors.get(vendorId);
  if (!vendor) {
    vendor = {
      vendorId,
      name: auth.user.displayName || 'Hawker',
      isAvailable: true,
      currentCoach: 'GS-2',
      currentTrain: '31617',
      activeDeviceId: deviceId,
      version: 1,
      lastUpdatedAt: Date.now(),
      todaySalesCount: 0,
      todayEarnings: 0
    };
    memoryStore.vendors.set(vendorId, vendor);
  }

  // GET: Return authoritative vendor state
  if (req.method === 'GET') {
    return res.status(200).json(successResponse({
      vendorId: vendor.vendorId,
      isAvailable: vendor.isAvailable,
      version: vendor.version || 1,
      activeDeviceId: vendor.activeDeviceId,
      lastUpdatedAt: vendor.lastUpdatedAt,
      currentCoach: vendor.currentCoach,
      currentTrain: vendor.currentTrain,
      todaySalesCount: vendor.todaySalesCount,
      todayEarnings: vendor.todayEarnings
    }));
  }

  if (req.method === 'POST') {
    const { isAvailable, currentCoach, currentTrain, timestamp, expectedVersion } = req.body || {};
    const serverNow = Date.now();

    // Optimistic Concurrency Check: If client specified expectedVersion, verify sequence
    if (expectedVersion !== undefined && expectedVersion !== null) {
      const currentVer = vendor.version || 1;
      if (Number(expectedVersion) !== currentVer) {
        return res.status(409).json(errorResponse(
          `Version conflict: Expected version ${expectedVersion}, but server state is at version ${currentVer}. Please fetch latest state and retry.`,
          'VERSION_CONFLICT'
        ));
      }
    }

    // Monotonic server revision increment
    vendor.version = (vendor.version || 1) + 1;
    vendor.isAvailable = Boolean(isAvailable);
    vendor.activeDeviceId = deviceId;
    vendor.lastUpdatedAt = serverNow;
    if (currentCoach) vendor.currentCoach = currentCoach;
    if (currentTrain) vendor.currentTrain = currentTrain;

    memoryStore.vendors.set(vendorId, vendor);

    return res.status(200).json(successResponse({
      vendorId,
      isAvailable: vendor.isAvailable,
      version: vendor.version,
      activeDeviceId: vendor.activeDeviceId,
      lastUpdatedAt: vendor.lastUpdatedAt,
      serverTimestamp: serverNow,
      currentCoach: vendor.currentCoach,
      currentTrain: vendor.currentTrain
    }));
  }

  return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
}
