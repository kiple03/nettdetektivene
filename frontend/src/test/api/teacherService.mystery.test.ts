import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  getMysterySubmissions,
  approveMysterySubmission,
  rejectMysterySubmission,
  featureMysterySubmission,
} from '../../api/teacherApi.ts';

vi.mock('../../api/client', () => ({
  default: {
    get: vi.fn(),
    patch: vi.fn(),
  },
}));

import apiClient from '../../api/client.ts';
const mockedGet = apiClient.get as ReturnType<typeof vi.fn>;
const mockedPatch = apiClient.patch as ReturnType<typeof vi.fn>;

const RAW_SUBMISSION = {
  id: 'sub-uuid-1',
  studentUsername: 'alice',
  content: 'My mystery answer',
  imageUrl: null,
  submittedAt: '2024-03-01T10:00:00Z',
  status: 'PENDING' as const,
  featured: false,
};

const MAPPED_SUBMISSION = {
  id: 'sub-uuid-1',
  studentUsername: 'alice',
  content: 'My mystery answer',
  imageUrl: null,
  submittedAt: '2024-03-01T10:00:00Z',
  status: 'pending' as const,
  featured: false,
  reviewedAt: null,
};

beforeEach(() => {
  vi.restoreAllMocks();
  vi.clearAllMocks();
});

describe('getMysterySubmissions', () => {
  it('calls correct endpoint and maps response', async () => {
    mockedGet.mockResolvedValueOnce({ data: [RAW_SUBMISSION] });

    const result = await getMysterySubmissions('classroom-uuid-1');

    expect(mockedGet).toHaveBeenCalledWith('/teacher/classrooms/classroom-uuid-1/mystery');
    expect(result).toEqual([MAPPED_SUBMISSION]);
  });

  it('maps status from uppercase to lowercase', async () => {
    mockedGet.mockResolvedValueOnce({
      data: [
        { ...RAW_SUBMISSION, status: 'APPROVED', featured: true },
        { ...RAW_SUBMISSION, id: 'sub-uuid-2', status: 'REJECTED' },
      ],
    });

    const result = await getMysterySubmissions('classroom-uuid-1');

    expect(result[0].status).toBe('approved');
    expect(result[1].status).toBe('rejected');
  });

  it('returns empty array when no submissions', async () => {
    mockedGet.mockResolvedValueOnce({ data: [] });

    const result = await getMysterySubmissions('classroom-uuid-1');
    expect(result).toEqual([]);
  });
});

describe('approveMysterySubmission', () => {
  it('patches correct endpoint with APPROVED status and maps response', async () => {
    const raw = { ...RAW_SUBMISSION, status: 'APPROVED' as const };
    mockedPatch.mockResolvedValueOnce({ data: raw });

    const result = await approveMysterySubmission('classroom-uuid-1', 'sub-uuid-1');

    expect(mockedPatch).toHaveBeenCalledWith(
      '/teacher/classrooms/classroom-uuid-1/mystery/sub-uuid-1',
      { status: 'APPROVED', featured: false },
    );
    expect(result.status).toBe('approved');
  });
});

describe('rejectMysterySubmission', () => {
  it('patches correct endpoint with REJECTED status and maps response', async () => {
    const raw = { ...RAW_SUBMISSION, status: 'REJECTED' as const };
    mockedPatch.mockResolvedValueOnce({ data: raw });

    const result = await rejectMysterySubmission('classroom-uuid-1', 'sub-uuid-1');

    expect(mockedPatch).toHaveBeenCalledWith(
      '/teacher/classrooms/classroom-uuid-1/mystery/sub-uuid-1',
      { status: 'REJECTED', featured: false },
    );
    expect(result.status).toBe('rejected');
    expect(result.featured).toBe(false);
  });
});

describe('featureMysterySubmission', () => {
  it('patches with featured=true', async () => {
    const raw = { ...RAW_SUBMISSION, status: 'APPROVED' as const, featured: true };
    mockedPatch.mockResolvedValueOnce({ data: raw });

    const result = await featureMysterySubmission('classroom-uuid-1', 'sub-uuid-1', true);

    expect(mockedPatch).toHaveBeenCalledWith(
      '/teacher/classrooms/classroom-uuid-1/mystery/sub-uuid-1',
      { featured: true },
    );
    expect(result.featured).toBe(true);
  });

  it('patches with featured=false', async () => {
    const raw = { ...RAW_SUBMISSION, status: 'APPROVED' as const, featured: false };
    mockedPatch.mockResolvedValueOnce({ data: raw });

    const result = await featureMysterySubmission('classroom-uuid-1', 'sub-uuid-1', false);

    expect(mockedPatch).toHaveBeenCalledWith(
      '/teacher/classrooms/classroom-uuid-1/mystery/sub-uuid-1',
      { featured: false },
    );
    expect(result.featured).toBe(false);
  });
});

describe('error propagation', () => {
  it('getMysterySubmissions rethrows API errors', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Network Error'));
    await expect(getMysterySubmissions('classroom-uuid-1')).rejects.toThrow('Network Error');
  });

  it('approveMysterySubmission rethrows API errors', async () => {
    mockedPatch.mockRejectedValueOnce(new Error('Server Error'));
    await expect(approveMysterySubmission('classroom-uuid-1', 'sub-uuid-1')).rejects.toThrow('Server Error');
  });

  it('rejectMysterySubmission rethrows API errors', async () => {
    mockedPatch.mockRejectedValueOnce(new Error('Server Error'));
    await expect(rejectMysterySubmission('classroom-uuid-1', 'sub-uuid-1')).rejects.toThrow('Server Error');
  });

  it('featureMysterySubmission rethrows API errors', async () => {
    mockedPatch.mockRejectedValueOnce(new Error('Server Error'));
    await expect(featureMysterySubmission('classroom-uuid-1', 'sub-uuid-1', true)).rejects.toThrow('Server Error');
  });
});
