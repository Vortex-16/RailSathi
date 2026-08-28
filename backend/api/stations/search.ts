import { railwayDataProvider } from '../../src/providers/RailwayDataProvider';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  const q = (req.query?.q || req.query?.query || '').toString();
  if (!q || q.trim().length === 0) {
    return res.status(400).json(errorResponse('Query parameter "q" is required'));
  }

  const result = await railwayDataProvider.searchStations(q);
  return res.status(200).json(successResponse(result.stations, result.source));
}
