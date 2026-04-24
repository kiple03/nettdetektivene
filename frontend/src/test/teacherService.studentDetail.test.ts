import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getStudentDetail } from '../api/teacherApi';
import axios from 'axios';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import apiClient from '../api/client';
const mockedGet = apiClient.get as ReturnType<typeof vi.fn>;

const RAW_STUDENT_DETAIL = {
  username: 'alice',
  completions: [
    { levelId: 1, completedAt: '2024-01-01T10:00:00Z' },
    { levelId: 2, completedAt: '2024-02-15T12:00:00Z' },
  ],
};

beforeEach(() => {
  vi.restoreAllMocks();
  vi.clearAllMocks();
});

describe('getStudentDetail', () => {
  it('calls correct endpoint and maps response to StudentDetail', async () => {
    mockedGet.mockResolvedValueOnce({ data: RAW_STUDENT_DETAIL });

    const result = await getStudentDetail('classroom-uuid-1', 'student-uuid-1');

    expect(mockedGet).toHaveBeenCalledWith(
      '/teacher/classrooms/classroom-uuid-1/students/student-uuid-1'
    );
    expect(result).toEqual({
      userId: 'student-uuid-1',
      username: 'alice',
      email: null,
      enrolledAt: null,
      levelCompletions: [
        { levelId: 1, completedAt: '2024-01-01T10:00:00Z' },
        { levelId: 2, completedAt: '2024-02-15T12:00:00Z' },
      ],
      currentStopName: null,
      currentLevelOrder: null,
      totalLevelsCompleted: null,
      gameCompleted: null,
    });
  });

  it('returns null on 404', async () => {
    const err = new axios.AxiosError('Not Found');
    err.response = { status: 404 } as typeof err.response;
    vi.spyOn(axios, 'isAxiosError').mockReturnValue(true);
    mockedGet.mockRejectedValueOnce(err);

    const result = await getStudentDetail('classroom-uuid-1', 'student-uuid-1');
    expect(result).toBeNull();
  });

  it('maps empty completions correctly', async () => {
    mockedGet.mockResolvedValueOnce({ data: { username: 'bob', completions: [] } });

    const result = await getStudentDetail('classroom-uuid-1', 'student-uuid-2');
    expect(result).toEqual({
      userId: 'student-uuid-2',
      username: 'bob',
      email: null,
      enrolledAt: null,
      levelCompletions: [],
      currentStopName: null,
      currentLevelOrder: null,
      totalLevelsCompleted: null,
      gameCompleted: null,
    });
  });

  it('maps summary-based student detail response when completions are omitted', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        username: 'charlie',
        currentStopName: 'Stopp 2',
        currentLevelOrder: 3,
        totalLevelsCompleted: 7,
        gameCompleted: false,
      },
    });

    const result = await getStudentDetail('classroom-uuid-1', 'student-uuid-3');

    expect(result).toEqual({
      userId: 'student-uuid-3',
      username: 'charlie',
      email: null,
      enrolledAt: null,
      levelCompletions: [],
      currentStopName: 'Stopp 2',
      currentLevelOrder: 3,
      totalLevelsCompleted: 7,
      gameCompleted: false,
    });
  });

  it('propagates non-404 API errors', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Network Error'));
    await expect(getStudentDetail('classroom-uuid-1', 'student-uuid-1')).rejects.toThrow('Network Error');
  });
});
