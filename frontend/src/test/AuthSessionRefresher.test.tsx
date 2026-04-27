import { render, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const sharedRefresh = vi.hoisted(() => vi.fn().mockResolvedValue('new-token'));

vi.mock('@/api/client', () => ({
  sharedRefresh,
}));

import AuthSessionRefresher from '@/components/AuthSessionRefresher';

function makeExpiredToken(): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) - 10 }));
  return ['header', payload, 'signature'].join('.');
}

describe('AuthSessionRefresher', () => {
  beforeEach(() => {
    localStorage.clear();
    sharedRefresh.mockClear();
  });

  it('refreshes immediately when the access token is expired', async () => {
    localStorage.setItem('token', makeExpiredToken());

    render(<AuthSessionRefresher />);

    await waitFor(() => {
      expect(sharedRefresh).toHaveBeenCalledTimes(1);
    });
  });
});