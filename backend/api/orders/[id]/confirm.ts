import { memoryStore } from '../../../src/db';
import { OrderStateMachine } from '../../../src/services/OrderStateMachine';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed'));
  }

  const requestId = (req.query?.id || '').toString();
  const { customerId } = req.body || {};

  const request = memoryStore.foodRequests.get(requestId);
  if (!request) {
    return res.status(404).json(errorResponse('Food request not found'));
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
    customerId: customerId || request.customerId,
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
