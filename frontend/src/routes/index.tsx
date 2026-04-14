import { createBrowserRouter } from "react-router-dom";

import StudentLoginPage from "@/pages/auth/StudentLoginPage";
import TeacherLoginPage from "@/pages/auth/TeacherLoginPage";
import FeideFake from "@/pages/auth/FeideFake";
import Homescreen from "@/pages/student/homescreen";
import GameMap from "@/pages/student/GameMap";
import TeacherDashboard from "@/pages/teacher/TeacherDashboard";
import CreateClassroom, { createClassroomAction } from "@/pages/teacher/CreateClassroom";
import ClassroomDetail from "@/pages/teacher/ClassroomDetail";
import StudentDetail from "@/pages/teacher/StudentDetail";
import MysterySubmissions from "@/pages/teacher/MysterySubmissions";
import Notifications from "@/pages/teacher/Notifications";

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
        element: <Homescreen />,
    },
    {
        path: '/map',
        element: <GameMap />,
    },
    // Teacher routes
    {
        path: '/teacher',
        element: <TeacherDashboard />,
        loader: () => getClassroomSummaries(),
    },
    {
        path: '/teacher/classroom/new',
        element: <CreateClassroom />,
        action: createClassroomAction,
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
            return { notifications, classrooms: summaries.map(s => s.classroom) };
        },
    },
]);
