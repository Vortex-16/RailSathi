import { VendorMatchingService } from '../../../src/services/VendorMatchingService';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'POST') {
    return res.status(405).json(errorResponse('Method Not Allowed'));
  }

  const requestId = (req.query?.id || '').toString();
  const { vendorId } = req.body || {};

  if (!requestId || !vendorId) {
    return res.status(400).json(errorResponse('requestId and vendorId are required'));
  }

  const claimResult = VendorMatchingService.atomicClaim(requestId, vendorId);
  if (!claimResult.success) {
    return res.status(409).json(errorResponse(claimResult.message, 'CLAIM_CONFLICT'));
  }

  return res.status(200).json(successResponse(claimResult.request));
}
