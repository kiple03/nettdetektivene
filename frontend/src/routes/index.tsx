import { createBrowserRouter } from "react-router-dom";
import MapPage from "../pages/student/MapPage";
import LevelOne from "../pages/student/LevelOnePage";
import StudentLoginPage from "@/pages/auth/StudentLoginPage";
import TeacherLoginPage from "@/pages/auth/TeacherLoginPage";
import FeideFake from "@/pages/auth/FeideFake";
import StudentHomePage from "@/pages/student/StudentHomePage";
import TeacherDashboard from "@/pages/teacher/TeacherDashboard";
import CreateClassroom, { createClassroomAction } from "@/pages/teacher/CreateClassroom";
import ClassroomDetail from "@/pages/teacher/ClassroomDetail";
import StudentDetail from "@/pages/teacher/StudentDetail";
import MysterySubmissions from "@/pages/teacher/MysterySubmissions";
import Notifications from "@/pages/teacher/Notifications";
import RequireAuth from "@/routes/RequireAuth";

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
    // Auth routes
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

    // Student routes
    {
        path: '/home',
        element: <RequireAuth role="STUDENT"><StudentHomePage /></RequireAuth>,
    },
    {
        path: '/map',
        element: <RequireAuth role="STUDENT"><MapPage /></RequireAuth>,
    },
    {
        path: '/levelOne',
        element: <RequireAuth role="STUDENT"><LevelOne /></RequireAuth>,
    },


      // Teacher routes
    {
        path: '/teacher',
        element: <RequireAuth role="TEACHER"><TeacherDashboard /></RequireAuth>,
        loader: () => getClassroomSummaries(),
    },
    {
        path: '/teacher/classroom/new',
        element: <RequireAuth role="TEACHER"><CreateClassroom /></RequireAuth>,
        action: createClassroomAction,
    },
    {
        path: '/teacher/classroom/:id',
        element: <RequireAuth role="TEACHER"><ClassroomDetail /></RequireAuth>,
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
        element: <RequireAuth role="TEACHER"><StudentDetail /></RequireAuth>,
        loader: async ({ params }) => getStudentDetail(Number(params.id), Number(params.studentId)),
    },
    {
        path: '/teacher/classroom/:id/mystery',
        element: <RequireAuth role="TEACHER"><MysterySubmissions /></RequireAuth>,
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
        element: <RequireAuth role="TEACHER"><Notifications /></RequireAuth>,
        loader: async () => {
            const [notifications, summaries] = await Promise.all([
                getNotifications(),
                getClassroomSummaries(),
            ]);
            return { notifications, classrooms: summaries.map(s => s.classroom) };
        },
    },
]);
