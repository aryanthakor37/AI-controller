import React from 'react';
import { Outlet, NavLink, Link, Navigate } from 'react-router-dom';
import { LayoutDashboard, MessageSquare, Mic, History, Smartphone, Settings, User, Bell, Layers } from 'lucide-react';
import { cn } from '../utils/cn';
import { useSelector } from 'react-redux';

const Sidebar = () => (
  <aside className="w-64 h-screen glass border-r border-white/10 flex flex-col pt-8 z-20">
    <div className="px-6 mb-10">
      <Link to="/">
        <h1 className="text-2xl font-bold bg-gradient-to-r from-primary to-accent-cyan bg-clip-text text-transparent hover:opacity-80 transition-opacity cursor-pointer">
          Agent.AI
        </h1>
      </Link>
    </div>
    <nav className="flex-1 px-4 space-y-2 overflow-y-auto">
      {[
        { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
        { name: 'AI Chat', path: '/dashboard/chat', icon: MessageSquare },
        { name: 'Voice Control', path: '/dashboard/voice', icon: Mic },
        { name: 'AI Routines', path: '/dashboard/routines', icon: Layers },
        { name: 'Reminders', path: '/dashboard/reminders', icon: Bell },
        { name: 'History', path: '/dashboard/history', icon: History },
        { name: 'Device', path: '/dashboard/device', icon: Smartphone },
        { name: 'Settings', path: '/dashboard/settings', icon: Settings },
      ].map((item) => (
        <NavLink
          key={item.name}
          to={item.path}
          end={item.path === '/dashboard'}
          className={({ isActive }) => cn(
            'flex items-center w-full px-4 py-3 rounded-xl transition-all',
            isActive ? 'bg-primary/20 text-primary-400 font-medium shadow-inner' : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
          )}
        >
          <item.icon className="w-5 h-5 mr-3" />
          {item.name}
        </NavLink>
      ))}
    </nav>
  </aside>
);

const DashboardLayout = () => {
  const { user, isAuthenticated } = useSelector(state => state.user);
  
  React.useEffect(() => {
    import('../services/socketService').then(module => {
      module.default.connect();
    });
  }, []);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="flex h-screen bg-background overflow-hidden selection:bg-primary/30">
      <Sidebar />
      <main className="flex-1 flex flex-col h-screen relative">
        {/* Ambient background glow */}
        <div className="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-primary/20 blur-[120px] rounded-full pointer-events-none" />
        <div className="absolute bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-accent-purple/20 blur-[120px] rounded-full pointer-events-none" />

        <header className="h-20 glass border-b border-white/5 flex items-center justify-between px-8 z-10">
          <h2 className="text-xl font-semibold text-slate-200">Overview</h2>
          <div className="flex items-center space-x-4">
            <Link to="/dashboard/profile">
              <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-primary to-accent-indigo p-0.5 cursor-pointer hover:scale-105 transition-transform">
                <img src={user.avatar} alt="Profile" className="w-full h-full bg-surface rounded-full object-cover" />
              </div>
            </Link>
          </div>
        </header>
        <div className="flex-1 overflow-y-auto p-8 z-10 flex flex-col">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default DashboardLayout;
