import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Zap, Plus, Play, Trash2, Moon, AlertTriangle, Briefcase, Navigation, 
  CheckCircle2, Clock, Volume2, ShieldAlert, Sparkles, Layers, RefreshCw
} from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Button } from '../components/atoms/Button';
import { Input } from '../components/atoms/Input';

const CATEGORY_COLORS = {
  EMERGENCY: 'from-red-500/20 to-rose-950/40 border-red-500/40 text-red-400',
  ROUTINE: 'from-indigo-500/20 to-purple-950/40 border-indigo-500/40 text-indigo-400',
  FOCUS: 'from-amber-500/20 to-yellow-950/40 border-amber-500/40 text-amber-400',
  TRAVEL: 'from-emerald-500/20 to-teal-950/40 border-emerald-500/40 text-emerald-400'
};

const CATEGORY_ICONS = {
  EMERGENCY: AlertTriangle,
  ROUTINE: Moon,
  FOCUS: Briefcase,
  TRAVEL: Navigation
};

const Routines = () => {
  const [routines, setRoutines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [executingId, setExecutingId] = useState(null);
  const [activeStep, setActiveStep] = useState(null);
  const [toast, setToast] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // New Routine Form State
  const [newTitle, setNewTitle] = useState('');
  const [newTrigger, setNewTrigger] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newCategory, setNewCategory] = useState('ROUTINE');
  const [actions, setActions] = useState([
    { intent: 'FLASHLIGHT_OFF', args: {} }
  ]);

  const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

  const fetchRoutines = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/routines`);
      const data = await res.json();
      if (data.success) {
        setRoutines(data.routines);
      }
    } catch (err) {
      console.error('Failed to fetch routines:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoutines();
  }, []);

  const handleExecute = async (routine) => {
    setExecutingId(routine._id);
    setActiveStep({ title: routine.title, current: 1, total: routine.actions.length });

    try {
      const res = await fetch(`${API_URL}/routines/execute/${routine._id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      const data = await res.json();
      if (data.success) {
        setToast(`Started routine: "${routine.title}"`);
        setTimeout(() => setToast(null), 4000);
      }
    } catch (err) {
      setToast('Network error triggering routine execution');
    } finally {
      setTimeout(() => {
        setExecutingId(null);
        setActiveStep(null);
      }, routine.actions.length * 600 + 500);
    }
  };

  const handleDelete = async (id, title) => {
    if (!window.confirm(`Delete routine "${title}"?`)) return;
    try {
      const res = await fetch(`${API_URL}/routines/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        setToast(`Routine "${title}" deleted`);
        fetchRoutines();
      } else {
        alert(data.message || 'Could not delete routine');
      }
    } catch (err) {
      alert('Failed to delete routine');
    }
  };

  const handleAddAction = () => {
    setActions([...actions, { intent: 'SET_ALARM', args: { time: '07:00' } }]);
  };

  const handleRemoveAction = (index) => {
    setActions(actions.filter((_, i) => i !== index));
  };

  const handleActionChange = (index, field, value) => {
    const updated = [...actions];
    if (field === 'intent') {
      updated[index].intent = value;
      if (value === 'SET_ALARM') updated[index].args = { time: '07:00' };
      else if (value === 'SEND_SMS') updated[index].args = { contact: 'Family', message: 'Hello!' };
      else updated[index].args = {};
    }
    setActions(updated);
  };

  const handleCreateRoutine = async (e) => {
    e.preventDefault();
    if (!newTitle || !newTrigger || actions.length === 0) {
      alert('Please fill out title, trigger phrase, and add at least 1 action');
      return;
    }

    try {
      const res = await fetch(`${API_URL}/routines`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: newTitle,
          triggerPhrase: newTrigger,
          description: newDesc,
          category: newCategory,
          actions
        })
      });
      const data = await res.json();
      if (data.success) {
        setToast(`Created routine "${newTitle}"`);
        setIsModalOpen(false);
        setNewTitle('');
        setNewTrigger('');
        setNewDesc('');
        setActions([{ intent: 'FLASHLIGHT_OFF', args: {} }]);
        fetchRoutines();
      } else {
        alert(data.message || 'Failed to create routine');
      }
    } catch (err) {
      alert('Error creating routine');
    }
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-cyan-400 via-indigo-300 to-purple-400 bg-clip-text text-transparent flex items-center">
            <Layers className="w-8 h-8 mr-3 text-cyan-400" />
            AI Routine Macros & Emergency Hub
          </h2>
          <p className="text-slate-400 text-sm mt-1">
            Chain multiple phone actions into single voice commands or one-click web triggers.
          </p>
        </div>
        <div className="flex space-x-3">
          <Button onClick={fetchRoutines} variant="ghost" className="border border-white/10 text-slate-300">
            <RefreshCw className="w-4 h-4 mr-2" /> Refresh
          </Button>
          <Button 
            onClick={() => setIsModalOpen(true)}
            className="bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700 text-white shadow-lg shadow-cyan-500/20"
          >
            <Plus className="w-4 h-4 mr-2" />
            Create Custom Routine
          </Button>
        </div>
      </div>

      {/* Success Toast */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 rounded-xl flex items-center space-x-3 shadow-lg"
          >
            <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0" />
            <span className="text-sm font-semibold">{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Execution Progress Bar */}
      <AnimatePresence>
        {activeStep && (
          <motion.div
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            className="p-4 bg-gradient-to-r from-blue-600/30 to-indigo-600/30 border border-blue-500/50 rounded-2xl shadow-xl space-y-2"
          >
            <div className="flex justify-between items-center text-sm font-semibold text-white">
              <span className="flex items-center">
                <Sparkles className="w-4 h-4 text-cyan-400 animate-spin mr-2" />
                Executing Routine: "{activeStep.title}"
              </span>
              <span className="text-xs bg-cyan-500/20 text-cyan-300 px-2 py-0.5 rounded-full font-mono">
                Running Step Sequencer
              </span>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
              <motion.div 
                initial={{ width: '0%' }}
                animate={{ width: '100%' }}
                transition={{ duration: 1.5 }}
                className="bg-gradient-to-r from-cyan-400 to-indigo-400 h-full"
              />
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Emergency Quick Action Banner */}
      <Card className="bg-gradient-to-r from-red-950/60 via-rose-900/40 to-slate-900 border border-red-500/40 p-6 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-6 shadow-2xl relative overflow-hidden">
        <div className="space-y-2 z-10">
          <div className="flex items-center space-x-2 text-red-400 font-bold uppercase tracking-wider text-xs">
            <ShieldAlert className="w-5 h-5 text-red-400 animate-pulse" />
            <span>Emergency Rapid Response Beacon</span>
          </div>
          <h3 className="text-2xl font-bold text-white">One-Click Emergency SOS Trigger</h3>
          <p className="text-slate-300 text-sm max-w-xl">
            Instantly turns on mobile flashlight, sends emergency location SMS to family contacts, and maximizes speaker alert volume.
          </p>
        </div>
        <Button
          onClick={() => {
            const sosRoutine = routines.find(r => r.category === 'EMERGENCY') || { _id: '', title: 'Emergency SOS Trigger', actions: [{},{},{}] };
            handleExecute(sosRoutine);
          }}
          className="z-10 bg-gradient-to-r from-red-600 to-rose-700 hover:from-red-500 hover:to-rose-600 text-white font-bold px-6 py-3 text-base shadow-xl shadow-red-600/30 flex items-center space-x-2 flex-shrink-0"
        >
          <AlertTriangle className="w-5 h-5" />
          <span>TRIGGER SOS BEACON</span>
        </Button>
      </Card>

      {/* Routines Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {routines.map((routine) => {
          const CategoryIcon = CATEGORY_ICONS[routine.category] || Zap;
          const colorClass = CATEGORY_COLORS[routine.category] || CATEGORY_COLORS.ROUTINE;
          const isExecuting = executingId === routine._id;

          return (
            <motion.div key={routine._id} initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }}>
              <Card className={`p-6 bg-gradient-to-br ${colorClass} backdrop-blur-md border space-y-4 rounded-2xl flex flex-col justify-between h-full`}>
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex items-center space-x-3">
                      <div className="p-3 bg-white/10 rounded-xl">
                        <CategoryIcon className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className="text-xl font-bold text-white">{routine.title}</h3>
                        <span className="text-xs font-mono bg-white/10 text-slate-300 px-2 py-0.5 rounded uppercase">
                          Trigger: "{routine.triggerPhrase}"
                        </span>
                      </div>
                    </div>
                    {!routine.isSystemDefault && (
                      <button
                        onClick={() => handleDelete(routine._id, routine.title)}
                        className="text-slate-400 hover:text-red-400 p-1.5 rounded-lg hover:bg-white/10 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>

                  <p className="text-sm text-slate-300 mb-4">{routine.description || 'Custom multi-step macro'}</p>

                  {/* Actions List */}
                  <div className="space-y-2 border-t border-white/10 pt-3">
                    <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Automated Steps ({routine.actions.length}):</span>
                    <div className="flex flex-wrap gap-2 pt-1">
                      {routine.actions.map((act, idx) => (
                        <span key={idx} className="text-xs bg-slate-900/60 border border-white/10 text-slate-200 px-2.5 py-1 rounded-lg flex items-center space-x-1 font-mono">
                          <span className="text-cyan-400 font-bold mr-1">#{idx + 1}</span>
                          <span>{act.intent.replace(/_/g, ' ')}</span>
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t border-white/10 flex justify-between items-center">
                  <span className="text-xs text-slate-400">
                    {routine.isSystemDefault ? '🔒 System Preset' : '👤 User Custom'}
                  </span>
                  <Button
                    onClick={() => handleExecute(routine)}
                    disabled={isExecuting}
                    className="bg-white/10 hover:bg-white/20 text-white font-semibold shadow border border-white/20"
                  >
                    {isExecuting ? (
                      <Sparkles className="w-4 h-4 mr-2 animate-spin text-cyan-400" />
                    ) : (
                      <Play className="w-4 h-4 mr-2 text-emerald-400 fill-emerald-400" />
                    )}
                    {isExecuting ? 'Executing...' : 'Run Routine'}
                  </Button>
                </div>
              </Card>
            </motion.div>
          );
        })}
      </div>

      {/* Modal: Create Custom Routine */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-slate-900 border border-white/10 rounded-2xl max-w-lg w-full p-6 space-y-6 shadow-2xl max-h-[90vh] overflow-y-auto"
          >
            <div className="flex justify-between items-center border-b border-white/10 pb-4">
              <h3 className="text-xl font-bold text-white flex items-center">
                <Plus className="w-5 h-5 mr-2 text-cyan-400" />
                Build Custom AI Routine Macro
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            <form onSubmit={handleCreateRoutine} className="space-y-4">
              <div>
                <label className="text-xs text-slate-400 font-semibold uppercase">Routine Title</label>
                <Input
                  placeholder="e.g., Cooking Mode, Morning Warmup"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  className="mt-1 bg-surface border-white/10"
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 font-semibold uppercase">Voice Trigger Phrase</label>
                <Input
                  placeholder="e.g., cooking mode, start morning"
                  value={newTrigger}
                  onChange={(e) => setNewTrigger(e.target.value)}
                  className="mt-1 bg-surface border-white/10"
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 font-semibold uppercase">Description</label>
                <Input
                  placeholder="Briefly describe what this routine does..."
                  value={newDesc}
                  onChange={(e) => setNewDesc(e.target.value)}
                  className="mt-1 bg-surface border-white/10"
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 font-semibold uppercase">Category</label>
                <select
                  value={newCategory}
                  onChange={(e) => setNewCategory(e.target.value)}
                  className="w-full mt-1 bg-surface border border-white/10 text-white rounded-xl p-2.5 text-sm"
                >
                  <option value="ROUTINE">Routine / Daily</option>
                  <option value="FOCUS">Focus / Productivity</option>
                  <option value="TRAVEL">Travel / Driving</option>
                  <option value="EMERGENCY">Emergency SOS</option>
                </select>
              </div>

              {/* Action Chaining list */}
              <div className="space-y-3 pt-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs text-slate-400 font-semibold uppercase">Action Steps Chain</label>
                  <Button type="button" onClick={handleAddAction} variant="ghost" className="text-cyan-400 text-xs py-1">
                    + Add Step
                  </Button>
                </div>

                {actions.map((act, index) => (
                  <div key={index} className="flex items-center space-x-2 bg-white/5 p-3 rounded-xl border border-white/10">
                    <span className="text-xs font-mono font-bold text-cyan-400">#{index + 1}</span>
                    <select
                      value={act.intent}
                      onChange={(e) => handleActionChange(index, 'intent', e.target.value)}
                      className="flex-1 bg-slate-800 border border-white/10 text-white rounded-lg p-2 text-xs"
                    >
                      <option value="FLASHLIGHT_ON">Turn Flashlight ON</option>
                      <option value="FLASHLIGHT_OFF">Turn Flashlight OFF</option>
                      <option value="INCREASE_VOLUME">Increase Volume</option>
                      <option value="DECREASE_VOLUME">Decrease Volume</option>
                      <option value="SET_ALARM">Set Alarm (07:00 AM)</option>
                      <option value="SET_TIMER">Set Timer (45 Min)</option>
                      <option value="OPEN_MAPS">Open Maps</option>
                      <option value="SEND_SMS">Send SMS</option>
                      <option value="CHECK_WEATHER">Check Weather</option>
                    </select>

                    {actions.length > 1 && (
                      <button
                        type="button"
                        onClick={() => handleRemoveAction(index)}
                        className="text-red-400 p-1 hover:bg-white/10 rounded"
                      >
                        ✕
                      </button>
                    )}
                  </div>
                ))}
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-white/10">
                <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>Cancel</Button>
                <Button type="submit" className="bg-cyan-500 hover:bg-cyan-600 text-white font-semibold">
                  Save Routine
                </Button>
              </div>
            </form>
          </motion.div>
        </div>
      )}
    </div>
  );
};

export default Routines;
