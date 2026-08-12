import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import DashboardLayout from '../layouts/DashboardLayout';
import PublicLayout from '../layouts/PublicLayout';
import Landing from '../pages/Landing';
import Dashboard from '../pages/Dashboard';
import Login from '../pages/Login';
import Register from '../pages/Register';
import Chat from '../pages/Chat';
import Voice from '../pages/Voice';
import History from '../pages/History';
import Profile from '../pages/Profile';
import Device from '../pages/Device';
import Settings from '../pages/Settings';
import Routines from '../pages/Routines';
import NotFound from '../pages/NotFound';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Landing />,
    errorElement: <NotFound />
  },
  {
    path: '/',
    element: <PublicLayout />,
    children: [
      { path: 'login', element: <Login /> },
      { path: 'register', element: <Register /> },
    ],
  },
  {
    path: '/dashboard',
    element: <DashboardLayout />,
    children: [
      { index: true, element: <Dashboard /> },
      { path: 'chat', element: <Chat /> },
      { path: 'voice', element: <Voice /> },
      { path: 'routines', element: <Routines /> },
      { path: 'history', element: <History /> },
      { path: 'device', element: <Device /> },
      { path: 'settings', element: <Settings /> },
      { path: 'profile', element: <Profile /> },
    ],
  },
]);

export const AppRouter = () => {
  return <RouterProvider router={router} />;
};
