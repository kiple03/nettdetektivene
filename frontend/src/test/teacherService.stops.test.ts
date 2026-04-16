import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getStops } from '../services/teacherService';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import apiClient from '../api/client';
const mockedGet = apiClient.get as ReturnType<typeof vi.fn>;

const RAW_STOPS = [
  { id: 'stop-uuid-1', title: 'Stop One', description: 'First stop', displayOrder: 1 },
  { id: 'stop-uuid-2', title: 'Stop Two', description: null, displayOrder: 2 },
];

beforeEach(() => {
  vi.restoreAllMocks();
  vi.clearAllMocks();
});

describe('getStops', () => {
  it('calls correct endpoint with no arguments', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });
    await getStops();
    expect(mockedGet).toHaveBeenCalledWith('/teacher/stops');
  });

  it('maps all fields correctly', async () => {
    mockedGet.mockResolvedValueOnce({ data: RAW_STOPS });
    const result = await getStops();
    expect(result).toEqual([
      { id: 'stop-uuid-1', title: 'Stop One', description: 'First stop', displayOrder: 1 },
      { id: 'stop-uuid-2', title: 'Stop Two', description: null, displayOrder: 2 },
    ]);
  });

  it('handles null description', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_STOPS[1]] });
    const result = await getStops();
    expect(result[0].description).toBeNull();
  });

  it('returns empty array when no stops', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });
    const result = await getStops();
    expect(result).toEqual([]);
  });

  it('rethrows API errors', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Network Error'));
    await expect(getStops()).rejects.toThrow('Network Error');
  });
});
