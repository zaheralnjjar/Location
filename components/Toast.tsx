
import React, { useState, useEffect } from 'react';

type ToastType = 'success' | 'error' | 'info';

interface ToastMessage {
  id: number;
  message: string;
  type: ToastType;
}

let toastId = 0;
const toastListeners = new Set<(toast: ToastMessage) => void>();

export const showToast = (message: string, type: ToastType = 'info') => {
  const newToast = { id: toastId++, message, type };
  toastListeners.forEach(listener => listener(newToast));
};

const Toast: React.FC<{ message: ToastMessage; onDismiss: (id: number) => void }> = ({ message, onDismiss }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onDismiss(message.id);
    }, 3000);
    return () => clearTimeout(timer);
  }, [message.id, onDismiss]);

  const typeClasses = {
    success: 'bg-green-500',
    error: 'bg-red-500',
    info: 'bg-blue-500',
  };
  
  const iconClasses = {
    success: 'fa-check-circle',
    error: 'fa-exclamation-triangle',
    info: 'fa-info-circle',
  }

  return (
    <div
      className={`flex items-center p-4 mb-4 text-white rounded-lg shadow-lg ${typeClasses[message.type]} animate-in slide-in-from-bottom-5 fade-in-0 duration-300`}
    >
        <i className={`fa-solid ${iconClasses[message.type]} mr-3`}></i>
        <span>{message.message}</span>
    </div>
  );
};

export const ToastContainer: React.FC = () => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  useEffect(() => {
    const addToast = (toast: ToastMessage) => {
      setToasts(currentToasts => [...currentToasts, toast]);
    };

    toastListeners.add(addToast);
    return () => {
      toastListeners.delete(addToast);
    };
  }, []);

  const handleDismiss = (id: number) => {
    setToasts(currentToasts => currentToasts.filter(toast => toast.id !== id));
  };

  return (
    <div className="fixed top-5 right-5 z-[100]">
      {toasts.map(toast => (
        <Toast key={toast.id} message={toast} onDismiss={handleDismiss} />
      ))}
    </div>
  );
};
