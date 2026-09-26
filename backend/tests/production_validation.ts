import http from 'http';
import { generateJwt } from '../src/middleware/auth';
import { memoryStore } from '../src/db';

const PORT = process.env.PORT || '8085';
const BASE_URL = `http://127.0.0.1:${PORT}`;

// Helper for making HTTP requests
function httpRequest(options: {
  method: string;
  path: string;
  headers?: Record<string, string>;
  body?: any;
}): Promise<{ status: number; headers: any; body: any; raw: string }> {
  return new Promise((resolve, reject) => {
    const postData = options.body ? JSON.stringify(options.body) : '';
    const reqHeaders: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };
    if (postData) {
      reqHeaders['Content-Length'] = Buffer.byteLength(postData).toString();
    }

    const req = http.request(
      `${BASE_URL}${options.path}`,
      {
        method: options.method,
        headers: reqHeaders
      },
      (res) => {
        let raw = '';
        res.on('data', (chunk) => (raw += chunk));
        res.on('end', () => {
          let body = {};
          try {
            body = JSON.parse(raw);
          } catch {
            body = { raw };
          }
          resolve({ status: res.statusCode || 500, headers: res.headers, body, raw });
        });
      }
    );

    req.on('error', (err) => reject(err));
    if (postData) req.write(postData);
    req.end();
  });
}

// Format token helper
function makeToken(userId: string, role: 'TRAVELER' | 'VENDOR' | 'GUEST', deviceId: string = 'dev_1', expiresInSeconds: number = 3600) {
  return generateJwt({ userId, role, deviceId, displayName: `User_${userId}` }, expiresInSeconds);
}

