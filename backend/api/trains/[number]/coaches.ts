import { railRadarClient } from '../../../src/providers/RailRadarClient';
import { query } from '../../../src/db';
import { successResponse, errorResponse } from '../../../src/utils/response';

const DEFAULT_COACH_COMPOSITIONS: Record<string, string[]> = {
  // Suburban EMU 9/12 car sequences
  'EMU': ['CAB-1', 'LD-1', 'VND-1', 'GS-1', 'GS-2', 'GS-3', 'VND-2', 'LD-2', 'CAB-2'],
  'EMU_12': ['CAB-1', 'LD-1', 'VND-1', 'GS-1', 'GS-2', 'GS-3', 'VND-2', 'LD-2', 'GS-4', 'GS-5', 'LD-3', 'CAB-2'],
  // Express / Mail sequences
  'EXP_DEFAULT': ['LOCO', 'EOG-1', 'GS-1', 'S1', 'S2', 'S3', 'S4', 'S5', 'B1', 'B2', 'B3', 'A1', 'H1', 'PC', 'GS-2', 'EOG-2']
};

export default async function handler(req: any, res: any) {
  if (req.method !== 'GET') {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }

  const { number, station } = req.query;
  if (!number) {
    return res.status(400).json(errorResponse('Train number is required'));
  }

  const trainNum = String(number);

  try {
    // 1. Check PostgreSQL cached coach layout
    const dbRes = await query(
      'SELECT coach_code, coach_type, position_sequence FROM train_coaches WHERE train_number = $1 ORDER BY position_sequence ASC',
      [trainNum]
    );

    if (dbRes && dbRes.rows && dbRes.rows.length > 0) {
      const coaches = dbRes.rows.map((r: any) => r.coach_code);
      return res.status(200).json(successResponse({
        trainNumber: trainNum,
        coaches,
        source: 'db'
      }));
    }

    // 2. Fetch from RailRadar via server API key
    const liveCoaches = await railRadarClient.getTrainCoaches(trainNum, station as string);
    if (liveCoaches && Array.isArray(liveCoaches) && liveCoaches.length > 0) {
      return res.status(200).json(successResponse({
        trainNumber: trainNum,
        coaches: liveCoaches,
        source: 'live'
      }));
    }

    // 3. Fallback to train-type specific rake composition
    const isEmu = trainNum.startsWith('3') || trainNum.startsWith('9') || trainNum.length === 5;
    const fallbackList = isEmu ? DEFAULT_COACH_COMPOSITIONS['EMU'] : DEFAULT_COACH_COMPOSITIONS['EXP_DEFAULT'];

    return res.status(200).json(successResponse({
      trainNumber: trainNum,
      coaches: fallbackList,
      source: 'fallback'
    }));
  } catch (err: any) {
    return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
  }
}
