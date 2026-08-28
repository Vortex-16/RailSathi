import http from 'http';
import url from 'url';
import dotenv from 'dotenv';

dotenv.config();

// Import API Handlers
import healthHandler from '../api/health';
import googleAuthHandler from '../api/auth/google';
import budgetHandler from '../api/budget/index';
import lookupStationsHandler from '../api/lookup/stations';
import lookupSearchHandler from '../api/lookup/search/index';
import orderPriceHandler from '../api/orders/[id]/price';
import orderConfirmHandler from '../api/orders/[id]/confirm';
import orderCompleteHandler from '../api/orders/[id]/complete';
import profileHandler from '../api/profile/index';
import requestsHandler from '../api/requests/index';
import requestAcceptHandler from '../api/requests/[id]/accept';
import sessionHandler from '../api/session/index';
import stationSearchHandler from '../api/stations/search';
import stationLiveHandler from '../api/stations/[code]/live';
import stationTrainsHandler from '../api/stations/[code]/trains';
import syncHandler from '../api/sync/index';
import trainsLocalHandler from '../api/trains/local';
import trainLiveHandler from '../api/trains/[number]/live';
import trainCoachesHandler from '../api/trains/[number]/coaches';

const PORT = parseInt(process.env.PORT || '8080', 10);

function enhanceResponse(res: http.ServerResponse) {
  const enhanced = res as any;
  enhanced.status = function (statusCode: number) {
    this.statusCode = statusCode;
    return this;
  };
  enhanced.json = function (data: any) {
    if (!this.getHeader('Content-Type')) {
      this.setHeader('Content-Type', 'application/json');
    }
    this.end(JSON.stringify(data));
    return this;
  };
  return enhanced;
}

async function parseBody(req: http.IncomingMessage): Promise<any> {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', (chunk) => {
      body += chunk.toString();
    });
    req.on('end', () => {
      if (!body) return resolve({});
      try {
        resolve(JSON.parse(body));
      } catch (e) {
        resolve({ raw: body });
      }
    });
  });
}

