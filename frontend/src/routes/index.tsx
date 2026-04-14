import { createBrowserRouter } from "react-router-dom";

import App from '../App';
import Homescreen from "../pages/Homescreen";
import GameMap from "../pages/GameMap";

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
        element: <GameMap />,
    }
]);