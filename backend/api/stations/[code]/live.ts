import { railRadarClient } from '../../../src/providers/RailRadarClient';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'GET') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  const { code } = req.query;
  if (!code) {
    return res.status(400).json(errorResponse('Station code is required'));
  }

  try {
    const live = await railRadarClient.getStationLiveBoard(code as string);
    if (live && Array.isArray(live)) {
      return res.status(200).json(successResponse(live, 'live'));
    }

    // Default live board payload based on local timetables
    return res.status(200).json(successResponse([], 'fallback'));
  } catch (err: any) {
    return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
  }
}
