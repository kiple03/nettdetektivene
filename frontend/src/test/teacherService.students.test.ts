import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getStudentProgress } from '../api/teacherApi';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import apiClient from '../api/client';
const mockedGet = apiClient.get as ReturnType<typeof vi.fn>;

const RAW_STUDENT = {
  userId: 'user-uuid-1',
  username: 'alice',
  email: 'alice@example.com',
  currentStopDisplayOrder: 3,
  totalStops: 10,
  medalsEarned: 3,
  score: 150,
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('getStudentProgress', () => {
  it('calls correct endpoint and maps response to StudentSummary[]', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_STUDENT] });

    const result = await getStudentProgress('classroom-uuid-1');

    expect(mockedGet).toHaveBeenCalledWith('/teacher/classrooms/classroom-uuid-1/students');
    expect(result).toEqual([
      {
        userId: 'user-uuid-1',
        username: 'alice',
        email: 'alice@example.com',
        currentStopDisplayOrder: 3,
        totalStops: 10,
        medalsEarned: 3,
        score: 150,
        equippedItems: [],
      },
    ]);
  });

  it('returns empty array when no students', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });
    const result = await getStudentProgress('classroom-uuid-1');
    expect(result).toEqual([]);
  });

  it('maps multiple students correctly', async () => {
    const raw2 = {
      userId: 'user-uuid-2',
      username: 'bob',
      email: 'bob@example.com',
      currentStopDisplayOrder: 1,
      totalStops: 10,
      medalsEarned: 1,
      score: 50,
    };
    mockedGet.mockResolvedValueOnce({ data: [RAW_STUDENT, raw2] });

    const result = await getStudentProgress('classroom-uuid-1');

    expect(result).toHaveLength(2);
    expect(result[1]).toEqual({
      userId: 'user-uuid-2',
      username: 'bob',
      email: 'bob@example.com',
      currentStopDisplayOrder: 1,
      totalStops: 10,
      medalsEarned: 1,
      score: 50,
      equippedItems: [],
    });
  });

  it('propagates API errors', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Network Error'));
    await expect(getStudentProgress('classroom-uuid-1')).rejects.toThrow('Network Error');
  });
});
