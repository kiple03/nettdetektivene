import { Form, Link, redirect, useActionData } from 'react-router-dom';
import type { ActionFunctionArgs } from 'react-router-dom';
import { createClassroom } from '@/services/teacherService.ts';
import './CreateClassroom.css';

type ActionResult = { error: string } | null;

export async function createClassroomAction({ request }: ActionFunctionArgs): Promise<Response | ActionResult> {
  const data = await request.formData();
  const title = ((data.get('title') ?? '') as string).trim();
  const description = ((data.get('description') ?? '') as string).trim() || null;

  if (!title) return { error: 'Klassenavn er påkrevd.' };

  try {
    const classroom = await createClassroom(title, description);
    return redirect(`/teacher/classroom/${classroom.id}`);
  } catch {
    return { error: 'Noe gikk galt. Prøv igjen.' };
  }
}

export default function CreateClassroom() {
  const result = useActionData() as ActionResult | undefined;

  return (
    <div className="create-classroom">
      <header className="create-classroom__header">
        <Link to="/teacher" className="create-classroom__back" aria-label="Tilbake til dashboard">
          ← Tilbake
        </Link>
        <h1 className="create-classroom__title">Opprett ny klasse</h1>
      </header>

      <Form method="post" className="create-classroom__form">
        {result?.error && (
          <p className="create-classroom__error" role="alert">{result.error}</p>
        )}

        <label className="create-classroom__label">
          Klassenavn <span aria-hidden="true">*</span>
          <input
            className="create-classroom__input"
            type="text"
            name="title"
            required
            minLength={2}
            maxLength={80}
            placeholder="f.eks. 6A – Nettdetektivene"
            autoFocus
          />
        </label>

        <label className="create-classroom__label">
          Beskrivelse <span className="create-classroom__optional">(valgfritt)</span>
          <textarea
            className="create-classroom__textarea"
            name="description"
            maxLength={300}
            rows={3}
            placeholder="Kort beskrivelse av klassen"
          />
        </label>

        <button type="submit" className="create-classroom__submit">
          Opprett klasse
        </button>
      </Form>
    </div>
  );
}
