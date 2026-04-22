import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getClassroomSummaries, getClassroom } from '../services/teacherService';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

import apiClient from '../api/client';
const mockedGet = apiClient.get as ReturnType<typeof vi.fn>;

const RAW_SUMMARY = {
  id: 'uuid-1',
  title: 'Test Class',
  description: 'A class',
  joinCode: 'ABC123',
  createdAt: '2025-01-01T10:00:00',
  studentCount: 3,
  assignmentCount: 8,
  completedCount: 5,
  progressPercent: 63,
  teacherUsernames: ['teacher-a'],
};

const RAW_DETAIL = {
  id: 'uuid-1',
  title: 'Test Class',
  description: 'A class',
  joinCode: 'ABC123',
  createdAt: '2025-01-01T10:00:00',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('getClassroomSummaries', () => {
  it('maps raw API response to ClassroomSummary[]', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_SUMMARY] });

    const result = await getClassroomSummaries();

    expect(mockedGet).toHaveBeenCalledWith('/teacher/classrooms');
    expect(result).toEqual([
      {
        id: 'uuid-1',
        title: 'Test Class',
        description: 'A class',
        joinCode: 'ABC123',
        createdAt: '2025-01-01T10:00:00',
        studentCount: 3,
        assignmentCount: 8,
        completedCount: 5,
        progressPercent: 63,
        teacherUsernames: ['teacher-a'],
      },
    ]);
  });

  it('returns empty array when no classrooms', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });

    const result = await getClassroomSummaries();

    expect(result).toEqual([]);
  });

  it('maps null description to empty string', async () => {
    mockedGet.mockResolvedValueOnce({
      data: [
        {
          ...RAW_SUMMARY,
          description: null,
        },
      ],
    });

    const result = await getClassroomSummaries();

    expect(result[0].description).toBe('');
  });
});

describe('getClassroom', () => {
  it('maps raw API response to ClassroomDetail', async () => {
    mockedGet.mockResolvedValueOnce({ data: RAW_DETAIL });

    const result = await getClassroom('uuid-1');

    expect(mockedGet).toHaveBeenCalledWith('/teacher/classrooms/uuid-1');
    expect(result).toEqual({
      id: 'uuid-1',
      title: 'Test Class',
      description: 'A class',
      joinCode: 'ABC123',
      createdAt: '2025-01-01T10:00:00',
    });
  });

  it('returns null on 404', async () => {
    mockedGet.mockRejectedValueOnce({ isAxiosError: true, response: { status: 404 } });

    const result = await getClassroom('not-found');

    expect(result).toBeNull();
  });

  it('rethrows non-404 errors', async () => {
    mockedGet.mockRejectedValueOnce({ isAxiosError: true, response: { status: 500 } });

    await expect(getClassroom('uuid-1')).rejects.toMatchObject({
      response: { status: 500 },
    });
  });

  it('rethrows network errors (no response object)', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Network Error'));

    await expect(getClassroom('uuid-1')).rejects.toThrow('Network Error');
  });
});