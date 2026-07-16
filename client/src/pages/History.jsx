import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Card } from '../components/atoms/Card';
import { Play, Loader2, AlertCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import { fetchHistory } from '../redux/slices/commandSlice';

const History = () => {
  const dispatch = useDispatch();
  const { history, loading, error } = useSelector((state) => state.commands);

  useEffect(() => {
    dispatch(fetchHistory());
  }, [dispatch]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Command History</h2>
        <p className="text-slate-400">View all previously executed commands on your device.</p>
      </div>

      <Card>
        {loading && (
          <div className="flex flex-col items-center justify-center py-12 text-slate-400">
            <Loader2 className="w-10 h-10 animate-spin text-primary mb-4" />
            <p>Loading command history...</p>
          </div>
        )}

        {error && (
          <div className="flex flex-col items-center justify-center py-12 text-red-400">
            <AlertCircle className="w-10 h-10 mb-4" />
            <p className="font-semibold">Failed to load history</p>
            <p className="text-sm text-slate-500 mt-1">{error}</p>
          </div>
        )}

        {!loading && !error && history.length === 0 && (
          <div className="flex flex-col items-center justify-center py-12 text-slate-400">
            <p className="text-center py-6 text-sm">No command execution logs found.</p>
          </div>
        )}

        {!loading && !error && history.length > 0 && (
          <div className="space-y-4">
            {history.map((cmd, i) => {
              const isSuccess = cmd.status === 'Success' || cmd.status === 'completed';
              const isPending = cmd.status === 'Pending';
              
              let statusClasses = 'bg-red-500/10 text-red-400';
              if (isSuccess) {
                statusClasses = 'bg-green-500/10 text-green-400';
              } else if (isPending) {
                statusClasses = 'bg-yellow-500/10 text-yellow-400';
              }

              return (
                <motion.div 
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: Math.min(i * 0.05, 1) }}
                  key={cmd._id || cmd.id || i} 
                  className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 transition-colors"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-12 h-12 rounded-xl bg-surface flex items-center justify-center">
                      <Play className="w-6 h-6 text-primary" />
                    </div>
                    <div>
                      <p className="font-semibold text-slate-200 text-lg">
                        {cmd.command || cmd.action}
                      </p>
                      <p className="text-slate-400 text-sm">
                        {cmd.intent ? cmd.intent.replace(/_/g, ' ').toLowerCase() : (cmd.target || 'UNKNOWN_COMMAND')}
                        {cmd.deviceName && ` • ${cmd.deviceName}`}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium uppercase tracking-wider ${statusClasses}`}>
                      {cmd.status}
                    </span>
                    <p className="text-sm text-slate-500 mt-2">
                      {cmd.createdAt ? new Date(cmd.createdAt).toLocaleString() : (cmd.time || '')}
                    </p>
                  </div>
                </motion.div>
              );
            })}
          </div>
        )}
      </Card>
    </div>
  );
};

export default History;

