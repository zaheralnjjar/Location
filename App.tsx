
import React, { useState, useEffect, useCallback } from 'react';
import { Parking } from './types';
import DashboardScreen from './components/screens/DashboardScreen';
import ParkingListScreen from './components/screens/ParkingListScreen';
import MapScreen from './components/screens/MapScreen';
import SettingsScreen from './components/screens/SettingsScreen';
import AddEditParkingScreen from './components/screens/AddEditParkingScreen';
import BottomNav from './components/BottomNav';
import { ToastContainer, showToast } from './components/Toast';
import { PREDEFINED_TAGS, LOCAL_STORAGE_KEY_PARKINGS, LOCAL_STORAGE_KEY_TAGS } from './constants';
import { Tag } from './types';

const App: React.FC = () => {
  const [activeScreen, setActiveScreen] = useState('dashboard');
  const [parkings, setParkings] = useState<Parking[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [editingParking, setEditingParking] = useState<Parking | null>(null);
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'system');

  useEffect(() => {
    // Load data from localStorage
    try {
      const storedParkings = localStorage.getItem(LOCAL_STORAGE_KEY_PARKINGS);
      if (storedParkings) {
        setParkings(JSON.parse(storedParkings));
      }

      const storedTags = localStorage.getItem(LOCAL_STORAGE_KEY_TAGS);
      if (storedTags) {
        setTags(JSON.parse(storedTags));
      } else {
        setTags(PREDEFINED_TAGS);
      }
    } catch (error) {
      console.error("Failed to load data from localStorage", error);
      showToast('فشل تحميل البيانات المحفوظة.', 'error');
    }
  }, []);

  useEffect(() => {
    // Save data to localStorage
    try {
      localStorage.setItem(LOCAL_STORAGE_KEY_PARKINGS, JSON.stringify(parkings));
      localStorage.setItem(LOCAL_STORAGE_KEY_TAGS, JSON.stringify(tags));
    } catch (error) {
      console.error("Failed to save data to localStorage", error);
      showToast('فشل حفظ البيانات.', 'error');
    }
  }, [parkings, tags]);

  useEffect(() => {
    if (theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    localStorage.setItem('theme', theme);
  }, [theme]);
  
  const handleAddParking = (newParking: Omit<Parking, 'id' | 'timestamp' | 'visits' | 'lastVisited'>) => {
    const parking: Parking = {
      ...newParking,
      id: Date.now().toString(),
      timestamp: Date.now(),
      visits: 1,
      lastVisited: Date.now(),
    };
    setParkings(prev => [parking, ...prev]);
    setActiveScreen('list');
    showToast('تمت إضافة الموقف بنجاح!', 'success');
  };

  const handleUpdateParking = (updatedParking: Parking) => {
    setParkings(prev => prev.map(p => p.id === updatedParking.id ? updatedParking : p));
    setEditingParking(null);
    setActiveScreen('list');
    showToast('تم تحديث الموقف بنجاح!', 'success');
  };

  const handleDeleteParking = (id: string) => {
    setParkings(prev => prev.filter(p => p.id !== id));
    showToast('تم حذف الموقف.', 'info');
  };

  const handleEditRequest = (parking: Parking) => {
    setEditingParking(parking);
    setActiveScreen('add');
  };
  
  const handleAddScreen = (coords?: {lat: number, lng: number}) => {
      setEditingParking(null);
      if (coords) {
          // Create a temporary partial parking object to pass coordinates
          const partialParking: Partial<Parking> = { latitude: coords.lat, longitude: coords.lng };
          setEditingParking(partialParking as Parking);
      }
      setActiveScreen('add');
  };
  
  const renderScreen = () => {
    switch (activeScreen) {
      case 'dashboard':
        return <DashboardScreen parkings={parkings} tags={tags} />;
      case 'list':
        return <ParkingListScreen parkings={parkings} tags={tags} onDelete={handleDeleteParking} onEdit={handleEditRequest} />;
      case 'map':
        return <MapScreen parkings={parkings} onAddParking={handleAddScreen} />;
      case 'settings':
        return <SettingsScreen parkings={parkings} setParkings={setParkings} tags={tags} setTags={setTags} theme={theme} setTheme={setTheme} />;
      case 'add':
        return <AddEditParkingScreen 
                    onSave={editingParking && editingParking.id ? handleUpdateParking : handleAddParking} 
                    existingParking={editingParking}
                    tags={tags}
                    onCancel={() => { setEditingParking(null); setActiveScreen('list'); }}
                />;
      default:
        return <DashboardScreen parkings={parkings} tags={tags} />;
    }
  };

  return (
    <div className="flex flex-col h-screen font-sans">
      <main className="flex-1 overflow-y-auto pb-20">
        {renderScreen()}
      </main>
      <BottomNav activeScreen={activeScreen} setActiveScreen={setActiveScreen} onAddClick={handleAddScreen} />
      <ToastContainer />
    </div>
  );
};

export default App;
