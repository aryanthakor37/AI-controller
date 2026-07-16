import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';

const NotFound = () => {
  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center text-center p-8">
      <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }}>
        <h1 className="text-9xl font-bold bg-gradient-to-br from-primary to-accent-purple bg-clip-text text-transparent">
          404
        </h1>
        <h2 className="text-2xl font-semibold mt-4 text-slate-200">Page not found</h2>
        <p className="text-slate-400 mt-2 max-w-md mx-auto">
          The page you are looking for doesn't exist or has been moved.
        </p>
        <Link to="/" className="inline-block mt-8 px-6 py-3 bg-primary text-white rounded-xl hover:bg-blue-600 transition-colors font-medium shadow-lg shadow-primary/25">
          Return Home
        </Link>
      </motion.div>
    </div>
  );
};

export default NotFound;
