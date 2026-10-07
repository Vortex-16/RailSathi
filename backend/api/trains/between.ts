import { railwayDataProvider } from '../../src/providers/RailwayDataProvider';
import { successResponse, errorResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  const from = (req.query?.from || '').toString().trim();
  const to = (req.query?.to || '').toString().trim();

  if (!from || !to) {
    return res.status(400).json(errorResponse('Both "from" and "to" station codes are required (e.g. ?from=HWH&to=HMZ)'));
  }

  try {
    const result = await railwayDataProvider.getTrainsBetweenStations(from, to);
    const source: 'live' | 'cache' | 'fallback' = result.source === 'fallback' ? 'fallback' : 'live';
    return res.status(200).json(successResponse(result.trains, source));
  } catch (error: any) {
    console.error(`[API /trains/between] Error for ${from}->${to}:`, error);
    return res.status(500).json(errorResponse(error?.message || 'Failed to fetch trains between stations'));
  }
}
