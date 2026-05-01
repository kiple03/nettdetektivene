import { test, expect } from '@playwright/test';
import { registerUser, setUsername } from './helpers/auth';

const studentEmail = `student-invalid-${crypto.randomUUID()}@test.com`;
const password = 'passord123';

test.beforeAll(async ({ request }) => {
  const token = await registerUser(request, studentEmail, password, 'STUDENT');
  await setUsername(request, token, 'Detektiv Testbruker');
});

test('student sees error when entering invalid join code', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Logg inn med Feide' }).click();
  await page.getByLabel('E-post').fill(studentEmail);
  await page.getByRole('textbox', { name: 'Passord' }).fill(password);
  await page.getByRole('button', { name: 'Logg inn' }).click();
  await expect(page).toHaveURL('/join-with-code');

  await page.getByLabel('Skriv inn kode').fill('FEIL99');
  await page.getByRole('button', { name: 'Bli med' }).click();

  await expect(page.getByText('Ugyldig kode. Prøv igjen.')).toBeVisible();
  await expect(page).toHaveURL('/join-with-code');
});
