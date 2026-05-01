import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/buttons/Button.tsx';
import { TextInput } from '@/components/ui/textInput/TextInput.tsx';
import { MyClassroomsBoard } from '@/components/teacher/classroomList/MyClassroomsBoard';
import { useClassroomSummaries } from '@/hooks/teacher/useTeacherQueries';
import './MyClassroomsPage.css';

/** Teacher landing page showing all classrooms with search, view toggle, and join-code copy. */
export default function MyClassroomsPage() {
    const { data: summaries = [] } = useClassroomSummaries();
    const navigate = useNavigate();

    const [search, setSearch] = useState('');
    const [view, setView] = useState<'grid' | 'list'>('grid');
    const [copiedClassroomId, setCopiedClassroomId] = useState<string | null>(null);

    const filtered = useMemo(() => {
        const query = search.toLowerCase();
        return summaries.filter((summary) =>
            [summary.title, summary.description ?? '', summary.joinCode]
                .join(' ')
                .toLowerCase()
                .includes(query),
        );
    }, [summaries, search]);

    /** Copies `joinCode` to clipboard and briefly highlights the card for `classroomId`. */
    async function handleCopyCode(joinCode: string, classroomId: string) {
        try {
            await navigator.clipboard.writeText(joinCode);
            setCopiedClassroomId(classroomId);
            window.setTimeout(() => {
                setCopiedClassroomId((current) => (current === classroomId ? null : current));
            }, 1800);
        } catch {
            setCopiedClassroomId(null);
        }
    }

    return (
        <div className="teacher-dashboard">
            <div className="teacher-dashboard__hero">
                <h1 className="teacher-dashboard__title">Mine klasserom</h1>
                <p className="teacher-dashboard__subtitle">
                    Administrer og følg med på dine elever
                </p>
            </div>

            <div className="teacher-dashboard__toolbar-wrap">
                <div className="teacher-dashboard__search-wrap">
                    <TextInput
                        label="Søk"
                        id="classroom-search"
                        placeholder="🔎  Søk i klasserom…"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="teacher-dashboard__search-input"
                    />
                </div>

                <Link to="/teacher/classrooms/new" className="teacher-dashboard__new-link">
                    <Button variant="default" className="teacher-dashboard__new-btn">
                        + Nytt klasserom
                    </Button>
                </Link>
            </div>

            <MyClassroomsBoard
                summaries={filtered}
                view={view}
                onViewChange={setView}
                copiedClassroomId={copiedClassroomId}
                onCopyCode={handleCopyCode}
                onOpenClassroom={(classroomId) => navigate(`/teacher/classrooms/${classroomId}`)}
            />

            <nav className="teacher-dashboard__bottom-nav">
                <div className="teacher-dashboard__bottom-nav-center">
                    <span className="bottom-nav-chip bottom-nav-chip--active">
                        Mine klasserom
                    </span>
                    <Link to="/teacher/leaderboard" className="bottom-nav-chip">
                        Ledertavle
                    </Link>
                </div>
            </nav>
        </div>
    );
}
