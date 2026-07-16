import React from 'react';
import { cn } from '../../utils/cn';
import { motion } from 'framer-motion';

export const ChatBubble = ({ role, content }) => {
  const isUser = role === 'user';
  
  return (
    <motion.div 
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={cn("flex w-full mb-4", isUser ? "justify-end" : "justify-start")}
    >
      <div className={cn(
        "max-w-[75%] rounded-2xl px-5 py-3 shadow-sm",
        isUser 
          ? "bg-primary text-white rounded-br-sm" 
          : "glass border border-white/10 text-slate-200 rounded-bl-sm"
      )}>
        <p className="text-sm leading-relaxed">{content}</p>
      </div>
    </motion.div>
  );
};
