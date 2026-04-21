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
import NotesPage from "@/pages/student/notebook/NotesPage.tsx";
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
    // test path for game - student
    {
        path: '/game',
        element: <RequireAuth role="STUDENT"><RequireClassroom><GamePage /></RequireClassroom></RequireAuth>
    },

    // Teacher routes — require TEACHER role
    {
        element: <RequireAuth role="TEACHER"><TeacherLayout /></RequireAuth>,
        children: [
            {
                path: '/teacher',
                element: <MyClassroomsPage />,
                loader: () => getClassroomSummaries(),
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
        ],
    },
]);
