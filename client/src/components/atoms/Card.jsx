import React from 'react';
import { cn } from '../../utils/cn';

export const Card = ({ className, children, ...props }) => {
  return (
    <div
      className={cn('glass-card p-6 overflow-hidden', className)}
      {...props}
    >
      {children}
    </div>
  );
};
