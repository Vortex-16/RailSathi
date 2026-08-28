// RailRadar Server-Side API Client
// Note: RAILRADAR_API_KEY is read strictly from server environment variables.

const RAILRADAR_BASE_URL = 'https://api.railradar.in/v1';

export class RailRadarClient {
  private apiKey: string | null;
  private cache: Map<string, { data: any; expiry: number }> = new Map();

  constructor() {
    this.apiKey = process.env.RAILRADAR_API_KEY || null;
  }

  private async fetchRailRadar<T>(endpoint: string, cacheTtlSeconds: number = 60): Promise<T | null> {
    const cacheKey = endpoint;
    const cached = this.cache.get(cacheKey);
    if (cached && cached.expiry > Date.now()) {
      return cached.data;
    }

    if (!this.apiKey) {
      // Graceful fallback if API key is not yet set in environment
      return null;
    }

    try {
      const url = `${RAILRADAR_BASE_URL}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${this.apiKey}`,
          'Accept': 'application/json'
        }
      });

      if (!response.ok) {
        console.warn(`[RailRadar] Request failed: ${endpoint} Status: ${response.status}`);
        return null;
      }

      const envelope: any = await response.json();
      if (envelope && envelope.success && envelope.data) {
        this.cache.set(cacheKey, {
          data: envelope.data,
          expiry: Date.now() + (cacheTtlSeconds * 1000)
        });
        return envelope.data;
      }
      return envelope?.data || null;
    } catch (err) {
      console.error(`[RailRadar] Network error fetching ${endpoint}:`, err);
      return null;
    }
  }

  // 1. Station Autocomplete & Directory
  async searchStations(query: string) {
    return this.fetchRailRadar(`/lookup/search/stations?q=${encodeURIComponent(query)}`, 300);
  }

  async getStationDirectory(code: string) {
    return this.fetchRailRadar(`/lookup/stations/${encodeURIComponent(code)}`, 3600);
  }

  // 2. Station Trains Board & Live Arrivals/Departures
  async getStationTrains(code: string) {
    return this.fetchRailRadar(`/stations/${encodeURIComponent(code)}/trains`, 180);
  }

  async getStationLiveBoard(code: string) {
    return this.fetchRailRadar(`/stations/${encodeURIComponent(code)}/live`, 30);
  }

  // 3. Train Details & Live Status
  async getTrainDetails(number: string) {
    return this.fetchRailRadar(`/trains/${encodeURIComponent(number)}`, 1800);
  }

  async getTrainLiveStatus(number: string) {
    return this.fetchRailRadar(`/trains/${encodeURIComponent(number)}/live`, 20);
  }

  async getTrainRoute(number: string) {
    return this.fetchRailRadar(`/trains/${encodeURIComponent(number)}/route`, 86400);
  }

  // 4. Suburban / Local Trains
  async getLocalTrains(city?: string) {
    const endpoint = city ? `/lookup/trains/local?city=${encodeURIComponent(city)}` : '/lookup/trains/local';
    return this.fetchRailRadar(endpoint, 1800);
  }

  async getLocalCities() {
    return this.fetchRailRadar('/lookup/trains/local/cities', 86400);
  }

  // 5. Trains Between Stations
  async getTrainsBetweenStations(fromCode: string, toCode: string) {
    return this.fetchRailRadar(`/trains/between/${encodeURIComponent(fromCode)}/${encodeURIComponent(toCode)}`, 600);
  }
}

export const railRadarClient = new RailRadarClient();
