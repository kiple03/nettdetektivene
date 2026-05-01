import { test, expect } from '@playwright/test';
import { registerUser } from './helpers/auth';

const teacherEmail = `teacher-create-${crypto.randomUUID()}@test.com`;
const password = 'passord123';

test.beforeAll(async ({ request }) => {
  await registerUser(request, teacherEmail, password, 'TEACHER');
});

test('teacher can create a classroom', async ({ page }) => {
  await page.goto('/laerer/login');
  await page.getByRole('button', { name: 'Logg inn med Feide' }).click();
  await page.getByLabel('E-post').fill(teacherEmail);
  await page.getByRole('textbox', { name: 'Passord' }).fill(password);
  await page.getByRole('button', { name: 'Logg inn' }).click();
  await expect(page).toHaveURL('/teacher');

  await page.goto('/teacher/classrooms/new');
  await page.getByRole('textbox', { name: 'Klassenavn' }).fill('Testklasse 1A');
  await page.getByRole('button', { name: 'Opprett klasse' }).click();

  await expect(page).toHaveURL(/\/teacher\/classrooms\/.+\/code/);
});
