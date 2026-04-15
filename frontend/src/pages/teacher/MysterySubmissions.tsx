import { Link, useLoaderData } from 'react-router-dom';
import { useState } from 'react';
import type { Classroom, MysterySubmission, MysterySubmissionWithStudent } from '@/types/classroom.ts';
import './MysterySubmissions.css';

type LoaderData = {
  classroom: Classroom | null;
  submissions: MysterySubmissionWithStudent[];
};

type Filter = 'all' | MysterySubmission['status'];

const FILTER_LABELS: Record<Filter, string> = {
  all: 'Alle',
  pending: 'Venter',
  approved: 'Godkjent',
  rejected: 'Avvist',
};

const STATUS_LABELS: Record<MysterySubmission['status'], string> = {
  pending: 'Venter',
  approved: 'Godkjent',
  rejected: 'Avvist',
};

export default function MysterySubmissions() {
  const { classroom, submissions: initial } = useLoaderData() as LoaderData;
  const [submissions, setSubmissions] = useState(initial);
  const [filter, setFilter] = useState<Filter>('all');

  const classroomId = classroom?.id;

  function approve(id: number) {
    setSubmissions(prev =>
      prev.map(s => s.id === id ? { ...s, status: 'approved', reviewed_at: new Date().toISOString(), reviewed_by_teacher_id: 1 } : s)
    );
  }

  function reject(id: number) {
    setSubmissions(prev =>
      prev.map(s => s.id === id ? { ...s, status: 'rejected', reviewed_at: new Date().toISOString(), reviewed_by_teacher_id: 1, is_featured: false } : s)
    );
  }

  function toggleFeatured(id: number) {
    setSubmissions(prev =>
      prev.map(s => s.id === id ? { ...s, is_featured: !s.is_featured } : s)
    );
  }

  const filtered = filter === 'all' ? submissions : submissions.filter(s => s.status === filter);

  const counts: Record<Filter, number> = {
    all: submissions.length,
    pending: submissions.filter(s => s.status === 'pending').length,
    approved: submissions.filter(s => s.status === 'approved').length,
    rejected: submissions.filter(s => s.status === 'rejected').length,
  };

  if (!classroom) {
    return (
      <div className="mystery">
        <Link to="/teacher" className="mystery__back">← Tilbake</Link>
        <p className="mystery__empty">Klassen ble ikke funnet.</p>
      </div>
    );
  }

  return (
    <div className="mystery">
      <header className="mystery__header">
        <Link to={`/teacher/classroom/${classroomId}`} className="mystery__back">← {classroom.title}</Link>
        <h1 className="mystery__title">Mystery-innleveringer</h1>
      </header>

      {/* Filter tabs */}
      <div className="mystery__filters" role="tablist">
        {(Object.keys(FILTER_LABELS) as Filter[]).map(f => (
          <button
            key={f}
            role="tab"
            aria-selected={filter === f}
            className={`mystery__filter-btn${filter === f ? ' mystery__filter-btn--active' : ''}`}
            onClick={() => setFilter(f)}
          >
            {FILTER_LABELS[f]}
            <span className="mystery__filter-count">{counts[f]}</span>
          </button>
        ))}
      </div>

      {/* Submission list */}
      {filtered.length === 0 ? (
        <p className="mystery__empty">Ingen innleveringer i denne kategorien.</p>
      ) : (
        <ul className="mystery__list">
          {filtered.map(sub => (
            <li key={sub.id} className="mystery__card">
              <div className="mystery__card-header">
                <span className="mystery__student">{sub.student_username}</span>
                <span className={`mystery__status mystery__status--${sub.status}`}>
                  {STATUS_LABELS[sub.status]}
                </span>
                <time className="mystery__date" dateTime={sub.submitted_at}>
                  {new Date(sub.submitted_at).toLocaleDateString('nb-NO')}
                </time>
              </div>

              {sub.content && (
                <p className="mystery__content">{sub.content}</p>
              )}

              {sub.image_url && (
                <img className="mystery__image" src={sub.image_url} alt="Innlevering" />
              )}

              <div className="mystery__actions">
                {sub.status !== 'approved' && (
                  <button className="mystery__btn mystery__btn--approve" onClick={() => approve(sub.id)}>
                    Godkjenn
                  </button>
                )}
                {sub.status !== 'rejected' && (
                  <button className="mystery__btn mystery__btn--reject" onClick={() => reject(sub.id)}>
                    Avvis
                  </button>
                )}
                {sub.status === 'approved' && (
                  <button
                    className={`mystery__btn mystery__btn--feature${sub.is_featured ? ' mystery__btn--featured' : ''}`}
                    onClick={() => toggleFeatured(sub.id)}
                  >
                    {sub.is_featured ? '★ Fremhevet' : '☆ Fremhev'}
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
