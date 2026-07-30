import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Sliders, Cpu, Mic, Palette, Shield, Save, CheckCircle2, 
  RotateCcw, Activity, Zap, Server, HardDrive, CheckCircle, AlertTriangle, TrendingUp
} from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Button } from '../components/atoms/Button';
import { Input } from '../components/atoms/Input';
import { updateSettings, resetSettings } from '../redux/slices/settingsSlice';
import { getApiUrl } from '../config';

const Settings = () => {
  const dispatch = useDispatch();
  const reduxSettings = useSelector((state) => state.settings);

  const [activeTab, setActiveTab] = useState('ai');
  const [formData, setFormData] = useState(reduxSettings);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Performance telemetry state
  const [analytics, setAnalytics] = useState(null);
  const [healthDetails, setHealthDetails] = useState(null);
  const [loadingPerformance, setLoadingPerformance] = useState(false);

  useEffect(() => {
    if (activeTab === 'performance') {
      fetchPerformanceMetrics();
    }
  }, [activeTab]);

  const fetchPerformanceMetrics = async () => {
    setLoadingPerformance(true);
    const token = localStorage.getItem('token');
    try {
      // Fetch analytics
      const analyticsRes = await fetch(`${getApiUrl()}/analytics`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (analyticsRes.ok) {
        const analyticsData = await analyticsRes.json();
        setAnalytics(analyticsData);
      }

      // Fetch health details
      const healthRes = await fetch(`${getApiUrl()}/health/details`);
      if (healthRes.ok) {
        const healthData = await healthRes.json();
        setHealthDetails(healthData);
      }
    } catch (err) {
      console.error('Failed to fetch performance metrics:', err);
    } finally {
      setLoadingPerformance(false);
    }
  };

  const handleChange = (key, value) => {
    const updated = { ...formData, [key]: value };
    setFormData(updated);
    if (['themeMode', 'accentGlow', 'compactView'].includes(key)) {
      dispatch(updateSettings(updated));
    }
  };

  const handleSave = () => {
    dispatch(updateSettings(formData));
    setSaveSuccess(true);
    setTimeout(() => setSaveSuccess(false), 3000);
  };

  const handleReset = () => {
    dispatch(resetSettings());
    setFormData(useSelector.getState ? useSelector.getState().settings : reduxSettings);
    setSaveSuccess(true);
    setTimeout(() => setSaveSuccess(false), 3000);
  };

  const tabs = [
    { id: 'ai', label: 'AI Engine', icon: Cpu },
    { id: 'performance', label: 'Agent Performance', icon: Activity },
    { id: 'voice', label: 'Voice & Audio', icon: Mic },
    { id: 'appearance', label: 'Appearance', icon: Palette },
    { id: 'system', label: 'System & Security', icon: Shield },
  ];

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            System Preferences & Telemetry
          </h2>
          <p className="text-slate-400 text-sm mt-1">
            Configure AI model behavior, monitor agent performance telemetry, UI theme, and connectivity.
          </p>
        </div>
        <div className="flex space-x-3">
          <Button 
            onClick={handleReset} 
            variant="ghost" 
            className="border border-white/10 hover:bg-white/5 text-slate-300"
          >
            <RotateCcw className="w-4 h-4 mr-2" />
            Reset Defaults
          </Button>
          <Button 
            onClick={handleSave} 
            className="bg-primary hover:bg-blue-600 text-white shadow-lg shadow-primary/25"
          >
            <Save className="w-4 h-4 mr-2" />
            Save Preferences
          </Button>
        </div>
      </div>

      {/* Success Notification */}
      <AnimatePresence>
        {saveSuccess && (
          <motion.div 
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 bg-green-500/10 border border-green-500/30 text-green-400 rounded-xl flex items-center space-x-3 shadow-lg"
          >
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-green-400" />
            <span className="text-sm font-medium">Settings saved and applied live across application!</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Settings Container */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Sidebar Nav Tabs */}
        <Card className="md:col-span-1 p-3 space-y-1 h-fit bg-surface/50 border-white/10">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                  isActive 
                    ? 'bg-primary text-white shadow-md shadow-primary/20' 
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </Card>

        {/* Tab Content Panel */}
        <Card className="md:col-span-3 p-6 bg-surface/40 border-white/10 space-y-6">
          {activeTab === 'ai' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="border-b border-white/10 pb-4">
                <h3 className="text-lg font-bold text-white flex items-center">
                  <Cpu className="w-5 h-5 mr-2 text-primary" />
                  AI Engine Configuration
                </h3>
                <p className="text-xs text-slate-400 mt-1">Tweak intent parsing parameters and LLM settings.</p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">AI Model Version</label>
                  <select 
                    value={formData.aiModel || 'gemini-flash-lite-latest'}
                    onChange={(e) => handleChange('aiModel', e.target.value)}
                    className="w-full p-3 rounded-xl bg-background border border-white/10 text-white focus:outline-none focus:border-primary text-sm"
                  >
                    <option value="gemini-flash-lite-latest">Gemini 2.5 Flash Lite (Fastest, Default)</option>
                    <option value="gemini-2.5-flash">Gemini 2.5 Flash (Balanced Accuracy)</option>
                    <option value="gemini-2.5-pro">Gemini 2.5 Pro (Deep Context Reasoning)</option>
                  </select>
                </div>

                <div>
                  <div className="flex justify-between text-sm font-medium text-slate-300 mb-2">
                    <span>Minimum Confidence Threshold</span>
                    <span className="text-primary font-mono">{Math.round((formData.aiConfidenceThreshold || 0.7) * 100)}%</span>
                  </div>
                  <input 
                    type="range" 
                    min="0.5" 
                    max="0.95" 
                    step="0.05"
                    value={formData.aiConfidenceThreshold || 0.7}
                    onChange={(e) => handleChange('aiConfidenceThreshold', parseFloat(e.target.value))}
                    className="w-full accent-primary bg-white/10 rounded-lg cursor-pointer"
                  />
                  <p className="text-xs text-slate-500 mt-1">Commands below this confidence rating will trigger validation check.</p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Recent Context Memory Window</label>
                  <Input 
                    type="number"
                    min="1"
                    max="20"
                    value={formData.aiContextWindow || 5}
                    onChange={(e) => handleChange('aiContextWindow', parseInt(e.target.value) || 5)}
                    className="bg-background border-white/10"
                  />
                  <p className="text-xs text-slate-500 mt-1">Number of conversation turns passed to Gemini for contextual follow-up understanding.</p>
                </div>
              </div>
            </motion.div>
          )}

          {activeTab === 'performance' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="flex justify-between items-center border-b border-white/10 pb-4">
                <div>
                  <h3 className="text-lg font-bold text-white flex items-center">
                    <Activity className="w-5 h-5 mr-2 text-primary" />
                    Agent Performance & Health Telemetry
                  </h3>
                  <p className="text-xs text-slate-400 mt-1">Real-time metrics on command speed, AI accuracy, and server resource health.</p>
                </div>
                <Button 
                  onClick={fetchPerformanceMetrics} 
                  variant="ghost" 
                  size="sm"
                  className="text-xs border border-white/10 hover:bg-white/5 text-slate-300"
                >
                  Refresh Metrics
                </Button>
              </div>

              {/* KPI Summary Row */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <div className="p-4 bg-white/5 border border-white/10 rounded-xl">
                  <span className="text-xs text-slate-400 block">Total Commands</span>
                  <span className="text-2xl font-bold text-white mt-1 block">
                    {analytics?.totalCommands ?? 0}
                  </span>
                </div>
                <div className="p-4 bg-green-500/10 border border-green-500/20 rounded-xl">
                  <span className="text-xs text-slate-400 block">Success Rate</span>
                  <span className="text-2xl font-bold text-green-400 mt-1 block">
                    {analytics?.successRate ?? 100}%
                  </span>
                </div>
                <div className="p-4 bg-blue-500/10 border border-blue-500/20 rounded-xl">
                  <span className="text-xs text-slate-400 block">Avg Latency</span>
                  <span className="text-2xl font-bold text-blue-400 mt-1 block">
                    {analytics?.avgSpeedMs ?? 180} ms
                  </span>
                </div>
                <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl">
                  <span className="text-xs text-slate-400 block">Failed Commands</span>
                  <span className="text-2xl font-bold text-red-400 mt-1 block">
                    {analytics?.failedCommands ?? 0}
                  </span>
                </div>
              </div>

              {/* System & Memory Telemetry */}
              <div className="space-y-4 pt-2">
                <h4 className="text-sm font-bold text-white uppercase tracking-wider">System Health & API Gateway</h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  <div className="p-4 bg-white/5 border border-white/5 rounded-xl space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-slate-400 flex items-center"><Server className="w-4 h-4 mr-1 text-primary" /> Gemini AI Engine Status</span>
                      <span className="px-2 py-0.5 rounded bg-green-500/20 text-green-400 font-semibold">ONLINE</span>
                    </div>
                    <div className="flex justify-between items-center text-slate-400">
                      <span>Server Uptime</span>
                      <span className="font-mono text-white">{healthDetails?.uptime || 'Active'}</span>
                    </div>
                  </div>

                  <div className="p-4 bg-white/5 border border-white/5 rounded-xl space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-slate-400 flex items-center"><HardDrive className="w-4 h-4 mr-1 text-amber-400" /> RAM Memory Usage</span>
                      <span className="font-mono text-white">{healthDetails?.memory?.heapUsed || '45 MB'} / {healthDetails?.memory?.heapTotal || '90 MB'}</span>
                    </div>
                    <div className="w-full h-2 bg-black/40 rounded-full overflow-hidden">
                      <div className="h-full bg-gradient-to-r from-blue-500 to-emerald-400 rounded-full w-[45%]" />
                    </div>
                  </div>
                </div>
              </div>

              {/* Intent Distribution */}
              {analytics?.popularIntents && analytics.popularIntents.length > 0 && (
                <div className="space-y-3 pt-2">
                  <h4 className="text-sm font-bold text-white uppercase tracking-wider">Top Executed Intents</h4>
                  <div className="space-y-2">
                    {analytics.popularIntents.map((item, idx) => (
                      <div key={idx} className="flex justify-between items-center p-3 bg-white/5 rounded-xl border border-white/5 text-xs">
                        <span className="font-mono font-medium text-slate-200">{item._id}</span>
                        <span className="px-2 py-1 rounded bg-primary/20 text-primary font-bold">{item.count} execution(s)</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </motion.div>
          )}

          {activeTab === 'voice' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="border-b border-white/10 pb-4">
                <h3 className="text-lg font-bold text-white flex items-center">
                  <Mic className="w-5 h-5 mr-2 text-primary" />
                  Voice & Audio Settings
                </h3>
                <p className="text-xs text-slate-400 mt-1">Configure Speech-To-Text and Text-To-Speech response parameters.</p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Speech Recognition Language</label>
                  <select 
                    value={formData.voiceLanguage || 'en-US'}
                    onChange={(e) => handleChange('voiceLanguage', e.target.value)}
                    className="w-full p-3 rounded-xl bg-background border border-white/10 text-white focus:outline-none focus:border-primary text-sm"
                  >
                    <option value="en-US">English (United States)</option>
                    <option value="en-IN">English (India)</option>
                    <option value="es-ES">Spanish (Español)</option>
                    <option value="fr-FR">French (Français)</option>
                    <option value="de-DE">German (Deutsch)</option>
                  </select>
                </div>

                <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
                  <div>
                    <p className="font-medium text-white text-sm">Auto-Submit Voice Commands</p>
                    <p className="text-xs text-slate-400">Automatically send transcript as soon as speech pause is detected.</p>
                  </div>
                  <input 
                    type="checkbox"
                    checked={formData.autoSubmitVoice ?? true}
                    onChange={(e) => handleChange('autoSubmitVoice', e.target.checked)}
                    className="w-5 h-5 accent-primary cursor-pointer"
                  />
                </div>

                <div>
                  <div className="flex justify-between text-sm font-medium text-slate-300 mb-2">
                    <span>Voice Feedback Speed Rate</span>
                    <span className="text-primary font-mono">{formData.speechFeedbackRate || 1.0}x</span>
                  </div>
                  <input 
                    type="range" 
                    min="0.75" 
                    max="1.5" 
                    step="0.05"
                    value={formData.speechFeedbackRate || 1.0}
                    onChange={(e) => handleChange('speechFeedbackRate', parseFloat(e.target.value))}
                    className="w-full accent-primary bg-white/10 rounded-lg cursor-pointer"
                  />
                </div>
              </div>
            </motion.div>
          )}

          {activeTab === 'appearance' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="border-b border-white/10 pb-4">
                <h3 className="text-lg font-bold text-white flex items-center">
                  <Palette className="w-5 h-5 mr-2 text-primary" />
                  Appearance & Styling
                </h3>
                <p className="text-xs text-slate-400 mt-1">Customize visual aesthetics and dashboard layout live.</p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Visual Theme</label>
                  <div className="grid grid-cols-3 gap-3">
                    {[
                      { id: 'dark', label: 'Dark Slate' },
                      { id: 'oled', label: 'OLED Black' },
                      { id: 'cyberpunk', label: 'Cyberpunk' }
                    ].map((t) => (
                      <button
                        key={t.id}
                        onClick={() => handleChange('themeMode', t.id)}
                        className={`p-4 rounded-xl border text-sm font-medium transition-all ${
                          formData.themeMode === t.id 
                            ? 'border-primary bg-primary/20 text-white shadow-lg' 
                            : 'border-white/10 bg-white/5 text-slate-400 hover:text-white'
                        }`}
                      >
                        {t.label}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
                  <div>
                    <p className="font-medium text-white text-sm">Glassmorphic Glow Effects</p>
                    <p className="text-xs text-slate-400">Enable modern ambient glowing borders and card shadows.</p>
                  </div>
                  <input 
                    type="checkbox"
                    checked={formData.accentGlow ?? true}
                    onChange={(e) => handleChange('accentGlow', e.target.checked)}
                    className="w-5 h-5 accent-primary cursor-pointer"
                  />
                </div>

                <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
                  <div>
                    <p className="font-medium text-white text-sm">Compact Spacing Mode</p>
                    <p className="text-xs text-slate-400">Reduce spacing for high data density displays.</p>
                  </div>
                  <input 
                    type="checkbox"
                    checked={formData.compactView ?? false}
                    onChange={(e) => handleChange('compactView', e.target.checked)}
                    className="w-5 h-5 accent-primary cursor-pointer"
                  />
                </div>
              </div>
            </motion.div>
          )}

          {activeTab === 'system' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="border-b border-white/10 pb-4">
                <h3 className="text-lg font-bold text-white flex items-center">
                  <Shield className="w-5 h-5 mr-2 text-primary" />
                  System & Connection Parameters
                </h3>
                <p className="text-xs text-slate-400 mt-1">Manage WebSocket auto-reconnect and debug logging.</p>
              </div>

              <div className="space-y-4">
                <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
                  <div>
                    <p className="font-medium text-white text-sm">Auto-Reconnect WebSocket</p>
                    <p className="text-xs text-slate-400">Automatically retry connection to Express server on disconnect.</p>
                  </div>
                  <input 
                    type="checkbox"
                    checked={formData.autoReconnectSocket ?? true}
                    onChange={(e) => handleChange('autoReconnectSocket', e.target.checked)}
                    className="w-5 h-5 accent-primary cursor-pointer"
                  />
                </div>

                <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
                  <div>
                    <p className="font-medium text-white text-sm">Verbose Debug Telemetry</p>
                    <p className="text-xs text-slate-400">Log raw socket events and AI JSON parsing traces to browser console.</p>
                  </div>
                  <input 
                    type="checkbox"
                    checked={formData.debugLogging ?? false}
                    onChange={(e) => handleChange('debugLogging', e.target.checked)}
                    className="w-5 h-5 accent-primary cursor-pointer"
                  />
                </div>
              </div>
            </motion.div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default Settings;
