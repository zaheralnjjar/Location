
import React from 'react';

interface BottomNavProps {
  activeScreen: string;
  setActiveScreen: (screen: string) => void;
  onAddClick: () => void;
}

const NavItem: React.FC<{ icon: string; label: string; isActive: boolean; onClick: () => void }> = ({ icon, label, isActive, onClick }) => (
  <button onClick={onClick} className={`flex flex-col items-center justify-center w-full transition-colors duration-200 ${isActive ? 'text-primary-500' : 'text-gray-500 dark:text-gray-400'}`}>
    <i className={`fa-solid ${icon} text-xl`}></i>
    <span className="text-xs mt-1">{label}</span>
  </button>
);

const BottomNav: React.FC<BottomNavProps> = ({ activeScreen, setActiveScreen, onAddClick }) => {
  return (
    <nav className="fixed bottom-0 right-0 left-0 bg-white dark:bg-gray-800 shadow-[0_-2px_10px_rgba(0,0,0,0.1)] dark:shadow-[0_-2px_10px_rgba(0,0,0,0.4)] h-16 flex items-center justify-around z-50">
      <NavItem icon="fa-chart-pie" label="لوحة التحكم" isActive={activeScreen === 'dashboard'} onClick={() => setActiveScreen('dashboard')} />
      <NavItem icon="fa-list-ul" label="القائمة" isActive={activeScreen === 'list'} onClick={() => setActiveScreen('list')} />
      
      <button onClick={onAddClick} className="w-16 h-16 -mt-8 rounded-full bg-primary-600 text-white flex items-center justify-center shadow-lg hover:bg-primary-700 focus:outline-none focus:ring-4 focus:ring-primary-300 transition-transform transform hover:scale-110">
        <i className="fa-solid fa-plus text-2xl"></i>
      </button>

      <NavItem icon="fa-map-location-dot" label="الخريطة" isActive={activeScreen === 'map'} onClick={() => setActiveScreen('map')} />
      <NavItem icon="fa-cogs" label="الإعدادات" isActive={activeScreen === 'settings'} onClick={() => setActiveScreen('settings')} />
    </nav>
  );
};

export default BottomNav;
