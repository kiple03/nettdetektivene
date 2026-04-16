import { useState } from 'react';
import { Link, useLoaderData } from 'react-router-dom';
import type { ClassroomDetail, StudentSummary, StopSummary } from '@/types/classroom.ts';
import './ClassroomDetail.css';

type LoaderData = {
  classroom: ClassroomDetail | null;
  students: StudentSummary[];
  stops: StopSummary[];
};

export default function ClassroomDetailPage() {
  const { classroom: initial, students, stops } = useLoaderData() as LoaderData;
  const [classroom, setClassroom] = useState(initial);
  const [editMode, setEditMode] = useState(false);
  const [editTitle, setEditTitle] = useState(initial?.title ?? '');
  const [editDescription, setEditDescription] = useState(initial?.description ?? '');

  if (!classroom) {
    return (
      <div className="classroom-detail">
        <Link to="/teacher" className="classroom-detail__back">← Tilbake</Link>
        <p className="classroom-detail__empty">Klassen ble ikke funnet.</p>
      </div>
    );
  }

  const totalStops = stops.length;

  function handleSave() {
    if (!editTitle.trim()) return;
    setClassroom({ ...classroom!, title: editTitle.trim(), description: editDescription.trim() || null });
    setEditMode(false);
  }

  function handleCancel() {
    setEditTitle(classroom!.title);
    setEditDescription(classroom!.description ?? '');
    setEditMode(false);
  }

  return (
    <div className="classroom-detail">
      <header className="classroom-detail__header">
        <Link to="/teacher" className="classroom-detail__back">← Tilbake</Link>
        <h1 className="classroom-detail__title">{classroom.title}</h1>
      </header>

      {/* Classroom info */}
      <section className="classroom-detail__section">
        <h2 className="classroom-detail__section-title">Klasseinformasjon</h2>
        {!editMode ? (
          <div className="classroom-detail__info">
            <div className="classroom-detail__info-row">
              <span className="classroom-detail__info-label">Bli-med-kode</span>
              <span className="classroom-detail__join-code">{classroom.joinCode}</span>
            </div>
            {classroom.description && (
              <div className="classroom-detail__info-row">
                <span className="classroom-detail__info-label">Beskrivelse</span>
                <span className="classroom-detail__info-value">{classroom.description}</span>
              </div>
            )}
            <button className="classroom-detail__edit-btn" onClick={() => setEditMode(true)}>
              Rediger
            </button>
          </div>
        ) : (
          <div className="classroom-detail__edit-form">
            <label className="classroom-detail__label">
              Klassenavn
              <input
                className="classroom-detail__input"
                type="text"
                value={editTitle}
                onChange={e => setEditTitle(e.target.value)}
                maxLength={80}
                autoFocus
              />
            </label>
            <label className="classroom-detail__label">
              Beskrivelse
              <textarea
                className="classroom-detail__textarea"
                value={editDescription}
                onChange={e => setEditDescription(e.target.value)}
                maxLength={300}
                rows={3}
              />
            </label>
            <div className="classroom-detail__edit-actions">
              <button className="classroom-detail__save-btn" onClick={handleSave}>Lagre</button>
              <button className="classroom-detail__cancel-btn" onClick={handleCancel}>Avbryt</button>
            </div>
          </div>
        )}
      </section>

      {/* Students */}
      <section className="classroom-detail__section">
        <h2 className="classroom-detail__section-title">
          Elever <span className="classroom-detail__count">({students.length})</span>
        </h2>
        {students.length === 0 ? (
          <p className="classroom-detail__empty">Ingen elever er påmeldt ennå.</p>
        ) : (
          <table className="classroom-detail__table">
            <thead>
              <tr>
                <th className="classroom-detail__th">Navn</th>
                <th className="classroom-detail__th classroom-detail__th--num">Stopp</th>
                <th className="classroom-detail__th classroom-detail__th--num">Medaljer</th>
              </tr>
            </thead>
            <tbody>
              {students.map((student) => (
                <tr key={student.userId} className="classroom-detail__tr">
                  <td className="classroom-detail__td">
                    <Link
                      to={`/teacher/classroom/${classroom.id}/student/${student.userId}`}
                      className="classroom-detail__student-link"
                    >
                      {student.username}
                    </Link>
                  </td>
                  <td className="classroom-detail__td classroom-detail__td--num">
                    {student.levelsCompleted}/{totalStops}
                  </td>
                  <td className="classroom-detail__td classroom-detail__td--num">{student.medalsEarned}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* Mystery submissions */}
      <section className="classroom-detail__section">
        <h2 className="classroom-detail__section-title">Mystery-innleveringer</h2>
        <Link
          to={`/teacher/classroom/${classroom.id}/mystery`}
          className="classroom-detail__mystery-link"
        >
          Se alle innleveringer →
        </Link>
      </section>
    </div>
  );
}
