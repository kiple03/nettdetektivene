import { createBrowserRouter } from "react-router-dom";
import MapPage from "../pages/student/MapPage";
import LevelOne from "../pages/student/LevelOnePage";
import StudentLoginPage from "@/pages/auth/StudentLoginPage";
import TeacherLoginPage from "@/pages/auth/TeacherLoginPage";
import FeideFake from "@/pages/auth/FeideFake";
import StudentHomePage from "@/pages/student/StudentHomePage";
import CreateClassroom, { createClassroomAction } from "@/pages/teacher/CreateClassroom";
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

// Route guard temporarily disabled during game development so we don't
// have to log in on every reload. Re-enable by restoring the <RequireAuth>
// wrappers below and uncommenting this import.
// import RequireAuth from "@/routes/RequireAuth";

import {
    getClassroomSummaries,
    getStudentProgress,
    getStudentDetail,
    getMysterySubmissions,
    getNotifications,
    getClassroom,
    getStops,
} from "@/services/teacherService";

export const router = createBrowserRouter([
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
        path: '/teacher',
        element: <MyClassroomsPage />,
        loader: () => getClassroomSummaries(),
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
            const id = Number(params.id);
            return await getClassroom(id);
        },
    },
    {
        path: '/teacher/classroom/:id',
        element: <ClassroomDetail />,
        loader: async ({ params }) => {
            const id = Number(params.id);
            const [classroom, students, stops] = await Promise.all([
                getClassroom(id),
                getStudentProgress(id),
                getStops(id),
            ]);
            return { classroom, students, stops };
        },
    },
    {
        path: '/teacher/classroom/:id/student/:studentId',
        element: <StudentDetail />,
        loader: async ({ params }) => getStudentDetail(Number(params.id), Number(params.studentId)),
    },
    {
        path: '/teacher/classroom/:id/mystery',
        element: <MysterySubmissions />,
        loader: async ({ params }) => {
            const id = Number(params.id);
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