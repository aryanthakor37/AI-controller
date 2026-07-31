import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { getApiUrl } from '../config';
import { motion } from 'framer-motion';
import { Battery, HardDrive, Smartphone, Zap, Play, Wifi, Cake, Bell, Calendar, VolumeX, Volume2, Flashlight, MonitorPlay } from 'lucide-react';
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

  useEffect(() => {
    socketService.connect();
    
    // Fallback: Fetch active devices directly via HTTP to prevent Socket.IO race conditions on reload
    fetch(`${getApiUrl()}/device/list-active`)
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0) {
          dispatch({ type: 'device/setDevices', payload: data });
        }
      })
      .catch(console.error);

    // Fetch DB command execution history
    const token = localStorage.getItem('token');
    fetch(`${getApiUrl()}/history`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) setDbHistory(data);
      })
      .catch(console.error);

    // Fetch analytics metrics
    fetch(`${getApiUrl()}/analytics`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(data => {
        if (data) setAnalytics(data);
      })
      .catch(console.error);

    // Fetch real saved Birthday & Event Reminders
    fetch(`${getApiUrl()}/reminders`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) setRemindersList(data);
      })
      .catch(console.error);

    return () => socketService.disconnect();
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
      alert(`${intent} command sent to device!`);
    } else {
      alert("No device connected!");
    }
  };

  return (
    <motion.div 
      variants={container}
      initial="hidden"
      animate="show"
      className="space-y-6"
    >
      <motion.div variants={item} className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Welcome back, {user ? user.name : 'Developer'}</h1>
          <p className="text-slate-400 mt-1">
            {activeDevices.length > 0 ? `${activeDevices.length} device(s) online.` : 'No devices connected currently.'}
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
                if(res.ok) alert(`Your Pairing Code is: ${data.pairingCode}`);
                else alert(data.message);
              } catch(e) { alert('Failed to generate code'); }
            }}
            className="flex items-center px-4 py-2 bg-blue-500/20 text-blue-400 border border-blue-500/50 rounded-xl hover:bg-blue-500/30 transition-colors"
          >
            Pair New Device
          </button>
        </div>
      </motion.div>

      {/* Remote Control Quick Actions */}
      <motion.div variants={item} className="mb-8">
        <h2 className="text-xl font-bold mb-4">Remote Control (Testing)</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <button onClick={() => sendRemoteCommand("VOLUME_MUTE")} className="flex flex-col items-center justify-center p-4 bg-red-500/10 border border-red-500/20 rounded-xl hover:bg-red-500/20 transition-colors text-red-400">
            <VolumeX className="w-6 h-6 mb-2" />
            <span className="font-semibold text-sm">Silent Mode</span>
          </button>
          <button onClick={() => sendRemoteCommand("VOLUME_UNMUTE")} className="flex flex-col items-center justify-center p-4 bg-green-500/10 border border-green-500/20 rounded-xl hover:bg-green-500/20 transition-colors text-green-400">
            <Volume2 className="w-6 h-6 mb-2" />
            <span className="font-semibold text-sm">General Mode</span>
          </button>
          <button onClick={() => sendRemoteCommand("FLASHLIGHT_ON")} className="flex flex-col items-center justify-center p-4 bg-yellow-500/10 border border-yellow-500/20 rounded-xl hover:bg-yellow-500/20 transition-colors text-yellow-400">
            <Flashlight className="w-6 h-6 mb-2" />
            <span className="font-semibold text-sm">Torch ON</span>
          </button>
          <button onClick={() => sendRemoteCommand("FLASHLIGHT_OFF")} className="flex flex-col items-center justify-center p-4 bg-slate-500/10 border border-slate-500/20 rounded-xl hover:bg-slate-500/20 transition-colors text-slate-400">
            <Flashlight className="w-6 h-6 mb-2 opacity-50" />
            <span className="font-semibold text-sm">Torch OFF</span>
          </button>
          <button onClick={() => sendRemoteCommand("OPEN_YOUTUBE")} className="flex flex-col items-center justify-center p-4 bg-red-600/10 border border-red-600/20 rounded-xl hover:bg-red-600/20 transition-colors text-red-500">
            <MonitorPlay className="w-6 h-6 mb-2" />
            <span className="font-semibold text-sm">Open YouTube</span>
          </button>
          <button onClick={() => sendRemoteCommand("OPEN_CAMERA")} className="flex flex-col items-center justify-center p-4 bg-primary/10 border border-primary/20 rounded-xl hover:bg-primary/20 transition-colors text-primary">
            <Zap className="w-6 h-6 mb-2" />
            <span className="font-semibold text-sm">Open Camera</span>
          </button>
        </div>
      </motion.div>

      {/* Real-Time Connected Devices List */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 mb-8">
        {activeDevices.map(device => (
          <motion.div variants={item} key={device.socketId}>
            <Card className="flex flex-col space-y-4 border-t-4 border-t-green-500">
              <div className="flex justify-between items-center">
                <div className="flex items-center space-x-3">
                  <div className="w-3 h-3 rounded-full bg-green-500 animate-pulse"></div>
                  <h3 className="text-xl font-bold">{device.deviceName}</h3>
                </div>
                <div className="flex items-center space-x-1 text-xs font-mono text-slate-400 bg-surface px-2 py-1 rounded-md">
                  <Wifi className="w-3 h-3 text-green-400" />
                  <span className={device.latency > 300 ? 'text-yellow-400' : 'text-green-400'}>
                    {device.latency || 0}ms
                  </span>
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-4 text-sm mt-4">
                <div>
                  <p className="text-slate-400 mb-1 flex items-center"><Battery className="w-4 h-4 mr-1" /> Battery</p>
                  <p className="font-semibold text-white">{device.batteryPercentage}%</p>
                </div>
                <div>
                  <p className="text-slate-400 mb-1 flex items-center"><Smartphone className="w-4 h-4 mr-1" /> Android</p>
                  <p className="font-semibold text-white">{device.androidVersion}</p>
                </div>
              </div>
              <p className="text-xs text-slate-500 pt-2 border-t border-white/5">
                Last Seen: {new Date(device.lastSeen).toLocaleTimeString()}
              </p>
            </Card>
          </motion.div>
        ))}
        {activeDevices.length === 0 && (
          <motion.div variants={item} className="col-span-full">
            <Card className="flex flex-col items-center justify-center py-12 text-slate-400 border border-dashed border-white/10">
              <Smartphone className="w-12 h-12 mb-4 opacity-50" />
              <p>Waiting for Android device connection...</p>
            </Card>
          </motion.div>
        )}
      </div>

      {/* Cloud Usage Analytics Row */}
      {analytics && (
        <motion.div variants={item} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card className="p-5 flex flex-col justify-between">
            <p className="text-sm text-slate-400">Total API Commands</p>
            <p className="text-3xl font-extrabold text-white mt-2">{analytics.totalCommands || 0}</p>
          </Card>
          <Card className="p-5 flex flex-col justify-between">
            <p className="text-sm text-slate-400">Success Execution Rate</p>
            <p className="text-3xl font-extrabold text-green-400 mt-2">{analytics.successRate || 0}%</p>
          </Card>
          <Card className="p-5 flex flex-col justify-between">
            <p className="text-sm text-slate-400">Failed Executions</p>
            <p className="text-3xl font-extrabold text-red-400 mt-2">{analytics.failedCommands || 0}</p>
          </Card>
          <Card className="p-5 flex flex-col justify-between">
            <p className="text-sm text-slate-400">Avg Response Delay</p>
            <p className="text-3xl font-extrabold text-blue-400 mt-2">{analytics.avgSpeedMs || 0} ms</p>
          </Card>
        </motion.div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-8">
        <motion.div variants={item}>
          <Card className="h-full">
            <h3 className="text-lg font-semibold mb-4">Command History Log</h3>
            <div className="space-y-4 max-h-[400px] overflow-y-auto pr-2">
              {dbHistory.map((cmd) => (
                <div key={cmd._id} className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 transition-colors">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 rounded-lg bg-surface flex items-center justify-center">
                      <Play className="w-5 h-5 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium text-slate-200">{cmd.command}</p>
                      <p className="text-sm text-slate-400">{cmd.intent ? cmd.intent.replace(/_/g, ' ').toLowerCase() : 'unknown command'}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className={`text-sm font-medium ${cmd.status === 'Success' ? 'text-green-400' : 'text-red-400'}`}>
                      {cmd.status}
                    </p>
                    <p className="text-xs text-slate-500">{new Date(cmd.createdAt).toLocaleTimeString()}</p>
                  </div>
                </div>
              ))}
              {dbHistory.length === 0 && (
                <p className="text-center text-slate-500 py-6 text-sm">No command execution logs found.</p>
              )}
            </div>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card className="h-full bg-gradient-to-br from-surface to-surface/50 border-pink-500/20">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold flex items-center text-white">
                <Cake className="w-5 h-5 mr-2 text-pink-400" />
                Birthdays & Active Reminders
              </h3>
              <Link to="/dashboard/reminders" className="text-xs text-pink-400 hover:text-pink-300 font-medium">
                Manage All →
              </Link>
            </div>
            <div className="space-y-3 max-h-[400px] overflow-y-auto pr-1">
              {remindersList.map((rem) => {
                const isBirthday = rem.title.toLowerCase().includes('birthday') || rem.repeat === 'YEARLY';
                return (
                  <div key={rem._id} className="p-3.5 rounded-xl bg-white/5 border border-white/5 flex items-center justify-between hover:bg-white/10 transition-colors">
                    <div className="flex items-center space-x-3">
                      <div className={`p-2 rounded-lg ${isBirthday ? 'bg-pink-500/20 text-pink-400' : 'bg-primary/20 text-primary'}`}>
                        {isBirthday ? <Cake className="w-4 h-4" /> : <Bell className="w-4 h-4" />}
                      </div>
                      <div>
                        <p className="font-semibold text-sm text-slate-100">{rem.title}</p>
                        <p className="text-xs text-slate-400 flex items-center mt-0.5">
                          <Calendar className="w-3 h-3 mr-1 text-slate-500" /> {rem.date} @ {rem.time}
                        </p>
                      </div>
                    </div>
                    <span className="text-[10px] font-mono px-2 py-0.5 bg-white/5 text-slate-300 rounded uppercase">
                      {rem.repeat || 'ONCE'}
                    </span>
                  </div>
                );
              })}
              {remindersList.length === 0 && (
                <div className="text-center py-8 text-slate-400 text-sm space-y-2">
                  <p>No active reminders stored.</p>
                  <p className="text-xs text-slate-500">Ask AI in chat e.g. <i>"Set birthday reminder for [Name]"</i></p>
                </div>
              )}
            </div>
          </Card>
        </motion.div>
      </div>
    </motion.div>
  );
};

export default Dashboard;
