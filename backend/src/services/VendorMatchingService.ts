// Vendor Matching & Claim Lock Service
// Provides atomic request claiming and deterministic fairness scoring.

import { memoryStore } from '../db';

export interface VendorCandidate {
  id: string;
  name: string;
  specialityItemId: string;
  currentTrainNumber?: string;
  currentCoach?: string;
  todaySalesCount: number;
  lastActiveAt: number;
  isActive: boolean;
}

export class VendorMatchingService {
  // Score vendors for fair distribution
  static scoreVendor(
    vendor: VendorCandidate,
    requestedItemId: string,
    targetCoach: string
  ): number {
    let score = 0;

    // 1. Speciality Match (Crucial)
    if (vendor.specialityItemId === requestedItemId) {
      score += 100;
    }

    // 2. Coach Proximity
    if (vendor.currentCoach === targetCoach) {
      score += 50;
    } else {
      score += 20;
    }

    // 3. Fair Income Distribution (Prefer vendors with fewer sales today)
    const salesPenalty = Math.min(vendor.todaySalesCount * 5, 40);
    score += (40 - salesPenalty);

    // 4. Idle time boost (more idle = higher score)
    const minutesIdle = (Date.now() - vendor.lastActiveAt) / 60000;
    score += Math.min(Math.floor(minutesIdle) * 2, 20);

    return score;
  }

  // Atomic One-To-One Claim Lock
  static atomicClaim(requestId: string, vendorId: string): { success: boolean; message: string; request?: any } {
    const req = memoryStore.foodRequests.get(requestId);
    if (!req) {
      return { success: false, message: 'Request not found or expired' };
    }

    if (req.status !== 'REQUESTED' && req.status !== 'MATCHING' && req.status !== 'OFFERED_TO_VENDOR') {
      return { success: false, message: 'Request already claimed by another vendor.' };
    }

    // Lock atomically
    req.matched_vendor_id = vendorId;
    req.status = 'VENDOR_ACCEPTED';
    req.updated_at = new Date().toISOString();
    memoryStore.foodRequests.set(requestId, req);

    return { success: true, message: 'Request successfully claimed', request: req };
  }
}
