import { test, expect } from '@playwright/test';

test('student can register a new account', async ({ page }) => {
  const email = `test-${crypto.randomUUID()}@gmail.com`;

  await page.goto('/');
  await page.getByRole('button', { name: 'Registrer ny bruker' }).click();
  await page.getByRole('textbox', { name: 'E-post' }).fill(email);
  await page.getByRole('textbox', { name: 'Passord', exact: true }).fill('passord123');
  await page.getByRole('textbox', { name: 'Bekreft passord' }).fill('passord123');
  await page.getByRole('button', { name: 'Opprett konto' }).click();

  await expect(page).toHaveURL('/avatar');
});
