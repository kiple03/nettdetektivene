import { test, expect } from '@playwright/test';

test('teacher can register a new account', async ({ page }) => {
  const email = `test-${Date.now()}@gmail.com`;

  await page.goto('/');
  await page.getByRole('link', { name: 'Er du lærer?' }).click();
  await page.getByRole('button', { name: 'Registrer ny lærerkonto' }).click();
  await page.getByRole('textbox', { name: 'E-post' }).fill(email);
  await page.getByRole('textbox', { name: 'Passord', exact: true }).fill('passord123');
  await page.getByRole('textbox', { name: 'Bekreft passord' }).fill('passord123');
  await page.getByRole('button', { name: 'Opprett lærerkonto' }).click();

  await expect(page).toHaveURL('/teacher');
});
