import { test, expect } from '@playwright/test';
import { registerUser } from './helpers/auth';

const email = `student-login-${Date.now()}@test.com`;
const password = 'passord123';

test.beforeAll(async ({ request }) => {
  await registerUser(request, email, password, 'STUDENT');
});

test('student can log in', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Logg inn med Feide' }).click();
  await page.getByLabel('E-post').fill(email);
  await page.getByRole('textbox', { name: 'Passord' }).fill(password);
  await page.getByRole('button', { name: 'Logg inn' }).click();

  await expect(page).toHaveURL('/join-with-code');
});
