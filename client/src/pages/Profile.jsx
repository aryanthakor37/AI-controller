import React from 'react';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { useSelector } from 'react-redux';

const Profile = () => {
  const { user } = useSelector((state) => state.user);

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Profile Settings</h2>
        <p className="text-slate-400">Manage your account details and preferences.</p>
      </div>

      <Card className="flex flex-col items-center p-8">
        <img src={user.avatar} alt="Profile" className="w-24 h-24 rounded-full bg-surface border-4 border-white/5 mb-6 shadow-xl" />
        <h3 className="text-xl font-bold">{user.name}</h3>
        <p className="text-slate-400 mb-8">{user.email}</p>
        
        <div className="w-full space-y-4">
          <Input label="Full Name" defaultValue={user.name} />
          <Input label="Email Address" defaultValue={user.email} />
          <Button className="w-full mt-4">Save Changes</Button>
        </div>
      </Card>
    </div>
  );
};

export default Profile;
