
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Parking, Tag } from '../../types';
import StarRating from '../StarRating';
import { showToast } from '../Toast';
import LoadingSpinner from '../LoadingSpinner';
import Header from '../Header';

interface AddEditParkingScreenProps {
  onSave: (parking: any) => void;
  existingParking: Parking | null;
  tags: Tag[];
  onCancel: () => void;
}

const CameraComponent: React.FC<{ onPhotosChange: (photos: string[]) => void; initialPhotos: string[] }> = ({ onPhotosChange, initialPhotos }) => {
    const [isCameraOpen, setIsCameraOpen] = useState(false);
    const [photos, setPhotos] = useState<string[]>(initialPhotos);
    const [stream, setStream] = useState<MediaStream | null>(null);
    const videoRef = useRef<HTMLVideoElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [facingMode, setFacingMode] = useState('environment');

    const openCamera = useCallback(async () => {
        try {
            const mediaStream = await navigator.mediaDevices.getUserMedia({ 
                video: { facingMode: facingMode } 
            });
            setStream(mediaStream);
            setIsCameraOpen(true);
        } catch (err) {
            console.error("Error accessing camera:", err);
            showToast("لا يمكن الوصول إلى الكاميرا. يرجى التحقق من الأذونات.", "error");
        }
    }, [facingMode]);

    useEffect(() => {
        if (isCameraOpen && stream && videoRef.current) {
            videoRef.current.srcObject = stream;
        }
    }, [isCameraOpen, stream]);
    
    const closeCamera = () => {
        stream?.getTracks().forEach(track => track.stop());
        setStream(null);
        setIsCameraOpen(false);
    };

    const takePicture = () => {
        if (videoRef.current && canvasRef.current) {
            const video = videoRef.current;
            const canvas = canvasRef.current;
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            const context = canvas.getContext('2d');
            context?.drawImage(video, 0, 0, video.videoWidth, video.videoHeight);
            const dataUrl = canvas.toDataURL('image/jpeg', 0.8);
            const newPhotos = [...photos, dataUrl];
            setPhotos(newPhotos);
            onPhotosChange(newPhotos);
            closeCamera();
        }
    };

    const switchCamera = () => {
        setFacingMode(prev => (prev === 'user' ? 'environment' : 'user'));
        closeCamera();
        // The camera will reopen via the button click with the new facing mode
    };
    
    const removePhoto = (index: number) => {
        const newPhotos = photos.filter((_, i) => i !== index);
        setPhotos(newPhotos);
        onPhotosChange(newPhotos);
    };

    if (isCameraOpen) {
        return (
            <div className="fixed inset-0 bg-black z-50 flex flex-col items-center justify-center p-4">
                <video ref={videoRef} autoPlay className="w-full h-auto max-h-[80%] rounded-lg"></video>
                <div className="absolute bottom-4 flex items-center gap-4">
                    <button onClick={closeCamera} className="w-16 h-16 rounded-full bg-gray-500 text-white text-xl flex items-center justify-center"><i className="fa-solid fa-times"></i></button>
                    <button onClick={takePicture} className="w-20 h-20 rounded-full bg-white border-4 border-gray-400"></button>
                    <button onClick={switchCamera} className="w-16 h-16 rounded-full bg-gray-500 text-white text-xl flex items-center justify-center"><i className="fa-solid fa-camera-rotate"></i></button>
                </div>
                <canvas ref={canvasRef} className="hidden"></canvas>
            </div>
        );
    }

    return (
        <div>
            <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-2 mb-3">
                {photos.map((photo, index) => (
                    <div key={index} className="relative group">
                        <img src={photo} alt={`parking photo ${index + 1}`} className="w-full h-24 object-cover rounded-md" />
                        <button onClick={() => removePhoto(index)} className="absolute top-1 right-1 w-6 h-6 bg-red-600 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                           <i className="fa-solid fa-times text-sm"></i>
                        </button>
                    </div>
                ))}
            </div>
             <button type="button" onClick={openCamera} className="w-full flex items-center justify-center gap-2 p-3 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors">
                <i className="fa-solid fa-camera"></i>
                <span>إضافة صورة</span>
            </button>
        </div>
    );
};


