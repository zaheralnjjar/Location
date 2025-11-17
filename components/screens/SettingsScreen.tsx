
import React from 'react';
import { Parking, Tag } from '../../types';
import { showToast } from '../Toast';
import { LOCAL_STORAGE_KEY_PARKINGS, LOCAL_STORAGE_KEY_TAGS } from '../../constants';
import Header from '../Header';

interface SettingsScreenProps {
  parkings: Parking[];
  setParkings: React.Dispatch<React.SetStateAction<Parking[]>>;
  tags: Tag[];
  setTags: React.Dispatch<React.SetStateAction<Tag[]>>;
  theme: string;
  setTheme: (theme: string) => void;
}

const SettingsScreen: React.FC<SettingsScreenProps> = ({ parkings, setParkings, tags, setTags, theme, setTheme }) => {
  const handleExport = () => {
    try {
      const data = {
        parkings,
        tags,
      };
      const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(JSON.stringify(data, null, 2))}`;
      const link = document.createElement('a');
      link.href = jsonString;
      link.download = `parking_backup_${new Date().toISOString().split('T')[0]}.json`;
      link.click();
      showToast('تم تصدير البيانات بنجاح!', 'success');
    } catch (error) {
      console.error("Export failed", error);
      showToast('فشل تصدير البيانات.', 'error');
    }
  };

  const handleImport = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const text = e.target?.result;
        if (typeof text !== 'string') throw new Error("Invalid file content");
        
        const data = JSON.parse(text);
        if (data.parkings && Array.isArray(data.parkings) && data.tags && Array.isArray(data.tags)) {
          // A simple merge strategy: add new, don't update existing
          const existingIds = new Set(parkings.map(p => p.id));
          const newParkings = data.parkings.filter((p: Parking) => !existingIds.has(p.id));
          setParkings(prev => [...prev, ...newParkings]);
          setTags(data.tags); // Usually, tags are replaced
          showToast('تم استيراد البيانات ودمجها بنجاح!', 'success');
        } else {
          throw new Error("Invalid data format");
        }
      } catch (error) {
        console.error("Import failed", error);
        showToast('فشل استيراد البيانات. تأكد من أن الملف صحيح.', 'error');
      }
    };
    reader.readAsText(file);
  };
  
  const handleDeleteAll = () => {
    if (window.confirm('هل أنت متأكد من أنك تريد حذف جميع البيانات؟ لا يمكن التراجع عن هذا الإجراء.')) {
        setParkings([]);
        // Optionally reset tags to default
        // setTags(PREDEFINED_TAGS); 
        localStorage.removeItem(LOCAL_STORAGE_KEY_PARKINGS);
        // localStorage.removeItem(LOCAL_STORAGE_KEY_TAGS);
        showToast('تم حذف جميع البيانات.', 'info');
    }
  };

  return (
    <div className="p-4 space-y-6">
      <Header title="الإعدادات" icon="fa-cogs" />

      <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-md">
        <h2 className="text-lg font-semibold mb-3">المظهر</h2>
        <div className="flex items-center space-x-4 space-x-reverse">
          <select value={theme} onChange={e => setTheme(e.target.value)} className="p-2 border rounded-md bg-white dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-primary-500 focus:outline-none">
            <option value="system">النظام</option>
            <option value="light">فاتح</option>
            <option value="dark">داكن</option>
          </select>
        </div>
      </div>
      
      <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-md">
        <h2 className="text-lg font-semibold mb-3">إدارة البيانات</h2>
        <div className="flex flex-col sm:flex-row gap-3">
          <button onClick={handleExport} className="flex-1 p-3 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors flex items-center justify-center gap-2">
            <i className="fa-solid fa-file-export"></i> تصدير البيانات
          </button>
          <label className="flex-1 p-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors cursor-pointer flex items-center justify-center gap-2">
            <i className="fa-solid fa-file-import"></i> استيراد البيانات
            <input type="file" accept=".json" onChange={handleImport} className="hidden" />
          </label>
        </div>
      </div>
      
      <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-md border-2 border-red-500">
        <h2 className="text-lg font-semibold mb-3 text-red-500">منطقة الخطر</h2>
        <button onClick={handleDeleteAll} className="w-full p-3 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors flex items-center justify-center gap-2">
            <i className="fa-solid fa-triangle-exclamation"></i> حذف جميع البيانات
        </button>
      </div>
    </div>
  );
};

export default SettingsScreen;
