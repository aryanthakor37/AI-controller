import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, Plus, Calendar, Clock, User, Trash2, Search, Cake, CheckCircle2 } from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Button } from '../components/atoms/Button';
import { Input } from '../components/atoms/Input';
import ReminderModal from '../components/reminders/ReminderModal';
import { fetchReminders, deleteReminder } from '../redux/slices/reminderSlice';

const Reminders = () => {
  const dispatch = useDispatch();
  const { reminders, loading } = useSelector((state) => state.reminders);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [successToast, setSuccessToast] = useState(null);

  useEffect(() => {
    dispatch(fetchReminders());
  }, [dispatch]);

  const handleDelete = async (id, title) => {
    if (window.confirm(`Are you sure you want to delete "${title}"?`)) {
      await dispatch(deleteReminder(id));
      setSuccessToast(`Reminder "${title}" removed.`);
      setTimeout(() => setSuccessToast(null), 3000);
    }
  };

  const filteredReminders = (reminders || []).filter((r) =>
    r.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (r.contact && r.contact.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-purple-400 via-pink-400 to-red-400 bg-clip-text text-transparent flex items-center">
            <Cake className="w-7 h-7 mr-3 text-pink-400" />
            Birthdays & Event Reminders
          </h2>
          <p className="text-slate-400 text-sm mt-1">
            Manage upcoming birthdays, anniversaries, and automated voice reminders stored in your account.
          </p>
        </div>
        <Button 
          onClick={() => setIsModalOpen(true)}
          className="bg-gradient-to-r from-purple-500 to-pink-600 hover:from-purple-600 hover:to-pink-700 text-white shadow-lg shadow-purple-500/25"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Birthday / Reminder
        </Button>
      </div>

      {/* Success Toast */}
      <AnimatePresence>
        {successToast && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 bg-green-500/10 border border-green-500/30 text-green-400 rounded-xl flex items-center space-x-3 shadow-lg"
          >
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-green-400" />
            <span className="text-sm font-medium">{successToast}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Search Filter Bar */}
      <div className="relative">
        <Search className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <Input 
          placeholder="Filter reminders by title or contact name..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10 bg-surface/50 border-white/10"
        />
      </div>

      {/* Reminders List */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredReminders.map((reminder) => {
          const isBirthday = reminder.title.toLowerCase().includes('birthday') || reminder.repeat === 'YEARLY';
          return (
            <motion.div key={reminder._id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}>
              <Card className={`flex flex-col space-y-4 border ${isBirthday ? 'border-pink-500/40 bg-gradient-to-br from-surface to-pink-950/20' : 'border-white/10'}`}>
                <div className="flex justify-between items-start">
                  <div className="flex items-center space-x-3">
                    <div className={`p-2.5 rounded-xl ${isBirthday ? 'bg-pink-500/20 text-pink-400' : 'bg-primary/20 text-primary'}`}>
                      {isBirthday ? <Cake className="w-6 h-6" /> : <Bell className="w-6 h-6" />}
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-base">{reminder.title}</h3>
                      <span className={`text-[11px] px-2 py-0.5 rounded font-mono font-semibold uppercase ${
                        reminder.repeat === 'YEARLY' 
                          ? 'bg-pink-500/20 text-pink-300 border border-pink-500/30' 
                          : 'bg-white/5 text-slate-400'
                      }`}>
                        {reminder.repeat || 'ONCE'}
                      </span>
                    </div>
                  </div>
                  <button 
                    onClick={() => handleDelete(reminder._id, reminder.title)}
                    className="text-slate-400 hover:text-red-400 p-1.5 rounded-lg hover:bg-white/5 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="space-y-2 text-xs border-t border-white/5 pt-3">
                  <div className="flex justify-between text-slate-300">
                    <span className="text-slate-400 flex items-center"><Calendar className="w-3.5 h-3.5 mr-1 text-primary" /> Date</span>
                    <span className="font-semibold text-white">{reminder.date}</span>
                  </div>
                  <div className="flex justify-between text-slate-300">
                    <span className="text-slate-400 flex items-center"><Clock className="w-3.5 h-3.5 mr-1 text-amber-400" /> Time</span>
                    <span className="font-semibold text-white">{reminder.time}</span>
                  </div>
                  {reminder.contact && (
                    <div className="flex justify-between text-slate-300">
                      <span className="text-slate-400 flex items-center"><User className="w-3.5 h-3.5 mr-1 text-green-400" /> Contact</span>
                      <span className="font-semibold text-white">{reminder.contact}</span>
                    </div>
                  )}
                </div>
              </Card>
            </motion.div>
          );
        })}

        {filteredReminders.length === 0 && !loading && (
          <div className="col-span-full">
            <Card className="flex flex-col items-center justify-center py-16 text-center border border-dashed border-white/10 space-y-3">
              <Cake className="w-12 h-12 text-pink-400/60" />
              <h4 className="text-lg font-semibold text-slate-300">No Reminders Found</h4>
              <p className="text-sm text-slate-400 max-w-md">
                Ask AI in chat e.g. <i>"Set birthday reminder for [Name] on [Date]"</i> or click below to manually add one!
              </p>
              <Button 
                onClick={() => setIsModalOpen(true)}
                className="mt-2 bg-pink-500/20 text-pink-300 border border-pink-500/40 hover:bg-pink-500/30"
              >
                Add Your First Reminder
              </Button>
            </Card>
          </div>
        )}
      </div>

      {/* Reminder Modal Popup */}
      <ReminderModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </div>
  );
};

export default Reminders;
