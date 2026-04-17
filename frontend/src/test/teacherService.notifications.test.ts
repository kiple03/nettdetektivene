import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getNotifications, notifLsKey } from '../services/teacherService';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import apiClient from '../api/client';
const mockedGet = apiClient.get as ReturnType<typeof vi.fn>;

const RAW_LEVEL_COMPLETED = {
  type: 'LEVEL_COMPLETED',
  message: 'alice completed a level in Class A',
  classroomId: 'classroom-uuid-1',
  studentUsername: 'alice',
  timestamp: '2024-03-01T10:00:00Z',
};

const RAW_MYSTERY_SUBMISSION = {
  type: 'MYSTERY_SUBMISSION',
  message: 'bob submitted a mystery answer in Class B',
  classroomId: 'classroom-uuid-2',
  studentUsername: 'bob',
  timestamp: '2024-03-02T12:00:00Z',
};

beforeEach(() => {
  vi.restoreAllMocks();
  vi.clearAllMocks();
  localStorage.clear();
});

describe('getNotifications', () => {
  it('calls correct endpoint', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });
    await getNotifications();
    expect(mockedGet).toHaveBeenCalledWith('/teacher/notifications');
  });

  it('maps LEVEL_COMPLETED to stop_completed', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_LEVEL_COMPLETED] });
    const result = await getNotifications();
    expect(result[0].type).toBe('stop_completed');
  });

  it('maps MYSTERY_SUBMISSION to mystery_submission', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_MYSTERY_SUBMISSION] });
    const result = await getNotifications();
    expect(result[0].type).toBe('mystery_submission');
  });

  it('maps all fields correctly', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_LEVEL_COMPLETED] });
    const result = await getNotifications();
    expect(result[0]).toEqual({
      type: 'stop_completed',
      message: 'alice completed a level in Class A',
      classroomId: 'classroom-uuid-1',
      studentUsername: 'alice',
      timestamp: '2024-03-01T10:00:00Z',
      read: false,
    });
  });

  it('sets read: false when localStorage entry is absent', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_LEVEL_COMPLETED] });
    const result = await getNotifications();
    expect(result[0].read).toBe(false);
  });

  it('sets read: true when localStorage entry is "true"', async () => {
    localStorage.setItem(notifLsKey('alice', '2024-03-01T10:00:00Z'), 'true');
    mockedGet.mockResolvedValueOnce({ data: [RAW_LEVEL_COMPLETED] });
    const result = await getNotifications();
    expect(result[0].read).toBe(true);
  });

  it('returns empty array when no notifications', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });
    const result = await getNotifications();
    expect(result).toEqual([]);
  });

  it('falls back to stop_completed and warns for unknown type', async () => {
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    mockedGet.mockResolvedValueOnce({
      data: [{ ...RAW_LEVEL_COMPLETED, type: 'UNKNOWN_FUTURE_TYPE' }],
    });
    const result = await getNotifications();
    expect(result[0].type).toBe('stop_completed');
    expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('UNKNOWN_FUTURE_TYPE'));
  });

  it('rethrows API errors', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Network Error'));
    await expect(getNotifications()).rejects.toThrow('Network Error');
  });
});
