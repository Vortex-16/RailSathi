import { railRadarClient } from './RailRadarClient';

export interface StationDto {
  code: string;
  name: string;
  state: string;
  zone: string;
  latitude?: number;
  longitude?: number;
}

export interface TrainCandidateDto {
  trainNumber: string;
  trainName: string;
  originStationCode: string;
  originStationName: string;
  destStationCode: string;
  destStationName: string;
  departureTime: string;
  platform: string;
  type: string;
  suburbanCity?: string;
}

// Built-in Eastern Railway / Suburban timetable dataset as reliable fallback
const FALLBACK_STATIONS: StationDto[] = [
  { code: 'SDAH', name: 'Sealdah', state: 'West Bengal', zone: 'ER', latitude: 22.5697, longitude: 88.3712 },
  { code: 'BNXR', name: 'Bidhan Nagar Road', state: 'West Bengal', zone: 'ER', latitude: 22.5855, longitude: 88.3908 },
  { code: 'DDJ', name: 'Dum Dum Junction', state: 'West Bengal', zone: 'ER', latitude: 22.6214, longitude: 88.3934 },
  { code: 'BLH', name: 'Belgharia', state: 'West Bengal', zone: 'ER', latitude: 22.6482, longitude: 88.3891 },
  { code: 'AGP', name: 'Agarpara', state: 'West Bengal', zone: 'ER', latitude: 22.6731, longitude: 88.3842 },
  { code: 'SEP', name: 'Sodpur', state: 'West Bengal', zone: 'ER', latitude: 22.6961, longitude: 88.3789 },
  { code: 'KDH', name: 'Khardaha', state: 'West Bengal', zone: 'ER', latitude: 22.7214, longitude: 88.3736 },
  { code: 'TGH', name: 'Titagarh', state: 'West Bengal', zone: 'ER', latitude: 22.7428, longitude: 88.3710 },
  { code: 'BP', name: 'Barrackpore', state: 'West Bengal', zone: 'ER', latitude: 22.7634, longitude: 88.3689 },
  { code: 'NH', name: 'Naihati Junction', state: 'West Bengal', zone: 'ER', latitude: 22.8942, longitude: 88.4231 },
  { code: 'RHA', name: 'Ranaghat Junction', state: 'West Bengal', zone: 'ER', latitude: 23.1812, longitude: 88.5804 },
  { code: 'HWH', name: 'Howrah Junction', state: 'West Bengal', zone: 'ER', latitude: 22.5839, longitude: 88.3426 },
  { code: 'BT', name: 'Barasat Junction', state: 'West Bengal', zone: 'ER', latitude: 22.7224, longitude: 88.4831 },
  { code: 'CSMT', name: 'Mumbai CSMT', state: 'Maharashtra', zone: 'CR', latitude: 18.9401, longitude: 72.8353 },
  { code: 'MAS', name: 'Chennai Central', state: 'Tamil Nadu', zone: 'SR', latitude: 13.0827, longitude: 80.2707 }
];

const FALLBACK_TRAINS: TrainCandidateDto[] = [
  { trainNumber: '31223', trainName: 'Sealdah - Barrackpore Local', originStationCode: 'SDAH', originStationName: 'Sealdah', destStationCode: 'BP', destStationName: 'Barrackpore', departureTime: '08:42 AM', platform: 'PF 4', type: 'EMU Local', suburbanCity: 'Kolkata' },
  { trainNumber: '31415', trainName: 'Sealdah - Naihati Local', originStationCode: 'SDAH', originStationName: 'Sealdah', destStationCode: 'NH', destStationName: 'Naihati Jn', departureTime: '08:51 AM', platform: 'PF 2', type: 'EMU Local', suburbanCity: 'Kolkata' },
  { trainNumber: '31617', trainName: 'Sealdah - Ranaghat Local', originStationCode: 'SDAH', originStationName: 'Sealdah', destStationCode: 'RHA', destStationName: 'Ranaghat Jn', departureTime: '09:03 AM', platform: 'PF 1', type: 'EMU Local', suburbanCity: 'Kolkata' },
  { trainNumber: '31821', trainName: 'Sealdah - Krishnanagar City Local', originStationCode: 'SDAH', originStationName: 'Sealdah', destStationCode: 'KNJ', destStationName: 'Krishnanagar City', departureTime: '09:18 AM', platform: 'PF 3', type: 'EMU Fast Local', suburbanCity: 'Kolkata' },
  { trainNumber: '33815', trainName: 'Sealdah - Bongaon Local', originStationCode: 'SDAH', originStationName: 'Sealdah', destStationCode: 'BNGA', destStationName: 'Bongaon Jn', departureTime: '09:25 AM', platform: 'PF 5', type: 'EMU Local', suburbanCity: 'Kolkata' }
];

export class RailwayDataProvider {
  async searchStations(query: string): Promise<{ stations: StationDto[]; source: 'live' | 'fallback' }> {
    const live = await railRadarClient.searchStations(query);
    if (live && Array.isArray(live) && live.length > 0) {
      return { stations: live as StationDto[], source: 'live' };
    }
    const q = query.trim().toLowerCase();
    const matches = FALLBACK_STATIONS.filter(s => s.name.toLowerCase().includes(q) || s.code.toLowerCase().includes(q));
    return { stations: matches.length > 0 ? matches : FALLBACK_STATIONS.slice(0, 5), source: 'fallback' };
  }

  async getStationByCode(code: string): Promise<StationDto | null> {
    const live = await railRadarClient.getStationDirectory(code);
    if (live) return live as StationDto;
    return FALLBACK_STATIONS.find(s => s.code.equalsIgnoreCase(code)) || null;
  }

  async getStationDepartures(stationCode: string): Promise<{ trains: TrainCandidateDto[]; source: 'live' | 'fallback' }> {
    const live = await railRadarClient.getStationTrains(stationCode);
    if (live && Array.isArray(live) && live.length > 0) {
      return { trains: live as TrainCandidateDto[], source: 'live' };
    }
    return { trains: FALLBACK_TRAINS, source: 'fallback' };
  }

  async getTrainDetails(trainNumber: string): Promise<{ train: any; source: 'live' | 'fallback' }> {
    const live = await railRadarClient.getTrainDetails(trainNumber);
    if (live) return { train: live, source: 'live' };
    const match = FALLBACK_TRAINS.find(t => t.trainNumber === trainNumber);
    return {
      train: match || {
        trainNumber,
        trainName: 'Suburban EMU Local',
        originStationCode: 'SDAH',
        originStationName: 'Sealdah',
        destStationCode: 'RHA',
        destStationName: 'Ranaghat Jn'
      },
      source: 'fallback'
    };
  }

  async getLiveStatus(trainNumber: string): Promise<{ status: any; source: 'live' | 'fallback' }> {
    const live = await railRadarClient.getTrainLiveStatus(trainNumber);
    if (live) return { status: live, source: 'live' };
    return {
      status: {
        trainNumber,
        currentStation: 'Barrackpore',
        status: 'ON_TIME',
        delayMinutes: 0,
        speedKmph: 42,
        isLiveAvailable: false
      },
      source: 'fallback'
    };
  }
}

declare global {
  interface String {
    equalsIgnoreCase(other: string): boolean;
  }
}

String.prototype.equalsIgnoreCase = function(other: string): boolean {
  return this.toLowerCase() === other.toLowerCase();
};

export const railwayDataProvider = new RailwayDataProvider();
