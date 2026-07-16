import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { Settings, AlertCircle } from 'lucide-react';
import MicrophoneButton from '../components/voice/MicrophoneButton';
import VoiceWaveAnimation from '../components/voice/VoiceWaveAnimation';
import VoiceSettingsModal from '../components/voice/VoiceSettingsModal';

const Voice = () => {
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  
  const { status, error } = useSelector((state) => state.voice);
  const { interimTranscript, finalTranscript } = useSelector((state) => state.speech);
  const { history } = useSelector((state) => state.conversation);

  return (
    <div className="flex flex-col h-full relative p-4 md:p-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-accent-cyan bg-clip-text text-transparent">
            Voice Assistant
          </h1>
          <p className="text-slate-400 mt-1">Speak naturally to control your device</p>
        </div>
        <button 
          onClick={() => setIsSettingsOpen(true)}
          className="p-3 glass rounded-xl hover:bg-white/10 transition-colors"
        >
          <Settings className="w-6 h-6 text-slate-300" />
        </button>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="mb-6 p-4 bg-red-500/20 border border-red-500/50 rounded-xl flex items-center space-x-3 text-red-200">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <p className="text-sm">{error}</p>
        </div>
      )}

      {/* Conversation History & Live Transcript */}
      <div className="flex-1 flex flex-col space-y-6 overflow-y-auto mb-8 pr-2 scrollbar-hide">
        {history.map((msg, index) => (
          <div key={index} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] p-4 rounded-2xl shadow-lg ${
              msg.role === 'user' 
                ? 'bg-primary/20 border border-primary/30 text-white rounded-br-sm' 
                : 'bg-surface/80 border border-white/10 text-slate-200 rounded-bl-sm'
            }`}>
              <p>{msg.text}</p>
              {msg.intent && (
                <p className="text-xs mt-2 text-accent-cyan/80 font-mono">
                  Intent: {msg.intent}
                </p>
              )}
            </div>
          </div>
        ))}
        
        {/* Live Interim Transcript */}
        {(status === 'listening' && interimTranscript) && (
          <div className="flex justify-end">
             <div className="max-w-[80%] p-4 rounded-2xl bg-white/5 border border-white/10 text-slate-400 rounded-br-sm italic">
                {interimTranscript}...
             </div>
          </div>
        )}
      </div>

      {/* Interactive Voice Controls */}
      <div className="mt-auto flex flex-col items-center justify-center space-y-8 glass p-8 rounded-3xl border border-white/5 relative overflow-hidden">
        
        <div className="absolute inset-0 bg-gradient-to-t from-background/50 to-transparent pointer-events-none" />

        <div className="h-16 flex items-center justify-center">
           <VoiceWaveAnimation status={status} />
        </div>
        
        <MicrophoneButton />
        
      </div>

      <VoiceSettingsModal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />
    </div>
  );
};

export default Voice;