const server = http.createServer(async (req, res) => {
  const enhancedRes = enhanceResponse(res);
  const parsedUrl = url.parse(req.url || '/', true);
  const pathname = parsedUrl.pathname || '/';
  const query = parsedUrl.query;

  // CORS headers
  enhancedRes.setHeader('Access-Control-Allow-Origin', '*');
  enhancedRes.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  enhancedRes.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-session-token');

  if (req.method === 'OPTIONS') {
    enhancedRes.writeHead(204);
    enhancedRes.end();
    return;
  }

  const enhancedReq = req as any;
  enhancedReq.query = query;
  if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH') {
    enhancedReq.body = await parseBody(req);
  } else {
    enhancedReq.body = {};
  }

  try {
    // 1. Health
    if (pathname === '/api/health' || pathname === '/health' || pathname === '/') {
      return await healthHandler(enhancedReq, enhancedRes);
    }

    // 2. Auth - Google OAuth & Session
    if (pathname === '/api/auth/google') {
      return await googleAuthHandler(enhancedReq, enhancedRes);
    }

    // 3. User Profile
    if (pathname === '/api/profile') {
      return await profileHandler(enhancedReq, enhancedRes);
    }

    // 4. Commuter Budget
    if (pathname === '/api/budget') {
      return await budgetHandler(enhancedReq, enhancedRes);
    }

    // 5. Lookups
    if (pathname === '/api/lookup/stations') {
      return await lookupStationsHandler(enhancedReq, enhancedRes);
    }
    if (pathname === '/api/lookup/search') {
      return await lookupSearchHandler(enhancedReq, enhancedRes);
    }

    // 6. Session & Sync
    if (pathname === '/api/session') {
      return await sessionHandler(enhancedReq, enhancedRes);
    }
    if (pathname === '/api/sync') {
      return await syncHandler(enhancedReq, enhancedRes);
    }

    // 7. Station Search
    if (pathname === '/api/stations/search') {
      return await stationSearchHandler(enhancedReq, enhancedRes);
    }

    // 8. Dynamic Station Code routes: /api/stations/:code/live and /api/stations/:code/trains
    const stationLiveMatch = pathname.match(/^\/api\/stations\/([a-zA-Z0-9]+)\/live\/?$/);
    if (stationLiveMatch) {
      enhancedReq.query.code = stationLiveMatch[1];
      return await stationLiveHandler(enhancedReq, enhancedRes);
    }

    const stationTrainsMatch = pathname.match(/^\/api\/stations\/([a-zA-Z0-9]+)\/trains\/?$/);
    if (stationTrainsMatch) {
      enhancedReq.query.code = stationTrainsMatch[1];
      return await stationTrainsHandler(enhancedReq, enhancedRes);
    }

    // 9. Trains routes: /api/trains/local, /api/trains/:number/live, /api/trains/:number/coaches
    if (pathname === '/api/trains/local') {
      return await trainsLocalHandler(enhancedReq, enhancedRes);
    }

    const trainLiveMatch = pathname.match(/^\/api\/trains\/([a-zA-Z0-9]+)\/live\/?$/);
    if (trainLiveMatch) {
      enhancedReq.query.number = trainLiveMatch[1];
      return await trainLiveHandler(enhancedReq, enhancedRes);
    }

    const trainCoachesMatch = pathname.match(/^\/api\/trains\/([a-zA-Z0-9]+)\/coaches\/?$/);
    if (trainCoachesMatch) {
      enhancedReq.query.number = trainCoachesMatch[1];
      return await trainCoachesHandler(enhancedReq, enhancedRes);
    }

    // 10. Requests routes: /api/requests and /api/requests/:id/accept
    if (pathname === '/api/requests') {
      return await requestsHandler(enhancedReq, enhancedRes);
    }

    const requestAcceptMatch = pathname.match(/^\/api\/requests\/([a-zA-Z0-9_-]+)\/accept\/?$/);
    if (requestAcceptMatch) {
      enhancedReq.query.id = requestAcceptMatch[1];
      return await requestAcceptHandler(enhancedReq, enhancedRes);
    }

    // 11. Orders dynamic routes: /api/orders/:id/price, /api/orders/:id/confirm, /api/orders/:id/complete
    const orderPriceMatch = pathname.match(/^\/api\/orders\/([a-zA-Z0-9_-]+)\/price\/?$/);
    if (orderPriceMatch) {
      enhancedReq.query.id = orderPriceMatch[1];
      return await orderPriceHandler(enhancedReq, enhancedRes);
    }

    const orderConfirmMatch = pathname.match(/^\/api\/orders\/([a-zA-Z0-9_-]+)\/confirm\/?$/);
    if (orderConfirmMatch) {
      enhancedReq.query.id = orderConfirmMatch[1];
      return await orderConfirmHandler(enhancedReq, enhancedRes);
    }

    const orderCompleteMatch = pathname.match(/^\/api\/orders\/([a-zA-Z0-9_-]+)\/complete\/?$/);
    if (orderCompleteMatch) {
      enhancedReq.query.id = orderCompleteMatch[1];
      return await orderCompleteHandler(enhancedReq, enhancedRes);
    }

    // 404 Route Not Found
    enhancedRes.status(404).json({
      success: false,
      error: {
        code: 'NOT_FOUND',
        message: `Endpoint ${req.method} ${pathname} not found on RailSaathi API server`
      }
    });
  } catch (error: any) {
    console.error(`Internal server error at ${pathname}:`, error);
    enhancedRes.status(500).json({
      success: false,
      error: {
        code: 'INTERNAL_ERROR',
        message: error?.message || 'Internal Server Error'
      }
    });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚂 RailSaathi Cloud API Server running on port ${PORT} (0.0.0.0:${PORT})`);
});
