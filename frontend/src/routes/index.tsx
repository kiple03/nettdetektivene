import { createBrowserRouter } from "react-router-dom";
import { lazy } from "react";
import RequireAuth from "@/routes/RequireAuth";
import RequireClassroom from "@/routes/RequireClassroom";
import CreateClassroom, { createClassroomAction } from "@/pages/teacher/CreateClassroom";

const TeacherLayout = lazy(() => import("@/components/teacher_layout/TeacherLayout"));
const MapPage = lazy(() => import("../pages/student/MapPage"));
const StudentLoginPage = lazy(() => import("@/pages/auth/StudentLoginPage"));
const TeacherLoginPage = lazy(() => import("@/pages/auth/TeacherLoginPage"));
const FeideFake = lazy(() => import("@/pages/auth/FeideFake"));
const StudentHomePage = lazy(() => import("@/pages/student/StudentHomePage"));
const ClassroomDetail = lazy(() => import("@/pages/teacher/detail/ClassroomDetail"));
const StudentDetail = lazy(() => import("@/pages/teacher/detail/StudentDetail"));
const LevelQuestionsPage = lazy(() => import("@/pages/teacher/detail/LevelQuestionsPage"));
const MysterySubmissions = lazy(() => import("@/pages/teacher/MysterySubmissions"));
const Notifications = lazy(() => import("@/pages/teacher/Notifications"));
const LeaderboardPage = lazy(() => import("@/pages/student/LeaderboardPage.tsx"));
const NotesPage = lazy(() => import("@/pages/student/NotesPage"));
const BadgesPage = lazy(() => import("@/pages/student/MedalionPage"));
const WeeklyMysteryPage = lazy(() => import("@/pages/student/WeeklyMysteryPage"));
const JoinWithCodePage = lazy(() => import("@/pages/student/JoinWithCodePage"));
const MyClassroomsPage = lazy(() => import("@/pages/teacher/MyClassroomsPage"));
const AvatarDevPage = lazy(() => import("@/pages/student/AvatarDevPage"));
const GamePage = lazy(() => import("@/pages/game/GamePage"));
const BossBattlePage = lazy(() => import("@/pages/game/BossBattlePage"));
const CodePage = lazy(() => import("@/pages/teacher/CodePage"));
const ProfilePage = lazy(() => import("@/pages/student/ProfilePage"));
const TeacherMapPage = lazy(() => import("@/pages/teacher/TeacherMapPage"));
const TeacherLevelPreviewPage = lazy(() => import("@/pages/teacher/detail/TeacherLevelPreviewPage"));

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
    {
        path: '/boss',
        element: <RequireAuth role="STUDENT"><RequireClassroom><BossBattlePage /></RequireClassroom></RequireAuth>
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
                path: '/teacher/classrooms/:classroomId/map',
                element: <TeacherMapPage />,
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
                path: '/teacher/classrooms/:classroomId/levels/:levelId/questions',
                element: <LevelQuestionsPage />,
            },
            {
                path: '/teacher/classrooms/:classroomId/levels/:levelId/preview',
                element: <TeacherLevelPreviewPage />,
            },
            {
                path: '/teacher/notifications',
                element: <Notifications />,
            },
        ],
    },
]);
