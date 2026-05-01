import { test, expect } from '@playwright/test';
import { createClassroom, registerUser, setUsername } from './helpers/auth';

const studentEmail = `student-join-${crypto.randomUUID()}@test.com`;
const password = 'passord123';
let joinCode: string;

test.beforeAll(async ({ request }) => {
  const teacherEmail = `teacher-join-${crypto.randomUUID()}@test.com`;
  const teacherToken = await registerUser(request, teacherEmail, password, 'TEACHER');
  const classroom = await createClassroom(request, teacherToken, 'Test Classroom');
  joinCode = classroom.joinCode;

  const studentToken = await registerUser(request, studentEmail, password, 'STUDENT');
  await setUsername(request, studentToken, 'Detektiv Testbruker');
});

test('student can join a classroom with a code', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Logg inn med Feide' }).click();
  await page.getByLabel('E-post').fill(studentEmail);
  await page.getByRole('textbox', { name: 'Passord' }).fill(password);
  await page.getByRole('button', { name: 'Logg inn' }).click();
  await expect(page).toHaveURL('/join-with-code');

  await page.getByLabel('Skriv inn kode').fill(joinCode);
  await page.getByRole('button', { name: 'Bli med' }).click();

  await expect(page).toHaveURL('/home');
});
