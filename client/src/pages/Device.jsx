import React, { useState, useEffect, useRef } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Smartphone, Battery, Wifi, Cpu, HardDrive, 
  ShieldCheck, Zap, RefreshCw, Search, Key, 
  CheckCircle2, AlertTriangle, Radio, MonitorPlay, X,
  Home, ArrowLeft, Square, Lock, Volume2, Volume1, Clipboard
} from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Button } from '../components/atoms/Button';
import { Input } from '../components/atoms/Input';
import { getApiUrl } from '../config';
import socketService from '../services/socketService';

const Device = () => {
  const dispatch = useDispatch();
  const { activeDevices } = useSelector((state) => state.device);
  const [searchQuery, setSearchQuery] = useState('');
  const [pairingCode, setPairingCode] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [actionSuccess, setActionSuccess] = useState(null);
  
  // New states for Screenshot, Live Screen, and Clipboard Sync
  const [screenshotData, setScreenshotData] = useState(null);
  const [liveScreenActiveDevice, setLiveScreenActiveDevice] = useState(null);
  const [liveScreenFrame, setLiveScreenFrame] = useState(null);
  const [liveScreenError, setLiveScreenError] = useState(null);
  const [clipboardToast, setClipboardToast] = useState(null);
  const gestureStartRef = useRef(null);

  const fetchDevices = async () => {
    try {
      const res = await fetch(`${getApiUrl()}/device/list-active`);
      const data = await res.json();
      if (Array.isArray(data)) {
        dispatch({ type: 'device/setDevices', payload: data });
      }
    } catch (err) {
      console.error('Failed to fetch devices:', err);
    }
  };

  useEffect(() => {
    fetchDevices();

    const handleScreenshotResult = (data) => {
      setScreenshotData(data.image);
    };

    const handleScreenFrame = (data) => {
      setLiveScreenFrame(data.frame);
      setLiveScreenError(null);
    };

    const handleScreenFrameError = (data) => {
      setLiveScreenError(data.error);
    };

    const handleClipboardChanged = (data) => {
      if (data.text) {
        // Auto-copy to PC clipboard if permission is granted
        if (navigator.clipboard) {
          navigator.clipboard.writeText(data.text).catch(() => {});
        }
        setClipboardToast({ text: data.text });
        setActionSuccess(`Copied from Phone Clipboard: "${data.text.substring(0, 25)}..."`);
        setTimeout(() => setClipboardToast(null), 8000);
        setTimeout(() => setActionSuccess(null), 4000);
      }
    };

    const handleCommandResult = (data) => {
      if (data.result && data.result.intent === 'GET_CLIPBOARD' && data.result.data) {
        handleClipboardChanged({ text: data.result.data });
      }
    };

    socketService.on('dashboard:screenshot_result', handleScreenshotResult);
    socketService.on('dashboard:screen_frame', handleScreenFrame);
    socketService.on('dashboard:screen_frame_error', handleScreenFrameError);
    socketService.on('dashboard:clipboard_changed', handleClipboardChanged);
    socketService.on('dashboard:command_result', handleCommandResult);

    return () => {
      socketService.off('dashboard:screenshot_result', handleScreenshotResult);
      socketService.off('dashboard:screen_frame', handleScreenFrame);
      socketService.off('dashboard:screen_frame_error', handleScreenFrameError);
      socketService.off('dashboard:clipboard_changed', handleClipboardChanged);
      socketService.off('dashboard:command_result', handleCommandResult);
    };
  }, [dispatch]);

  const handleSyncPcClipboard = async (socketId) => {
    try {
      let text = '';
      if (navigator.clipboard) {
        text = await navigator.clipboard.readText();
      }
      if (!text) {
        text = prompt('Enter or paste text to copy to phone:');
      }
      if (text) {
        socketService.emit('dashboard:sync_clipboard', {
          socketId,
          text
        });
        setActionSuccess(`Synced clipboard to phone: "${text.substring(0, 20)}..."`);
        setTimeout(() => setActionSuccess(null), 3000);
      }
    } catch (err) {
      const text = prompt('Enter text to copy to phone:');
      if (text) {
        socketService.emit('dashboard:sync_clipboard', {
          socketId,
          text
        });
        setActionSuccess(`Synced clipboard to phone: "${text.substring(0, 20)}..."`);
        setTimeout(() => setActionSuccess(null), 3000);
      }
    }
  };

  const handlePointerDown = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width;
    const y = (e.clientY - rect.top) / rect.height;
    gestureStartRef.current = { x, y, time: Date.now() };
  };

  const handlePointerUp = (e) => {
    if (!gestureStartRef.current) return;
    
    const rect = e.currentTarget.getBoundingClientRect();
    const endX = (e.clientX - rect.left) / rect.width;
    const endY = (e.clientY - rect.top) / rect.height;
    const { x: startX, y: startY, time: startTime } = gestureStartRef.current;
    const durationMs = Date.now() - startTime;

    if (liveScreenActiveDevice) {
      socketService.emit('dashboard:perform_gesture', {
        socketId: liveScreenActiveDevice,
        gesture: { startX, startY, endX, endY, durationMs }
      });
    }
    
    gestureStartRef.current = null;
  };

  const handleGeneratePairingCode = async () => {
    setIsGenerating(true);
    try {
      const token = localStorage.getItem('token');
      const res = await fetch(`${getApiUrl()}/device/generate-code`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok) {
        setPairingCode(data.pairingCode);
      } else {
        alert(data.message || 'Could not generate pairing code');
      }
    } catch (err) {
      alert('Network error while generating pairing code');
    } finally {
      setIsGenerating(false);
    }
  };

  const triggerDeviceCommand = (socketId, intent, deviceName) => {
    socketService.sendCommand(socketId, { intent });
    setActionSuccess(`Sent "${intent.replace(/_/g, ' ')}" to ${deviceName}`);
    setTimeout(() => setActionSuccess(null), 3000);
  };

  // Ghost Typing: Global Keyboard Listener for Live Stream
  useEffect(() => {
    const handleGlobalKeyDown = (e) => {
      if (!liveScreenActiveDevice) return;
      
      // Ignore if typing in an input field on the dashboard itself
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

      // Prevent default for some keys to avoid scrolling dashboard
      if (['Space', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.code)) {
        e.preventDefault();
      }

      let charToInject = e.key;
      // Handle special keys
      if (e.key === 'Enter') charToInject = 'Enter';
      else if (e.key === 'Backspace') charToInject = 'Backspace';
      else if (e.key.length > 1) return; // Ignore Shift, Ctrl, etc.
      
      socketService.emit('dashboard:inject_text', {
        socketId: liveScreenActiveDevice,
        text: charToInject
      });
    };

    window.addEventListener('keydown', handleGlobalKeyDown);
    return () => window.removeEventListener('keydown', handleGlobalKeyDown);
  }, [liveScreenActiveDevice]);

  const filteredDevices = activeDevices.filter(device => 
    (device.deviceName || 'Android Device').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (device.socketId || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            Device Management Hub
          </h2>
          <p className="text-slate-400 mt-1 text-sm">
            Monitor device telemetry, battery health, and send direct control commands.
          </p>
        </div>
        <div className="flex items-center space-x-3 w-full sm:w-auto">
          <Button 
            onClick={fetchDevices}
            variant="ghost"
            className="border border-white/10 hover:bg-white/5 text-slate-300"
          >
            <RefreshCw className="w-4 h-4 mr-2" />
            Refresh
          </Button>
          <Button 
            onClick={handleGeneratePairingCode}
            disabled={isGenerating}
            className="bg-primary hover:bg-blue-600 text-white shadow-lg shadow-primary/20"
          >
            <Key className="w-4 h-4 mr-2" />
            Pairing Code
          </Button>
        </div>
      </div>

      {/* Action Toast Notification */}
      <AnimatePresence>
        {actionSuccess && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 bg-green-500/10 border border-green-500/30 text-green-400 rounded-xl flex items-center space-x-3 shadow-lg"
          >
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-green-400" />
            <span className="text-sm font-medium">{actionSuccess}</span>
          </motion.div>
        )}
        {clipboardToast && (
          <motion.div 
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 bg-indigo-900/90 border border-indigo-500/50 rounded-xl shadow-2xl flex items-center justify-between space-x-3 text-white backdrop-blur-md"
          >
            <div className="flex items-center space-x-3 overflow-hidden">
              <Clipboard className="w-5 h-5 flex-shrink-0 text-indigo-400 animate-bounce" />
              <div className="truncate">
                <span className="text-xs text-indigo-300 font-bold block">Copied from Phone Clipboard:</span>
                <span className="text-sm font-mono text-white truncate">{clipboardToast.text}</span>
              </div>
            </div>
            <button 
              onClick={() => {
                if (navigator.clipboard) navigator.clipboard.writeText(clipboardToast.text);
              }}
              className="px-3 py-1 bg-indigo-600 hover:bg-indigo-500 text-xs font-semibold rounded-lg transition-colors flex-shrink-0"
            >
              Copy
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Pairing Code Modal Popup */}
      {pairingCode && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="p-6 bg-surface/90 border border-primary/40 rounded-2xl shadow-2xl space-y-4"
        >
          <div className="flex justify-between items-center border-b border-white/10 pb-3">
            <h3 className="font-bold text-lg text-white flex items-center">
              <Key className="w-5 h-5 mr-2 text-primary" />
              Device Pairing Code
            </h3>
            <button 
              onClick={() => setPairingCode(null)}
              className="text-slate-400 hover:text-white text-sm"
            >
              Close
            </button>
          </div>
          <p className="text-slate-300 text-sm">
            Enter this 6-digit code in the Android App settings to connect your phone:
          </p>
          <div className="p-4 bg-black/40 rounded-xl text-center border border-white/10">
            <span className="text-4xl font-mono font-extrabold tracking-widest text-primary">
              {pairingCode}
            </span>
          </div>
          <p className="text-xs text-slate-500 text-center">Code expires in 10 minutes.</p>
        </motion.div>
      )}

      {/* Overview Metric Row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-4 flex items-center space-x-4 border-l-4 border-l-green-500">
          <div className="p-3 bg-green-500/10 rounded-xl text-green-400">
            <Radio className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <p className="text-xs text-slate-400">Connected Devices</p>
            <p className="text-2xl font-bold text-white">{activeDevices.length}</p>
          </div>
        </Card>

        <Card className="p-4 flex items-center space-x-4 border-l-4 border-l-blue-500">
          <div className="p-3 bg-blue-500/10 rounded-xl text-blue-400">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400">System Security</p>
            <p className="text-2xl font-bold text-white">Encrypted WebSocket</p>
          </div>
        </Card>

        <Card className="p-4 flex items-center space-x-4 border-l-4 border-l-amber-500">
          <div className="p-3 bg-amber-500/10 rounded-xl text-amber-400">
            <Zap className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400">Avg Command Delay</p>
            <p className="text-2xl font-bold text-white">
              {activeDevices.length > 0 && activeDevices[0].latency 
                ? `${activeDevices[0].latency} ms` 
                : '12 ms'}
            </p>
          </div>
        </Card>
      </div>

      {/* Device Filter & Search Bar */}
      <div className="relative">
        <Search className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <Input 
          placeholder="Filter paired devices by name or socket ID..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10 bg-surface/50 border-white/10"
        />
      </div>

      {/* Device Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredDevices.map((device) => (
          <Card 
            key={device.socketId}
            className="flex flex-col space-y-4 border border-white/10 hover:border-primary/50 transition-all duration-200"
          >
            <div className="flex justify-between items-start">
              <div className="flex items-center space-x-3">
                <div className="p-2 bg-primary/10 text-primary rounded-lg">
                  <Smartphone className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-white text-lg">{device.deviceName || 'Android Device'}</h3>
                  <span className="text-xs text-slate-400 font-mono">ID: {device.socketId?.substring(0, 10)}...</span>
                </div>
              </div>
              <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-green-500/10 text-green-400 border border-green-500/30 flex items-center space-x-1">
                <span className="w-2 h-2 rounded-full bg-green-400 animate-ping"></span>
                <span>Online</span>
              </span>
            </div>

            <div className="space-y-3 pt-2">
              <div>
                <div className="flex justify-between text-xs text-slate-400 mb-1">
                  <span className="flex items-center"><Battery className="w-3.5 h-3.5 mr-1 text-green-400" /> Battery Level</span>
                  <span className="font-bold text-white">{device.batteryPercentage || 85}%</span>
                </div>
                <div className="w-full h-2 bg-black/40 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-gradient-to-r from-green-500 to-emerald-400 rounded-full" 
                    style={{ width: `${device.batteryPercentage || 85}%` }}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="p-2 bg-white/5 rounded-lg border border-white/5">
                  <span className="text-slate-400 flex items-center"><Wifi className="w-3 h-3 mr-1" /> Latency</span>
                  <span className="font-semibold text-slate-200 mt-1 block">{device.latency || 15} ms</span>
                </div>
                <div className="p-2 bg-white/5 rounded-lg border border-white/5">
                  <span className="text-slate-400 flex items-center"><Cpu className="w-3 h-3 mr-1" /> Android OS</span>
                  <span className="font-semibold text-slate-200 mt-1 block">v{device.androidVersion || '13.0'}</span>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-white/5 space-y-2">
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Quick Commands</p>
              <div className="grid grid-cols-2 gap-2">
                <button 
                  onClick={() => triggerDeviceCommand(device.socketId, 'OPEN_CAMERA', device.deviceName)}
                  className="px-3 py-2 text-xs bg-white/5 hover:bg-primary/20 hover:text-primary text-slate-300 rounded-lg transition-colors text-center border border-white/5"
                >
                  📷 Camera
                </button>
                <Button 
                  variant="primary" 
                  className="w-full justify-center bg-slate-800 hover:bg-slate-700 text-sm py-2"
                  onClick={() => triggerDeviceCommand(device.socketId, 'TAKE_SCREENSHOT', device.deviceName)}
                >
                  📸 Screenshot
                </Button>
                <Button 
                  variant="primary" 
                  className="w-full justify-center bg-slate-800 hover:bg-indigo-600 text-sm py-2"
                  onClick={() => {
                    setLiveScreenActiveDevice(device.socketId);
                    triggerDeviceCommand(device.socketId, 'START_SCREEN_STREAM', device.deviceName);
                  }}
                >
                  <MonitorPlay className="w-4 h-4 mr-2" /> Live Screen
                </Button>
                <button 
                  onClick={() => triggerDeviceCommand(device.socketId, 'TOGGLE_WIFI', device.deviceName)}
                  className="px-3 py-2 text-xs bg-white/5 hover:bg-primary/20 hover:text-primary text-slate-300 rounded-lg transition-colors text-center border border-white/5"
                >
                  📶 Toggle Wi-Fi
                </button>
                <button 
                  onClick={() => triggerDeviceCommand(device.socketId, 'GET_BATTERY_STATUS', device.deviceName)}
                  className="px-3 py-2 text-xs bg-white/5 hover:bg-primary/20 hover:text-primary text-slate-300 rounded-lg transition-colors text-center border border-white/5"
                >
                  🔋 Sync Battery
                </button>
                <button 
                  onClick={() => handleSyncPcClipboard(device.socketId)}
                  className="px-3 py-2 text-xs bg-white/5 hover:bg-indigo-500/20 hover:text-indigo-400 text-slate-300 rounded-lg transition-colors text-center border border-white/5 flex items-center justify-center space-x-1"
                  title="Send PC clipboard to Phone"
                >
                  <Clipboard className="w-3.5 h-3.5 text-indigo-400" />
                  <span>Send Clipboard</span>
                </button>
                <button 
                  onClick={() => triggerDeviceCommand(device.socketId, 'GET_CLIPBOARD', device.deviceName)}
                  className="px-3 py-2 text-xs bg-white/5 hover:bg-emerald-500/20 hover:text-emerald-400 text-slate-300 rounded-lg transition-colors text-center border border-white/5 flex items-center justify-center space-x-1"
                  title="Get copied text from Phone to PC"
                >
                  <Clipboard className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Get Phone Clipboard</span>
                </button>
              </div>
            </div>
          </Card>
        ))}

        {filteredDevices.length === 0 && (
          <div className="col-span-full">
            <Card className="flex flex-col items-center justify-center py-16 text-center border border-dashed border-white/10 space-y-3">
              <Smartphone className="w-12 h-12 text-slate-500 opacity-60" />
              <h4 className="text-lg font-semibold text-slate-300">No Paired Devices Active</h4>
              <p className="text-sm text-slate-400 max-w-md">
                Pair your Android smartphone using the pair code generator above to remotely execute voice and text commands.
              </p>
              <Button 
                onClick={handleGeneratePairingCode}
                className="mt-2 bg-primary/20 text-primary border border-primary/40 hover:bg-primary/30"
              >
                Generate Pairing Code Now
              </Button>
            </Card>
          </div>
        )}
      </div>

      {/* Screenshot Modal */}
      <AnimatePresence>
        {screenshotData && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
          >
            <motion.div 
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              className="bg-slate-900 border border-slate-700 rounded-xl overflow-hidden max-w-2xl w-full"
            >
              <div className="flex justify-between items-center p-4 border-b border-slate-800">
                <h3 className="text-lg font-bold text-white">Device Screenshot</h3>
                <button onClick={() => setScreenshotData(null)} className="text-slate-400 hover:text-white">
                  <X className="w-6 h-6" />
                </button>
              </div>
              <div className="p-4 flex justify-center bg-slate-950">
                <img src={`data:image/jpeg;base64,${screenshotData}`} alt="Screenshot" className="max-h-[70vh] rounded-lg shadow-2xl" />
              </div>
              <div className="p-4 border-t border-slate-800 flex justify-end">
                <a 
                  href={`data:image/jpeg;base64,${screenshotData}`} 
                  download={`screenshot_${new Date().getTime()}.jpg`}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg transition-colors"
                >
                  Download Image
                </a>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Live Screen Modal */}
      <AnimatePresence>
        {liveScreenActiveDevice && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md p-4"
          >
            <motion.div 
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.95 }}
              className="bg-slate-900 border border-indigo-500/50 rounded-2xl overflow-hidden max-w-sm w-full shadow-[0_0_50px_rgba(99,102,241,0.2)]"
            >
              <div className="flex justify-between items-center p-4 bg-slate-900 border-b border-slate-800">
                <div className="flex items-center">
                  <div className="w-2 h-2 rounded-full bg-red-500 animate-pulse mr-2"></div>
                  <h3 className="text-sm font-bold text-white tracking-wider">LIVE STREAM</h3>
                </div>
                <button 
                  onClick={() => {
                    triggerDeviceCommand(liveScreenActiveDevice, 'STOP_SCREEN_STREAM', 'Device');
                    setLiveScreenActiveDevice(null);
                    setLiveScreenFrame(null);
                  }} 
                  className="text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 p-1 rounded-full transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <div className="bg-black flex justify-center items-center min-h-[60vh] relative">
                {liveScreenFrame ? (
                  <>
                    <img 
                      src={`data:image/jpeg;base64,${liveScreenFrame}`} 
                      alt="Live Screen" 
                      className="w-full h-full object-contain cursor-crosshair touch-none" 
                      onPointerDown={handlePointerDown}
                      onPointerUp={handlePointerUp}
                      onPointerLeave={() => { gestureStartRef.current = null; }}
                      draggable="false"
                    />
                    
                    {/* Hardware Buttons Panel (Phantom Touch) */}
                    <div className="absolute right-4 top-1/2 -translate-y-1/2 flex flex-col gap-2 p-2 bg-slate-900/70 backdrop-blur-md rounded-2xl border border-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.5)]">
                      <button onClick={() => triggerDeviceCommand(liveScreenActiveDevice, 'GO_HOME', 'Device')} className="p-2.5 text-slate-300 hover:text-white hover:bg-white/20 rounded-xl transition-colors" title="Home">
                        <Home className="w-5 h-5" />
                      </button>
                      <button onClick={() => triggerDeviceCommand(liveScreenActiveDevice, 'GO_RECENTS', 'Device')} className="p-2.5 text-slate-300 hover:text-white hover:bg-white/20 rounded-xl transition-colors" title="Recent Apps">
                        <Square className="w-5 h-5" />
                      </button>
                      <button onClick={() => triggerDeviceCommand(liveScreenActiveDevice, 'GO_BACK', 'Device')} className="p-2.5 text-slate-300 hover:text-white hover:bg-white/20 rounded-xl transition-colors" title="Back">
                        <ArrowLeft className="w-5 h-5" />
                      </button>
                      <div className="w-full h-px bg-white/10 my-1"></div>
                      <button onClick={() => triggerDeviceCommand(liveScreenActiveDevice, 'INCREASE_VOLUME', 'Device')} className="p-2.5 text-slate-300 hover:text-white hover:bg-white/20 rounded-xl transition-colors" title="Volume Up">
                        <Volume2 className="w-5 h-5" />
                      </button>
                      <button onClick={() => triggerDeviceCommand(liveScreenActiveDevice, 'DECREASE_VOLUME', 'Device')} className="p-2.5 text-slate-300 hover:text-white hover:bg-white/20 rounded-xl transition-colors" title="Volume Down">
                        <Volume1 className="w-5 h-5" />
                      </button>
                      <div className="w-full h-px bg-white/10 my-1"></div>
                      <button onClick={() => triggerDeviceCommand(liveScreenActiveDevice, 'LOCK_SCREEN', 'Device')} className="p-2.5 text-red-400 hover:text-red-300 hover:bg-red-500/20 rounded-xl transition-colors" title="Lock Screen">
                        <Lock className="w-5 h-5" />
                      </button>
                    </div>

                    {/* Dedicated Text Input Bar for reliable typing */}
                    <div className="absolute bottom-6 left-1/2 -translate-x-1/2 w-[85%] max-w-sm flex items-center bg-slate-900/90 backdrop-blur-xl border border-white/20 rounded-2xl px-4 py-3 shadow-[0_10px_40px_rgba(0,0,0,0.8)] z-50">
                      <input 
                        type="text" 
                        placeholder="Type here & press Enter to send..." 
                        className="bg-transparent border-none outline-none text-white text-sm w-full placeholder:text-slate-400 font-medium"
                        onKeyDown={(e) => {
                          e.stopPropagation();
                          if (e.key === 'Enter') {
                            const val = e.currentTarget.value;
                            if (!val) return;
                            socketService.emit('dashboard:inject_text', {
                              socketId: liveScreenActiveDevice,
                              text: val
                            });
                            e.currentTarget.value = '';
                          }
                        }}
                      />
                    </div>
                  </>
                ) : (
                  <div className="text-center p-8">
                    {!liveScreenError && <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>}
                    <p className={`text-sm ${liveScreenError ? 'text-red-400 font-bold' : 'text-slate-400'}`}>
                      {liveScreenError ? `ERROR: ${liveScreenError}` : 'Waiting for device to accept prompt...'}
                    </p>
                    {!liveScreenError && <p className="text-slate-500 text-xs mt-2">Please click 'Start Now' on your phone</p>}
                  </div>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

    </div>
  );
};

export default Device;