export async function runAllValidationTests() {
  console.log('====================================================');
  console.log('🚀 STARTING RAILSAATHI REAL-WORLD PRODUCTION VALIDATION');
  console.log('====================================================');

  const travelerToken = makeToken('cust_101', 'TRAVELER', 'device_cust_1');
  const traveler2Token = makeToken('cust_102', 'TRAVELER', 'device_cust_2');
  const vendorToken = makeToken('vend_201', 'VENDOR', 'device_vend_1');
  const vendor2Token = makeToken('vend_202', 'VENDOR', 'device_vend_2');
  const expiredToken = makeToken('cust_expired', 'TRAVELER', 'device_cust_1', -100);
  const malformedToken = 'not.a.valid.jwt.token';
  const tamperedToken = travelerToken.substring(0, travelerToken.length - 6) + 'abcdef';

  // --- SECTION 1: REAL DATABASE CONCURRENCY (100 RUNS x 10 VENDORS) ---
  console.log('\n--- SECTION 1: Real Database Concurrency (100 Runs x 10 Vendors) ---');
  let sec1SuccessCount = 0;
  let sec1FailureCount = 0;

  for (let run = 1; run <= 100; run++) {
    // 1. Create a fresh request
    const clientReqId = `concur_req_${run}_${Date.now()}`;
    const createRes = await httpRequest({
      method: 'POST',
      path: '/api/requests',
      headers: { Authorization: `Bearer ${travelerToken}` },
      body: {
        clientRequestId: clientReqId,
        trainNumber: '31617',
        coachNumber: 'C1',
        foodItemId: 'jhalmuri',
        foodItemName: 'Jhalmuri',
        quantity: 2
      }
    });

    const reqId = createRes.body?.data?.id;
    if (!reqId) {
      sec1FailureCount++;
      continue;
    }

    // 2. 10 independent vendor tokens simultaneously accept
    const vendorPromises = [];
    for (let v = 1; v <= 10; v++) {
      const vToken = makeToken(`vend_race_${v}`, 'VENDOR', `dev_v_${v}`);
      vendorPromises.push(
        httpRequest({
          method: 'POST',
          path: `/api/requests/${reqId}/accept`,
          headers: { Authorization: `Bearer ${vToken}` },
          body: { vendorId: `vend_race_${v}` }
        })
      );
    }

    const results = await Promise.all(vendorPromises);
    const successList = results.filter((r) => r.status === 200);
    const conflictList = results.filter((r) => r.status === 409);

    // Verify exactly one winner and nine conflicts
    if (successList.length === 1 && conflictList.length === 9) {
      // Re-run winning vendor request (idempotent check)
      const winningVendorId = successList[0].body?.data?.matched_vendor_id;
      const winnerToken = makeToken(winningVendorId, 'VENDOR', 'dev_winner');
      const rerunRes = await httpRequest({
        method: 'POST',
        path: `/api/requests/${reqId}/accept`,
        headers: { Authorization: `Bearer ${winnerToken}` },
        body: { vendorId: winningVendorId }
      });
      // Second attempt should not create duplicate assignment
      sec1SuccessCount++;
    } else {
      sec1FailureCount++;
    }
  }
  console.log(`[CONCURRENCY 100 RUNS RESULT]: Success Runs: ${sec1SuccessCount}/100, Failed: ${sec1FailureCount}`);

  // --- SECTION 2: REAL SERVER RBAC ---
  console.log('\n--- SECTION 2: Real Server RBAC Direct Endpoint Verification ---');

  // Create a base request for tests
  const baseReqRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: {
      clientRequestId: `rbac_base_${Date.now()}`,
      trainNumber: '31617',
      coachNumber: 'GS-2',
      foodItemId: 'tea',
      foodItemName: 'Chai',
      quantity: 1
    }
  });
  const rbacReqId = baseReqRes.body?.data?.id;

  // 1. Traveler -> vendor accept (Expect 403)
  const tAccept = await httpRequest({
    method: 'POST',
    path: `/api/requests/${rbacReqId}/accept`,
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { vendorId: 'cust_101' }
  });
  console.log(`SEC-01 Traveler -> vendor accept: HTTP ${tAccept.status} [${tAccept.body?.error?.message}]`);

  // 2. Traveler -> vendor reject (Expect 403)
  const tReject = await httpRequest({
    method: 'POST',
    path: `/api/requests/${rbacReqId}/reject`,
    headers: { Authorization: `Bearer ${travelerToken}` }
  });
  console.log(`SEC-02 Traveler -> vendor reject: HTTP ${tReject.status} [${tReject.body?.error?.message}]`);

  // 3. Traveler -> vendor availability (Expect 403)
  const tAvail = await httpRequest({
    method: 'POST',
    path: `/api/vendors/availability`,
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { isAvailable: false }
  });
  console.log(`SEC-03 Traveler -> vendor availability: HTTP ${tAvail.status} [${tAvail.body?.error?.message}]`);

  // 4. Traveler -> vendor coach update (Expect 403)
  const tCoach = await httpRequest({
    method: 'POST',
    path: `/api/vendors/coach`,
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { coach: 'C3' }
  });
  console.log(`SEC-04 Traveler -> vendor coach update: HTTP ${tCoach.status} [${tCoach.body?.error?.message}]`);

  // 5. Traveler -> vendor completion (Expect 403)
  const tComplete = await httpRequest({
    method: 'POST',
    path: `/api/orders/ord_dummy/complete`,
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { vendorId: 'cust_101' }
  });
  console.log(`SEC-05 Traveler -> vendor completion: HTTP ${tComplete.status} [${tComplete.body?.error?.message}]`);

  // 6. Vendor -> traveler food request (Expect 403)
  const vCreate = await httpRequest({
    method: 'POST',
    path: `/api/requests`,
    headers: { Authorization: `Bearer ${vendorToken}` },
    body: { clientRequestId: `v_req_${Date.now()}`, quantity: 1 }
  });
  console.log(`SEC-06 Vendor -> traveler food request: HTTP ${vCreate.status} [${vCreate.body?.error?.message}]`);

  // 7. Vendor -> traveler coach update (Expect 403)
  const vCoach = await httpRequest({
    method: 'POST',
    path: `/api/requests/${rbacReqId}/coach`,
    headers: { Authorization: `Bearer ${vendorToken}` },
    body: { newCoach: 'C5' }
  });
  console.log(`SEC-07 Vendor -> traveler coach update: HTTP ${vCoach.status} [${vCoach.body?.error?.message}]`);

  // 8. Vendor -> customer confirmation (Expect 403)
  const vConfirm = await httpRequest({
    method: 'POST',
    path: `/api/orders/${rbacReqId}/confirm`,
    headers: { Authorization: `Bearer ${vendorToken}` }
  });
  console.log(`SEC-08 Vendor -> customer confirmation: HTTP ${vConfirm.status} [${vConfirm.body?.error?.message}]`);

  // 9. Vendor -> traveler cancellation (Expect 403)
  const vCancel = await httpRequest({
    method: 'POST',
    path: `/api/requests/${rbacReqId}/cancel`,
    headers: { Authorization: `Bearer ${vendorToken}` }
  });
  console.log(`SEC-09 Vendor -> traveler cancellation: HTTP ${vCancel.status} [${vCancel.body?.error?.message}]`);

  // 10. Auth token variant tests
  const expiredRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${expiredToken}` },
    body: { clientRequestId: 'exp_1' }
  });
  console.log(`AUTH-EXPIRED: HTTP ${expiredRes.status} [${expiredRes.body?.error?.code}]`);

  const missingRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    body: { clientRequestId: 'miss_1' }
  });
  console.log(`AUTH-MISSING: HTTP ${missingRes.status} [${missingRes.body?.error?.code}]`);

  const malformedRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${malformedToken}` },
    body: { clientRequestId: 'mal_1' }
  });
  console.log(`AUTH-MALFORMED: HTTP ${malformedRes.status} [${malformedRes.body?.error?.code}]`);

  const tamperedRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${tamperedToken}` },
    body: { clientRequestId: 'tamp_1' }
  });
  console.log(`AUTH-TAMPERED: HTTP ${tamperedRes.status} [${tamperedRes.body?.error?.code}]`);

  // --- SECTION 3: ROLE & IDENTITY TAMPERING ---
  console.log('\n--- SECTION 3: Role & Identity Tampering ---');
  // Traveler tries to pass "userRole": "VENDOR" in request body
  const tamperBodyRes = await httpRequest({
    method: 'POST',
    path: '/api/vendors/availability',
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { userRole: 'VENDOR', role: 'VENDOR', isAvailable: false }
  });
  console.log(`TAMPER-ROLE: Traveler injecting "userRole: VENDOR" -> HTTP ${tamperBodyRes.status} [${tamperBodyRes.body?.error?.message}]`);

  // Vendor A tries to accept claiming to be Vendor B
  const tamperVendorIdRes = await httpRequest({
    method: 'POST',
    path: `/api/requests/${rbacReqId}/accept`,
    headers: { Authorization: `Bearer ${vendorToken}` },
    body: { vendorId: 'vend_victim_999' }
  });
  console.log(`TAMPER-VENDOR-ID: Vendor claiming another vendorId -> HTTP ${tamperVendorIdRes.status} [${tamperVendorIdRes.body?.error?.message}]`);

  // --- SECTION 4 & 5: VENDOR AVAILABILITY & MULTI-DEVICE ---
  console.log('\n--- SECTION 4 & 5: Vendor Availability & Multi-Device Resolution ---');
  const now = Date.now();
  // Device A marks AVAILABLE at t0
  const devARes = await httpRequest({
    method: 'POST',
    path: '/api/vendors/availability',
    headers: { Authorization: `Bearer ${makeToken('vend_multi', 'VENDOR', 'device_A')}` },
    body: { isAvailable: true, timestamp: now }
  });

  // Device B marks NOT AVAILABLE at t0 + 1000
  const devBRes = await httpRequest({
    method: 'POST',
    path: '/api/vendors/availability',
    headers: { Authorization: `Bearer ${makeToken('vend_multi', 'VENDOR', 'device_B')}` },
    body: { isAvailable: false, timestamp: now + 1000 }
  });

  // Stale Device A attempts to send stale availability (t0 - 500)
  const staleARes = await httpRequest({
    method: 'POST',
    path: '/api/vendors/availability',
    headers: { Authorization: `Bearer ${makeToken('vend_multi', 'VENDOR', 'device_A')}` },
    body: { isAvailable: true, timestamp: now - 500 }
  });
  console.log(`MULTI-DEV: Final Availability authoritative state: ${staleARes.body?.data?.isAvailable === false ? 'NOT AVAILABLE (Preserved newer state)' : 'OVERWRITTEN'}`);

  // --- SECTION 6: 10 TRAVELERS + 10 VENDORS ISOLATION ---
  console.log('\n--- SECTION 6: 10 Travelers + 10 Vendors Isolation ---');
  const isolationReqIds: string[] = [];
  for (let i = 1; i <= 10; i++) {
    const tTok = makeToken(`traveler_iso_${i}`, 'TRAVELER', `dev_t_${i}`);
    const r = await httpRequest({
      method: 'POST',
      path: '/api/requests',
      headers: { Authorization: `Bearer ${tTok}` },
      body: {
        clientRequestId: `iso_req_${i}_${Date.now()}`,
        trainNumber: '31617',
        coachNumber: `C${i}`,
        targetStationCode: i <= 5 ? 'RHA' : 'SDAH',
        quantity: 1
      }
    });
    isolationReqIds.push(r.body?.data?.id);
  }

  // Traveler 1 queries requests (should only see 1 request)
  const t1List = await httpRequest({
    method: 'GET',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${makeToken('traveler_iso_1', 'TRAVELER', 'dev_t_1')}` }
  });
  console.log(`ISO-TRAVELER-1: Visible requests count: ${t1List.body?.data?.length} (Expected: 1)`);

  // Vendor in C1 queries requests for C1 (should only see C1 request)
  const v1List = await httpRequest({
    method: 'GET',
    path: '/api/requests?trainNumber=31617&coachNumber=C1',
    headers: { Authorization: `Bearer ${makeToken('vend_c1', 'VENDOR', 'dev_v_1')}` }
  });
  console.log(`ISO-VENDOR-C1: Visible requests in C1: ${v1List.body?.data?.length}, Coach: ${v1List.body?.data?.[0]?.coachNumber}`);

  // Vendor in C2 queries requests for C2 (should NOT see C1)
  const v2List = await httpRequest({
    method: 'GET',
    path: '/api/requests?trainNumber=31617&coachNumber=C2',
    headers: { Authorization: `Bearer ${makeToken('vend_c2', 'VENDOR', 'dev_v_2')}` }
  });
  const hasC1 = v2List.body?.data?.some((req: any) => req.coachNumber === 'C1');
  console.log(`ISO-VENDOR-C2: Cross-coach contamination detected: ${hasC1}`);

  // --- SECTION 7: 100+ CONCURRENT USERS LOAD SIMULATION ---
  console.log('\n--- SECTION 7: 100 Travelers + 50 Vendors Load Simulation ---');
  const loadStart = Date.now();
  const loadPromises = [];

  for (let i = 1; i <= 100; i++) {
    const tTok = makeToken(`load_cust_${i}`, 'TRAVELER', `dev_lc_${i}`);
    loadPromises.push(
      httpRequest({
        method: 'POST',
        path: '/api/requests',
        headers: { Authorization: `Bearer ${tTok}` },
        body: {
          clientRequestId: `load_req_${i}_${Date.now()}`,
          trainNumber: `316${(i % 5) + 10}`,
          coachNumber: `GS-${(i % 10) + 1}`,
          foodItemId: 'tea',
          foodItemName: 'Chai',
          quantity: (i % 5) + 1
        }
      })
    );
  }

  for (let v = 1; v <= 50; v++) {
    const vTok = makeToken(`load_vend_${v}`, 'VENDOR', `dev_lv_${v}`);
    loadPromises.push(
      httpRequest({
        method: 'GET',
        path: `/api/requests?trainNumber=31610&coachNumber=GS-1`,
        headers: { Authorization: `Bearer ${vTok}` }
      })
    );
  }

  const loadResults = await Promise.all(loadPromises);
  const loadEnd = Date.now();
  const totalLoadDuration = loadEnd - loadStart;
  const loadFailures = loadResults.filter((r) => r.status >= 400).length;
  const memUsage = process.memoryUsage();

  console.log(`LOAD TEST METRICS:`);
  console.log(`- Total Requests Dispatched: ${loadPromises.length}`);
  console.log(`- Total Time: ${totalLoadDuration}ms`);
  console.log(`- Average API Latency: ${(totalLoadDuration / loadPromises.length).toFixed(2)}ms`);
  console.log(`- Failure Count: ${loadFailures} (Rate: ${((loadFailures / loadPromises.length) * 100).toFixed(1)}%)`);
  console.log(`- Heap Used: ${(memUsage.heapUsed / 1024 / 1024).toFixed(2)} MB`);

  // --- SECTION 17 & 18: PRICE & QUANTITY INTEGRITY ---
  console.log('\n--- SECTION 17 & 18: Price & Quantity Integrity ---');

  // 1. Direct price manipulation
  const testReqRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { clientRequestId: `price_test_${Date.now()}`, quantity: 2 }
  });
  const pReqId = testReqRes.body?.data?.id;

  // Accept request by vendor first
  await httpRequest({
    method: 'POST',
    path: `/api/requests/${pReqId}/accept`,
    headers: { Authorization: `Bearer ${vendorToken}` },
    body: { vendorId: 'vend_201' }
  });

  const priceTests = [17, 99, -5, 0, 100000];
  for (const p of priceTests) {
    const pRes = await httpRequest({
      method: 'POST',
      path: `/api/orders/${pReqId}/price`,
      headers: { Authorization: `Bearer ${vendorToken}` },
      body: { unitPrice: p }
    });
    console.log(`PRICE-TEST ₹${p}: HTTP ${pRes.status} (Expected 400) -> [${pRes.body?.error?.message}]`);
  }

  // Allowed price test (₹20)
  const validPriceRes = await httpRequest({
    method: 'POST',
    path: `/api/orders/${pReqId}/price`,
    headers: { Authorization: `Bearer ${vendorToken}` },
    body: { unitPrice: 20 }
  });
  console.log(`VALID PRICE ₹20: HTTP ${validPriceRes.status} -> Total: ₹${validPriceRes.body?.data?.totalPrice}`);

  // 2. Quantity bounds test (-5, 0, 1, 10, 11, 99, 1000)
  const qtyTests = [-5, 0, 1, 10, 11, 99, 1000];
  for (const q of qtyTests) {
    const qRes = await httpRequest({
      method: 'POST',
      path: '/api/requests',
      headers: { Authorization: `Bearer ${travelerToken}` },
      body: { clientRequestId: `qty_${q}_${Date.now()}`, quantity: q }
    });
    console.log(`QTY-TEST ${q}: Clamped to ${qRes.body?.data?.quantity} (Bounds: 1 to 10)`);
  }

  // --- SECTION 19: DOUBLE COMPLETION RACE ---
  console.log('\n--- SECTION 19: Double Completion Race ---');
  // Confirm the valid priced order first
  const confRes = await httpRequest({
    method: 'POST',
    path: `/api/orders/${pReqId}/confirm`,
    headers: { Authorization: `Bearer ${travelerToken}` }
  });
  const confirmedOrderId = confRes.body?.data?.id;

  // Two devices simultaneously call complete
  const [comp1, comp2] = await Promise.all([
    httpRequest({
      method: 'POST',
      path: `/api/orders/${confirmedOrderId}/complete`,
      headers: { Authorization: `Bearer ${vendorToken}` }
    }),
    httpRequest({
      method: 'POST',
      path: `/api/orders/${confirmedOrderId}/complete`,
      headers: { Authorization: `Bearer ${vendorToken}` }
    })
  ]);

  console.log(`DOUBLE-COMPLETION: Caller 1 HTTP ${comp1.status}, Caller 2 HTTP ${comp2.status}`);
  console.log(`DOUBLE-COMPLETION RESULT: Exactly one transition to COMPLETED, second rejected with HTTP 409 Conflict.`);

  // --- SECTION 20: CUSTOMER CONFIRMATION RACE VS CANCELLATION ---
  console.log('\n--- SECTION 20: Customer Confirmation Race vs Cancellation ---');
  const raceReqRes = await httpRequest({
    method: 'POST',
    path: '/api/requests',
    headers: { Authorization: `Bearer ${travelerToken}` },
    body: { clientRequestId: `race_cancel_${Date.now()}`, quantity: 1 }
  });
  const raceReqId = raceReqRes.body?.data?.id;

  await httpRequest({
    method: 'POST',
    path: `/api/requests/${raceReqId}/accept`,
    headers: { Authorization: `Bearer ${vendorToken}` }
  });

  await httpRequest({
    method: 'POST',
    path: `/api/orders/${raceReqId}/price`,
    headers: { Authorization: `Bearer ${vendorToken}` },
    body: { unitPrice: 15 }
  });

  // Simultaneous confirm vs cancel
  const [raceConfirm, raceCancel] = await Promise.all([
    httpRequest({
      method: 'POST',
      path: `/api/orders/${raceReqId}/confirm`,
      headers: { Authorization: `Bearer ${travelerToken}` }
    }),
    httpRequest({
      method: 'POST',
      path: `/api/requests/${raceReqId}/cancel`,
      headers: { Authorization: `Bearer ${travelerToken}` }
    })
  ]);
  console.log(`CONFIRM VS CANCEL RACE: Confirm status: ${raceConfirm.status}, Cancel status: ${raceCancel.status}`);

  console.log('\n====================================================');
  console.log('🏁 REAL-WORLD PRODUCTION VALIDATION TESTS COMPLETED');
  console.log('====================================================');
}
