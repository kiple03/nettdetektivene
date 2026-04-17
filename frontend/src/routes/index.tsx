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
import MyClassroomsPage from "@/pages/teacher/MyClassroomsPage.tsx";
import TestPage from "@/pages/game/TestGamePage.tsx";

import RequireAuth from "@/routes/RequireAuth";
import RequireClassroom from "@/routes/RequireClassroom";

import {
    getClassroomSummaries,
    getStudentProgress,
    getStudentDetail,
    getMysterySubmissions,
    getNotifications,
    getClassroom,
    getStops,
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

    // Student routes — require STUDENT role
    {
        path: '/home',
        element: <RequireAuth role="STUDENT"><RequireClassroom><StudentHomePage /></RequireClassroom></RequireAuth>,
    },
    {
        path: '/join-with-code',
        element: <RequireAuth role="STUDENT"><JoinWithCodePage /></RequireAuth>,
    },
    {
        path: '/map',
        element: <RequireAuth role="STUDENT"><RequireClassroom><MapPage /></RequireClassroom></RequireAuth>,
    },
    {
        path: '/levelOne',
        element: <RequireAuth role="STUDENT"><RequireClassroom><LevelOne /></RequireClassroom></RequireAuth>,
    },
    {
        path: '/leaderboard',
        element: <RequireAuth role="STUDENT"><RequireClassroom><LeaderboardPage /></RequireClassroom></RequireAuth>,
    },
    {
        path: '/notes',
        element: <RequireAuth role="STUDENT"><RequireClassroom><NotesPage /></RequireClassroom></RequireAuth>,
    },
    {
        path: '/badges',
        element: <RequireAuth role="STUDENT"><RequireClassroom><BadgesPage /></RequireClassroom></RequireAuth>,
    },
    {
        path: '/weekly-mystery',
        element: <RequireAuth role="STUDENT"><RequireClassroom><WeeklyMysteryPage /></RequireClassroom></RequireAuth>,
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

    // Teacher routes — require TEACHER role
    {
        path: '/teacher',
        element: <RequireAuth role="TEACHER"><MyClassroomsPage /></RequireAuth>,
        loader: () => getClassroomSummaries(),
    },
    {
        path: '/teacher/map',
        element: <MapPage />,
    },
    {
        path: '/teacher/classroom/new',
        element: <RequireAuth role="TEACHER"><CreateClassroom /></RequireAuth>,
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
        element: <RequireAuth role="TEACHER"><ClassroomDetail /></RequireAuth>,
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
        element: <RequireAuth role="TEACHER"><StudentDetail /></RequireAuth>,
        loader: async ({ params }) => getStudentDetail(Number(params.id), Number(params.studentId)),
    },
    {
        path: '/teacher/classroom/:id/mystery',
        element: <RequireAuth role="TEACHER"><MysterySubmissions /></RequireAuth>,
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
        element: <RequireAuth role="TEACHER"><Notifications /></RequireAuth>,
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
