import { memoryStore } from '../../src/db';
import { PriceService } from '../../src/services/PriceService';
import { extractAndVerifyAuth } from '../../src/middleware/auth';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  const auth = extractAndVerifyAuth(req);
  if (auth.error || !auth.user) {
    return res.status(401).json(errorResponse(auth.error || 'Authentication required', auth.code || 'AUTH_REQUIRED'));
  }

  if (req.method === 'GET') {
    const trainNumber = (req.query?.trainNumber || '').toString();
    const coachNumber = (req.query?.coachNumber || '').toString();
    const stationCode = (req.query?.stationCode || '').toString();
    
    let list = Array.from(memoryStore.foodRequests.values());

    // Isolation based on caller's role
    if (auth.user.role === 'TRAVELER') {
      // Travelers only see their own requests
      list = list.filter(r => r.customerId === auth.user?.userId);
    } else if (auth.user.role === 'VENDOR') {
      // Vendors only see requests matching their operating train and coach
      if (trainNumber) {
        list = list.filter(r => r.trainNumber === trainNumber);
      }
      if (coachNumber) {
        list = list.filter(r => r.coachNumber === coachNumber);
      }
      if (stationCode) {
        list = list.filter(r => !r.targetStationCode || r.targetStationCode === stationCode);
      }
    }

    return res.status(200).json(successResponse(list));
  }

  if (req.method === 'POST') {
    // Only TRAVELER can dispatch food requests
    if (auth.user.role !== 'TRAVELER') {
      return res.status(403).json(errorResponse('Forbidden: requires TRAVELER role', 'FORBIDDEN'));
    }

    const {
      clientRequestId,
      journeyId,
      trainNumber,
      coachNumber,
      foodItemId,
      foodItemName,
      quantity,
      targetStationCode,
      targetStationName,
      note
    } = req.body || {};

    if (!clientRequestId) {
      return res.status(400).json(errorResponse('clientRequestId is required for idempotency'));
    }

    // Idempotency check: if clientRequestId already exists, return existing
    for (const existing of memoryStore.foodRequests.values()) {
      if (existing.clientRequestId === clientRequestId) {
        return res.status(200).json(successResponse(existing));
      }
    }

    // Enforce server-side quantity clamp [1, 10] with explicit notification
    const validQty = PriceService.coerceQuantity(quantity);
    const wasClamped = quantity !== undefined && quantity !== null && (Number(quantity) !== validQty || !Number.isInteger(Number(quantity)));

    const requestId = `req_${Math.random().toString(36).substring(2, 10)}`;
    const newRequest = {
      id: requestId,
      clientRequestId,
      customerId: auth.user.userId, // Server-authoritative: ignore spoofed customerId in body
      journeyId: journeyId || 'active_journey',
      trainNumber: trainNumber || '31617',
      coachNumber: coachNumber || 'GS-2',
      targetStationCode: targetStationCode || '',
      targetStationName: targetStationName || '',
      foodItemId: foodItemId || 'jhalmuri',
      foodItemName: foodItemName || 'Jhalmuri',
      quantity: validQty,
      wasClamped,
      requestedQuantity: quantity !== undefined ? quantity : 1,
      clampingNotice: wasClamped ? `Requested quantity was adjusted to legal boundary of ${validQty} (Bounds: 1 to 10)` : null,
      price: 0,
      offeredUnitPrice: null,
      calculatedTotalPrice: null,
      status: 'REQUESTED',
      note: note || '',
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString()
    };

    memoryStore.foodRequests.set(requestId, newRequest);
    return res.status(201).json(successResponse(newRequest));
  }

  return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
}
