import React from 'react';
import { Card } from '../components/atoms/Card';

const Device = () => {
  return (
    <div>
      <h2 className="text-2xl font-bold mb-6">Device Status</h2>
      <Card className="h-[400px] flex items-center justify-center">
        <p className="text-slate-400">Detailed device metrics coming soon...</p>
      </Card>
    </div>
  );
};

export default Device;
