import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { updateVoiceSettings, toggleMute } from '../../redux/slices/settingsSlice';
import { Settings, X, VolumeX, Volume2 } from 'lucide-react';

const VoiceSettingsModal = ({ isOpen, onClose }) => {
  const dispatch = useDispatch();
  const { language, pitch, rate, isMuted } = useSelector((state) => state.settings);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div className="glass-card w-full max-w-md p-6 relative">
        <button onClick={onClose} className="absolute top-4 right-4 text-slate-400 hover:text-white">
          <X className="w-5 h-5" />
        </button>
        
        <div className="flex items-center space-x-2 mb-6">
          <Settings className="w-5 h-5 text-accent-cyan" />
          <h2 className="text-xl font-semibold">Voice Settings</h2>
        </div>

        <div className="space-y-6">
          {/* Mute Toggle */}
          <div className="flex items-center justify-between bg-white/5 p-3 rounded-xl border border-white/10">
            <span className="font-medium">Mute AI Voice</span>
            <button 
              onClick={() => dispatch(toggleMute())}
              className={`p-2 rounded-full transition-colors ${isMuted ? 'bg-red-500/20 text-red-500' : 'bg-primary/20 text-primary'}`}
            >
              {isMuted ? <VolumeX className="w-5 h-5" /> : <Volume2 className="w-5 h-5" />}
            </button>
          </div>

          {/* Language Selection */}
          <div>
            <label className="block text-sm font-medium text-slate-400 mb-2">Language</label>
            <select 
              value={language}
              onChange={(e) => dispatch(updateVoiceSettings({ language: e.target.value }))}
              className="w-full bg-surface border border-white/10 rounded-lg p-3 text-white outline-none focus:border-primary transition-colors"
            >
              <option value="en-US">English (US)</option>
              <option value="en-GB">English (UK)</option>
              <option value="es-ES">Spanish</option>
              <option value="fr-FR">French</option>
              <option value="de-DE">German</option>
              <option value="hi-IN">Hindi</option>
            </select>
          </div>

          {/* Speech Rate */}
          <div>
            <div className="flex justify-between text-sm text-slate-400 mb-2">
              <label>Speech Speed</label>
              <span>{rate}x</span>
            </div>
            <input 
              type="range" min="0.5" max="2" step="0.1" value={rate}
              onChange={(e) => dispatch(updateVoiceSettings({ rate: parseFloat(e.target.value) }))}
              className="w-full accent-primary"
            />
          </div>

          {/* Pitch */}
          <div>
            <div className="flex justify-between text-sm text-slate-400 mb-2">
              <label>Voice Pitch</label>
              <span>{pitch}</span>
            </div>
            <input 
              type="range" min="0.5" max="2" step="0.1" value={pitch}
              onChange={(e) => dispatch(updateVoiceSettings({ pitch: parseFloat(e.target.value) }))}
              className="w-full accent-accent-purple"
            />
          </div>
        </div>

      </div>
    </div>
  );
};

export default VoiceSettingsModal;
