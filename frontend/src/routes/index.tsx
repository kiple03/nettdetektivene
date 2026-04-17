import {createBrowserRouter} from "react-router-dom";
import MapPage from "../pages/student/MapPage";
import LevelOne from "../pages/student/LevelOnePage";
import StudentLoginPage from "@/pages/auth/StudentLoginPage";
import TeacherLoginPage from "@/pages/auth/TeacherLoginPage";
import FeideFake from "@/pages/auth/FeideFake";
import StudentHomePage from "@/pages/student/StudentHomePage";
import CreateClassroom, {createClassroomAction} from "@/pages/teacher/CreateClassroom";
import ClassroomDetail from "@/pages/teacher/detail/ClassroomDetail.tsx";
import StudentDetail from "@/pages/teacher/detail/StudentDetail.tsx";
import MysterySubmissions from "@/pages/teacher/MysterySubmissions";
import Notifications from "@/pages/teacher/Notifications";
import LeaderboardPage from "@/pages/student/LeaderboardPage.tsx";
import NotesPage from "@/pages/student/NotesPage";
import BadgesPage from "@/pages/student/BadgesPage";
import WeeklyMysteryPage from "@/pages/student/WeeklyMysteryPage";
import JoinWithCodePage from "@/pages/student/JoinWithCodePage";
import CodePage from "@/pages/teacher/CodePage";
import MyClassroomsPage from "@/pages/teacher/MyClassroomsPage";
import AvatarDevPage from "@/pages/student/AvatarDevPage";
import GamePage from "@/pages/game/GamePage.tsx";

// Route guard temporarily disabled during game development so we don't
// have to log in on every reload. Re-enable by restoring the <RequireAuth>
// wrappers below and uncommenting this import.
// import RequireAuth from "@/routes/RequireAuth";
import {
    getClassroom,
    getClassroomSummaries,
    getMysterySubmissions,
    getNotifications,
    getStops,
    getStudentDetail,
    getStudentProgress,
} from "@/services/teacherService";
import ProfilePage from "@/pages/student/ProfilePage.tsx";

export const router = createBrowserRouter([
    // Auth routes (public)
    {
        path: '/',
        element: <StudentLoginPage />,
    },
    {
        path: '/laerer/login',
        element: <TeacherLoginPage />,
    },
    {
        path: '/auth/feide',
        element: <FeideFake />,
    },

    // Student routes — open during development
    {
        path: '/home',
        element: <StudentHomePage />,
    },
    {
        path: '/bli-med',
        element: <JoinWithCodePage />,
    },
    {
        path: '/map',
        element: <MapPage />,
    },
    {
        path: '/levelOne',
        element: <LevelOne />,
    },
    {
        path: '/leaderboard',
        element: <LeaderboardPage />,
    },
    {
        path: '/notes',
        element: <NotesPage />,
    },
    {
        path: '/badges',
        element: <BadgesPage />,
    },
    {
        path: '/weekly-mystery',
        element: <WeeklyMysteryPage />,
    },
    {
        path: '/profile',
        element: <ProfilePage />
    },
    // test path for game - student
    {
        path: '/game',
        element: <GamePage />
    },

    // Teacher routes
    // NOTE: <RequireAuth role="TEACHER"> wrappers removed during dev (see above).
    {
        path: '/teacher',
        element: <MyClassroomsPage />,
    },
    {
        path: '/teacher/map',
        element: <MapPage />,
    },
    {
        path: '/teacher/mystery-submissions',
        element: <MysterySubmissions />,
    },
    {
        path: '/teacher/classroom/new',
        element: <CreateClassroom />,
        action: createClassroomAction,
    },
    {
        path: '/teacher/classroom/:id/code',
        element: <CodePage />,
        loader: async ({ params }) => {
            const id = params.id!;
            return await getClassroom(id);
        },
    },
    {
        path: '/teacher/classroom/:id',
        element: <ClassroomDetail />,
        loader: async ({ params }) => {
            const id = params.id!;
            const [classroom, students, stops] = await Promise.all([
                getClassroom(id),
                getStudentProgress(id),
                getStops(),
            ]);
            return { classroom, students, stops };
        },
    },
    {
        path: '/teacher/classroom/:id/student/:studentId',
        element: <StudentDetail />,
        loader: async ({ params }) => getStudentDetail(params.id!, params.studentId!),
    },
    {
        path: '/teacher/classroom/:id/mystery',
        element: <MysterySubmissions />,
        loader: async ({ params }) => {
            const id = params.id!;
            const [classroom, submissions] = await Promise.all([
                getClassroom(id),
                getMysterySubmissions(id),
            ]);
            return { classroom, submissions };
        },
    },
    {
        path: '/teacher/notifications',
        element: <Notifications />,
        loader: async () => {
            const [notifications, summaries] = await Promise.all([
                getNotifications(),
                getClassroomSummaries(),
            ]);
            return { notifications, classrooms: summaries };
        },
    },
    {
        path: "/avatar",
        element: <AvatarDevPage />,
    },
]);
