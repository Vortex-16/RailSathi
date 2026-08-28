import url from 'url';

// Unified Serverless Handler (single function for all /api/* routes)
import healthHandler from './health';
import googleAuthHandler from './auth/google';
import budgetHandler from './budget/index';
import lookupStationsHandler from './lookup/stations';
import lookupSearchHandler from './lookup/search/index';
import orderPriceHandler from './orders/[id]/price';
import orderConfirmHandler from './orders/[id]/confirm';
import orderCompleteHandler from './orders/[id]/complete';
import profileHandler from './profile/index';
import requestsHandler from './requests/index';
import requestAcceptHandler from './requests/[id]/accept';
import sessionHandler from './session/index';
import stationSearchHandler from './stations/search';
import stationLiveHandler from './stations/[code]/live';
import stationTrainsHandler from './stations/[code]/trains';
import syncHandler from './sync/index';
import trainsLocalHandler from './trains/local';
import trainLiveHandler from './trains/[number]/live';
import trainCoachesHandler from './trains/[number]/coaches';

export default async function handler(req: any, res: any) {
  const parsedUrl = url.parse(req.url || '/', true);
  const pathname = parsedUrl.pathname || '/';
  req.query = { ...parsedUrl.query, ...(req.query || {}) };

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-session-token');

  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  // 1. Health
  if (pathname === '/api/health' || pathname === '/health' || pathname === '/') {
    return await healthHandler(req, res);
  }

  // 2. Auth - Google OAuth & Session
  if (pathname === '/api/auth/google') {
    return await googleAuthHandler(req, res);
  }

  // 3. User Profile
  if (pathname === '/api/profile') {
    return await profileHandler(req, res);
  }

  // 4. Commuter Budget
  if (pathname === '/api/budget') {
    return await budgetHandler(req, res);
  }

  // 5. Lookups
  if (pathname === '/api/lookup/stations') {
    return await lookupStationsHandler(req, res);
  }
  if (pathname === '/api/lookup/search') {
    return await lookupSearchHandler(req, res);
  }

  // 6. Session & Sync
  if (pathname === '/api/session') {
    return await sessionHandler(req, res);
  }
  if (pathname === '/api/sync') {
    return await syncHandler(req, res);
  }

  // 7. Station Search
  if (pathname === '/api/stations/search') {
    return await stationSearchHandler(req, res);
  }

  // 8. Dynamic Station Code routes
  const stationLiveMatch = pathname.match(/^\/api\/stations\/([a-zA-Z0-9]+)\/live\/?$/);
  if (stationLiveMatch) {
    req.query.code = stationLiveMatch[1];
    return await stationLiveHandler(req, res);
  }

  const stationTrainsMatch = pathname.match(/^\/api\/stations\/([a-zA-Z0-9]+)\/trains\/?$/);
  if (stationTrainsMatch) {
    req.query.code = stationTrainsMatch[1];
    return await stationTrainsHandler(req, res);
  }

  // 9. Trains routes
  if (pathname === '/api/trains/local') {
    return await trainsLocalHandler(req, res);
  }

  const trainLiveMatch = pathname.match(/^\/api\/trains\/([a-zA-Z0-9]+)\/live\/?$/);
  if (trainLiveMatch) {
    req.query.number = trainLiveMatch[1];
    return await trainLiveHandler(req, res);
  }

  const trainCoachesMatch = pathname.match(/^\/api\/trains\/([a-zA-Z0-9]+)\/coaches\/?$/);
  if (trainCoachesMatch) {
    req.query.number = trainCoachesMatch[1];
    return await trainCoachesHandler(req, res);
  }

  // 10. Requests routes
  if (pathname === '/api/requests') {
    return await requestsHandler(req, res);
  }

  const requestAcceptMatch = pathname.match(/^\/api\/requests\/([a-zA-Z0-9_-]+)\/accept\/?$/);
  if (requestAcceptMatch) {
    req.query.id = requestAcceptMatch[1];
    return await requestAcceptHandler(req, res);
  }

  // 11. Orders dynamic routes
  const orderPriceMatch = pathname.match(/^\/api\/orders\/([a-zA-Z0-9_-]+)\/price\/?$/);
  if (orderPriceMatch) {
    req.query.id = orderPriceMatch[1];
    return await orderPriceHandler(req, res);
  }

  const orderConfirmMatch = pathname.match(/^\/api\/orders\/([a-zA-Z0-9_-]+)\/confirm\/?$/);
  if (orderConfirmMatch) {
    req.query.id = orderConfirmMatch[1];
    return await orderConfirmHandler(req, res);
  }

  const orderCompleteMatch = pathname.match(/^\/api\/orders\/([a-zA-Z0-9_-]+)\/complete\/?$/);
  if (orderCompleteMatch) {
    req.query.id = orderCompleteMatch[1];
    return await orderCompleteHandler(req, res);
  }

  return res.status(404).json({
    success: false,
    error: {
      code: 'NOT_FOUND',
      message: `Endpoint ${req.method} ${pathname} not found`
    }
  });
}
