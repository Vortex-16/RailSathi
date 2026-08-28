import { railwayDataProvider } from '../../../src/providers/RailwayDataProvider';
import { successResponse, errorResponse } from '../../../src/utils/response';

export default async function handler(req: any, res: any) {
  const number = (req.query?.number || '').toString();
  if (!number) {
    return res.status(400).json(errorResponse('Train number is required'));
  }

  const result = await railwayDataProvider.getLiveStatus(number);
  return res.status(200).json(successResponse(result.status, result.source));
}
