
import React from 'react';

const LoadingSpinner: React.FC<{ text?: string }> = ({ text = 'جاري التحميل...' }) => (
  <div className="flex flex-col items-center justify-center space-y-2 p-4">
    <div className="w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
    <p className="text-sm text-gray-500 dark:text-gray-400">{text}</p>
  </div>
);

export default LoadingSpinner;
