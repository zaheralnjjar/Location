
import React from 'react';
import { Parking, Tag } from '../../types';
import Header from '../Header';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface DashboardScreenProps {
  parkings: Parking[];
  tags: Tag[];
}

const StatCard: React.FC<{ icon: string; title: string; value: string | number; color: string }> = ({ icon, title, value, color }) => (
  <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-md flex items-center transition-transform transform hover:scale-105">
    <div className={`w-12 h-12 rounded-full flex items-center justify-center ${color} text-white text-xl mr-4`}>
      <i className={`fa-solid ${icon}`}></i>
    </div>
    <div>
      <p className="text-sm text-gray-500 dark:text-gray-400">{title}</p>
      <p className="text-2xl font-bold">{value}</p>
    </div>
  </div>
);

const DashboardScreen: React.FC<DashboardScreenProps> = ({ parkings, tags }) => {
  const totalParkings = parkings.length;
  const totalPhotos = parkings.reduce((sum, p) => sum + p.photos.length, 0);
  const avgRating = totalParkings > 0 ? (parkings.reduce((sum, p) => sum + p.rating, 0) / totalParkings).toFixed(1) : 'N/A';
  
  const mostVisited = totalParkings > 0 ? parkings.reduce((max, p) => p.visits > max.visits ? p : max, parkings[0]) : null;

  const parkingsByMonth = parkings.reduce((acc, p) => {
    const month = new Date(p.timestamp).toLocaleString('ar-SA', { month: 'long', year: 'numeric' });
    acc[month] = (acc[month] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  const chartData = Object.entries(parkingsByMonth)
    .map(([name, count]) => ({ name, 'عدد المواقف': count }))
    .reverse();

  return (
    <div className="p-4 space-y-6">
      <Header title="لوحة التحكم" icon="fa-chart-pie" />

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon="fa-car" title="إجمالي المواقف" value={totalParkings} color="bg-primary-500" />
        <StatCard icon="fa-camera" title="إجمالي الصور" value={totalPhotos} color="bg-green-500" />
        <StatCard icon="fa-star" title="متوسط التقييم" value={avgRating} color="bg-yellow-500" />
        <StatCard icon="fa-trophy" title="الأكثر زيارة" value={mostVisited ? mostVisited.name.substring(0,10)+'...' : 'لا يوجد'} color="bg-purple-500" />
      </div>

      <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-md">
        <h2 className="text-lg font-semibold mb-4">المواقف حسب الشهر</h2>
        {parkings.length > 0 ? (
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
              <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 20, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(128, 128, 128, 0.3)" />
                <XAxis type="number" allowDecimals={false} />
                <YAxis type="category" dataKey="name" width={80} />
                <Tooltip wrapperClassName="dark:!bg-gray-700 !border-gray-600" />
                <Legend />
                <Bar dataKey="عدد المواقف" fill="#3b82f6" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <p className="text-center text-gray-500 dark:text-gray-400 py-8">لا توجد بيانات لعرضها. أضف موقفاً لتبدأ!</p>
        )}
      </div>
      
    </div>
  );
};

export default DashboardScreen;
