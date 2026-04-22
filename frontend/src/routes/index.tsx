import { createBrowserRouter } from "react-router-dom";
import TeacherLayout from "@/components/layout/TeacherLayout";
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
import ProfilePage from "@/pages/student/ProfilePage.tsx";
import {
    getClassroomSummaries,
    getClassroom,
    getStudentProgress,
    getStops,
    getStudentDetail,
    getMysterySubmissions,
    getNotifications,
    getCoTeachers,
} from "@/services/teacherService";

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
        element: <RequireAuth role="STUDENT"><RequireClassroom><ProfilePage/></RequireClassroom></RequireAuth>
    },
    {
        path: "/avatar",
        element: <RequireAuth role="STUDENT"><AvatarDevPage /></RequireAuth>
    },
    {
        path: '/game/:levelId',
        element: <RequireAuth role="STUDENT"><RequireClassroom><GamePage /></RequireClassroom></RequireAuth>
    },

    // Teacher routes — require TEACHER role
    {
        element: <RequireAuth role="TEACHER"><TeacherLayout /></RequireAuth>,
        children: [
            {
                path: '/teacher',
                element: <MyClassroomsPage />,
            },
            {
                path: '/teacher/map',
                element: <MapPage />,
            },
            {
                path: '/teacher/leaderboard',
                element: <LeaderboardPage />,
            },
            {
                path: '/teacher/medals',
                element: <BadgesPage />,
            },
            {
                path: '/teacher/classrooms/new',
                element: <CreateClassroom />,
                action: createClassroomAction,
            },
            {
                path: '/teacher/classrooms/:id/code',
                element: <CodePage />,
            },
            {
                path: '/teacher/classrooms/:id',
                element: <ClassroomDetail />,
            },
            {
                path: '/teacher/classrooms/:id/students/:studentId',
                element: <StudentDetail />,
            },
            {
                path: '/teacher/classrooms/:id/mystery-submissions',
                element: <MysterySubmissions />,
            },
            {
                path: '/teacher/notifications',
                element: <Notifications />,
            },
        ],
    },
]);
