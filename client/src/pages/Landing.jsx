import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '../components/atoms/Button';
import { Bot, Mic, Shield, Smartphone } from 'lucide-react';

const Landing = () => {
  return (
    <div className="min-h-screen bg-background text-slate-100 flex flex-col">
      <header className="py-6 px-10 flex justify-between items-center glass border-b border-white/5 relative z-10">
        <h1 className="text-2xl font-bold bg-gradient-to-r from-primary to-accent-cyan bg-clip-text text-transparent">
          Agent.AI
        </h1>
        <div className="space-x-4">
          <Link to="/login" className="text-slate-300 hover:text-white transition-colors">Sign In</Link>
          <Link to="/register">
            <Button>Get Started</Button>
          </Link>
        </div>
      </header>

      <main className="flex-1 flex flex-col items-center justify-center relative px-6 text-center overflow-hidden">
        {/* Glows */}
        <div className="absolute top-1/4 left-1/4 w-[400px] h-[400px] bg-primary/20 blur-[120px] rounded-full pointer-events-none" />
        <div className="absolute bottom-1/4 right-1/4 w-[400px] h-[400px] bg-accent-purple/20 blur-[120px] rounded-full pointer-events-none" />

        <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }} className="max-w-3xl z-10">
          <h2 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6 leading-tight">
            Control your device with <br />
            <span className="bg-gradient-to-r from-primary via-accent-indigo to-accent-purple bg-clip-text text-transparent">True AI</span>
          </h2>
          <p className="text-xl text-slate-400 mb-10">
            Send texts, open apps, and manage your Android phone remotely using natural language voice and text commands.
          </p>
          <Link to="/register">
            <Button size="lg" className="px-8 shadow-primary/30 shadow-2xl">
              Start Your Free Trial
            </Button>
          </Link>
        </motion.div>

        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.4 }} className="mt-24 grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl z-10">
          <div className="glass-card p-6 flex flex-col items-center text-center">
            <Mic className="w-10 h-10 text-accent-cyan mb-4" />
            <h3 className="text-lg font-semibold mb-2">Voice Commands</h3>
            <p className="text-sm text-slate-400">Speak naturally to execute complex actions instantly.</p>
          </div>
          <div className="glass-card p-6 flex flex-col items-center text-center">
            <Bot className="w-10 h-10 text-primary mb-4" />
            <h3 className="text-lg font-semibold mb-2">Powered by Gemini</h3>
            <p className="text-sm text-slate-400">Advanced AI intent parsing guarantees accurate execution.</p>
          </div>
          <div className="glass-card p-6 flex flex-col items-center text-center">
            <Shield className="w-10 h-10 text-accent-purple mb-4" />
            <h3 className="text-lg font-semibold mb-2">Secure & Private</h3>
            <p className="text-sm text-slate-400">End-to-end encryption for all your remote commands.</p>
          </div>
        </motion.div>
      </main>
    </div>
  );
};

export default Landing;
