import { test, expect } from '@playwright/test';

test('unauthenticated user is redirected from /teacher to teacher login', async ({ page }) => {
  await page.goto('/teacher');
  await expect(page).toHaveURL('/laerer/login');
});

test('unauthenticated user is redirected from /home to student login', async ({ page }) => {
  await page.goto('/home');
  await expect(page).toHaveURL('/');
});
