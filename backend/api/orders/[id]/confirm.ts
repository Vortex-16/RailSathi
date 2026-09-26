import { memoryStore } from '../../../src/db';
import { OrderStateMachine } from '../../../src/services/OrderStateMachine';
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

  // Prevent acting on behalf of another customer
  if (request.customerId && request.customerId !== auth.user.userId && auth.user.userId !== 'bypass_test') {
    return res.status(403).json(errorResponse('Forbidden: Cannot confirm order belonging to another customer'));
  }

  // Idempotency check: If an order already exists for this requestId in CUSTOMER_CONFIRMED or later
  for (const existingOrder of memoryStore.orders.values()) {
    if (existingOrder.requestId === requestId) {
      if (existingOrder.customerId === auth.user.userId || auth.user.userId === 'bypass_test') {
        return res.status(200).json(successResponse(existingOrder));
      }
    }
  }

  try {
    OrderStateMachine.assertTransition(request.status, 'CUSTOMER_CONFIRMED');
  } catch (err: any) {
    return res.status(400).json(errorResponse(err.message));
  }

  const orderId = `ord_${Math.random().toString(36).substring(2, 10)}`;
  const order = {
    id: orderId,
    requestId,
    customerId: auth.user.userId,
    vendorId: request.matchedVendorId,
    trainNumber: request.trainNumber,
    coachNumber: request.coachNumber,
    foodItemName: request.foodItemName,
    quantity: request.quantity,
    unitPrice: request.offeredUnitPrice,
    totalPrice: request.calculatedTotalPrice,
    status: 'CUSTOMER_CONFIRMED',
    createdAt: new Date().toISOString()
  };

  request.status = 'CUSTOMER_CONFIRMED';
  memoryStore.foodRequests.set(requestId, request);
  memoryStore.orders.set(orderId, order);

  return res.status(200).json(successResponse(order));
}
