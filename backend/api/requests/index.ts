import { memoryStore } from '../../src/db';
import { PriceService } from '../../src/services/PriceService';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method === 'GET') {
    const trainNumber = (req.query?.trainNumber || '').toString();
    const coachNumber = (req.query?.coachNumber || '').toString();
    
    let list = Array.from(memoryStore.foodRequests.values());
    if (trainNumber) {
      list = list.filter(r => r.trainNumber === trainNumber);
    }
    if (coachNumber) {
      list = list.filter(r => r.coachNumber === coachNumber);
    }
    return res.status(200).json(successResponse(list));
  }

  if (req.method === 'POST') {
    const {
      clientRequestId,
      customerId,
      journeyId,
      trainNumber,
      coachNumber,
      foodItemId,
      foodItemName,
      quantity,
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

    let validQty = 1;
    try {
      validQty = PriceService.validateQuantity(Number(quantity) || 1);
    } catch (err: any) {
      return res.status(400).json(errorResponse(err.message));
    }

    const requestId = `req_${Math.random().toString(36).substring(2, 10)}`;
    const newRequest = {
      id: requestId,
      clientRequestId,
      customerId: customerId || 'anon_cust',
      journeyId: journeyId || 'active_journey',
      trainNumber: trainNumber || '31617',
      coachNumber: coachNumber || 'GS-2',
      foodItemId: foodItemId || 'jhalmuri',
      foodItemName: foodItemName || 'Jhalmuri',
      quantity: validQty,
      note: note || '',
      status: 'REQUESTED',
      matchedVendorId: null,
      offeredUnitPrice: null,
      calculatedTotalPrice: null,
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString()
    };

    memoryStore.foodRequests.set(requestId, newRequest);
    return res.status(201).json(successResponse(newRequest));
  }

  return res.status(405).json(errorResponse('Method Not Allowed'));
}
