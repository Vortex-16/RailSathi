import { railRadarClient } from '../../src/providers/RailRadarClient';
import { successResponse } from '../../src/utils/response';

export default async function handler(req: any, res: any) {
  const city = (req.query?.city || 'Kolkata').toString();
  const data = await railRadarClient.getLocalTrains(city);
  return res.status(200).json(successResponse(data || []));
}
