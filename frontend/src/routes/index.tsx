import { createBrowserRouter } from "react-router-dom";

import StudentLoginPage from "../pages/auth/StudentLoginPage";
import TeacherLoginPage from "../pages/auth/TeacherLoginPage";
import Homescreen from "../pages/student/homescreen";
import GameMap from "../pages/student/GameMap";

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

    // Student routes
    {
        path: '/home',
        element: <Homescreen />,
    },
    {
        path: '/map',
        element: <GameMap />,
    },
]);
