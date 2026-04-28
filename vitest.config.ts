import path from 'node:path';

export default {
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['frontend/src/test/setup.ts'],
    include: ['frontend/src/test/**/*.test.ts'],
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'frontend/src'),
    },
  },
};
