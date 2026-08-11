import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { getApiUrl } from '../config';
import { motion } from 'framer-motion';
import { Battery, HardDrive, Smartphone, Zap, Play, Wifi, Cake, Bell, Calendar, VolumeX, Volume2, Flashlight, MonitorPlay, Bluetooth, Loader2, Check, Plus, History } from 'lucide-react';
import { Card } from '../components/atoms/Card';
import socketService from '../services/socketService';

const Dashboard = () => {
  const dispatch = useDispatch();
  const { status, activeDevices } = useSelector((state) => state.device);
  const { history } = useSelector((state) => state.commands);
  const { user } = useSelector((state) => state.user);

  const [dbHistory, setDbHistory] = React.useState([]);
  const [analytics, setAnalytics] = React.useState(null);
  const [remindersList, setRemindersList] = React.useState([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [commandStatus, setCommandStatus] = React.useState({});

  useEffect(() => {
    setIsLoading(true);
    const token = localStorage.getItem('token');

    Promise.allSettled([
      fetch(`${getApiUrl()}/device/list-active`).then(res => res.json()).then(data => {
        if (data && data.length > 0) dispatch({ type: 'device/setDevices', payload: data });
      }),
      fetch(`${getApiUrl()}/history`, { headers: { 'Authorization': `Bearer ${token}` } }).then(res => res.json()).then(data => {
        if (Array.isArray(data)) setDbHistory(data);
      }),
      fetch(`${getApiUrl()}/analytics`, { headers: { 'Authorization': `Bearer ${token}` } }).then(res => res.json()).then(data => {
        if (data) setAnalytics(data);
      }),
      fetch(`${getApiUrl()}/reminders`, { headers: { 'Authorization': `Bearer ${token}` } }).then(res => res.json()).then(data => {
        if (Array.isArray(data)) setRemindersList(data);
      })
    ]).finally(() => {
      setIsLoading(false);
    });
  }, [dispatch]);

  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: { staggerChildren: 0.1 }
    }
  };

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  const sendRemoteCommand = (intent) => {
    if (activeDevices.length > 0) {
      const targetSocketId = activeDevices[0].socketId;
      socketService.sendCommand(targetSocketId, { intent });

      setCommandStatus(prev => ({ ...prev, [intent]: 'loading' }));
      setTimeout(() => {
        setCommandStatus(prev => ({ ...prev, [intent]: 'success' }));
        setTimeout(() => {
          setCommandStatus(prev => ({ ...prev, [intent]: null }));
        }, 2000);
      }, 800);
    } else {
      alert("No device connected!");
    }
  };

  const CommandButton = ({ intent, icon: Icon, label, colorClass = "text-slate-400 hover:text-primary", destructive = false }) => {
    const status = commandStatus[intent];
    let btnClass = "btn-space flex flex-col items-center justify-center p-3 rounded-xl relative overflow-hidden group h-[80px] ";
    btnClass += destructive ? "text-slate-400 hover:text-red-400 hover:bg-red-500/5" : colorClass;

    return (
      <button onClick={() => sendRemoteCommand(intent)} disabled={status === 'loading'} className={btnClass}>
        <div className={`transition-transform duration-300 absolute inset-0 flex flex-col items-center justify-center ${status ? 'scale-0 opacity-0' : 'scale-100 opacity-100'}`}>
          <Icon className="w-6 h-6 mb-2 group-hover:scale-110 transition-transform" />
          <span className="font-semibold text-sm">{label}</span>
        </div>
        <div className={`transition-transform duration-300 absolute inset-0 flex flex-col items-center justify-center ${status === 'loading' ? 'scale-100 opacity-100' : 'scale-0 opacity-0'}`}>
          <Loader2 className="w-6 h-6 animate-spin" />
        </div>
        <div className={`transition-transform duration-300 absolute inset-0 flex flex-col items-center justify-center ${status === 'success' ? 'scale-100 opacity-100' : 'scale-0 opacity-0'}`}>
          <Check className="w-8 h-8 text-green-500 drop-shadow-[0_0_8px_rgba(34,197,94,0.5)]" />
        </div>
      </button>
    );
  };

  return (
    <div className="relative w-full min-h-screen">
      {/* Background Animated Blobs */}
      <div className="absolute top-20 left-10 w-96 h-96 bg-primary/20 rounded-full mix-blend-screen filter blur-[100px] opacity-50 animate-blob pointer-events-none"></div>
      <div className="absolute top-40 right-20 w-72 h-72 bg-accent-cyan/20 rounded-full mix-blend-screen filter blur-[100px] opacity-50 animate-blob animation-delay-2000 pointer-events-none"></div>
      <div className="absolute -bottom-8 left-40 w-80 h-80 bg-accent-indigo/30 rounded-full mix-blend-screen filter blur-[120px] opacity-50 animate-blob animation-delay-4000 pointer-events-none"></div>

      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="relative z-10 w-full pb-10"
      >
      <motion.div variants={item} className="flex justify-between items-end mb-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white/90">Overview</h1>
          <p className="text-white/40 mt-1 text-sm font-medium">
            {activeDevices.length > 0 ? `${activeDevices.length} active device(s)` : 'System standing by. No active devices.'}
          </p>
        </div>
        <div className="flex space-x-4">
          <button
            onClick={async () => {
              try {
                const token = localStorage.getItem('token');
                const res = await fetch(`${getApiUrl()}/device/generate-code`, {
                  headers: { 'Authorization': `Bearer ${token}` }
                });
                const data = await res.json();
                if (res.ok) alert(`Your Pairing Code is: ${data.pairingCode}`);
                else alert(data.message);
              } catch (e) { alert('Failed to generate code'); }
            }}
            className="btn-space flex items-center px-5 py-2.5 text-sm font-medium hover:bg-primary/20 hover:border-primary/50 text-white shadow-[0_0_15px_rgba(255,0,127,0.3)] hover:shadow-[0_0_25px_rgba(255,0,127,0.6)] transition-all duration-300"
          >
            <Plus className="w-4 h-4 mr-2" />
            Pair New Device
          </button>
        </div>
      </motion.div>

      {/* BENTO GRID */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-8">
        
        {/* ROW 1: Analytics */}
        {analytics && (
          <motion.div variants={item} className="col-span-1 md:col-span-12 grid grid-cols-2 lg:grid-cols-4 gap-6">
            <Card className="p-6 flex flex-col justify-between group hover:bg-white/[0.05] transition-all duration-300 border-white/10 hover:border-primary/50 relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-primary/20 rounded-full blur-3xl -mr-10 -mt-10 pointer-events-none group-hover:bg-primary/40 transition-colors duration-500"></div>
              <div className="flex justify-between items-start mb-6">
                <p className="text-[11px] font-bold text-white/50 uppercase tracking-[0.2em]">Total API Commands</p>
                <span className="text-[10px] font-bold text-green-400 bg-green-500/10 px-2 py-0.5 rounded border border-green-500/20 shadow-[0_0_10px_rgba(34,197,94,0.2)]">+14%</span>
              </div>
              <p className="text-5xl font-light text-white tracking-tighter drop-shadow-md">{analytics.totalCommands || 0}</p>
            </Card>
            <Card className="p-6 flex flex-col justify-between group hover:bg-white/[0.05] transition-all duration-300 border-white/10 hover:border-accent-indigo/50 relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-accent-indigo/20 rounded-full blur-3xl -mr-10 -mt-10 pointer-events-none group-hover:bg-accent-indigo/40 transition-colors duration-500"></div>
              <div className="flex justify-between items-start mb-6">
                <p className="text-[11px] font-bold text-white/50 uppercase tracking-[0.2em]">Success Rate</p>
                <span className="text-[10px] font-bold text-green-400 bg-green-500/10 px-2 py-0.5 rounded border border-green-500/20 shadow-[0_0_10px_rgba(34,197,94,0.2)]">Optimal</span>
              </div>
              <p className="text-5xl font-light text-white tracking-tighter drop-shadow-md">{analytics.successRate || 0}<span className="text-2xl text-white/30 ml-1">%</span></p>
            </Card>
            <Card className="p-6 flex flex-col justify-between group hover:bg-white/[0.05] transition-all duration-300 border-white/10 hover:border-red-500/50 relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-red-500/20 rounded-full blur-3xl -mr-10 -mt-10 pointer-events-none group-hover:bg-red-500/40 transition-colors duration-500"></div>
              <div className="flex justify-between items-start mb-6">
                <p className="text-[11px] font-bold text-white/50 uppercase tracking-[0.2em]">Failed Executions</p>
                <span className="text-[10px] font-bold text-white/60 bg-white/5 px-2 py-0.5 rounded border border-white/20 shadow-[0_0_10px_rgba(255,255,255,0.1)]">0 issues</span>
              </div>
              <p className="text-5xl font-light text-white tracking-tighter drop-shadow-md">{analytics.failedCommands || 0}</p>
            </Card>
            <Card className="p-6 flex flex-col justify-between group hover:bg-white/[0.05] transition-all duration-300 border-white/10 hover:border-accent-cyan/50 relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-accent-cyan/20 rounded-full blur-3xl -mr-10 -mt-10 pointer-events-none group-hover:bg-accent-cyan/40 transition-colors duration-500"></div>
              <div className="flex justify-between items-start mb-6">
                <p className="text-[11px] font-bold text-white/50 uppercase tracking-[0.2em]">Avg Latency</p>
                <span className="text-[10px] font-bold text-yellow-400 bg-yellow-500/10 px-2 py-0.5 rounded border border-yellow-500/20 shadow-[0_0_10px_rgba(250,204,21,0.2)]">Stable</span>
              </div>
              <p className="text-5xl font-light text-white tracking-tighter drop-shadow-md">{analytics.avgSpeedMs || 0}<span className="text-2xl text-white/30 ml-2">ms</span></p>
            </Card>
          </motion.div>
        )}

        {/* ROW 2: Remote Control & Device Status */}
        <motion.div variants={item} className="col-span-1 md:col-span-8 flex flex-col">
          <Card className="flex-grow p-6 flex flex-col group border-white/10 hover:border-white/20 transition-all duration-300">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-sm font-semibold text-white/90 flex items-center tracking-wider">
                <Zap className="w-5 h-5 mr-2 text-primary animate-pulse-glow" /> Remote Control Matrix
              </h2>
              <div className="w-2 h-2 rounded-full bg-primary/80 group-hover:bg-primary shadow-[0_0_10px_#FF007F] transition-all duration-300"></div>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 flex-grow">
              <CommandButton intent="VOLUME_MUTE" icon={VolumeX} label="Silent Mode" destructive={true} />
              <CommandButton intent="VOLUME_UNMUTE" icon={Volume2} label="General Mode" />
              <CommandButton intent="FLASHLIGHT_ON" icon={Flashlight} label="Torch ON" />
              <CommandButton intent="FLASHLIGHT_OFF" icon={Flashlight} label="Torch OFF" />
              <CommandButton intent="OPEN_YOUTUBE" icon={MonitorPlay} label="Open YouTube" />
              <CommandButton intent="OPEN_CAMERA" icon={Zap} label="Open Camera" />
              <CommandButton intent="TOGGLE_WIFI" icon={Wifi} label="Toggle Wi-Fi" />
              <CommandButton intent="TOGGLE_BLUETOOTH" icon={Bluetooth} label="Toggle Bluetooth" />
            </div>
          </Card>
        </motion.div>

        <motion.div variants={item} className="col-span-1 md:col-span-4 flex flex-col">
          <Card className="flex-grow p-6 border-white/5 shadow-none flex flex-col relative overflow-hidden">
            <h2 className="text-sm font-semibold text-white/80 flex items-center tracking-wide mb-6">
              <Smartphone className="w-4 h-4 mr-2 text-white/40" /> Active Device Link
            </h2>
            
            {isLoading ? (
              <div className="flex-grow flex flex-col justify-center animate-pulse">
                <div className="h-4 bg-white/5 rounded w-1/2 mb-4"></div>
                <div className="h-10 bg-white/5 rounded w-full mb-2"></div>
                <div className="h-10 bg-white/5 rounded w-full"></div>
              </div>
            ) : activeDevices.length > 0 ? (
              <div className="flex-grow flex flex-col justify-between">
                <div>
                  <div className="flex items-center space-x-3 mb-6">
                    <div className="relative flex h-3 w-3">
                      <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                      <span className="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
                    </div>
                    <h3 className="text-2xl font-light text-white/90 tracking-tight">{activeDevices[0].deviceName}</h3>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-3">
                    <div className="bg-white/[0.02] border border-white/5 rounded-lg p-3">
                      <p className="text-[10px] font-bold text-white/30 uppercase tracking-widest mb-1">Battery</p>
                      <p className="text-lg font-medium text-white/80">{activeDevices[0].batteryPercentage}%</p>
                    </div>
                    <div className="bg-white/[0.02] border border-white/5 rounded-lg p-3">
                      <p className="text-[10px] font-bold text-white/30 uppercase tracking-widest mb-1">Latency</p>
                      <p className="text-lg font-medium text-white/80">{activeDevices[0].latency || 0}ms</p>
                    </div>
                  </div>
                </div>
                <div className="mt-6 pt-4 border-t border-white/5 flex justify-between items-center text-xs text-white/40 font-mono">
                  <span>ID: {activeDevices[0].socketId.substring(0, 8)}</span>
                  <span>v{activeDevices[0].androidVersion}</span>
                </div>
              </div>
            ) : (
              <div className="flex-grow flex flex-col items-center justify-center text-center">
                <div className="w-12 h-12 bg-white/5 rounded-full flex items-center justify-center mb-4 border border-white/5">
                  <Smartphone className="w-5 h-5 text-white/20" />
                </div>
                <p className="text-sm font-medium text-white/70 mb-2">No Uplink Established</p>
                <p className="text-[11px] text-white/40 max-w-[200px] leading-relaxed">
                  Generate a pairing code and connect your Android agent to begin telemetry.
                </p>
              </div>
            )}
          </Card>
        </motion.div>

        {/* ROW 3: History & Reminders */}
        <motion.div variants={item} className="col-span-1 md:col-span-6 flex flex-col">
          <Card className="flex-grow p-0 border-white/5 shadow-none overflow-hidden h-[340px] flex flex-col">
            <div className="p-5 border-b border-white/5 flex justify-between items-center bg-white/[0.01]">
              <h2 className="text-sm font-semibold text-white/80 flex items-center tracking-wide">
                <HardDrive className="w-4 h-4 mr-2 text-white/40" /> Execution Log
              </h2>
            </div>
            <div className="overflow-y-auto flex-grow p-2 space-y-1">
              {dbHistory.map((cmd) => (
                <div key={cmd._id} className="flex items-center justify-between p-3 rounded-lg hover:bg-white/5 transition-colors group">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 rounded-md bg-white/5 border border-white/5 flex items-center justify-center group-hover:bg-primary/10 group-hover:border-primary/20 transition-colors">
                      <Play className="w-3 h-3 text-white/40 group-hover:text-primary" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-white/80">{cmd.command}</p>
                      <p className="text-[10px] text-white/40 font-mono mt-0.5">{cmd.intent || 'unknown'}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className={`text-[10px] font-bold tracking-wider uppercase px-2 py-1 rounded-md ${cmd.status === 'Success' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'}`}>
                      {cmd.status}
                    </div>
                    <p className="text-[10px] text-white/30 font-mono mt-1">{new Date(cmd.createdAt).toLocaleTimeString()}</p>
                  </div>
                </div>
              ))}
              {dbHistory.length === 0 && (
                <div className="flex flex-col items-center justify-center h-full text-center p-6 relative">
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <History className="w-48 h-48 text-white/5 blur-xl" />
                  </div>
                  <p className="text-sm text-white/50 font-medium relative z-10 mb-1">No execution logs found</p>
                  <p className="text-[10px] text-white/30 relative z-10 uppercase tracking-widest">Awaiting Commands</p>
                </div>
              )}
            </div>
          </Card>
        </motion.div>

        <motion.div variants={item} className="col-span-1 md:col-span-6 flex flex-col">
          <Card className="flex-grow p-0 border-white/5 shadow-none overflow-hidden h-[340px] flex flex-col">
            <div className="p-5 border-b border-white/5 flex justify-between items-center bg-white/[0.01]">
              <h2 className="text-sm font-semibold text-white/80 flex items-center tracking-wide">
                <Bell className="w-4 h-4 mr-2 text-white/40" /> Active Reminders
              </h2>
              <Link to="/dashboard/reminders" className="text-[10px] font-bold uppercase tracking-widest text-primary hover:text-primary/80 transition-colors">
                Manage All
              </Link>
            </div>
            <div className="overflow-y-auto flex-grow p-2 space-y-1">
              {remindersList.map((rem) => {
                const isBirthday = rem.title.toLowerCase().includes('birthday') || rem.repeat === 'YEARLY';
                return (
                  <div key={rem._id} className="p-3 rounded-lg hover:bg-white/5 transition-colors flex items-center justify-between group">
                    <div className="flex items-center space-x-3">
                      <div className={`w-8 h-8 rounded-md flex items-center justify-center border ${isBirthday ? 'bg-pink-500/10 border-pink-500/20 text-pink-400' : 'bg-white/5 border-white/10 text-white/40 group-hover:text-white/80'}`}>
                        {isBirthday ? <Cake className="w-3 h-3" /> : <Calendar className="w-3 h-3" />}
                      </div>
                      <div>
                        <p className="text-sm font-medium text-white/80">{rem.title}</p>
                        <p className="text-[10px] text-white/40 font-mono mt-0.5">
                          {rem.date} @ {rem.time}
                        </p>
                      </div>
                    </div>
                    <span className="text-[9px] font-bold px-1.5 py-0.5 border border-white/10 text-white/30 rounded uppercase tracking-widest bg-white/5">
                      {rem.repeat || 'ONCE'}
                    </span>
                  </div>
                );
              })}
              {remindersList.length === 0 && (
                <div className="flex flex-col items-center justify-center h-full text-center p-6 relative">
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <Bell className="w-48 h-48 text-white/5 blur-xl" />
                  </div>
                  <p className="text-sm text-white/50 font-medium relative z-10 mb-1">No active reminders</p>
                  <p className="text-[10px] text-white/30 relative z-10 uppercase tracking-widest">Use voice to set one</p>
                </div>
              )}
            </div>
          </Card>
        </motion.div>

      </div>
      </motion.div>
    </div>
  );
};

export default Dashboard;
