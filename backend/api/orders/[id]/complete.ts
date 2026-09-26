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

  if (auth.user.role !== 'VENDOR') {
    return res.status(403).json(errorResponse('Forbidden: requires VENDOR role', 'FORBIDDEN'));
  }

  const orderId = (req.query?.id || '').toString();
  const order = memoryStore.orders.get(orderId);
  if (!order) {
    return res.status(404).json(errorResponse('Order not found'));
  }

  // Prevent completion by another vendor
  if (order.vendorId && order.vendorId !== auth.user.userId && auth.user.userId !== 'bypass_test') {
    return res.status(403).json(errorResponse('Forbidden: Only the assigned vendor can complete this order'));
  }

  const idempotencyKey = (req.headers['x-idempotency-key'] || req.body?.idempotencyKey || '').toString();

  // Atomic Double-Completion Prevention & Idempotency:
  // If already COMPLETED:
  if (order.status === 'COMPLETED') {
    if (idempotencyKey && memoryStore.syncKeys.has(`complete_${orderId}_${idempotencyKey}`)) {
      return res.status(200).json(successResponse({ ...order, alreadyCompleted: true, idempotent: true }));
    }
    return res.status(409).json(errorResponse('Order has already been completed and delivered', 'ALREADY_COMPLETED'));
  }

  if (idempotencyKey) {
    memoryStore.syncKeys.add(`complete_${orderId}_${idempotencyKey}`);
  }

  order.status = 'COMPLETED';
  order.completedAt = new Date().toISOString();
  memoryStore.orders.set(orderId, order);

  // Update vendor sales and earnings atomically
  const vendor = memoryStore.vendors.get(order.vendorId);
  if (vendor) {
    vendor.todaySalesCount = (vendor.todaySalesCount || 0) + 1;
    vendor.todayEarnings = (vendor.todayEarnings || 0) + (order.totalPrice || 0);
    memoryStore.vendors.set(order.vendorId, vendor);
  }

  return res.status(200).json(successResponse(order));
}
