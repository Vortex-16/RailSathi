import { railwayDataProvider } from '../../../src/providers/RailwayDataProvider';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  const code = (req.query?.code || '').toString();
  if (!code) {
    return res.status(400).json(errorResponse('Station code is required'));
  }

  const result = await railwayDataProvider.getStationDepartures(code);
  return res.status(200).json(successResponse(result.trains, result.source));
}
