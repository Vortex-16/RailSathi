import { memoryStore } from '../../src/db';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed'));
  }

  const { items } = req.body || {};
  if (!items || !Array.isArray(items)) {
    return res.status(400).json(errorResponse('items array is required'));
  }

  const results = [];

  for (const item of items) {
    const { idempotencyKey, operationType, payload } = item;
    if (!idempotencyKey) continue;

    if (memoryStore.syncKeys.has(idempotencyKey)) {
      results.push({ idempotencyKey, status: 'ALREADY_SYNCED' });
      continue;
    }

    memoryStore.syncKeys.add(idempotencyKey);
    // Process offline sync payload
    if (operationType === 'CREATE_ORDER' && payload) {
      const orderId = payload.orderId || `ord_${Math.random().toString(36).substring(2, 10)}`;
      memoryStore.orders.set(orderId, {
        ...payload,
        id: orderId,
        syncedAt: new Date().toISOString()
      });
    }

    results.push({ idempotencyKey, status: 'SUCCESS' });
  }

  return res.status(200).json(successResponse({ processed: results.length, results }));
}
