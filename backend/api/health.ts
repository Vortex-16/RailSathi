import { isDbConnected } from '../src/db';
import { successResponse } from '../src/utils/response';

export default async function handler(req: any, res: any) {
  const dbStatus = await isDbConnected();
  
  const payload = {
    status: 'ok',
    service: 'RailSaathi API',
    database: dbStatus ? 'connected' : 'memory_fallback',
    version: '1.0.0',
    railRadarEnabled: Boolean(process.env.RAILRADAR_API_KEY)
  };

  res.setHeader('Content-Type', 'application/json');
  return res.status(200).json(successResponse(payload));
}
