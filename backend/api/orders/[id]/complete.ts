import { memoryStore } from '../../../src/db';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed'));
  }

  const orderId = (req.query?.id || '').toString();
  const { vendorId } = req.body || {};

  const order = memoryStore.orders.get(orderId);
  if (!order) {
    return res.status(404).json(errorResponse('Order not found'));
  }

  order.status = 'COMPLETED';
  order.completedAt = new Date().toISOString();
  memoryStore.orders.set(orderId, order);

  return res.status(200).json(successResponse(order));
}
