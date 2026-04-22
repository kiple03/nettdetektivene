import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createClassroom } from '../api/teacherApi';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import apiClient from '../api/client';
const mockedPost = apiClient.post as ReturnType<typeof vi.fn>;

const RAW_DETAIL = {
  id: 'uuid-new',
  title: 'New Class',
  description: 'A new class',
  joinCode: 'XYZ999',
  createdAt: '2025-06-01T10:00:00',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('createClassroom', () => {
  it('posts to /teacher/classrooms and maps response', async () => {
    mockedPost.mockResolvedValueOnce({ data: RAW_DETAIL });

    const result = await createClassroom('New Class', 'A new class');

    expect(mockedPost).toHaveBeenCalledWith('/teacher/classrooms', {
      title: 'New Class',
      description: 'A new class',
    });
    expect(result).toEqual({
      id: 'uuid-new',
      title: 'New Class',
      description: 'A new class',
      joinCode: 'XYZ999',
      createdAt: '2025-06-01T10:00:00',
    });
  });

  it('maps null description correctly', async () => {
    mockedPost.mockResolvedValueOnce({ data: { ...RAW_DETAIL, description: null } });

    const result = await createClassroom('New Class', null);

    expect(result.description).toBeNull();
  });

  it('rethrows errors from the API', async () => {
    mockedPost.mockRejectedValueOnce(new Error('Server Error'));

    await expect(createClassroom('Fail', null)).rejects.toThrow('Server Error');
  });
});
