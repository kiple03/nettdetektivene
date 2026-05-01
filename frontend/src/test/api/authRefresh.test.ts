import { beforeEach, describe, expect, it, vi } from 'vitest';

const axiosMock = vi.hoisted(() => ({
  post: vi.fn(),
  requestUse: vi.fn(),
  responseUse: vi.fn(),
}));

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      defaults: { baseURL: 'http://localhost:8080/api' },
      interceptors: {
        request: { use: axiosMock.requestUse },
        response: { use: axiosMock.responseUse },
      },
    })),
    post: axiosMock.post,
  },
}));

import { sharedRefresh } from '@/api/client.ts';

describe('sharedRefresh', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('skips the network after an explicit logout', async () => {
    localStorage.setItem('auth-session-ended', 'true');

    await expect(sharedRefresh()).resolves.toBeNull();
    expect(axiosMock.post).not.toHaveBeenCalled();
  });
});