
import React from 'react';

interface HeaderProps {
  title: string;
  icon: string;
}

const Header: React.FC<HeaderProps> = ({ title, icon }) => {
  return (
    <header className="bg-white dark:bg-gray-800 shadow-md p-4 sticky top-0 z-40">
      <h1 className="text-xl font-bold text-gray-800 dark:text-gray-200 flex items-center gap-3">
        <i className={`fa-solid ${icon} text-primary-500`}></i>
        <span>{title}</span>
      </h1>
    </header>
  );
};

export default Header;
