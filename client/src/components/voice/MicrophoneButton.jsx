import React from 'react';
import { motion } from 'framer-motion';
import { Mic, MicOff, Loader2, Volume2 } from 'lucide-react';
import { useSelector } from 'react-redux';
import useSpeechToText from '../../hooks/useSpeechToText';
import useTextToSpeech from '../../hooks/useTextToSpeech';

const MicrophoneButton = () => {
  const { status } = useSelector((state) => state.voice);
  const { startListening, stopListening, isListening, isProcessing } = useSpeechToText();
  const { stop } = useTextToSpeech();

  const isSpeaking = status === 'speaking';

  const handleClick = () => {
    if (isListening) {
      stopListening();
    } else if (isSpeaking) {
      stop(); // Stop AI voice
    } else if (!isProcessing) {
      startListening();
    }
  };

  let Icon = Mic;
  let colorClass = "bg-primary text-white";
  let pulseAnimation = {};

  if (isListening) {
    colorClass = "bg-red-500 text-white";
    pulseAnimation = {
      boxShadow: ["0px 0px 0px 0px rgba(239, 68, 68, 0.7)", "0px 0px 0px 20px rgba(239, 68, 68, 0)"],
      transition: { repeat: Infinity, duration: 1.5 }
    };
  } else if (isProcessing) {
    Icon = Loader2;
    colorClass = "bg-accent-purple text-white";
  } else if (isSpeaking) {
    Icon = Volume2;
    colorClass = "bg-accent-cyan text-slate-900";
    pulseAnimation = {
      boxShadow: ["0px 0px 0px 0px rgba(0, 229, 255, 0.5)", "0px 0px 0px 15px rgba(0, 229, 255, 0)"],
      transition: { repeat: Infinity, duration: 1 }
    };
  }

  return (
    <div className="flex flex-col items-center">
      <motion.button
        onClick={handleClick}
        animate={pulseAnimation}
        disabled={isProcessing}
        className={`w-20 h-20 rounded-full flex items-center justify-center ${colorClass} shadow-lg transition-colors hover:opacity-90 disabled:opacity-75`}
      >
        <Icon className={`w-8 h-8 ${isProcessing ? 'animate-spin' : ''}`} />
      </motion.button>
      
      <p className="mt-4 text-sm text-slate-400 font-medium tracking-wide uppercase">
        {status === 'idle' && 'Tap to Speak'}
        {status === 'listening' && 'Listening...'}
        {status === 'processing' && 'Thinking...'}
        {status === 'speaking' && 'Speaking... (Tap to stop)'}
        {status === 'completed' && 'Done'}
        {status === 'error' && 'Error'}
      </p>
    </div>
  );
};

export default MicrophoneButton;