const AddEditParkingScreen: React.FC<AddEditParkingScreenProps> = ({ onSave, existingParking, tags, onCancel }) => {
  const [name, setName] = useState('');
  const [notes, setNotes] = useState('');
  const [rating, setRating] = useState(3);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [photos, setPhotos] = useState<string[]>([]);
  const [location, setLocation] = useState<{ lat: number, lng: number } | null>(null);
  const [address, setAddress] = useState('جاري تحديد العنوان...');
  const [loadingLocation, setLoadingLocation] = useState(true);

  useEffect(() => {
    if (existingParking) {
      setName(existingParking.name || '');
      setNotes(existingParking.notes || '');
      setRating(existingParking.rating || 3);
      setSelectedTags(existingParking.tags || []);
      setPhotos(existingParking.photos || []);
      setLocation({ lat: existingParking.latitude, lng: existingParking.longitude });
      setAddress(existingParking.address || 'لا يمكن تحديد العنوان');
      setLoadingLocation(false);
    } else {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setLocation({ lat: latitude, lng: longitude });
          // In a real app, use a geocoding service here.
          setAddress(`خط عرض: ${latitude.toFixed(4)}, خط طول: ${longitude.toFixed(4)}`);
          setName(`موقف ${new Date().toLocaleTimeString('ar-SA')}`);
          setLoadingLocation(false);
        },
        (error) => {
          console.error(error);
          showToast('لا يمكن تحديد الموقع. يرجى تمكين خدمات الموقع.', 'error');
          setAddress('فشل تحديد الموقع');
          setLoadingLocation(false);
        },
        { enableHighAccuracy: true }
      );
    }
  }, [existingParking]);

  const handleTagToggle = (tagId: string) => {
    setSelectedTags(prev => 
      prev.includes(tagId) ? prev.filter(id => id !== tagId) : [...prev, tagId]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      showToast('اسم الموقف مطلوب.', 'error');
      return;
    }
    if (!location) {
        showToast('موقع الموقف مطلوب.', 'error');
        return;
    }

    const parkingData = {
      ...(existingParking || {}),
      name,
      notes,
      rating,
      tags: selectedTags,
      photos,
      latitude: location.lat,
      longitude: location.lng,
      address,
    };
    onSave(parkingData);
  };
  
  if (loadingLocation) {
      return <div className="p-4"><LoadingSpinner text="جاري تحديد موقعك..." /></div>;
  }

  return (
    <div className="p-4">
    <Header title={existingParking?.id ? 'تعديل الموقف' : 'إضافة موقف جديد'} icon="fa-plus-circle" />
    <form onSubmit={handleSubmit} className="space-y-6 mt-4">
      <div>
        <label htmlFor="name" className="block text-sm font-medium mb-1">اسم الموقف</label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-full p-2 border rounded-md bg-white dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-primary-500 focus:outline-none"
        />
      </div>
      
      <div>
          <p className="text-sm font-medium mb-1">الموقع</p>
          <p className="text-sm p-2 bg-gray-100 dark:bg-gray-700 rounded-md">{address}</p>
      </div>

      <div>
        <label htmlFor="notes" className="block text-sm font-medium mb-1">ملاحظات</label>
        <textarea
          id="notes"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          rows={3}
          className="w-full p-2 border rounded-md bg-white dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-primary-500 focus:outline-none"
        ></textarea>
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">التقييم</label>
        <StarRating rating={rating} onRatingChange={setRating} />
      </div>

      <div>
        <label className="block text-sm font-medium mb-2">العلامات</label>
        <div className="flex flex-wrap gap-2">
          {tags.map(tag => (
            <button
              type="button"
              key={tag.id}
              onClick={() => handleTagToggle(tag.id)}
              className={`px-3 py-1 text-sm font-semibold text-white rounded-full transition-all ${tag.color} ${selectedTags.includes(tag.id) ? 'ring-2 ring-offset-2 ring-offset-gray-100 dark:ring-offset-gray-900 ring-white' : 'opacity-60'}`}
            >
              {tag.name}
            </button>
          ))}
        </div>
      </div>

      <div>
          <label className="block text-sm font-medium mb-2">الصور</label>
          <CameraComponent onPhotosChange={setPhotos} initialPhotos={photos} />
      </div>

      <div className="flex justify-end gap-3 pt-4 border-t dark:border-gray-700">
        <button type="button" onClick={onCancel} className="px-6 py-2 bg-gray-200 dark:bg-gray-600 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors">إلغاء</button>
        <button type="submit" className="px-6 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors">
            <i className="fa-solid fa-save mr-2"></i>
            حفظ
        </button>
      </div>
    </form>
    </div>
  );
};

export default AddEditParkingScreen;
