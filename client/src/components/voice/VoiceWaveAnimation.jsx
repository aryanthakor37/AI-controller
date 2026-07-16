import React from 'react';
import { motion } from 'framer-motion';

const VoiceWaveAnimation = ({ status }) => {
  if (status === 'idle' || status === 'completed' || status === 'error') return null;

  const isListening = status === 'listening';
  const isSpeaking = status === 'speaking';
  const isProcessing = status === 'processing';

  const baseClasses = "flex items-center justify-center space-x-1 h-12";
  
  // Choose colors based on status
  let color = "bg-primary";
  if (isSpeaking) color = "bg-accent-cyan";
  if (isProcessing) color = "bg-accent-purple";

  // Animation variants
  const barVariants = {
    listening: {
      height: ["8px", "24px", "8px"],
      transition: { repeat: Infinity, duration: 1, ease: "easeInOut" }
    },
    speaking: {
      height: ["12px", "32px", "12px", "24px", "12px"],
      transition: { repeat: Infinity, duration: 0.8, ease: "easeInOut" }
    },
    processing: {
      height: ["16px", "16px"],
      opacity: [0.5, 1, 0.5],
      transition: { repeat: Infinity, duration: 1.5, ease: "linear" }
    }
  };

  const currentVariant = isListening ? 'listening' : isSpeaking ? 'speaking' : 'processing';

  return (
    <div className={baseClasses}>
      {[0, 1, 2, 3, 4].map((i) => (
        <motion.div
          key={i}
          className={`w-2 rounded-full ${color}`}
          variants={barVariants}
          animate={currentVariant}
          // Delay each bar slightly for a wave effect
          transition={{ ...barVariants[currentVariant].transition, delay: i * 0.15 }}
        />
      ))}
    </div>
  );
};

export default VoiceWaveAnimation;
