import React from 'react';
import { Outlet } from 'react-router-dom';

const PublicLayout = () => {
  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center relative overflow-hidden">
      {/* Ambient background glow */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/20 blur-[120px] rounded-full pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-accent-purple/20 blur-[120px] rounded-full pointer-events-none" />

      <div className="w-full max-w-md p-8 z-10">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-accent-cyan bg-clip-text text-transparent">
            Agent.AI
          </h1>
          <p className="text-slate-400 mt-2">Control your world with a whisper.</p>
        </div>
        <Outlet />
      </div>
    </div>
  );
};

export default PublicLayout;
