import { render, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const sharedRefresh = vi.hoisted(() => vi.fn().mockResolvedValue('new-token'));
const forceRefresh = vi.hoisted(() => vi.fn().mockResolvedValue('new-token'));
const mockIsAuthReady = vi.hoisted(() => vi.fn(() => true));
const mockSetAuthReady = vi.hoisted(() => vi.fn());
const mockIsSessionEnded = vi.hoisted(() => vi.fn(() => false));
const mockGetToken = vi.hoisted(() => vi.fn(() => null as string | null));
const mockDecodeToken = vi.hoisted(() => vi.fn(() => null as { exp?: number } | null));

vi.mock('@/api/client', () => ({
  sharedRefresh,
  forceRefresh,
}));

vi.mock('@/stores/authStore', () => ({
  isAuthReady: mockIsAuthReady,
  setAuthReady: mockSetAuthReady,
  isSessionEnded: mockIsSessionEnded,
  getToken: mockGetToken,
  decodeToken: mockDecodeToken,
}));

import AuthSessionRefresher from '@/components/AuthSessionRefresher';

describe('AuthSessionRefresher', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockIsAuthReady.mockReturnValue(true);
    mockIsSessionEnded.mockReturnValue(false);
    mockGetToken.mockReturnValue(null);
    mockDecodeToken.mockReturnValue(null);
  });

  describe('startup probe', () => {
    it('calls sharedRefresh on mount when !isAuthReady (expired token)', async () => {
      mockIsAuthReady.mockReturnValue(false);

      render(<AuthSessionRefresher />);

      await waitFor(() => {
        expect(sharedRefresh).toHaveBeenCalledTimes(1);
      });
    });

    it('calls setAuthReady after startup probe settles', async () => {
      mockIsAuthReady.mockReturnValue(false);

      render(<AuthSessionRefresher />);

      await waitFor(() => {
        expect(mockSetAuthReady).toHaveBeenCalledTimes(1);
      });
    });

    it('calls setAuthReady even when the probe fails', async () => {
      mockIsAuthReady.mockReturnValue(false);
      sharedRefresh.mockRejectedValueOnce(new Error('network error'));

      render(<AuthSessionRefresher />);

      await waitFor(() => {
        expect(mockSetAuthReady).toHaveBeenCalledTimes(1);
      });
    });

    it('does not call sharedRefresh when isAuthReady is already true', () => {
      mockIsAuthReady.mockReturnValue(true);

      render(<AuthSessionRefresher />);

      expect(sharedRefresh).not.toHaveBeenCalled();
    });
  });

  describe('proactive timer', () => {
    beforeEach(() => {
      // Fix time to a clean second boundary so exp * 1000 - Date.now() has no sub-ms drift.
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2025-01-01T00:00:00.000Z'));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('calls forceRefresh 15 seconds before token expiry', () => {
      const exp = Math.floor(Date.now() / 1000) + 60; // expires in 60s
      mockGetToken.mockReturnValue('header.payload.sig');
      mockDecodeToken.mockReturnValue({ exp });

      render(<AuthSessionRefresher />);

      // Should NOT fire before 45s (60s - 15s leeway)
      vi.advanceTimersByTime(44_999);
      expect(forceRefresh).not.toHaveBeenCalled();

      // Should fire at exactly 45s
      vi.advanceTimersByTime(1);
      expect(forceRefresh).toHaveBeenCalledTimes(1);
    });

    it('calls forceRefresh immediately when token is already within the leeway window', () => {
      const exp = Math.floor(Date.now() / 1000) + 10; // expires in 10s (< 15s leeway)
      mockGetToken.mockReturnValue('header.payload.sig');
      mockDecodeToken.mockReturnValue({ exp });

      render(<AuthSessionRefresher />);

      // delay = max(10s - 15s, 0) = 0 → fires immediately
      vi.advanceTimersByTime(0);
      expect(forceRefresh).toHaveBeenCalledTimes(1);
    });

    it('does not schedule timer when session is ended', () => {
      const exp = Math.floor(Date.now() / 1000) + 60;
      mockGetToken.mockReturnValue('header.payload.sig');
      mockDecodeToken.mockReturnValue({ exp });
      mockIsSessionEnded.mockReturnValue(true);

      render(<AuthSessionRefresher />);

      vi.advanceTimersByTime(60_000);
      expect(forceRefresh).not.toHaveBeenCalled();
    });

    it('does not schedule timer when token has no exp claim', () => {
      mockGetToken.mockReturnValue('header.payload.sig');
      mockDecodeToken.mockReturnValue({}); // no exp

      render(<AuthSessionRefresher />);

      vi.advanceTimersByTime(60_000);
      expect(forceRefresh).not.toHaveBeenCalled();
    });

    it('does not schedule timer when there is no token', () => {
      mockGetToken.mockReturnValue(null);

      render(<AuthSessionRefresher />);

      vi.advanceTimersByTime(60_000);
      expect(forceRefresh).not.toHaveBeenCalled();
    });
  });
});
