import { railwayDataProvider } from '../../src/providers/RailwayDataProvider';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  if (req.method !== 'GET') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  try {
    const result = await railwayDataProvider.searchStations('');
    return res.status(200).json(successResponse(result.stations, result.source));
  } catch (err: any) {
    return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
  }
}
