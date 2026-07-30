import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { motion, AnimatePresence } from 'framer-motion';
import { User, Mail, Shield, CheckCircle2, Lock, Save, Camera } from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { setUser } from '../redux/slices/userSlice';

const Profile = () => {
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.user) || { user: { name: 'Developer', email: 'dev@example.com' } };

  const [name, setName] = useState(user?.name || 'Developer');
  const [email, setEmail] = useState(user?.email || 'admin@aimobile.dev');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [toastMessage, setToastMessage] = useState(null);
  const [isSaving, setIsSaving] = useState(false);

  const handleSaveProfile = (e) => {
    e.preventDefault();
    setIsSaving(true);

    setTimeout(() => {
      dispatch(setUser({ ...user, name, email }));
      setIsSaving(false);
      setToastMessage('Profile details updated successfully!');
      setTimeout(() => setToastMessage(null), 3000);
    }, 600);
  };

  const handlePasswordChange = (e) => {
    e.preventDefault();
    if (!currentPassword) {
      alert('Please enter your current password.');
      return;
    }
    if (newPassword !== confirmPassword) {
      alert('New password and confirmation do not match.');
      return;
    }

    setToastMessage('Password updated successfully!');
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setTimeout(() => setToastMessage(null), 3000);
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      {/* Header */}
      <div>
        <h2 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
          User Account Profile
        </h2>
        <p className="text-slate-400 text-sm mt-1">Manage personal details, avatar, and security credentials.</p>
      </div>

      {/* Success Toast Notification */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div 
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 bg-green-500/10 border border-green-500/30 text-green-400 rounded-xl flex items-center space-x-3 shadow-lg"
          >
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-green-400" />
            <span className="text-sm font-medium">{toastMessage}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Profile Info Card */}
      <Card className="p-8 bg-surface/40 border-white/10 space-y-6">
        <div className="flex flex-col items-center border-b border-white/10 pb-8">
          <div className="relative group">
            <img 
              src={user?.avatar || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80"} 
              alt="Profile Avatar" 
              className="w-24 h-24 rounded-full bg-surface border-4 border-primary/40 shadow-xl object-cover" 
            />
            <button className="absolute bottom-0 right-0 p-2 bg-primary text-white rounded-full shadow-lg hover:bg-blue-600 transition-colors">
              <Camera className="w-4 h-4" />
            </button>
          </div>
          <h3 className="text-xl font-bold text-white mt-4">{name}</h3>
          <p className="text-slate-400 text-sm">{email}</p>
        </div>

        {/* Profile Info Form */}
        <form onSubmit={handleSaveProfile} className="space-y-4">
          <h4 className="text-base font-bold text-white flex items-center">
            <User className="w-4 h-4 mr-2 text-primary" />
            Personal Information
          </h4>
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1">Full Name</label>
            <Input 
              value={name} 
              onChange={(e) => setName(e.target.value)} 
              className="bg-background border-white/10" 
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1">Email Address</label>
            <Input 
              type="email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              className="bg-background border-white/10" 
            />
          </div>
          <Button 
            type="submit" 
            disabled={isSaving}
            className="w-full bg-primary hover:bg-blue-600 text-white shadow-lg shadow-primary/25 mt-2"
          >
            <Save className="w-4 h-4 mr-2" />
            {isSaving ? 'Saving Changes...' : 'Save Profile Details'}
          </Button>
        </form>
      </Card>

      {/* Password Security Card */}
      <Card className="p-8 bg-surface/40 border-white/10 space-y-6">
        <h4 className="text-base font-bold text-white flex items-center">
          <Lock className="w-4 h-4 mr-2 text-primary" />
          Security & Password
        </h4>

        <form onSubmit={handlePasswordChange} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1">Current Password</label>
            <Input 
              type="password" 
              placeholder="••••••••" 
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="bg-background border-white/10" 
            />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1">New Password</label>
              <Input 
                type="password" 
                placeholder="••••••••" 
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="bg-background border-white/10" 
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1">Confirm New Password</label>
              <Input 
                type="password" 
                placeholder="••••••••" 
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="bg-background border-white/10" 
              />
            </div>
          </div>
          <Button 
            type="submit" 
            variant="ghost" 
            className="w-full border border-white/10 hover:bg-white/5 text-slate-200 mt-2"
          >
            Update Security Password
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default Profile;
