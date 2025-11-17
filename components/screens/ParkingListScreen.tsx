
import React, { useState, useMemo } from 'react';
import { Parking, Tag } from '../../types';
import Header from '../Header';
import StarRating from '../StarRating';
import TagPill from '../TagPill';
import Modal from '../Modal';

interface ParkingListScreenProps {
  parkings: Parking[];
  tags: Tag[];
  onDelete: (id: string) => void;
  onEdit: (parking: Parking) => void;
}

const ParkingCard: React.FC<{ parking: Parking; tags: Tag[]; onDelete: () => void; onEdit: () => void; }> = ({ parking, tags, onDelete, onEdit }) => {
  const getTagById = (id: string) => tags.find(t => t.id === id);
  const parkingTags = parking.tags.map(getTagById).filter(Boolean) as Tag[];

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg overflow-hidden transition-transform transform hover:-translate-y-1">
      {parking.photos.length > 0 && (
        <img src={parking.photos[0]} alt={parking.name} className="w-full h-40 object-cover" />
      )}
      <div className="p-4">
        <div className="flex justify-between items-start">
          <h3 className="text-lg font-bold mb-2">{parking.name}</h3>
          <StarRating rating={parking.rating} readOnly />
        </div>
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-2">{parking.address}</p>
        <p className="text-xs text-gray-500 mb-3">{new Date(parking.timestamp).toLocaleString('ar-SA')}</p>
        {parking.notes && <p className="text-sm bg-gray-100 dark:bg-gray-700 p-2 rounded-md mb-3 italic">"{parking.notes}"</p>}
        <div className="flex flex-wrap gap-2 mb-4">
          {parkingTags.map(tag => <TagPill key={tag.id} tag={tag} />)}
        </div>
        <div className="flex justify-end gap-2 border-t pt-3 dark:border-gray-700">
          <button onClick={onEdit} className="px-3 py-1 text-sm bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition-colors"><i className="fa-solid fa-pen-to-square mr-1"></i> تعديل</button>
          <button onClick={onDelete} className="px-3 py-1 text-sm bg-red-500 text-white rounded-md hover:bg-red-600 transition-colors"><i className="fa-solid fa-trash mr-1"></i> حذف</button>
        </div>
      </div>
    </div>
  );
};

const ParkingListScreen: React.FC<ParkingListScreenProps> = ({ parkings, tags, onDelete, onEdit }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortKey, setSortKey] = useState('timestamp');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [parkingToDelete, setParkingToDelete] = useState<string | null>(null);

  const handleDeleteRequest = (id: string) => {
    setParkingToDelete(id);
    setIsModalOpen(true);
  };
  
  const confirmDelete = () => {
    if (parkingToDelete) {
      onDelete(parkingToDelete);
    }
    setIsModalOpen(false);
    setParkingToDelete(null);
  };
  
  const filteredAndSortedParkings = useMemo(() => {
    return parkings
      .filter(p => 
        p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.address.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.notes.toLowerCase().includes(searchTerm.toLowerCase())
      )
      .sort((a, b) => {
        if (sortKey === 'timestamp') return b.timestamp - a.timestamp;
        if (sortKey === 'rating') return b.rating - a.rating;
        if (sortKey === 'name') return a.name.localeCompare(b.name);
        return 0;
      });
  }, [parkings, searchTerm, sortKey]);

  return (
    <>
      <Header title="قائمة المواقف" icon="fa-list-ul" />
      <div className="p-4 space-y-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <input
            type="text"
            placeholder="ابحث في المواقف..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-grow p-2 border rounded-md bg-white dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-primary-500 focus:outline-none"
          />
          <select
            value={sortKey}
            onChange={(e) => setSortKey(e.target.value)}
            className="p-2 border rounded-md bg-white dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-primary-500 focus:outline-none"
          >
            <option value="timestamp">الأحدث أولاً</option>
            <option value="rating">الأعلى تقييماً</option>
            <option value="name">أبجدي</option>
          </select>
        </div>

        {filteredAndSortedParkings.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredAndSortedParkings.map(p => (
              <ParkingCard key={p.id} parking={p} tags={tags} onDelete={() => handleDeleteRequest(p.id)} onEdit={() => onEdit(p)} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <i className="fa-solid fa-car-on text-5xl text-gray-400 mb-4"></i>
            <h3 className="text-xl font-semibold text-gray-700 dark:text-gray-300">لا توجد مواقف</h3>
            <p className="text-gray-500 dark:text-gray-400">لم يتم العثور على مواقف تطابق بحثك أو لم تقم بإضافة أي مواقف بعد.</p>
          </div>
        )}
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="تأكيد الحذف">
        <p>هل أنت متأكد من أنك تريد حذف هذا الموقف؟ لا يمكن التراجع عن هذا الإجراء.</p>
        <div className="flex justify-end gap-3 mt-5">
          <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 bg-gray-200 dark:bg-gray-600 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors">إلغاء</button>
          <button onClick={confirmDelete} className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors">نعم، احذف</button>
        </div>
      </Modal>
    </>
  );
};

export default ParkingListScreen;
