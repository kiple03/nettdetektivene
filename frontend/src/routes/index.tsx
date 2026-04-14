import { createBrowserRouter } from "react-router-dom";

import App from '../App';
import Homescreen from "../pages/student/homescreen";
import MapPage from "../pages/student/MapPage";
import LevelOne from "../pages/student/LevelOnePage";

export const router = createBrowserRouter([
    {
        path: '/',
        element: <App />,
    },
    {
        path: '/home',
        element: <Homescreen />,
    },
    {
        path: '/map',
        element: <MapPage />,
    },
    {
        path: '/levelOne',
        element: <LevelOne />,
    },
]);