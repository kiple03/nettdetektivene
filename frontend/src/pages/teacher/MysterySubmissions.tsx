import { Link, useParams } from 'react-router-dom';
import { useState } from 'react';
import type { MysterySubmissionResponse } from '@/types/classroom.ts';
import { useClassroom, useMysterySubmissions } from '@/hooks/teacher/useTeacherQueries';
import {
  useApproveMysterySubmission,
  useFeatureMysterySubmission,
  useRejectMysterySubmission,
} from '@/hooks/teacher/useTeacherMutations';
import './MysterySubmissions.css';
import { useBackTo } from '@/hooks/teacher/useBackTo';

type Filter = 'all' | MysterySubmissionResponse['status'];

const FILTER_LABELS: Record<Filter, string> = {
  all: 'Alle',
  pending: 'Venter',
  approved: 'Godkjent',
  rejected: 'Avvist',
};

const STATUS_LABELS: Record<MysterySubmissionResponse['status'], string> = {
  pending: 'Venter',
  approved: 'Godkjent',
  rejected: 'Avvist',
};

export default function MysterySubmissions() {
  const { id: classroomId = '' } = useParams<{ id: string }>();
  const { data: classroom = null } = useClassroom(classroomId);
  const { data: submissions = [] } = useMysterySubmissions(classroomId);
  const [filter, setFilter] = useState<Filter>('all');

  const approveMutation = useApproveMysterySubmission(classroomId);
  const rejectMutation = useRejectMysterySubmission(classroomId);
  const featureMutation = useFeatureMysterySubmission(classroomId);

  useBackTo(`/teacher/classrooms/${classroomId}`);

  function approve(id: string) {
    approveMutation.mutate(id);
  }

  function reject(id: string) {
    rejectMutation.mutate(id);
  }

  function toggleFeatured(id: string) {
    const current = submissions.find(s => s.id === id);
    if (!current) return;
    featureMutation.mutate({ submissionId: id, featured: !current.featured });
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
        <Link to={`/teacher/classrooms/${classroomId}`} className="mystery__back">← {classroom.title}</Link>
        <h1 className="mystery__title">Mystery-innleveringer</h1>
      </header>

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

      {filtered.length === 0 ? (
        <p className="mystery__empty">Ingen innleveringer i denne kategorien.</p>
      ) : (
        <ul className="mystery__list">
          {filtered.map(sub => (
            <li key={sub.id} className="mystery__card">
              <div className="mystery__card-header">
                <span className="mystery__student">{sub.studentUsername}</span>
                <span className={`mystery__status mystery__status--${sub.status}`}>
                  {STATUS_LABELS[sub.status]}
                </span>
                <time className="mystery__date" dateTime={sub.submittedAt}>
                  {new Date(sub.submittedAt).toLocaleDateString('nb-NO')}
                </time>
              </div>

              {sub.content && (
                <p className="mystery__content">{sub.content}</p>
              )}

              {sub.imageUrl && (
                <img className="mystery__image" src={sub.imageUrl} alt="Innlevering" />
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
                    className={`mystery__btn mystery__btn--feature${sub.featured ? ' mystery__btn--featured' : ''}`}
                    onClick={() => toggleFeatured(sub.id)}
                  >
                    {sub.featured ? '★ Fremhevet' : '☆ Fremhev'}
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
