export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
  };
  meta: {
    traceId: string;
    timestamp: string;
    source: 'live' | 'cache' | 'fallback';
  };
}

export function successResponse<T>(data: T, source: 'live' | 'cache' | 'fallback' = 'live'): ApiResponse<T> {
  return {
    success: true,
    data,
    meta: {
      traceId: Math.random().toString(36).substring(2, 10),
      timestamp: new Date().toISOString(),
      source
    }
  };
}

export function errorResponse(message: string, code: string = 'BAD_REQUEST'): ApiResponse {
  return {
    success: false,
    error: {
      code,
      message
    },
    meta: {
      traceId: Math.random().toString(36).substring(2, 10),
      timestamp: new Date().toISOString(),
      source: 'live'
    }
  };
}
