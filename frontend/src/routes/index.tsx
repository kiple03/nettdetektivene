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
import AvatarDevPage from "@/pages/student/AvatarDevPage";
import GamePage from "@/pages/game/GamePage.tsx";
import CodePage from "@/pages/teacher/CodePage.tsx";

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
    },
    {
        path: '/teacher/classroom/:id',
        element: <RequireAuth role="TEACHER"><ClassroomDetail /></RequireAuth>,
    },
    {
        path: '/teacher/classroom/:id/student/:studentId',
        element: <RequireAuth role="TEACHER"><StudentDetail /></RequireAuth>,
    },
    {
        path: '/teacher/classroom/:id/mystery',
        element: <RequireAuth role="TEACHER"><MysterySubmissions /></RequireAuth>,
    },
    {
        path: '/teacher/notifications',
        element: <RequireAuth role="TEACHER"><Notifications /></RequireAuth>,
    },
    {
        path: "/avatar",
        element: <AvatarDevPage />,
    },
]);
