import { memoryStore } from '../../../src/db';
import { PriceService } from '../../../src/services/PriceService';
import { OrderStateMachine } from '../../../src/services/OrderStateMachine';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed'));
  }

  const requestId = (req.query?.id || '').toString();
  const { vendorId, unitPrice } = req.body || {};

  const request = memoryStore.foodRequests.get(requestId);
  if (!request) {
    return res.status(404).json(errorResponse('Food request not found'));
  }

  if (request.matchedVendorId && request.matchedVendorId !== vendorId) {
    return res.status(403).json(errorResponse('Forbidden: Only matched vendor can set price'));
  }

  const numericPrice = Number(unitPrice);
  if (!PriceService.isPriceAllowed(numericPrice)) {
    return res.status(400).json(errorResponse(`Invalid price ₹${unitPrice}. Allowed prices: ₹5, ₹10, ₹15, ₹20, ₹30, ₹40, ₹50`));
  }

  try {
    OrderStateMachine.assertTransition(request.status, 'PRICE_CONFIRMED');
  } catch (err: any) {
    return res.status(400).json(errorResponse(err.message));
  }

  const totalPrice = PriceService.calculateTotal(request.quantity, numericPrice);

  request.offeredUnitPrice = numericPrice;
  request.calculatedTotalPrice = totalPrice;
  request.status = 'PRICE_CONFIRMED';
  request.updatedAt = new Date().toISOString();
  memoryStore.foodRequests.set(requestId, request);

  return res.status(200).json(successResponse({
    requestId,
    unitPrice: numericPrice,
    quantity: request.quantity,
    totalPrice,
    status: request.status
  }));
}
