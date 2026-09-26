import { VendorMatchingService } from '../../../src/services/VendorMatchingService';
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

  const requestId = (req.query?.id || '').toString();
  const { vendorId } = req.body || {};

  // Prevent identity tampering: caller cannot claim on behalf of another vendor
  const effectiveVendorId = auth.user.userId;
  if (vendorId && vendorId !== effectiveVendorId) {
    return res.status(403).json(errorResponse('Forbidden: Cannot claim order on behalf of another vendorId', 'FORBIDDEN'));
  }

  if (!requestId) {
    return res.status(400).json(errorResponse('requestId is required'));
  }

  const claimResult = VendorMatchingService.atomicClaim(requestId, effectiveVendorId);
  if (!claimResult.success) {
    return res.status(409).json(errorResponse(claimResult.message, 'CLAIM_CONFLICT'));
  }

  return res.status(200).json(successResponse(claimResult.request));
}
